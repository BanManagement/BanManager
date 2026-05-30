package me.confuser.banmanager.common.impl.event;

import me.confuser.banmanager.api.event.BanManagerEvent;
import me.confuser.banmanager.api.event.CancellableEvent;
import me.confuser.banmanager.api.event.EventBus;
import me.confuser.banmanager.api.event.EventPriority;
import me.confuser.banmanager.api.event.Subscription;
import me.confuser.banmanager.common.CommonLogger;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Default {@link EventBus} implementation.
 *
 * <p>Per-event-type listener lists are kept sorted by {@link EventPriority}
 * (LOWEST first, MONITOR last) so dispatch never re-sorts; subscriptions are
 * comparatively rare while publishes are hot. Lists are guarded by a
 * per-list lock — small enough to be uncontended in practice and small
 * enough that {@code copy-on-write} would be wasteful.</p>
 *
 * <p>Dispatch walks the entire {@link BanManagerEvent} super-type graph
 * (classes <i>and</i> interfaces, transitively) so a listener registered
 * against {@link CancellableEvent} or {@link BanManagerEvent} fires for
 * every concrete event in the hierarchy. Errors thrown by listeners are
 * caught and logged with the listener's class name so one bad plugin can't
 * stop the rest of the chain.</p>
 */
public final class EventBusImpl implements EventBus {

  private static final Comparator<Registration<?>> ORDERING =
      Comparator.comparingInt(reg -> reg.priority.ordinal());

  private final Map<Class<? extends BanManagerEvent>, SortedRegistrationList> registrations =
      new ConcurrentHashMap<>();
  private final CommonLogger logger;

  public EventBusImpl(CommonLogger logger) {
    this.logger = logger;
  }

  @Override
  public <E extends BanManagerEvent> Subscription subscribe(Class<E> type, Consumer<E> handler) {
    return subscribe(type, EventPriority.NORMAL, false, handler);
  }

  @Override
  public <E extends BanManagerEvent> Subscription subscribe(Class<E> type, EventPriority priority, Consumer<E> handler) {
    return subscribe(type, priority, false, handler);
  }

  @Override
  public <E extends BanManagerEvent> Subscription subscribe(Class<E> type, EventPriority priority, boolean ignoreCancelled, Consumer<E> handler) {
    Registration<E> reg = new Registration<>(this, type, priority, ignoreCancelled, handler);
    registrations.computeIfAbsent(type, k -> new SortedRegistrationList()).insert(reg);
    return reg;
  }

  @Override
  public <E extends BanManagerEvent> E publish(E event) {
    Set<Class<?>> visited = new HashSet<>();
    dispatchClass(event.getClass(), event, visited);
    return event;
  }

  /**
   * Walk the class hierarchy starting at {@code type}, firing matching
   * listeners and recursing into every interface implemented at each level
   * so subscriptions to {@link CancellableEvent} and {@link BanManagerEvent}
   * receive events whose direct supertype is an abstract class.
   */
  private <E extends BanManagerEvent> void dispatchClass(Class<?> type, E event, Set<Class<?>> visited) {
    if (type == null || !visited.add(type)) return;
    if (!BanManagerEvent.class.isAssignableFrom(type)) return;

    fire(type, event);

    for (Class<?> iface : type.getInterfaces()) {
      dispatchInterface(iface, event, visited);
    }
    dispatchClass(type.getSuperclass(), event, visited);
  }

  private <E extends BanManagerEvent> void dispatchInterface(Class<?> iface, E event, Set<Class<?>> visited) {
    if (iface == null || !visited.add(iface)) return;
    if (!BanManagerEvent.class.isAssignableFrom(iface)) return;

    fire(iface, event);

    for (Class<?> parent : iface.getInterfaces()) {
      dispatchInterface(parent, event, visited);
    }
  }

