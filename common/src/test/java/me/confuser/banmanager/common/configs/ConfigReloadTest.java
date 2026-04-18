package me.confuser.banmanager.common.configs;

import me.confuser.banmanager.common.BasePluginTest;
import me.confuser.banmanager.common.CommonLogger;
import me.confuser.banmanager.common.TestLogger;
import me.confuser.banmanager.common.util.Message;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

public class ConfigReloadTest extends BasePluginTest {

  @Test
  public void configLoadReturnsFalseForInvalidYaml() throws IOException {
    File invalidFile = new File(temporaryFolder, "invalid.yml");
    try (FileWriter writer = new FileWriter(invalidFile)) {
      writer.write("invalid: yaml: content: [unclosed");
    }

    CommonLogger logger = new TestLogger();
    TestConfig config = new TestConfig(temporaryFolder, invalidFile, logger);

    boolean result = config.load();

    assertFalse(result, "Config.load() should return false for invalid YAML");
  }

  @Test
  public void configLoadReturnsTrueForValidYaml() throws IOException {
    File validFile = new File(temporaryFolder, "valid.yml");
    try (FileWriter writer = new FileWriter(validFile)) {
      writer.write("key: value\n");
      writer.write("nested:\n");
      writer.write("  child: data\n");
    }

    CommonLogger logger = new TestLogger();
    TestConfig config = new TestConfig(temporaryFolder, validFile, logger);

    boolean result = config.load();

    assertTrue(result, "Config.load() should return true for valid YAML");
    assertEquals("value", config.conf.getString("key"));
    assertEquals("data", config.conf.getString("nested.child"));
  }

  @Test
  public void configLoadReturnsFalseForMissingFile() {
    File missingFile = new File(temporaryFolder, "nonexistent.yml");

    CommonLogger logger = new TestLogger();
    TestConfig config = new TestConfig(temporaryFolder, missingFile, logger);

    boolean result = config.load();

    assertFalse(result, "Config.load() should return false for missing file");
  }

  @Test
  public void setupConfigsPreservesPreviousSettingsOnReloadFailure() throws IOException {
    assertNotNull(plugin.getConfig(), "Initial config should be loaded");
    DefaultConfig originalConfig = plugin.getConfig();

    File configFile = new File(temporaryFolder, "config.yml");
    try (FileWriter writer = new FileWriter(configFile)) {
      writer.write("invalid: yaml: [unclosed bracket");
    }

    plugin.setupConfigs();

    assertSame(originalConfig, plugin.getConfig(), "Config should be preserved after failed reload");
  }

  @Test
  public void setupConfigsReplacesConfigOnSuccessfulReload() throws IOException {
    DefaultConfig originalConfig = plugin.getConfig();
    assertNotNull(originalConfig, "Initial config should be loaded");

    plugin.setupConfigs();

    assertNotSame(originalConfig, plugin.getConfig(), "Config should be replaced after successful reload");
  }

  @Test
  public void messagesArePreservedWhenConfigFails() throws IOException {
    String originalMessage = Message.getString("configReloaded");
    assertNotNull(originalMessage, "configReloaded message should exist");

    File messagesEnFile = new File(temporaryFolder, "messages/messages_en.yml");
    try (FileWriter writer = new FileWriter(messagesEnFile)) {
      writer.write("invalid: yaml: [unclosed");
    }

    plugin.setupConfigs();

    String afterReloadMessage = Message.getString("configReloaded");
    assertEquals(originalMessage, afterReloadMessage, "Messages should be preserved after failed reload");
  }

  @Test
  public void messagesAreUpdatedOnSuccessfulReload() throws IOException {
    String originalMessage = Message.getString("configReloaded");
    assertNotNull(originalMessage, "configReloaded message should exist");

    File messagesFile = new File(temporaryFolder, "messages/messages_en.yml");
    try (FileWriter writer = new FileWriter(messagesFile)) {
      writer.write("messages:\n");
      writer.write("  configReloaded: \"New reload message\"\n");
    }

    plugin.setupConfigs();

    String afterReloadMessage = Message.getString("configReloaded");
    assertEquals("New reload message", afterReloadMessage, "Messages should be updated after successful reload");
  }

  /**
   * Simple test config implementation for testing the base Config class
   */
  private static class TestConfig extends Config {
    private boolean afterLoadCalled = false;

