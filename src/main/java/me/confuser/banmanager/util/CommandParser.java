package me.confuser.banmanager.util;

import com.sampullara.cli.Args;
import com.sampullara.cli.Argument;
import lombok.Getter;

import java.util.List;

public class CommandParser {

  @Getter
  private final String[] args;

  @Argument(alias = "s")
  @Getter
  private boolean silent = false;


  public CommandParser(String[] args) {
    List<String> parsedArgs = Args.parse(this, args, false);
    this.args = parsedArgs.toArray(new String[parsedArgs.size()]);
  }

}
