package me.confuser.banmanager.common.util;

import org.junit.Test;

import java.time.ZoneOffset;

import static org.junit.Assert.assertEquals;

public class DateUtilsTest {

  @Test
  public void shouldFormatTimestamp() {
    long timestamp = 1583321267L;

    assertEquals("04-03-2020 11:27:47", DateUtils.format("dd-MM-yyyy HH:mm:ss", timestamp, ZoneOffset.UTC));
    assertEquals("2020-03-04 11:27:47 +0000", DateUtils.format("yyyy-MM-dd HH:mm:ss Z", timestamp, ZoneOffset.UTC));
    assertEquals("2020-03-04_11-27-47", DateUtils.format("yyyy-MM-dd_HH-mm-ss", timestamp, ZoneOffset.UTC));
  }
}
