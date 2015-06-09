package nl.knaw.huygens.timbuctoo.config;

/*
 * #%L
 * Timbuctoo core
 * =======
 * Copyright (C) 2012 - 2015 Huygens ING
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.util.Text;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.lang.StringUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class Configuration {

  public static final String DEFAULT_CONFIG_FILE = "config.xml";

  private static final String SETTINGS_PREFIX = "settings.";
  public static final String KEY_HOME_DIR = "home.directory";
  public static final String EXPIRATION_DURATION_KEY = "login.expirationTime";
  public static final String EXPIRATION_TIME_UNIT_KEY = "login.timeUnit";

  private final XMLConfiguration xmlConfig;

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

  public boolean hasSetting(String key) {
    return StringUtils.isNotBlank(getSetting(key));
  }

  public String getSetting(String key) {
    return xmlConfig.getString(SETTINGS_PREFIX + key, "");
  }

  public String getSetting(String key, String defaultValue) {
    return xmlConfig.getString(SETTINGS_PREFIX + key, defaultValue);
  }

  public String[] getSettings(String key) {
    String value = getSetting(key, "");
    // Use characters ',' and ' ' as item separators
    return StringUtils.split(value, ", ");
  }

  public boolean getBooleanSetting(String key) {
    return getBooleanSetting(key, false);
  }

  public boolean getBooleanSetting(String key, boolean defaultValue) {
    return xmlConfig.getBoolean(SETTINGS_PREFIX + key, defaultValue);
  }

  public int getIntSetting(String key) {
    return getIntSetting(key, 0);
  }

  public int getIntSetting(String key, int defaultValue) {
    return xmlConfig.getInt(SETTINGS_PREFIX + key, defaultValue);
  }

  public List<String> getSettingKeys(String prefix) {
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
    Iterator<String> it = xmlConfig.getKeys(SETTINGS_PREFIX);
    Map<String, String> rv = Maps.newHashMap();
    while (it.hasNext()) {
      String k = it.next().replaceFirst(SETTINGS_PREFIX, "");
      String v = getSetting(k);
      rv.put(k, v);
    }
    return rv;
  }

  public String getSolrHomeDir() {
    return getDirectory("solr.directory");
  }

  public String pathInUserHome(String path) {
    return concatenatePaths(System.getProperty("user.home"), path);
  }

  private String concatenatePaths(String part1, String part2) {
    part2 = Character.toString(part2.charAt(0)).equals("/") ? part2 : "/" + part2;
    return part1 + part2;
  }

  public String getDirectory(String key) {
    String path = concatenatePaths(getSetting(KEY_HOME_DIR), getSetting(key));
    return getBooleanSetting("home.use_user_home") ? pathInUserHome(path) : path;
  }

  // --- VRE's -----------------------------------------------------------------

  /**
   * Returns VRE definitions.
   * TODO return a list of VRE's instead.
   */
  public List<VREDef> getVREDefs() {
    List<VREDef> vreDefs = Lists.newArrayList();
    for (HierarchicalConfiguration cfg : xmlConfig.configurationsAt(SETTINGS_PREFIX + "vre-defs.vre")) {
      VREDef vreDef = new VREDef();
      vreDef.id = getString(cfg, "[@id]");
      vreDef.description = getString(cfg, "description");
      vreDef.modelPackage = getString(cfg, "model-package");
      vreDef.receptions = Lists.newArrayList();
      for (Object item : cfg.getList("receptions.reception")) {
        vreDef.receptions.add(item.toString());
      }
      vreDefs.add(vreDef);
    }
    return vreDefs;
  }

  private String getString(HierarchicalConfiguration cfg, String key) {
    return Text.normalizeWhitespace(cfg.getString(key));
  }

  public static class VREDef {
    public String id;
    public String description;
    public String modelPackage;
    public List<String> receptions;
  }

}
