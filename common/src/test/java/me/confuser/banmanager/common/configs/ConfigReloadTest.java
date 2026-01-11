package me.confuser.banmanager.common.configs;

import me.confuser.banmanager.common.BasePluginTest;
import me.confuser.banmanager.common.CommonLogger;
import me.confuser.banmanager.common.TestLogger;
import me.confuser.banmanager.common.util.Message;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.Assert.*;

public class ConfigReloadTest extends BasePluginTest {

  @Test
  public void configLoadReturnsFalseForInvalidYaml() throws IOException {
    // Create a file with invalid YAML
    File invalidFile = new File(temporaryFolder.getRoot(), "invalid.yml");
    try (FileWriter writer = new FileWriter(invalidFile)) {
      writer.write("invalid: yaml: content: [unclosed");
    }

    CommonLogger logger = new TestLogger();
    TestConfig config = new TestConfig(temporaryFolder.getRoot(), invalidFile, logger);

    boolean result = config.load();

    assertFalse("Config.load() should return false for invalid YAML", result);
  }

  @Test
  public void configLoadReturnsTrueForValidYaml() throws IOException {
    // Create a file with valid YAML
    File validFile = new File(temporaryFolder.getRoot(), "valid.yml");
    try (FileWriter writer = new FileWriter(validFile)) {
      writer.write("key: value\n");
      writer.write("nested:\n");
      writer.write("  child: data\n");
    }

    CommonLogger logger = new TestLogger();
    TestConfig config = new TestConfig(temporaryFolder.getRoot(), validFile, logger);

    boolean result = config.load();

    assertTrue("Config.load() should return true for valid YAML", result);
    assertEquals("value", config.conf.getString("key"));
    assertEquals("data", config.conf.getString("nested.child"));
  }

  @Test
  public void configLoadReturnsFalseForMissingFile() {
    File missingFile = new File(temporaryFolder.getRoot(), "nonexistent.yml");

    CommonLogger logger = new TestLogger();
    TestConfig config = new TestConfig(temporaryFolder.getRoot(), missingFile, logger);

    boolean result = config.load();

    assertFalse("Config.load() should return false for missing file", result);
  }

  @Test
  public void setupConfigsPreservesPreviousSettingsOnReloadFailure() throws IOException {
    // First, verify the initial config loaded successfully
    assertNotNull("Initial config should be loaded", plugin.getConfig());
    DefaultConfig originalConfig = plugin.getConfig();

    // Corrupt the config.yml file with invalid YAML
    File configFile = new File(temporaryFolder.getRoot(), "config.yml");
    try (FileWriter writer = new FileWriter(configFile)) {
      writer.write("invalid: yaml: [unclosed bracket");
    }

    // Reload configs
    plugin.setupConfigs();

    // The original config should still be in place
    assertSame("Config should be preserved after failed reload", originalConfig, plugin.getConfig());
  }

  @Test
  public void setupConfigsReplacesConfigOnSuccessfulReload() throws IOException {
    // Get the original config
    DefaultConfig originalConfig = plugin.getConfig();
    assertNotNull("Initial config should be loaded", originalConfig);

    // Reload configs (file is still valid)
    plugin.setupConfigs();

    // A new config object should have been created
    assertNotSame("Config should be replaced after successful reload", originalConfig, plugin.getConfig());
  }

  @Test
  public void messagesArePreservedWhenConfigFails() throws IOException {
    // Get a message that should exist
    String originalMessage = Message.getString("configReloaded");
    assertNotNull("configReloaded message should exist", originalMessage);

    // Corrupt the messages.yml file
    File messagesFile = new File(temporaryFolder.getRoot(), "messages.yml");
    try (FileWriter writer = new FileWriter(messagesFile)) {
      writer.write("invalid: yaml: [unclosed");
    }

    // Reload configs
    plugin.setupConfigs();

    // Messages should still be available
    String afterReloadMessage = Message.getString("configReloaded");
    assertEquals("Messages should be preserved after failed reload", originalMessage, afterReloadMessage);
  }

