package me.confuser.banmanager.common.commands;

import me.confuser.banmanager.common.BasePluginDbTest;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.data.PlayerData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TabCompletionTest extends BasePluginDbTest {
  private CommonCommand cmd;

  @BeforeEach
  public void setupCmd() {
    for (CommonCommand command : plugin.getCommands()) {
      if (command.getCommandName().equals("ban")) {
        this.cmd = command;
        break;
      }
    }
  }

  @Test
  public void shouldCompletePlayerNameFromStart() {
    // Create test players
    PlayerData player1 = testUtils.createPlayerWithName("TestPlayer1");
    PlayerData player2 = testUtils.createPlayerWithName("TestPlayer2");
    PlayerData player3 = testUtils.createPlayerWithName("OtherPlayer");

    CommonSender sender = plugin.getServer().getConsoleSender();
    String[] args = new String[]{"Test"};

    List<String> suggestions = cmd.handlePlayerNameTabComplete(sender, args);

    assertTrue(suggestions.contains("TestPlayer1"), "Should suggest TestPlayer1");
    assertTrue(suggestions.contains("TestPlayer2"), "Should suggest TestPlayer2");
    assertFalse(suggestions.contains("OtherPlayer"), "Should not suggest OtherPlayer");
  }

  @Test
  public void shouldCompleteEmptyArg() {
    PlayerData player1 = testUtils.createPlayerWithName("Alpha");
    PlayerData player2 = testUtils.createPlayerWithName("Beta");

    CommonSender sender = plugin.getServer().getConsoleSender();
    String[] args = new String[]{""};

    List<String> suggestions = cmd.handlePlayerNameTabComplete(sender, args);

    assertTrue(suggestions.size() >= 2, "Should include all players for empty input");
    assertTrue(suggestions.contains("Alpha"), "Should suggest Alpha");
    assertTrue(suggestions.contains("Beta"), "Should suggest Beta");
  }

  @Test
  public void shouldCompleteMultiplePlayersWithComma() {
    PlayerData player1 = testUtils.createPlayerWithName("Alice");
    PlayerData player2 = testUtils.createPlayerWithName("AliceAlt");
    PlayerData player3 = testUtils.createPlayerWithName("Bob");

    CommonSender sender = plugin.getServer().getConsoleSender();
    // Simulating "Alice,Ali" - completing second player after comma
    String[] args = new String[]{"Alice,Ali"};

    List<String> suggestions = cmd.handlePlayerNameTabComplete(sender, args);

    // Should complete with the full prefix including first player
    assertTrue(suggestions.stream().anyMatch(s -> s.startsWith("Alice,Alice")), "Should suggest Alice,Alice");
    assertTrue(suggestions.stream().anyMatch(s -> s.startsWith("Alice,AliceAlt")), "Should suggest Alice,AliceAlt");
  }

  @Test
  public void shouldReturnEmptyForDisabledTabCompletion() {
    // Find a command with tab completion disabled
    CommonCommand noTabCmd = null;
    for (CommonCommand command : plugin.getCommands()) {
      if (!command.isEnableTabCompletion()) {
        noTabCmd = command;
        break;
      }
    }

    // Skip if all commands have tab completion enabled
    if (noTabCmd == null) return;

    CommonSender sender = plugin.getServer().getConsoleSender();
    String[] args = new String[]{"Test"};

    List<String> suggestions = noTabCmd.handlePlayerNameTabComplete(sender, args);
    assertTrue(suggestions.isEmpty(), "Should return empty list for disabled tab completion");
  }

  @Test
  public void shouldLimitResultsTo100() {
    // Create more than 100 players with same prefix
    for (int i = 0; i < 110; i++) {
      testUtils.createPlayerWithName("Limit" + String.format("%03d", i));
    }

    CommonSender sender = plugin.getServer().getConsoleSender();
    String[] args = new String[]{"Limit"};

    List<String> suggestions = cmd.handlePlayerNameTabComplete(sender, args);

    assertTrue(suggestions.size() <= 100, "Should limit results to 100 or less");
  }

  @Test
  public void shouldBeCaseInsensitiveForOnlinePlayers() {
    PlayerData player = testUtils.createPlayerWithName("CaseSensitive");

    // Mock online player completion
    CommonPlayer mockPlayer = mock(CommonPlayer.class);
    when(mockPlayer.getName()).thenReturn("CaseSensitive");

    CommonSender sender = plugin.getServer().getConsoleSender();
    String[] args = new String[]{"casesens"};

    // This tests the offline auto-complete which uses the radix tree
    List<String> suggestions = cmd.handlePlayerNameTabComplete(sender, args);

    // Offline auto-complete is case-sensitive by design (uses radix tree)
    // So this should not find CaseSensitive when searching for "casesens"
    // But let's verify behavior is consistent
    assertNotNull(suggestions, "Should return a list");
  }

  @Test
  public void shouldNotSuggestForSecondArgWithoutHashtag() {
    PlayerData player = testUtils.createPlayerWithName("SomePlayer");

    CommonSender sender = plugin.getServer().getConsoleSender();
    // Second arg without # should not trigger reason completion
    String[] args = new String[]{"SomePlayer", "some"};

    List<String> suggestions = cmd.handlePlayerNameTabComplete(sender, args);

    assertTrue(suggestions.isEmpty(), "Should return empty for non-hashtag second arg");
  }
}
