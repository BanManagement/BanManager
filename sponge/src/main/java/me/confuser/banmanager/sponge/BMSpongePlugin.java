package me.confuser.banmanager.sponge;

import com.google.inject.Inject;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.CommonLogger;
import ninja.leaping.configurate.ConfigurationNode;
import org.slf4j.Logger;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;

import java.io.IOException;
import java.nio.file.Path;

@Plugin(
        id = "banmanager",
        name = "BanManager",
        version = "@version@",
        authors = "confuser",
        description = "A punishment plugin",
        url = "https://banmanagement.com"
)
public class BMSpongePlugin {

  @Inject
  private CommonLogger logger;
  private BanManagerPlugin plugin;

  @Inject
  @ConfigDir(sharedRoot = false)
  private Path dataFolder;

  @Inject
  private PluginContainer pluginContainer;

  @Inject
  public BMSpongePlugin(Logger logger) throws IOException {
    this.logger = new PluginLogger(logger);

    this.plugin = new BanManagerPlugin(this.logger, dataFolder.toFile());

//    YAMLConfigurationLoader.@NonNull Builder builder = YAMLConfigurationLoader.builder()
//                                                                              .setFile(new File(dataFolder, "config.yml");
//
//    builder.build().
  }

  @Listener
  public void onDisable(GameStoppingServerEvent event) {
    this.plugin.disable();
  }

  @Listener
  public void onEnable(GamePreInitializationEvent event) {
    this.plugin.enable();
  }

  public CommonLogger getLogger() {
    return logger;
  }

  private CommonConfig loadCommonConfig(ConfigurationNode config) {


    CommonConfig commonConfig = new CommonConfig(
            new LocalDatabaseConfig(config.getChildrenMap().entrySet()),
            globalConfig, timeLimits, mutedBlacklistCommands);
  }
}
