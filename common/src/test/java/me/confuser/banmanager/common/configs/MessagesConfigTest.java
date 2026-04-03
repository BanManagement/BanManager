package me.confuser.banmanager.common.configs;

import me.confuser.banmanager.common.BasePluginTest;
import me.confuser.banmanager.common.TestLogger;
import me.confuser.banmanager.common.configuration.file.YamlConfiguration;
import me.confuser.banmanager.common.util.Message;
import org.junit.Test;

import java.io.StringReader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class MessagesConfigTest extends BasePluginTest {

  @Test
  public void isValid() {
    // @TODO test tokens for all messages
    assertEquals("&6Currently banned for &4abc&6 by def at 8th July which expires in 1d", Message
            .get("info.ban.temporary").set("reason", "abc").set("actor", "def").set("created",
                    "8th July").set("expires", "1d").toString());
  }

  @Test
  public void doubleNewlineProducesNonEmptyLine() {
    String yaml = "messages:\n  test:\n    greeting: 'Hello\\n\\nWorld'";
    YamlConfiguration config = YamlConfiguration.loadConfiguration(new StringReader(yaml));
    Message.load(config, new TestLogger());

    String result = Message.get("test.greeting").toString();

    assertEquals("Hello\n \nWorld", result);
  }

  @Test
  public void tripleNewlineProducesNonEmptyLines() {
    String yaml = "messages:\n  test:\n    greeting: 'Hello\\n\\n\\nWorld'";
    YamlConfiguration config = YamlConfiguration.loadConfiguration(new StringReader(yaml));
    Message.load(config, new TestLogger());

    String result = Message.get("test.greeting").toString();

    assertEquals("Hello\n \n \nWorld", result);
  }

  @Test
  public void singleNewlineIsUnchanged() {
    String yaml = "messages:\n  test:\n    greeting: 'Hello\\nWorld'";
    YamlConfiguration config = YamlConfiguration.loadConfiguration(new StringReader(yaml));
    Message.load(config, new TestLogger());

    String result = Message.get("test.greeting").toString();

    assertEquals("Hello\nWorld", result);
  }

  @Test
  public void noConsecutiveEmptyLinesInLoadedMessages() {
    String yaml = "messages:\n  test:\n    msg: 'Line1\\n\\nLine2\\n\\n\\nLine3'";
    YamlConfiguration config = YamlConfiguration.loadConfiguration(new StringReader(yaml));
    Message.load(config, new TestLogger());

    String result = Message.get("test.msg").toString();

    for (String line : result.split("\n", -1)) {
      assertFalse("Empty lines should contain a space for chat rendering", line.isEmpty());
    }
  }
}
