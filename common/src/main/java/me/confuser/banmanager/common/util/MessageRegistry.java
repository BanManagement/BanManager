package me.confuser.banmanager.common.util;

import java.util.*;

public class MessageRegistry {

  private record Snapshot(String defaultLocale, Map<String, Map<String, String>> locales) {
  }

  private volatile Snapshot snapshot;

  public MessageRegistry(String defaultLocale) {
    this.snapshot = new Snapshot(normaliseLocale(defaultLocale), new HashMap<>());
  }

  public static String normaliseLocale(String locale) {
    if (locale == null || locale.isEmpty()) return "en";
    return locale.toLowerCase(Locale.ROOT).replace('-', '_');
  }

  public String getDefaultLocale() {
    return snapshot.defaultLocale();
  }

  public void loadLocale(String locale, Map<String, String> messages) {
    String normalised = normaliseLocale(locale);
    Snapshot s = this.snapshot;
    Map<String, Map<String, String>> copy = new HashMap<>(s.locales());
    copy.put(normalised, Map.copyOf(messages));
    this.snapshot = new Snapshot(s.defaultLocale(), copy);
  }

  public String getMessage(String key, String locale) {
    String normalised = normaliseLocale(locale);
    Snapshot s = this.snapshot;

    String value = getFromLocale(s.locales(), key, normalised);
    if (value != null) return value;

    int underscore = normalised.indexOf('_');
    if (underscore > 0) {
      String baseLanguage = normalised.substring(0, underscore);
      value = getFromLocale(s.locales(), key, baseLanguage);
      if (value != null) return value;
    }

    if (!normalised.equals(s.defaultLocale())) {
      value = getFromLocale(s.locales(), key, s.defaultLocale());
      if (value != null) return value;
    }

    return null;
  }

  public String getMessage(String key) {
    return getMessage(key, snapshot.defaultLocale());
  }

  public void putMessage(String key, String message) {
    putMessage(key, message, snapshot.defaultLocale());
  }

  public void putMessage(String key, String message, String locale) {
    String normalised = normaliseLocale(locale);
    Snapshot s = this.snapshot;
    Map<String, Map<String, String>> copy = new HashMap<>(s.locales());
    Map<String, String> localeMessages = copy.get(normalised);

    if (localeMessages == null) {
      localeMessages = new HashMap<>();
    } else {
      localeMessages = new HashMap<>(localeMessages);
    }

    localeMessages.put(key, message);
    copy.put(normalised, Map.copyOf(localeMessages));
    this.snapshot = new Snapshot(s.defaultLocale(), copy);
  }

  public Set<String> getAvailableLocales() {
    return Set.copyOf(snapshot.locales().keySet());
  }

  public Set<String> getKeys(String locale) {
    Map<String, String> localeMessages = snapshot.locales().get(normaliseLocale(locale));
    if (localeMessages == null) return Collections.emptySet();
    return Collections.unmodifiableSet(localeMessages.keySet());
  }

  public Map<String, String> getMessages(String locale) {
    Map<String, String> localeMessages = snapshot.locales().get(normaliseLocale(locale));
    if (localeMessages == null) return Collections.emptyMap();
    return localeMessages;
  }

  public boolean hasAnyMessages() {
    Snapshot s = this.snapshot;
    for (Map<String, String> msgs : s.locales().values()) {
      if (!msgs.isEmpty()) return true;
    }
    return false;
  }

  public int getMissingKeyCount(String locale) {
    Snapshot s = this.snapshot;
    Set<String> defaultKeys = getKeysFromSnapshot(s, s.defaultLocale());
    Set<String> localeKeys = getKeysFromSnapshot(s, normaliseLocale(locale));
    int missing = 0;

    for (String key : defaultKeys) {
      if (!localeKeys.contains(key)) missing++;
    }

    return missing;
  }

  public void atomicSwap(MessageRegistry newRegistry) {
    this.snapshot = newRegistry.snapshot;
  }

  private static Set<String> getKeysFromSnapshot(Snapshot s, String locale) {
    Map<String, String> messages = s.locales().get(locale);
    if (messages == null) return Collections.emptySet();
    return messages.keySet();
  }

  private static String getFromLocale(Map<String, Map<String, String>> locales, String key, String locale) {
    Map<String, String> messages = locales.get(locale);
    if (messages == null) return null;
    return messages.get(key);
  }
}
