package me.confuser.banmanager.common.commands;

import com.google.common.collect.ImmutableList;
import me.confuser.banmanager.common.commands.report.*;
import me.confuser.banmanager.common.command.abstraction.Command;
import me.confuser.banmanager.common.command.abstraction.MainCommand;
import me.confuser.banmanager.common.locale.LocaleManager;
import me.confuser.banmanager.common.locale.command.CommandSpec;
import me.confuser.banmanager.common.plugin.BanManagerPlugin;
import me.confuser.banmanager.common.sender.Sender;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class ReportsCommand extends MainCommand<String, String> {

  public ReportsCommand(LocaleManager locale) {
    super(CommandSpec.REPORTS.localize(locale), "reports", 0, ImmutableList.<Command<String, ?>>builder()
    .add(new AssignSubCommand(locale))
            .add(new CloseSubCommand(locale))
            .add(new InfoSubCommand(locale))
            .add(new ListSubCommand(locale))
            .add(new TeleportSubCommand(locale))
            .add(new UnassignSubCommand(locale))
            .build()
    );
  }


  @Override
  protected List<String> getTargets(BanManagerPlugin plugin) {
    return null;
  }

  @Override
  protected String parseTarget(String target, BanManagerPlugin plugin, Sender sender) {
    return null;
  }

  @Override
  protected ReentrantLock getLockForTarget(String target) {
    return null;
  }

  @Override
  protected String getTarget(String target, BanManagerPlugin plugin, Sender sender) {
    return null;
  }

  @Override
  protected void cleanup(String s, BanManagerPlugin plugin) {

  }

}
