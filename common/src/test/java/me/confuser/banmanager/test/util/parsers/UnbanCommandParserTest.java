package me.confuser.banmanager.test.util.parsers;

import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.configs.DefaultConfig;
import me.confuser.banmanager.util.parsers.UnbanCommandParser;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
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
public class UnbanCommandParserTest {

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
  public void parsesDeleteFlag() {
    String[] args = new String[] { "confuser", "-d" };
    UnbanCommandParser parser = new UnbanCommandParser(args, 1);

    assert (parser.isDelete());
    assertEquals("confuser", parser.getArgs()[0]);
    assertEquals(1, parser.getArgs().length);
  }

  @Test
  public void parsesDeleteFlagWithReason() {
    String[] args = new String[] { "confuser", "-d", "Testing", "Hello" };
    UnbanCommandParser parser = new UnbanCommandParser(args, 1);

    assert (parser.isDelete());
    assertEquals("confuser", parser.getArgs()[0]);
    assertEquals("Testing Hello", parser.getReason().getMessage());
  }

  @Test
  public void handlesNoReason() {
    String[] args = new String[] { "confuser" };
    UnbanCommandParser parser = new UnbanCommandParser(args, 1);

    assertEquals(false, parser.isDelete());
    assertEquals("confuser", parser.getArgs()[0]);
  }
}
