package me.confuser.banmanager.common.configs;

import me.confuser.banmanager.common.BasePluginTest;
import org.junit.Test;

import static org.junit.Assert.*;

public class NotificationsConfigTest extends BasePluginTest {

  @Test
  public void loadsStaffChannels() {
    NotificationsConfig config = plugin.getNotificationsConfig();
    assertNotNull(config);
    assertFalse(config.getStaffChannels().isEmpty());
  }

  @Test
  public void banChannelHasChat() {
    NotificationsConfig.NotificationChannel channel = plugin.getNotificationsConfig().getStaffChannel("ban");
    assertTrue(channel.isChat());
  }

  @Test
  public void banChannelHasSound() {
    NotificationsConfig.NotificationChannel channel = plugin.getNotificationsConfig().getStaffChannel("ban");
    assertTrue(channel.hasSound());
    assertEquals("entity.experience_orb.pickup", channel.getSound());
  }

  @Test
  public void unknownEventReturnsChatOnly() {
    NotificationsConfig.NotificationChannel channel = plugin.getNotificationsConfig().getStaffChannel("nonexistent");
    assertTrue(channel.isChat());
    assertFalse(channel.isActionbar());
    assertFalse(channel.isTitle());
    assertFalse(channel.hasSound());
  }

  @Test
  public void mutedActionBarDefaults() {
    assertTrue(plugin.getNotificationsConfig().isMutedActionBar());
  }
}
