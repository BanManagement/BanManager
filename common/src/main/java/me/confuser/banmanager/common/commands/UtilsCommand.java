package me.confuser.banmanager.common.commands;

import com.google.common.collect.ImmutableList;
import me.confuser.banmanager.common.commands.utils.MissingPlayersSubCommand;
import me.confuser.banmanager.common.command.abstraction.Command;
import me.confuser.banmanager.common.command.abstraction.MainCommand;
import me.confuser.banmanager.common.locale.LocaleManager;
import me.confuser.banmanager.common.locale.command.CommandSpec;
import me.confuser.banmanager.common.plugin.BanManagerPlugin;
import me.confuser.banmanager.common.sender.Sender;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class UtilsCommand extends MainCommand<String, String> {

  public UtilsCommand(LocaleManager locale) {
    super(CommandSpec.BMUTILS.localize(locale), "bmutils", 0, ImmutableList.<Command<String, ?>>builder()
    .add(new MissingPlayersSubCommand(locale)).build()
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
