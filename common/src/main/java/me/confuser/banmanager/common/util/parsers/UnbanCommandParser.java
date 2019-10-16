package me.confuser.banmanager.common.util.parsers;

import com.sampullara.cli.Argument;
import lombok.Getter;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.commands.CommandParser;

public class UnbanCommandParser extends CommandParser {

  @Argument(alias = "d")
  @Getter
  private boolean delete;

  public UnbanCommandParser(BanManagerPlugin plugin, String[] args) {
    super(plugin, args);
  }

  public UnbanCommandParser(BanManagerPlugin plugin, String[] args, int start) {
    super(plugin, args, start);
  }
}
