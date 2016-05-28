package me.confuser.banmanager.util;

import com.sampullara.cli.Args;
import com.sampullara.cli.Argument;
import lombok.Getter;
import me.confuser.banmanager.util.parsers.Reason;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.util.StringUtil;

import java.util.Arrays;
import java.util.List;

public class CommandParser {

  @Getter
  protected String[] args;

  @Argument(alias = "s")
  @Getter
  private boolean silent = false;

  @Argument(alias = "st")
  @Getter
  private boolean soft = false;

  @Getter
  private Reason reason;


  public CommandParser(String[] args) {
    List<String> parsedArgs = Args.parse(this, args, false);
    this.args = parsedArgs.toArray(new String[parsedArgs.size()]);
  }

  public CommandParser(String[] args, int start) {
    reason = CommandUtils.getReason(start, args);
    String[] newArgs = reason.getMessage().split(" ");

    if (args.length > start) {
      // @TODO inefficient
      for (int i = start - 1; i >= 0; i--) {
        newArgs = (String[]) ArrayUtils.add(newArgs, 0, args[i]);
      }
    }

    List<String> parsedArgs = Args.parse(this, newArgs, false);
    this.args = parsedArgs.toArray(new String[parsedArgs.size()]);

    reason.setMessage(StringUtils.join(this.args, " ", start, this.args.length));
  }

}
