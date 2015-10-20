package me.confuser.banmanager.util.parsers;

import com.sampullara.cli.Argument;
import lombok.Getter;
import me.confuser.banmanager.util.CommandParser;

public class SoftCommandParser extends CommandParser {
  @Argument(alias = "st")
  @Getter
  private boolean soft = false;

  public SoftCommandParser(String[] args) {
    super(args);
  }
}
