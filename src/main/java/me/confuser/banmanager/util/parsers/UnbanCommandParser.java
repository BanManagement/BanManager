package me.confuser.banmanager.util.parsers;

import com.sampullara.cli.Args;
import com.sampullara.cli.Argument;
import lombok.Getter;
import me.confuser.banmanager.util.CommandUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import java.util.List;

public class UnbanCommandParser {

  @Getter
  protected String[] args;

  @Argument(alias = "d")
  @Getter
  private boolean delete = false;

  @Getter
  private Reason reason;

  // @TODO Reduce duplication with CommandParser
  public UnbanCommandParser(String[] args, int start) {
    reason = CommandUtils.getReason(start, args);

    if (reason.getMessage().length() == 0) {
      List<String> parsedArgs = Args.parse(this, args, false);
      this.args = parsedArgs.toArray(new String[parsedArgs.size()]);

      return;
    }

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
