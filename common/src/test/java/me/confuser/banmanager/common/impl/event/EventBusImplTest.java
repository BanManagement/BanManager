package me.confuser.banmanager.common.impl.event;

import me.confuser.banmanager.api.event.AbstractCancellableEvent;
import me.confuser.banmanager.api.event.BanManagerEvent;
import me.confuser.banmanager.api.event.CancellableEvent;
import me.confuser.banmanager.api.event.EventPriority;
import me.confuser.banmanager.api.event.Subscription;
import me.confuser.banmanager.common.CommonLogger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link EventBusImpl}'s priority ordering, transitive supertype
 * dispatch, cancellation gating, error isolation, and unsubscribe semantics.
 */
public class EventBusImplTest {

  interface MarkerEvent extends BanManagerEvent {}

  static final class ConcreteEvent implements MarkerEvent {}

  static final class CancellableConcreteEvent extends AbstractCancellableEvent implements MarkerEvent {}

  abstract static class AbstractMidEvent implements BanManagerEvent {}

  static final class LeafEvent extends AbstractMidEvent {}

  private RecordingLogger logger;
  private EventBusImpl bus;

  @BeforeEach
  public void setUp() {
    logger = new RecordingLogger();
    bus = new EventBusImpl(logger);
  }

  @Test
  public void publishReturnsSameEventInstance() {
    ConcreteEvent event = new ConcreteEvent();
    assertEquals(event, bus.publish(event));
  }

  @Test
  public void priorityOrderingFiresLowestFirst() {
    List<EventPriority> order = new ArrayList<>();

    bus.subscribe(ConcreteEvent.class, EventPriority.HIGH, e -> order.add(EventPriority.HIGH));
    bus.subscribe(ConcreteEvent.class, EventPriority.LOWEST, e -> order.add(EventPriority.LOWEST));
    bus.subscribe(ConcreteEvent.class, EventPriority.MONITOR, e -> order.add(EventPriority.MONITOR));
    bus.subscribe(ConcreteEvent.class, EventPriority.NORMAL, e -> order.add(EventPriority.NORMAL));
    bus.subscribe(ConcreteEvent.class, EventPriority.LOW, e -> order.add(EventPriority.LOW));
    bus.subscribe(ConcreteEvent.class, EventPriority.HIGHEST, e -> order.add(EventPriority.HIGHEST));

    bus.publish(new ConcreteEvent());

    assertEquals(List.of(
        EventPriority.LOWEST,
        EventPriority.LOW,
        EventPriority.NORMAL,
        EventPriority.HIGH,
        EventPriority.HIGHEST,
        EventPriority.MONITOR), order);
  }

  @Test
  public void priorityOrderingHonoursInsertionForEqualPriorities() {
    List<String> order = new ArrayList<>();

    bus.subscribe(ConcreteEvent.class, EventPriority.NORMAL, e -> order.add("first"));
    bus.subscribe(ConcreteEvent.class, EventPriority.NORMAL, e -> order.add("second"));
    bus.subscribe(ConcreteEvent.class, EventPriority.NORMAL, e -> order.add("third"));

    bus.publish(new ConcreteEvent());

    assertEquals(List.of("first", "second", "third"), order);
  }

  @Test
  public void dispatchIncludesDirectSuperinterface() {
    AtomicInteger marker = new AtomicInteger();
    AtomicInteger concrete = new AtomicInteger();

    bus.subscribe(MarkerEvent.class, e -> marker.incrementAndGet());
    bus.subscribe(ConcreteEvent.class, e -> concrete.incrementAndGet());

    bus.publish(new ConcreteEvent());

    assertEquals(1, marker.get(), "marker interface listener should fire");
    assertEquals(1, concrete.get(), "concrete listener should fire");
  }

  @Test
  public void dispatchIncludesRootBanManagerEvent() {
    AtomicInteger root = new AtomicInteger();

    bus.subscribe(BanManagerEvent.class, e -> root.incrementAndGet());

    bus.publish(new ConcreteEvent());

    assertEquals(1, root.get(), "root marker subscription should observe every event");
  }

