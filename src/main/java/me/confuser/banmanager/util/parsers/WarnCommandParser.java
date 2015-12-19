package me.confuser.banmanager.util.parsers;

import com.sampullara.cli.Args;
import com.sampullara.cli.Argument;
import lombok.Getter;

import java.util.List;

public class WarnCommandParser {

  @Getter
  protected String[] args;

  @Argument(alias = "s")
  @Getter
  private boolean silent = false;

  @Argument(alias = "p")
  @Getter
  private Integer points = 1;


  public WarnCommandParser(String[] args) {
    List<String> parsedArgs = Args.parse(this, args, false);
    this.args = parsedArgs.toArray(new String[parsedArgs.size()]);
  }

}
