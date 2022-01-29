package me.confuser.banmanager.common.commands;

import lombok.Getter;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.cli.Args;
import me.confuser.banmanager.common.cli.Argument;
import me.confuser.banmanager.common.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CommandParser {

  @Getter
  protected String[] args;

  private BanManagerPlugin plugin;

  @Argument(alias = "s")
  @Getter
  private boolean silent = false;

  @Argument(alias = "st")
  @Getter
  private boolean soft = false;

  @Getter
  private Reason reason;

  @Getter
  private boolean invalidReason = false;


  public CommandParser(BanManagerPlugin plugin, String[] args) {
    this.plugin = plugin;

    List<String> parsedArgs = Args.parse(this, args, false);
    this.args = parsedArgs.toArray(new String[parsedArgs.size()]);
  }

  public CommandParser(BanManagerPlugin plugin, String[] args, int start) {
    this.plugin = plugin;
    this.args = args;
    reason = getReason(start);
    ArrayList<String> newArgs = new ArrayList<>();
    String[] reasonArgs = reason.getMessage().split(" ");

    if (reasonArgs.length > 0 && !reasonArgs[0].isEmpty()) {
      Collections.addAll(newArgs, reasonArgs);
    }

    if (args.length > start) {
      // @TODO inefficient
      for (int i = start - 1; i >= 0; i--) {
        newArgs.add(0, args[i]);
      }
    }

    List<String> parsedArgs = Args.parse(this, newArgs.toArray(new String[0]), false);
    this.args = parsedArgs.toArray(new String[parsedArgs.size()]);

    reason.setMessage(StringUtils.join(this.args, " ", start, this.args.length));
  }

  public Reason getReason(int start) {
    String reason = StringUtils.join(args, " ", start, args.length);
    List<String> notes = new ArrayList<>();

    String[] matches = null;
    if (plugin.getConfig().isCreateNoteReasons()) {
      matches = StringUtils.substringsBetween(reason, "(", ")");
    }

    if (matches != null) notes = Arrays.asList(matches);

    for (int i = start; i < args.length; i++) {
      if (!args[i].startsWith("#")) continue;

      String key = args[i].replace("#", "");
      String replace = plugin.getReasonsConfig().getReason(key);

      if (replace != null) reason = reason.replace("#" + key, replace);
      if (replace != null) {
        reason = reason.replace("#" + key, replace);
      } else if(plugin.getConfig().isBlockInvalidReasons()) {
        invalidReason = true;
      }
    }

    for (String note : notes) {
      reason = reason.replace("(" + note + ")", "");
    }

    reason = reason.trim();

    return new Reason(reason, notes);
  }

}
