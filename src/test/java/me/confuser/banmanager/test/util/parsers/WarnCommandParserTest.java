package me.confuser.banmanager.test.util.parsers;

import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.configs.DefaultConfig;
import me.confuser.banmanager.util.parsers.WarnCommandParser;
import me.confuser.banmanager.common.configuration.InvalidConfigurationException;
import me.confuser.banmanager.common.configuration.file.YamlConfiguration;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

@RunWith(PowerMockRunner.class)
@PrepareForTest(fullyQualifiedNames = "me.confuser.banmanager.*")
public class WarnCommandParserTest {

  @Before
  public void setup() throws IOException, InvalidConfigurationException {
    BanManager plugin = Mockito.mock(BanManager.class);

    PowerMockito.mockStatic(BanManager.class);
    Mockito.when(BanManager.getPlugin()).thenReturn(plugin);

    File configFile = new File(getClass().getResource("/config.yml").getPath());

    YamlConfiguration configuration = new YamlConfiguration();
    configuration.load(configFile.getAbsolutePath());

    DefaultConfig defaultConfig = new DefaultConfig(configFile);

    Mockito.doReturn(configuration).when(plugin).getConfig();
    Mockito.doReturn(defaultConfig).when(plugin).getConfiguration();
  }

  @Test
  public void parsesSilentFlag() {
    String[] args = new String[] { "confuser", "-s" };
    WarnCommandParser parser = new WarnCommandParser(args, 1);

    assert (parser.isSilent());
    assertEquals("confuser", parser.getArgs()[0]);
    assertEquals(1, parser.getArgs().length);
  }

  @Test
  public void parsesPointsFlag() {
    String[] args = new String[] { "confuser", "-p 5" };
    WarnCommandParser parser = new WarnCommandParser(args, 1);

    assertEquals("confuser", parser.getArgs()[0]);
    assertEquals(5.0, parser.getPoints(), 0.0);
    assertEquals(1, parser.getArgs().length);
  }

  @Test
  public void parsesPointsFlagWithReason() {
    String[] args = new String[] { "confuser", "-p 5", "Testing", "Hello" };
    WarnCommandParser parser = new WarnCommandParser(args, 1);

    assertEquals("confuser", parser.getArgs()[0]);
    assertEquals(5.0, parser.getPoints(), 0.0);
    assertEquals("Testing Hello", parser.getReason().getMessage());
  }
}
