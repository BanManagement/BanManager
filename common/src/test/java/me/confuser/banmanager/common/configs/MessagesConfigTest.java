package me.confuser.banmanager.common.configs;

import me.confuser.banmanager.common.BasePluginTest;
import me.confuser.banmanager.common.util.Message;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MessagesConfigTest extends BasePluginTest {

  @Test
  public void isValid() {
    // @TODO test tokens for all messages
    assertEquals("&6Currently banned for &4abc&6 by def at 8th July which expires in 1d", Message
            .get("info.ban.temporary").set("reason", "abc").set("actor", "def").set("created",
                    "8th July").set("expires", "1d").toString());
  }
}
