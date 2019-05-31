package me.confuser.banmanager.util.parsers;

import com.sampullara.cli.Args;
import com.sampullara.cli.Argument;
import lombok.Getter;

import java.util.List;

public class InfoCommandParser {

  @Getter
  private final String[] args;

  @Argument(alias = "k")
  @Getter
  private boolean kicks = false;

  @Argument(alias = "w")
  @Getter
  private boolean warnings = false;

  @Argument(alias = "b")
  @Getter
  private boolean bans = false;

  @Argument(alias = "m")
  @Getter
  private boolean mutes = false;

  @Argument(alias = "n")
  @Getter
  private boolean notes = false;

  @Argument(alias = "t")
  @Getter
  private String time;

  @Argument(alias = "i")
  @Getter
  private Integer ips;

  public InfoCommandParser(String[] args) throws IllegalArgumentException {
    List<String> parsedArgs = Args.parse(this, args, false);
    this.args = parsedArgs.toArray(new String[parsedArgs.size()]);
  }

}
