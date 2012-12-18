package nl.knaw.huygens.repository.util;

import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class Configuration {
  private static final String DEFAULT_CONFIG_FILE = "../config.xml";
  private static final String SETTINGS_PREFIX = "settings.";
  private XMLConfiguration xmlConfig;

  public Configuration() throws ConfigurationException {
    this(DEFAULT_CONFIG_FILE);
  }

  public Configuration(String configFile) throws ConfigurationException {
    xmlConfig = new XMLConfiguration();
    xmlConfig.setDelimiterParsingDisabled(true);
    xmlConfig.clear();
    InputStream in = Configuration.class.getClassLoader().getResourceAsStream(configFile);
    try {
      xmlConfig.load(in);
    } catch (ConfigurationException e) {
      System.err.println("ERROR: unable to load configuration!");
      throw e;
    }
  }

  public String getSetting(String setting) {
    return xmlConfig.getString(SETTINGS_PREFIX + setting, "");
  }

  public String getSetting(String setting, String defaultValue) {
    return xmlConfig.getString(SETTINGS_PREFIX + setting, defaultValue);
  }

  public boolean getBooleanSetting(String setting) {
    return getBooleanSetting(setting, false);
  }

  public boolean getBooleanSetting(String setting, boolean defaultValue) {
    return xmlConfig.getBoolean(SETTINGS_PREFIX + setting, defaultValue);
  }

  public int getIntSetting(String setting) {
    return getIntSetting(setting, 0);
  }

  public int getIntSetting(String setting, int defaultValue) {
    return xmlConfig.getInt(SETTINGS_PREFIX + setting, defaultValue);
  }

  public List<String> getSettingKeys(String prefix) {
    @SuppressWarnings("unchecked")
    Iterator<String> it = xmlConfig.getKeys(SETTINGS_PREFIX + prefix);
    List<String> rv = Lists.newArrayList();
    while (it.hasNext()) {
      rv.add(it.next().replaceFirst(SETTINGS_PREFIX + prefix, ""));
    }
    return rv;
  }

  public String getSettingProperty(String key, String property) {
    return getSettingProperty(key, property, "");
  }

  public String getSettingProperty(String key, String property, String defaultValue) {
    return xmlConfig.getString(key + "[@" + property + "]", defaultValue);
  }

  public Map<String, String> getAll() {
    @SuppressWarnings("unchecked")
    Iterator<String> it = xmlConfig.getKeys(SETTINGS_PREFIX);
    Map<String, String> rv = Maps.newHashMap();
    while (it.hasNext()) {
      String k = it.next().replaceFirst(SETTINGS_PREFIX, "");
      rv.put(k, getSetting(k));
    }
    return rv;
  }
}
