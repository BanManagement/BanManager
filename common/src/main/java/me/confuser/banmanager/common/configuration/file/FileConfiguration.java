package me.confuser.banmanager.common.configuration.file;

import me.confuser.banmanager.common.configuration.Configuration;
import me.confuser.banmanager.common.configuration.InvalidConfigurationException;
import me.confuser.banmanager.common.configuration.MemoryConfiguration;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

/**
 * This is a base class for all File based implementations of {@link
 * Configuration}.
 */
public abstract class FileConfiguration extends MemoryConfiguration {

  /**
   * Creates an empty FileConfiguration with no default values.
   */
  FileConfiguration() {
  }

  /**
   * Creates an empty FileConfiguration using the specified {@link
   * Configuration} as a source for all default values.
   *
   * @param defaults Default value provider
   */
  public FileConfiguration(Configuration defaults) {
    super(defaults);
  }

  /**
   * Saves this FileConfiguration to the specified location.
   * <p>
   * <p>If the file does not exist, it will be created. If already exists, it
   * will be overwritten. If it cannot be overwritten or created, an
   * exception will be thrown.
   * <p>
   * <p>This method will save using the system default encoding, or possibly
   * using UTF8.
   *
   * @param file File to save to.
   *
   * @throws IOException Thrown when the given file cannot be written to for
   *                     any reason.
   */
  public void save(File file) throws IOException {
    File parent = file.getParentFile();
    if (parent != null) {
      parent.mkdirs();
    }

    String data = saveToString();

    try (Writer writer = new OutputStreamWriter(new FileOutputStream(file),
            StandardCharsets.UTF_8)) {
      writer.write(data);
    }
  }

  /**
   * Saves this FileConfiguration to a string, and returns it.
   *
   * @return String containing this configuration.
   */
  public abstract String saveToString();

  /**
   * Loads this FileConfiguration from the specified location.
   * <p>
   * <p>All the values contained within this configuration will be removed,
   * leaving only settings and defaults, and the new values will be loaded
   * from the given file.
   * <p>
   * <p>If the file cannot be loaded for any reason, an exception will be
   * thrown.
   *
   * @param file File to load from.
   *
   * @throws FileNotFoundException         Thrown when the given file cannot be
   *                                       opened.
   * @throws IOException                   Thrown when the given file cannot be read.
   * @throws InvalidConfigurationException Thrown when the given file is not
   *                                       a valid Configuration.
   * @throws IllegalArgumentException      Thrown when file is null.
   */
  public void load(File file) throws IOException, InvalidConfigurationException {

    try (FileInputStream stream = new FileInputStream(file)) {
      load(new InputStreamReader(stream, StandardCharsets.UTF_8));
    }
  }

  /**
   * Loads this FileConfiguration from the specified reader.
   * <p>
   * <p>All the values contained within this configuration will be removed,
   * leaving only settings and defaults, and the new values will be loaded
   * from the given stream.
   *
   * @param reader the reader to load from
   *
   * @throws IOException                   thrown when underlying reader throws an IOException
   * @throws InvalidConfigurationException thrown when the reader does not
   *                                       represent a valid Configuration
   */
  public void load(Reader reader) throws IOException, InvalidConfigurationException {

    String builder;

    try (BufferedReader input = reader instanceof BufferedReader ?
            (BufferedReader) reader :
            new BufferedReader(reader)) {

      builder = input.lines().map(line -> line + '\n').collect(Collectors.joining());
    }

    loadFromString(builder);
  }

  /**
   * Loads this FileConfiguration from the specified string, as
   * opposed to from file.
   * <p>
   * <p>All the values contained within this configuration will be removed,
   * leaving only settings and defaults, and the new values will be loaded
   * from the given string.
   * <p>
   * <p>If the string is invalid in any way, an exception will be thrown.
   *
   * @param contents Contents of a Configuration to load.
   *
   * @throws InvalidConfigurationException Thrown if the specified string is
   *                                       invalid.
   */
  public abstract void loadFromString(String contents) throws InvalidConfigurationException;

  /**
   * Compiles the header for this FileConfiguration and returns the
   * result.
   * <p>
   * <p>This will use the header from {@link #options()} -> {@link
   * FileConfigurationOptions#header()}, respecting the rules of {@link
   * FileConfigurationOptions#copyHeader()} if set.
   *
   * @return Compiled header
   */
  protected abstract String buildHeader();

  @Override
  public FileConfigurationOptions options() {
    if (this.options == null) {
      this.options = new FileConfigurationOptions(this);
    }

    return (FileConfigurationOptions) this.options;
  }
}
