package me.confuser.banmanager.test.util.parsers;

import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.configs.DefaultConfig;
import me.confuser.banmanager.util.parsers.InfoCommandParser;
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
public class InfoCommandParserTest {

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
  public void parsesKicksFlag() {
    String[] args = new String[] { "confuser", "-k" };
    InfoCommandParser parser = new InfoCommandParser(args);

    assert (parser.isKicks());
    assertEquals("confuser", parser.getArgs()[0]);
    assertEquals(1, parser.getArgs().length);
  }

  @Test
  public void parsesWarningsFlag() {
    String[] args = new String[] { "confuser", "-w" };
    InfoCommandParser parser = new InfoCommandParser(args);

    assert (parser.isWarnings());
    assertEquals("confuser", parser.getArgs()[0]);
    assertEquals(1, parser.getArgs().length);
  }

  @Test
  public void parsesBansFlag() {
    String[] args = new String[] { "confuser", "-b" };
    InfoCommandParser parser = new InfoCommandParser(args);

    assert (parser.isBans());
    assertEquals("confuser", parser.getArgs()[0]);
    assertEquals(1, parser.getArgs().length);
  }

  @Test
  public void parsesMutesFlag() {
    String[] args = new String[] { "confuser", "-m" };
    InfoCommandParser parser = new InfoCommandParser(args);

    assert (parser.isMutes());
    assertEquals("confuser", parser.getArgs()[0]);
    assertEquals(1, parser.getArgs().length);
  }

  @Test
  public void parsesNotesFlag() {
    String[] args = new String[] { "confuser", "-n" };
    InfoCommandParser parser = new InfoCommandParser(args);

    assert (parser.isNotes());
    assertEquals("confuser", parser.getArgs()[0]);
    assertEquals(1, parser.getArgs().length);
  }

  @Test
  public void parsesTimeFlag() {
    String[] args = new String[] { "confuser", "-t", "5d" };
    InfoCommandParser parser = new InfoCommandParser(args);

    assertEquals("confuser", parser.getArgs()[0]);
    assertEquals("5d", parser.getTime());
    assertEquals(1, parser.getArgs().length);
  }

  @Test
  public void parsesIpsFlag() {
    String[] args = new String[] { "confuser", "-i", "5" };
    InfoCommandParser parser = new InfoCommandParser(args);

    assertEquals("confuser", parser.getArgs()[0]);
    assertEquals(5, parser.getIps(), 0);
    assertEquals(1, parser.getArgs().length);
  }
}
