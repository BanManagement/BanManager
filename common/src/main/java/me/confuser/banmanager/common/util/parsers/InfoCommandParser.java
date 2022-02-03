package me.confuser.banmanager.common.util.parsers;

import lombok.Getter;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.cli.Argument;
import me.confuser.banmanager.common.commands.CommandParser;

public class InfoCommandParser extends CommandParser {

  @Argument(alias = "k")
  @Getter
  private boolean kicks;

  @Argument(alias = "w")
  @Getter
  private boolean warnings;

  @Argument(alias = "b")
  @Getter
  private boolean bans;

  @Argument(alias = "m")
  @Getter
  private boolean mutes;

  @Argument(alias = "n")
  @Getter
  private boolean notes;

  @Argument(alias = "t")
  @Getter
  private String time;

  @Argument(alias = "i")
  @Getter
  private Integer ips;

  public InfoCommandParser(BanManagerPlugin plugin, String[] args) {
    super(plugin, args);
  }

  public InfoCommandParser(BanManagerPlugin plugin, String[] args, int start) {
    super(plugin, args, start);
  }

}