    public TestConfig(File dataFolder, File file, CommonLogger logger) {
      super(dataFolder, file, logger);
    }

    @Override
    public void afterLoad() {
      afterLoadCalled = true;
    }

    @Override
    public void onSave() {
    }

    public boolean wasAfterLoadCalled() {
      return afterLoadCalled;
    }
  }

  @Test
  public void afterLoadIsNotCalledOnFailure() throws IOException {
    File invalidFile = new File(temporaryFolder, "invalid.yml");
    try (FileWriter writer = new FileWriter(invalidFile)) {
      writer.write("invalid: yaml: [unclosed");
    }

    CommonLogger logger = new TestLogger();
    TestConfig config = new TestConfig(temporaryFolder, invalidFile, logger);

    config.load();

    assertFalse(config.wasAfterLoadCalled(), "afterLoad() should not be called when loading fails");
  }

  @Test
  public void afterLoadIsCalledOnSuccess() throws IOException {
    File validFile = new File(temporaryFolder, "valid.yml");
    try (FileWriter writer = new FileWriter(validFile)) {
      writer.write("key: value\n");
    }

    CommonLogger logger = new TestLogger();
    TestConfig config = new TestConfig(temporaryFolder, validFile, logger);

    config.load();

    assertTrue(config.wasAfterLoadCalled(), "afterLoad() should be called when loading succeeds");
  }

  @Test
  public void saveDoesNotOverwriteFileAfterFailedReload() throws IOException {
    File configFile = new File(temporaryFolder, "saveable.yml");
    try (FileWriter writer = new FileWriter(configFile)) {
      writer.write("key: originalValue\n");
    }

    CommonLogger logger = new TestLogger();
    SaveableTestConfig config = new SaveableTestConfig(temporaryFolder, configFile, logger);
    assertTrue(config.load(), "Initial load should succeed");

    String invalidContent = "key: editedValue\ninvalid: yaml: [unclosed";
    try (FileWriter writer = new FileWriter(configFile)) {
      writer.write(invalidContent);
    }

    assertFalse(config.load(), "Reload should fail for invalid YAML");

    config.save();

    String fileContent = new String(Files.readAllBytes(configFile.toPath()));
    assertEquals(invalidContent, fileContent, "File should not be overwritten after failed reload");
  }

  @Test
  public void saveOverwritesFileAfterSuccessfulReload() throws IOException {
    File configFile = new File(temporaryFolder, "saveable.yml");
    try (FileWriter writer = new FileWriter(configFile)) {
      writer.write("key: originalValue\n");
    }

    CommonLogger logger = new TestLogger();
    SaveableTestConfig config = new SaveableTestConfig(temporaryFolder, configFile, logger);
    assertTrue(config.load(), "Initial load should succeed");

    config.conf.set("key", "modifiedValue");

    config.save();

    String fileContent = new String(Files.readAllBytes(configFile.toPath()));
    assertTrue(fileContent.contains("modifiedValue"), "File should contain modified value");
  }

  @Test
  public void saveInAfterLoadPersistsChanges() throws IOException {
    File configFile = new File(temporaryFolder, "self-save.yml");
    try (FileWriter writer = new FileWriter(configFile)) {
      writer.write("key: originalValue\n");
    }

    CommonLogger logger = new TestLogger();
    SelfSavingTestConfig config = new SelfSavingTestConfig(temporaryFolder, configFile, logger);

    assertTrue(config.load(), "Load should succeed for valid YAML");

    String fileContent = new String(Files.readAllBytes(configFile.toPath()));
    assertTrue(fileContent.contains("updatedInAfterLoad"), "afterLoad() save should persist updated value");
  }

  /**
   * Test config that tracks save behavior
   */
  private static class SaveableTestConfig extends Config {
    public SaveableTestConfig(File dataFolder, File file, CommonLogger logger) {
      super(dataFolder, file, logger);
    }

    @Override
    public void afterLoad() {
    }

    @Override
    public void onSave() {
    }
  }

  /**
   * Test config that mutates and saves during afterLoad.
   */
  private static class SelfSavingTestConfig extends Config {
    public SelfSavingTestConfig(File dataFolder, File file, CommonLogger logger) {
      super(dataFolder, file, logger);
    }

    @Override
    public void afterLoad() {
      conf.set("key", "updatedInAfterLoad");
      save();
    }

    @Override
    public void onSave() {
    }
  }
}