  @Test
  public void dispatchIncludesIndirectInterfaceViaAbstractClass() {
    AtomicInteger cancellableHits = new AtomicInteger();
    AtomicInteger markerHits = new AtomicInteger();

    bus.subscribe(CancellableEvent.class, e -> cancellableHits.incrementAndGet());
    bus.subscribe(MarkerEvent.class, e -> markerHits.incrementAndGet());

    bus.publish(new CancellableConcreteEvent());

    assertEquals(1, cancellableHits.get(),
        "CancellableEvent listener should fire for an event whose direct supertype is AbstractCancellableEvent");
    assertEquals(1, markerHits.get(),
        "MarkerEvent listener should fire even though the event extends an abstract class first");
  }

  @Test
  public void dispatchWalksAbstractSuperclassChain() {
    AtomicInteger leaf = new AtomicInteger();
    AtomicInteger root = new AtomicInteger();

    bus.subscribe(LeafEvent.class, e -> leaf.incrementAndGet());
    bus.subscribe(BanManagerEvent.class, e -> root.incrementAndGet());

    bus.publish(new LeafEvent());

    assertEquals(1, leaf.get());
    assertEquals(1, root.get(),
        "BanManagerEvent listener should fire even when LeafEvent's supertype is abstract");
  }

  @Test
  public void supertypeListenersAreNotInvokedTwiceForDiamondHierarchy() {
    AtomicInteger root = new AtomicInteger();

    bus.subscribe(BanManagerEvent.class, e -> root.incrementAndGet());

    bus.publish(new CancellableConcreteEvent());

    assertEquals(1, root.get(),
        "BanManagerEvent must only fire once even though it's reachable via both class and interface");
  }

  @Test
  public void cancelledEventsSkipLaterListenersByDefault() {
    AtomicInteger first = new AtomicInteger();
    AtomicInteger second = new AtomicInteger();

    bus.subscribe(CancellableConcreteEvent.class, EventPriority.LOW, e -> {
      first.incrementAndGet();
      e.cancel();
    });
    bus.subscribe(CancellableConcreteEvent.class, EventPriority.HIGH, e -> second.incrementAndGet());

    bus.publish(new CancellableConcreteEvent());

    assertEquals(1, first.get());
    assertEquals(0, second.get(), "second listener should be skipped because event was cancelled");
  }

  @Test
  public void ignoreCancelledListenersStillFireWhenCancelled() {
    AtomicInteger second = new AtomicInteger();

    bus.subscribe(CancellableConcreteEvent.class, EventPriority.LOW, false, CancellableEvent::cancel);
    bus.subscribe(CancellableConcreteEvent.class, EventPriority.HIGH, true, e -> second.incrementAndGet());

    bus.publish(new CancellableConcreteEvent());

    assertEquals(1, second.get(),
        "ignoreCancelled=true listener should fire even after a prior listener cancelled the event");
  }

  @Test
  public void uncancelAllowsLaterListenersToProceed() {
    AtomicInteger third = new AtomicInteger();

    bus.subscribe(CancellableConcreteEvent.class, EventPriority.LOW, CancellableEvent::cancel);
    bus.subscribe(CancellableConcreteEvent.class, EventPriority.NORMAL, true, CancellableEvent::uncancel);
    bus.subscribe(CancellableConcreteEvent.class, EventPriority.HIGH, e -> third.incrementAndGet());

    bus.publish(new CancellableConcreteEvent());

    assertEquals(1, third.get(),
        "after uncancel(), default-cancellation-respecting listeners should resume firing");
  }

