package me.confuser.banmanager.common.configuration.file;

import me.confuser.banmanager.common.configuration.Configuration;
import me.confuser.banmanager.common.configuration.ConfigurationSection;
import me.confuser.banmanager.common.configuration.InvalidConfigurationException;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.representer.Representer;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.Map;

/**
 * An implementation of {@link Configuration} which saves all files in Yaml.
 * Note that this implementation is not synchronized.
 */
public class YamlConfiguration extends FileConfiguration {

  private static final String COMMENT_PREFIX = "# ";
  private static final String BLANK_CONFIG = "{}\n";
  private final DumperOptions yamlOptions = new DumperOptions();
  private final Representer yamlRepresenter = new YamlRepresenter();
  private final Yaml yaml = new Yaml(new YamlConstructor(), yamlRepresenter, yamlOptions);

  /**
   * Creates a new {@link YamlConfiguration}, loading from the given file.
   * <p>
   * <p>Any errors loading the Configuration will be logged and then ignored.
   * If the specified input is not a valid config, a blank config will be
   * returned.
   * <p>
   * <p>The encoding used may follow the system dependent default.
   *
   * @param file Input file
   *
   * @return Resulting configuration
   */
  public static YamlConfiguration loadConfiguration(File file) {
    YamlConfiguration config = new YamlConfiguration();

    try {
      config.load(file);
    } catch ( IOException |InvalidConfigurationException e){
      e.printStackTrace();
    }

    return config;
  }

  /**
   * Creates a new {@link YamlConfiguration}, loading from the given reader.
   * <p>
   * Any errors loading the Configuration will be logged and then ignored.
   * If the specified input is not a valid config, a blank config will be
   * returned.
   *
   * @param reader input
   * @return resulting configuration
   * @throws IllegalArgumentException Thrown if stream is null
   */
  public static YamlConfiguration loadConfiguration(Reader reader) {
    YamlConfiguration config = new YamlConfiguration();

    try {
      config.load(reader);
    } catch (InvalidConfigurationException | IOException e) {
      e.printStackTrace();
    }

    return config;
  }

  @Override
  public String saveToString() {
    yamlOptions.setIndent(options().indent());
    yamlOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
    yamlRepresenter.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

    String header = buildHeader();
    String dump = yaml.dump(getValues(false));

    if (dump.equals(BLANK_CONFIG)) {
      dump = "";
    }

    return header + dump;
  }

  @Override
  public void loadFromString(String contents) throws InvalidConfigurationException {

    Map<?, ?> input;
    try {
      input = (Map<?, ?>) yaml.load(contents);
    } catch (YAMLException e) {
      throw new InvalidConfigurationException(e);
    } catch (ClassCastException ignored) {
      throw new InvalidConfigurationException("Top level is not a Map.");
    }

    String header = parseHeader(contents);
    if (!header.isEmpty()) {
      options().header(header);
    }

    if (input != null) {
      convertMapsToSections(input, this);
    }
  }

  protected void convertMapsToSections(Map<?, ?> input, ConfigurationSection section) {
    for (Map.Entry<?, ?> entry : input.entrySet()) {
      String key = entry.getKey().toString();
      Object value = entry.getValue();

      if (value instanceof Map) {
        convertMapsToSections((Map<?, ?>) value, section.createSection(key));
      } else {
        section.set(key, value);
      }
    }
  }

  protected String parseHeader(String input) {
    String[] lines = input.split("\r?\n", -1);
    StringBuilder result = new StringBuilder();
    boolean readingHeader = true;
    boolean foundHeader = false;

    for (int i = 0; (i < lines.length) && readingHeader; i++) {
      String line = lines[i];

      if (line.startsWith(COMMENT_PREFIX)) {
        if (i > 0) {
          result.append('\n');
        }

        if (line.length() > COMMENT_PREFIX.length()) {
          result.append(line.substring(COMMENT_PREFIX.length()));
        }

        foundHeader = true;
      } else if (foundHeader && line.isEmpty()) {
        result.append('\n');
      } else if (foundHeader) {
        readingHeader = false;
      }
    }

    return result.toString();
  }

  @Override
  protected String buildHeader() {
    String header = options().header();

    if (options().copyHeader()) {
      Configuration def = getDefaults();

      if (def instanceof FileConfiguration) {
        FileConfiguration fileDefaults = (FileConfiguration) def;
        String defaultsHeader = fileDefaults.buildHeader();

        if ((defaultsHeader != null) && !defaultsHeader.isEmpty()) {
          return defaultsHeader;
        }
      }
    }

    if (header == null) {
      return "";
    }

    StringBuilder builder = new StringBuilder();
    String[] lines = header.split("\r?\n", -1);
    boolean startedHeader = false;

    for (int i = lines.length - 1; i >= 0; i--) {
      builder.insert(0, '\n');

      if (startedHeader || !lines[i].isEmpty()) {
        builder.insert(0, lines[i]);
        builder.insert(0, COMMENT_PREFIX);
        startedHeader = true;
      }
    }

    return builder.toString();
  }

  @Override
  public YamlConfigurationOptions options() {
    if (options == null) {
      options = new YamlConfigurationOptions(this);
    }

    return (YamlConfigurationOptions) options;
  }
}
