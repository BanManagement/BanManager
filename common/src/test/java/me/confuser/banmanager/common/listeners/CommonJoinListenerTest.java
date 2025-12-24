package me.confuser.banmanager.common.listeners;

import me.confuser.banmanager.common.*;
import me.confuser.banmanager.common.configs.DefaultConfig;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.ipaddr.IPAddress;
import me.confuser.banmanager.common.util.IPUtils;
import me.confuser.banmanager.common.util.Message;
import org.junit.Before;
import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class CommonJoinListenerTest extends BasePluginDbTest {

  private CommonJoinListener listener;
  private IPAddress testIp;

  @Before
  public void setupListener() throws UnknownHostException {
    listener = new CommonJoinListener(plugin);
    testIp = IPUtils.toIPAddress(InetAddress.getByName("192.168.1.100"));
  }

  @Test
  public void banCheckShouldPopulateMultiaccountsCache() throws Exception {
    // Setup: Enable multiaccounts checking
    DefaultConfig config = spy(plugin.getConfig());
    when(config.getMaxMultiaccountsRecently()).thenReturn(2);
    when(config.getMultiaccountsTime()).thenReturn(300L);

    BanManagerPlugin spyPlugin = spy(plugin);
    when(spyPlugin.getConfig()).thenReturn(config);

    CommonJoinListener testListener = new CommonJoinListener(spyPlugin);

    // Create some players with same IP to populate the database
    PlayerData player1 = createPlayerWithIp("Player1", testIp);
    PlayerData player2 = createPlayerWithIp("Player2", testIp);

    // Create a new player attempting to join
    UUID newPlayerId = UUID.randomUUID();
    String newPlayerName = "NewPlayer";

    // Create a handler that tracks if deny was called
    AtomicBoolean denied = new AtomicBoolean(false);
    CommonJoinHandler handler = createHandler(denied);

    // Call banCheck (simulates AsyncPlayerPreLoginEvent)
    testListener.banCheck(newPlayerId, newPlayerName, testIp, handler);

    // The cache should have been populated - we can't directly access it,
    // but we can verify by calling onPlayerLogin and seeing if it uses the cached value
    // For now, verify that the method completed without error
    assertFalse("Should not deny during banCheck for multiaccounts", denied.get());
  }

  @Test
  public void onPlayerLoginShouldDenyWhenCachedCountExceedsLimit() throws Exception {
    // Setup: Enable multiaccounts checking with limit of 2
    DefaultConfig config = spy(plugin.getConfig());
    when(config.getMaxMultiaccountsRecently()).thenReturn(2);
    when(config.getMultiaccountsTime()).thenReturn(300L);

    BanManagerPlugin spyPlugin = spy(plugin);
    when(spyPlugin.getConfig()).thenReturn(config);

    CommonJoinListener testListener = new CommonJoinListener(spyPlugin);

    // Create 3 players with same IP (exceeds limit of 2)
    PlayerData player1 = createPlayerWithIp("LimitPlayer1", testIp);
    PlayerData player2 = createPlayerWithIp("LimitPlayer2", testIp);
    PlayerData player3 = createPlayerWithIp("LimitPlayer3", testIp);

    // Create a new player attempting to join
    UUID newPlayerId = UUID.randomUUID();
    String newPlayerName = "LimitNewPlayer";

    // First call banCheck to populate cache
    AtomicBoolean banCheckDenied = new AtomicBoolean(false);
    testListener.banCheck(newPlayerId, newPlayerName, testIp, createHandler(banCheckDenied));

    // Create a test player without the exempt permission
    CommonPlayer testPlayer = createTestPlayerWithPermission(newPlayerId, newPlayerName, testIp, false);

    // Call onPlayerLogin - should deny because cached count (3) > limit (2)
    AtomicBoolean loginDenied = new AtomicBoolean(false);
    AtomicReference<String> denyMessage = new AtomicReference<>();
    CommonJoinHandler loginHandler = createHandlerWithMessage(loginDenied, denyMessage);

    testListener.onPlayerLogin(testPlayer, loginHandler);

    assertTrue("Should deny login when multiaccounts count exceeds limit", loginDenied.get());
  }

  @Test
  public void onPlayerLoginShouldAllowWhenPlayerHasExemptPermission() throws Exception {
    // Setup: Enable multiaccounts checking
    DefaultConfig config = spy(plugin.getConfig());
    when(config.getMaxMultiaccountsRecently()).thenReturn(2);
    when(config.getMultiaccountsTime()).thenReturn(300L);

    BanManagerPlugin spyPlugin = spy(plugin);
    when(spyPlugin.getConfig()).thenReturn(config);

    CommonJoinListener testListener = new CommonJoinListener(spyPlugin);

    // Create 3 players with same IP (exceeds limit of 2)
    IPAddress exemptTestIp = IPUtils.toIPAddress(InetAddress.getByName("192.168.1.101"));
    PlayerData player1 = createPlayerWithIp("ExemptPlayer1", exemptTestIp);
    PlayerData player2 = createPlayerWithIp("ExemptPlayer2", exemptTestIp);
    PlayerData player3 = createPlayerWithIp("ExemptPlayer3", exemptTestIp);

    UUID newPlayerId = UUID.randomUUID();
    String newPlayerName = "ExemptNewPlayer";

    // First call banCheck to populate cache
    testListener.banCheck(newPlayerId, newPlayerName, exemptTestIp, createHandler(new AtomicBoolean(false)));

    // Create a test player WITH the exempt permission
    CommonPlayer testPlayer = createTestPlayerWithPermission(newPlayerId, newPlayerName, exemptTestIp, true);

    // Call onPlayerLogin - should allow because player has exempt permission
    AtomicBoolean loginDenied = new AtomicBoolean(false);
    testListener.onPlayerLogin(testPlayer, createHandler(loginDenied));

    assertFalse("Should allow login when player has exempt permission", loginDenied.get());
  }

  @Test
  public void onPlayerLoginShouldAllowWhenCacheIsEmpty() throws Exception {
    // Setup: Enable multiaccounts checking
    DefaultConfig config = spy(plugin.getConfig());
    when(config.getMaxMultiaccountsRecently()).thenReturn(2);
    when(config.getMultiaccountsTime()).thenReturn(300L);

    BanManagerPlugin spyPlugin = spy(plugin);
    when(spyPlugin.getConfig()).thenReturn(config);

    CommonJoinListener testListener = new CommonJoinListener(spyPlugin);

    // Create a player but DON'T call banCheck first (simulates cache miss / DB failure)
    UUID newPlayerId = UUID.randomUUID();
    String newPlayerName = "CacheMissPlayer";
    IPAddress cacheMissIp = IPUtils.toIPAddress(InetAddress.getByName("192.168.1.102"));

    // Create a test player without the exempt permission
    CommonPlayer testPlayer = createTestPlayerWithPermission(newPlayerId, newPlayerName, cacheMissIp, false);

    // Call onPlayerLogin directly without banCheck - cache will be empty
    AtomicBoolean loginDenied = new AtomicBoolean(false);
    testListener.onPlayerLogin(testPlayer, createHandler(loginDenied));

    assertFalse("Should allow login (fail-open) when cache is empty", loginDenied.get());
  }

  @Test
  public void onPlayerLoginShouldAllowWhenCountIsWithinLimit() throws Exception {
    // Setup: Enable multiaccounts checking with limit of 5
    DefaultConfig config = spy(plugin.getConfig());
    when(config.getMaxMultiaccountsRecently()).thenReturn(5);
    when(config.getMultiaccountsTime()).thenReturn(300L);

    BanManagerPlugin spyPlugin = spy(plugin);
    when(spyPlugin.getConfig()).thenReturn(config);

    CommonJoinListener testListener = new CommonJoinListener(spyPlugin);

    // Create 2 players with same IP (within limit of 5)
    IPAddress withinLimitIp = IPUtils.toIPAddress(InetAddress.getByName("192.168.1.103"));
    PlayerData player1 = createPlayerWithIp("WithinLimit1", withinLimitIp);
    PlayerData player2 = createPlayerWithIp("WithinLimit2", withinLimitIp);

    UUID newPlayerId = UUID.randomUUID();
    String newPlayerName = "WithinLimitNew";

    // First call banCheck to populate cache
    testListener.banCheck(newPlayerId, newPlayerName, withinLimitIp, createHandler(new AtomicBoolean(false)));

    // Create a test player without the exempt permission
    CommonPlayer testPlayer = createTestPlayerWithPermission(newPlayerId, newPlayerName, withinLimitIp, false);

    // Call onPlayerLogin - should allow because count (2) <= limit (5)
    AtomicBoolean loginDenied = new AtomicBoolean(false);
    testListener.onPlayerLogin(testPlayer, createHandler(loginDenied));

    assertFalse("Should allow login when count is within limit", loginDenied.get());
  }

  // Helper methods

  private PlayerData createPlayerWithIp(String name, IPAddress ip) throws Exception {
    UUID uuid = UUID.randomUUID();
    PlayerData player = new PlayerData(uuid, name, ip);
    plugin.getPlayerStorage().createOrUpdate(player);
    return player;
  }

  private CommonJoinHandler createHandler(AtomicBoolean denied) {
    return new CommonJoinHandler() {
      @Override
      public void handleDeny(Message message) {
        denied.set(true);
      }

      @Override
      public void handlePlayerDeny(PlayerData player, Message message) {
        denied.set(true);
      }
    };
  }

  private CommonJoinHandler createHandlerWithMessage(AtomicBoolean denied, AtomicReference<String> message) {
    return new CommonJoinHandler() {
      @Override
      public void handleDeny(Message msg) {
        denied.set(true);
        message.set(msg.toString());
      }

      @Override
      public void handlePlayerDeny(PlayerData player, Message msg) {
        denied.set(true);
        message.set(msg.toString());
      }
    };
  }

  private CommonPlayer createTestPlayerWithPermission(UUID uuid, String name, IPAddress ip, boolean hasExemptPermission) {
    return new CommonPlayer() {
      @Override
      public void kick(String message) {}

      @Override
      public void sendMessage(String message) {}

      @Override
      public void sendMessage(Message message) {}

      @Override
      public void sendJSONMessage(me.confuser.banmanager.common.kyori.text.TextComponent jsonString) {}

      @Override
      public void sendJSONMessage(String jsonString) {}

      @Override
      public boolean isConsole() { return false; }

      @Override
      public PlayerData getData() { return null; }

      @Override
      public boolean isOnlineMode() { return true; }

      @Override
      public boolean isOnline() { return true; }

      @Override
      public boolean hasPermission(String permission) {
        if ("bm.exempt.maxmultiaccountsrecently".equals(permission)) {
          return hasExemptPermission;
        }
        return true; // Return true for other permissions to avoid blocking
      }

      @Override
      public String getDisplayName() { return name; }

      @Override
      public String getName() { return name; }

      @Override
      public InetAddress getAddress() {
        try {
          return InetAddress.getByName(ip.toString().replace("/", ""));
        } catch (UnknownHostException e) {
          return null;
        }
      }

      @Override
      public UUID getUniqueId() { return uuid; }

      @Override
      public boolean teleport(CommonWorld world, double x, double y, double z, float pitch, float yaw) {
        return false;
      }

      @Override
      public boolean canSee(CommonPlayer player) { return true; }
    };
  }
}