  @Test
  public void throwingHandlerDoesNotInterruptOthersAndIsLogged() {
    AtomicInteger after = new AtomicInteger();

    bus.subscribe(ConcreteEvent.class, EventPriority.LOW, e -> {
      throw new RuntimeException("boom");
    });
    bus.subscribe(ConcreteEvent.class, EventPriority.HIGH, e -> after.incrementAndGet());

    bus.publish(new ConcreteEvent());

    assertEquals(1, after.get(), "later listeners should still run after a sibling throws");
    assertEquals(1, logger.warningMessages.size(), "the failure should be logged exactly once");
    assertNotNull(logger.warningThrowables.get(0));
    assertEquals("boom", logger.warningThrowables.get(0).getMessage());
  }

  @Test
  public void errorLogIncludesHandlerIdentity() {
    bus.subscribe(ConcreteEvent.class, new NamedHandler());
    bus.publish(new ConcreteEvent());

    assertEquals(1, logger.warningMessages.size());
    String msg = logger.warningMessages.get(0);
    assertTrue(msg.contains(NamedHandler.class.getName()),
        "warning should name the offending handler class; was: " + msg);
    assertTrue(msg.contains(ConcreteEvent.class.getName()),
        "warning should name the event type; was: " + msg);
  }

  @Test
  public void errorLogIdentifiesLambdaByEnclosingClass() {
    bus.subscribe(ConcreteEvent.class, e -> {
      throw new RuntimeException("lambda boom");
    });
    bus.publish(new ConcreteEvent());

    assertEquals(1, logger.warningMessages.size());
    String msg = logger.warningMessages.get(0);
    assertTrue(msg.contains(EventBusImplTest.class.getName()),
        "lambda warning should name the enclosing class; was: " + msg);
    assertTrue(msg.contains("lambda"),
        "lambda warning should identify the handler as a lambda; was: " + msg);
  }

  @Test
  public void unsubscribeStopsFurtherDispatch() {
    AtomicInteger counter = new AtomicInteger();
    Subscription sub = bus.subscribe(ConcreteEvent.class, e -> counter.incrementAndGet());

    bus.publish(new ConcreteEvent());
    assertEquals(1, counter.get());

    assertFalse(sub.isCancelled());
    sub.unsubscribe();
    assertTrue(sub.isCancelled());

    bus.publish(new ConcreteEvent());
    assertEquals(1, counter.get(), "unsubscribed handler must not fire");
  }

  @Test
  public void unsubscribeIsIdempotent() {
    Subscription sub = bus.subscribe(ConcreteEvent.class, e -> {});
    sub.unsubscribe();
    sub.unsubscribe();
    assertTrue(sub.isCancelled());
  }

  @Test
  public void publishWithNoSubscribersIsNoop() {
    ConcreteEvent event = new ConcreteEvent();
    assertEquals(event, bus.publish(event));
    assertEquals(0, logger.warningMessages.size());
  }

  /**
   * Concrete (non-lambda) handler so we can assert the class name appears in
   * the error log without relying on the JVM's lambda naming scheme.
   */
  static final class NamedHandler implements java.util.function.Consumer<ConcreteEvent> {
    @Override
    public void accept(ConcreteEvent concreteEvent) {
      throw new IllegalStateException("named handler boom");
    }
  }

  /**
   * Captures messages routed through {@link CommonLogger} so tests can assert
   * what got logged without going through real JUL infrastructure. Overrides
   * {@code warning(String, Throwable)} so we can keep the throwable alongside
   * the message instead of letting the default impl flatten it onto a second
   * call to {@code warning(String)}.
   */
  static final class RecordingLogger implements CommonLogger {
    final List<String> warningMessages = new ArrayList<>();
    final List<Throwable> warningThrowables = new ArrayList<>();

    @Override public void info(String s) { /* tests don't assert on info output */ }

    @Override public void warning(String s) {
      warningMessages.add(s);
      warningThrowables.add(null);
    }

    @Override public void warning(String s, Throwable t) {
      warningMessages.add(s);
      warningThrowables.add(t);
    }

    @Override public void severe(String s) { /* tests don't assert on severe output */ }
  }
}
