package me.confuser.banmanager.util.parsers;

import com.sampullara.cli.Args;
import com.sampullara.cli.Argument;
import lombok.Getter;
import me.confuser.banmanager.util.CommandUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class WarnCommandParser {

  @Getter
  protected String[] args;

  @Argument(alias = "s")
  @Getter
  private boolean silent = false;

  @Argument(alias = "p")
  @Getter
  private Double points = 1D;

  @Getter
  private Reason reason;

  // @TODO Reduce duplication with CommandParser
  public WarnCommandParser(String[] args, int start) {
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