  @SuppressWarnings("unchecked")
  private <E extends BanManagerEvent> void fire(Class<?> type, E event) {
    SortedRegistrationList list = registrations.get(type);
    if (list == null) return;

    Registration<?>[] snapshot = list.snapshot();
    if (snapshot.length == 0) return;

    boolean cancellable = event instanceof CancellableEvent;
    for (Registration<?> reg : snapshot) {
      if (reg.cancelled) continue;
      if (cancellable && ((CancellableEvent) event).isCancelled() && !reg.ignoreCancelled) continue;

      try {
        ((Consumer<BanManagerEvent>) reg.handler).accept(event);
      } catch (Throwable t) {
        logger.warning("Event handler " + describe(reg) + " for " + type.getName() + " threw an exception", t);
      }
    }
  }

  /**
   * Best-effort identity for a handler. Lambdas are synthetic classes whose
   * own {@code getName()} is opaque ({@code Foo$$Lambda/0x...}); look for
   * the host class either via {@link Class#getEnclosingClass()} (works for
   * inner-class lambdas on some JVMs) or by stripping the synthetic suffix
   * from the runtime name (always works) so the log entry still points
   * operators at the right plugin.
   */
  static String describe(Registration<?> reg) {
    Class<?> handlerCls = reg.handler.getClass();
    String name = handlerCls.getName();
    int lambdaIdx = name.indexOf("$$Lambda");
    if (lambdaIdx < 0) return name;

    Class<?> enclosing = handlerCls.getEnclosingClass();
    if (enclosing != null) return enclosing.getName() + " (lambda)";
    return name.substring(0, lambdaIdx) + " (lambda)";
  }

  void remove(Registration<?> reg) {
    SortedRegistrationList list = registrations.get(reg.type);
    if (list != null) list.remove(reg);
  }

  /**
   * Single subscription record. Implements {@link Subscription} so callers can
   * detach via the returned handle.
   */
  static final class Registration<E extends BanManagerEvent> implements Subscription {

    private final EventBusImpl bus;
    private final Class<E> type;
    private final EventPriority priority;
    private final boolean ignoreCancelled;
    private final Consumer<E> handler;
    private volatile boolean cancelled;

    Registration(EventBusImpl bus, Class<E> type, EventPriority priority, boolean ignoreCancelled, Consumer<E> handler) {
      this.bus = bus;
      this.type = type;
      this.priority = priority;
      this.ignoreCancelled = ignoreCancelled;
      this.handler = handler;
    }

    @Override
    public boolean isCancelled() {
      return cancelled;
    }

    @Override
    public void unsubscribe() {
      if (cancelled) return;
      cancelled = true;
      bus.remove(this);
    }
  }

  /**
   * Listener list that maintains priority order at insertion time and
   * publishes a fresh snapshot for each dispatch so concurrent subscriptions
   * are safe but already-running iterations keep their original view.
   */
  private static final class SortedRegistrationList {

    private static final Registration<?>[] EMPTY = new Registration<?>[0];

    private volatile Registration<?>[] sorted = EMPTY;

    synchronized void insert(Registration<?> reg) {
      Registration<?>[] current = sorted;
      Registration<?>[] next = new Registration<?>[current.length + 1];
      int idx = 0;
      while (idx < current.length && ORDERING.compare(current[idx], reg) <= 0) {
        next[idx] = current[idx];
        idx++;
      }
      next[idx] = reg;
      System.arraycopy(current, idx, next, idx + 1, current.length - idx);
      sorted = next;
    }

    synchronized void remove(Registration<?> reg) {
      Registration<?>[] current = sorted;
      int found = -1;
      for (int i = 0; i < current.length; i++) {
        if (current[i] == reg) {
          found = i;
          break;
        }
      }
      if (found < 0) return;

      Registration<?>[] next = new Registration<?>[current.length - 1];
      System.arraycopy(current, 0, next, 0, found);
      System.arraycopy(current, found + 1, next, found, current.length - found - 1);
      sorted = next;
    }

    Registration<?>[] snapshot() {
      return sorted;
    }
  }
}
