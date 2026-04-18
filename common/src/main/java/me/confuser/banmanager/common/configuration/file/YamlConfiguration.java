package me.confuser.banmanager.common.configuration.file;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.configuration.Configuration;
import me.confuser.banmanager.common.configuration.ConfigurationSection;
import me.confuser.banmanager.common.configuration.InvalidConfigurationException;
import me.confuser.banmanager.common.snakeyaml.DumperOptions;
import me.confuser.banmanager.common.snakeyaml.LoaderOptions;
import me.confuser.banmanager.common.snakeyaml.Yaml;
import me.confuser.banmanager.common.snakeyaml.error.YAMLException;
import me.confuser.banmanager.common.snakeyaml.representer.Representer;

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
  private final LoaderOptions loaderOptions = createLoaderOptions();
  private final Representer yamlRepresenter = new YamlRepresenter(yamlOptions);
  private final Yaml yaml = new Yaml(new YamlConstructor(loaderOptions), yamlRepresenter, yamlOptions, loaderOptions);

  /**
   * Build {@link LoaderOptions} that preserve SnakeYAML 1.x-compatible
   * behaviour for existing user-authored config files. The 2.x defaults made
   * three potentially breaking changes for our use case:
   *
   * <ul>
   *   <li>{@code allowDuplicateKeys} flipped from {@code true} to {@code false}.
   *   Operators routinely hand-edit messages.yml and may have introduced
   *   duplicate keys; refusing to load would brick the plugin on upgrade. The
   *   last value still wins, matching legacy behaviour.</li>
   *   <li>{@code codePointLimit} now defaults to 3 MB. Large message bundles
   *   (translations, big punishment payloads) can exceed this, so we raise it
   *   to 32 MB.</li>
   *   <li>{@code nestingDepthLimit} now defaults to 50. Bumped to 100 to give
   *   headroom for deeply nested webhook payloads.</li>
   * </ul>
   */
  private static LoaderOptions createLoaderOptions() {
    LoaderOptions options = new LoaderOptions();
    options.setAllowDuplicateKeys(true);
    options.setCodePointLimit(32 * 1024 * 1024);
    options.setNestingDepthLimit(100);
    return options;
  }

  /**
   * Creates a new {@link YamlConfiguration}, loading from the given file.
   * <p>Any errors loading the Configuration will be logged and then ignored.
   * If the specified input is not a valid config, a blank config will be
   * returned.</p>
   * <p>The encoding used may follow the system dependent default.</p>
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
      BanManagerPlugin.getInstance().getLogger().warning("Failed to load YAML configuration", e);
    }

    return config;
  }

  /**
   * Creates a new {@link YamlConfiguration}, loading from the given reader.
   * <p>
   * Any errors loading the Configuration will be logged and then ignored.
   * If the specified input is not a valid config, a blank config will be
   * returned.</p>
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
      BanManagerPlugin.getInstance().getLogger().warning("Failed to load YAML configuration", e);
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
