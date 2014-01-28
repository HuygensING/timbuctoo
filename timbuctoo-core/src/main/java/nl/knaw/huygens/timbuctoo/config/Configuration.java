package nl.knaw.huygens.timbuctoo.config;

/*
 * #%L
 * Timbuctoo core
 * =======
 * Copyright (C) 2012 - 2014 Huygens ING
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

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.vre.BaseScope;
import nl.knaw.huygens.timbuctoo.vre.DutchCaribbeanScope;
import nl.knaw.huygens.timbuctoo.vre.Scope;
import nl.knaw.huygens.timbuctoo.vre.CuraScope;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.lang.StringUtils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class Configuration {

  public static final String DEFAULT_CONFIG_FILE = "../config.xml";

  private static final String SETTINGS_PREFIX = "settings.";

  private final XMLConfiguration xmlConfig;
  private final List<Scope> scopes;
  private final Map<String, Scope> scopeMap;

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
    try {
      // TODO determine dynamically
      scopes = ImmutableList.<Scope> of(new BaseScope(), new DutchCaribbeanScope(), new CuraScope());
      scopeMap = Maps.newHashMap();
      for (Scope scope : scopes) {
        scopeMap.put(scope.getId(), scope);
      }
    } catch (IOException e) {
      System.err.println("ERROR: unable to obtain scopes!");
      throw new ConfigurationException(e);
    }
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
    String path = getSetting("solr.directory");
    return getBooleanSetting("solr.use_user_home") ? pathInUserHome(path) : path;
  }

  public String pathInUserHome(String path) {
    path = Character.toString(path.charAt(0)).equals("/") ? path : "/" + path;
    return System.getProperty("user.home") + path;
  }

}
