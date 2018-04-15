package me.confuser.banmanager.util;

import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.data.PlayerBanData;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.bukkitutil.Message;
import me.rayzr522.jsonmessage.JSONMessage;
import org.bukkit.ChatColor;

import java.sql.SQLException;
import java.util.List;

public class JSONCommandUtils {
  private static BanManager plugin = BanManager.getPlugin();

  public static JSONMessage alts(List<PlayerData> players) {
    JSONMessage message = JSONMessage.create();
    int index = 0;

    for (PlayerData player : players) {
      ChatColor colour = ChatColor.GREEN;

      if (plugin.getPlayerBanStorage().isBanned(player.getUUID())) {
        PlayerBanData ban = plugin.getPlayerBanStorage().getBan(player.getUUID());

        if (ban.getExpires() == 0) {
          colour = ChatColor.RED;
        } else {
          colour = ChatColor.GOLD;
        }
      } else {
        try {
          if (plugin.getPlayerBanRecordStorage().getCount(player) != 0) {
            colour = ChatColor.YELLOW;
          }
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }

      message.then(player.getName()).color(colour).runCommand("/bminfo " + player.getName());

      if (index != players.size() - 1) {
        message.then(", ");
      }

      index++;
    }

    return message;
  }

  public static JSONMessage notesAmount(String playerName, Message text) {
    JSONMessage message = JSONMessage.create();

    message.then(text.toString()).runCommand("/notes " + playerName);

    return message;
  }
}
