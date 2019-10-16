package me.confuser.banmanager.common.util.parsers;

import com.sampullara.cli.Argument;
import lombok.Getter;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.commands.CommandParser;

public class WarnCommandParser extends CommandParser {

  @Argument(alias = "p")
  @Getter
  private Double points;

  public WarnCommandParser(BanManagerPlugin plugin, String[] args) {
    super(plugin, args);

    if (points == null) points = 1D;
  }

  public WarnCommandParser(BanManagerPlugin plugin, String[] args, int start) {
    super(plugin, args, start);

    if (points == null) points = 1D;
  }

}