  @Test
  public void messagesAreUpdatedOnSuccessfulReload() throws IOException {
    // Get the original message
    String originalMessage = Message.getString("configReloaded");
    assertNotNull("configReloaded message should exist", originalMessage);

    // Modify the messages.yml file with a new message value
    File messagesFile = new File(temporaryFolder.getRoot(), "messages.yml");
    try (FileWriter writer = new FileWriter(messagesFile)) {
      writer.write("messages:\n");
      writer.write("  configReloaded: \"New reload message\"\n");
    }

    // Reload configs
    plugin.setupConfigs();

    // Message should be updated
    String afterReloadMessage = Message.getString("configReloaded");
    assertEquals("Messages should be updated after successful reload", "New reload message", afterReloadMessage);
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
      // No-op for testing
    }

    public boolean wasAfterLoadCalled() {
      return afterLoadCalled;
    }
  }

  @Test
  public void afterLoadIsNotCalledOnFailure() throws IOException {
    // Create a file with invalid YAML
    File invalidFile = new File(temporaryFolder.getRoot(), "invalid.yml");
    try (FileWriter writer = new FileWriter(invalidFile)) {
      writer.write("invalid: yaml: [unclosed");
    }

    CommonLogger logger = new TestLogger();
    TestConfig config = new TestConfig(temporaryFolder.getRoot(), invalidFile, logger);

    config.load();

    assertFalse("afterLoad() should not be called when loading fails", config.wasAfterLoadCalled());
  }

  @Test
  public void afterLoadIsCalledOnSuccess() throws IOException {
    // Create a file with valid YAML
    File validFile = new File(temporaryFolder.getRoot(), "valid.yml");
    try (FileWriter writer = new FileWriter(validFile)) {
      writer.write("key: value\n");
    }

    CommonLogger logger = new TestLogger();
    TestConfig config = new TestConfig(temporaryFolder.getRoot(), validFile, logger);

    config.load();

    assertTrue("afterLoad() should be called when loading succeeds", config.wasAfterLoadCalled());
  }

  @Test
  public void saveDoesNotOverwriteFileAfterFailedReload() throws IOException {
    // Create initial valid config
    File configFile = new File(temporaryFolder.getRoot(), "saveable.yml");
    try (FileWriter writer = new FileWriter(configFile)) {
      writer.write("key: originalValue\n");
    }

    CommonLogger logger = new TestLogger();
    SaveableTestConfig config = new SaveableTestConfig(temporaryFolder.getRoot(), configFile, logger);
    assertTrue("Initial load should succeed", config.load());

    // User edits the file (makes it invalid)
    String invalidContent = "key: editedValue\ninvalid: yaml: [unclosed";
    try (FileWriter writer = new FileWriter(configFile)) {
      writer.write(invalidContent);
    }

    // Reload fails
    assertFalse("Reload should fail for invalid YAML", config.load());

    // Now save is called (e.g., on plugin disable)
    config.save();

    // The file should NOT have been overwritten with old content
    String fileContent = new String(Files.readAllBytes(configFile.toPath()));
    assertEquals("File should not be overwritten after failed reload", invalidContent, fileContent);
  }

  @Test
  public void saveOverwritesFileAfterSuccessfulReload() throws IOException {
    // Create initial valid config
    File configFile = new File(temporaryFolder.getRoot(), "saveable.yml");
    try (FileWriter writer = new FileWriter(configFile)) {
      writer.write("key: originalValue\n");
    }

    CommonLogger logger = new TestLogger();
    SaveableTestConfig config = new SaveableTestConfig(temporaryFolder.getRoot(), configFile, logger);
    assertTrue("Initial load should succeed", config.load());

    // Modify the in-memory config
    config.conf.set("key", "modifiedValue");

    // Save should work
    config.save();

    // Verify file was updated
    String fileContent = new String(Files.readAllBytes(configFile.toPath()));
    assertTrue("File should contain modified value", fileContent.contains("modifiedValue"));
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
      // No-op
    }

    @Override
    public void onSave() {
      // No-op
    }
  }
}
