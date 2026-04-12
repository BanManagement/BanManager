package me.confuser.banmanager.common.configs;

import me.confuser.banmanager.common.BasePluginTest;
import me.confuser.banmanager.common.TestLogger;
import me.confuser.banmanager.common.configuration.file.YamlConfiguration;
import me.confuser.banmanager.common.kyori.text.Component;
import me.confuser.banmanager.common.util.Message;
import me.confuser.banmanager.common.util.MessageRenderer;
import me.confuser.banmanager.common.util.MessageRegistry;
import org.junit.Test;

import java.io.StringReader;

import static org.junit.Assert.*;


public class MessagesConfigTest extends BasePluginTest {

  @Test
  public void isValid() {
    Component component = Message.get("info.ban.temporary")
            .set("reason", "abc")
            .set("actor", "def")
            .set("created", "8th July")
            .set("expires", "1d")
            .resolveComponent();

    String plain = MessageRenderer.getInstance().toPlainText(component);
    assertEquals("Currently banned for abc by def at 8th July which expires in 1d", plain);
  }

  private void loadTestMessages(String yaml) {
    YamlConfiguration config = YamlConfiguration.loadConfiguration(new StringReader(yaml));
    MessageRegistry registry = new MessageRegistry("en");

    for (String key : config.getConfigurationSection("messages").getKeys(true)) {
      String value = config.getString("messages." + key);
      if (value != null) {
        registry.putMessage(key, value.replace("\\n", "\n").replaceAll("(?<=\\n)(?=\\n)", " "));
      }
    }

    Message.init(registry, new TestLogger());
  }

  @Test
  public void doubleNewlineProducesNonEmptyLine() {
    loadTestMessages("messages:\n  test:\n    greeting: 'Hello\\n\\nWorld'");
    String result = Message.get("test.greeting").toString();
    assertEquals("Hello\n \nWorld", result);
  }

  @Test
  public void tripleNewlineProducesNonEmptyLines() {
    loadTestMessages("messages:\n  test:\n    greeting: 'Hello\\n\\n\\nWorld'");
    String result = Message.get("test.greeting").toString();
    assertEquals("Hello\n \n \nWorld", result);
  }

  @Test
  public void singleNewlineIsUnchanged() {
    loadTestMessages("messages:\n  test:\n    greeting: 'Hello\\nWorld'");
    String result = Message.get("test.greeting").toString();
    assertEquals("Hello\nWorld", result);
  }

  @Test
  public void noConsecutiveEmptyLinesInLoadedMessages() {
    loadTestMessages("messages:\n  test:\n    msg: 'Line1\\n\\nLine2\\n\\n\\nLine3'");
    String result = Message.get("test.msg").toString();

    for (String line : result.split("\n", -1)) {
      assertFalse("Empty lines should contain a space for chat rendering", line.isEmpty());
    }
  }
}
