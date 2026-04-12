package me.confuser.banmanager.common.util;

import org.junit.Before;
import org.junit.Test;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

public class ConfirmationManagerTest {

  private ConfirmationManager manager;

  @Before
  public void setUp() {
    manager = ConfirmationManager.getInstance();
  }

  @Test
  public void submitAndGetThenExecute() {
    UUID playerId = UUID.randomUUID();
    AtomicBoolean executed = new AtomicBoolean(false);

    manager.submit(playerId, "test action", () -> executed.set(true));

    ConfirmationManager.PendingAction action = manager.get(playerId);
    assertNotNull(action);

    manager.cancel(playerId);
    assertNull(manager.get(playerId));

    action.getAction().run();
    assertTrue(executed.get());
  }

  @Test
  public void submitAndCancel() {
    UUID playerId = UUID.randomUUID();
    AtomicBoolean executed = new AtomicBoolean(false);

    manager.submit(playerId, "test action", () -> executed.set(true));

    assertTrue(manager.cancel(playerId));
    assertFalse(executed.get());
    assertNull(manager.get(playerId));
  }

  @Test
  public void cancelWithoutPending() {
    UUID playerId = UUID.randomUUID();
    assertFalse(manager.cancel(playerId));
  }

  @Test
  public void pendingActionDescription() {
    UUID playerId = UUID.randomUUID();
    manager.submit(playerId, "ban Steve", () -> {});

    ConfirmationManager.PendingAction action = manager.get(playerId);
    assertNotNull(action);
    assertEquals("ban Steve", action.getDescription());

    manager.cancel(playerId);
  }
}
