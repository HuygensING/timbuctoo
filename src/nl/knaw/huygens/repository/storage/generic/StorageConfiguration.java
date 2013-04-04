package nl.knaw.huygens.repository.storage.generic;

import java.util.Set;

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;
import com.google.inject.Inject;

import nl.knaw.huygens.repository.Configuration;

public class StorageConfiguration {
  private String host;
  private int port;
  private String dbName;
  private String user;
  private String password;
  private String type;
  private String key;
  private Set<String> documentTypes;
  private Set<String> versionedTypes;
  private Set<String> variationTypes;

  public StorageConfiguration(String host, int port, String dbName, String user, String password, String type) {
    this.host = host;
    this.port = port;
    this.dbName = dbName;
    this.user = user;
    this.password = password;
    this.type = type;
  }

  @Inject
  public StorageConfiguration(Configuration conf) {
    type = conf.getSetting("database.type", "unknown");
    host = conf.getSetting("database.host", "localhost");
    port = conf.getIntSetting("database.port", 27017);
    dbName = conf.getSetting("database.name", conf.getSetting("project.internalname", "data"));
    user = conf.getSetting("database.user", null);
    password = conf.getSetting("database.password", null);
    key = Joiner.on(":").useForNull("null").join(host, port, dbName, user, password);
    String docTypes = conf.getSetting("doctypes");
    documentTypes = Sets.newHashSet(docTypes.split(","));
    String versionedDocTypes = conf.getSetting("versioneddoctypes", docTypes);
    versionedTypes = Sets.newHashSet(versionedDocTypes.split(","));
    String variationDocTypes = conf.getSetting("variationdoctypes", docTypes);
    variationTypes = Sets.newHashSet(variationDocTypes.split(",")); 
  }

  public boolean requiresAuth() {
    return user != null;
  }

  public String getHost() {
    return host;
  }

  public int getPort() {
    return port;
  }

  public String getDbName() {
    return dbName;
  }

  public String getUser() {
    return user;
  }

  public String getPassword() {
    return password;
  }

  public StorageType getType() {
    StorageType t;
    try {
      t = StorageType.valueOf(type.toUpperCase());
    } catch (Exception ex) {
      System.err.println("Unknown storage type " + type);
      throw new RuntimeException(ex);
    }
    return t;
  }

  public String getKey() {
    return key;
  }

  public Set<String> getDocumentTypes() {
    return documentTypes;
  }

  public Set<String> getVersionedTypes() {
    return versionedTypes;
  }

  public Set<String> getVariationDocumentTypes() {
    return variationTypes;
  }
}