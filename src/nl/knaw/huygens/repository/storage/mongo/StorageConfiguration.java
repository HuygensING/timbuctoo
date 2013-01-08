package nl.knaw.huygens.repository.storage.mongo;

import java.util.List;

import nl.knaw.huygens.repository.storage.mongo.StorageFactory.StorageType;
import nl.knaw.huygens.repository.util.Configuration;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

public class StorageConfiguration {
  private String host;
  private int port;
  private String dbName;
  private String user;
  private String password;
  private String type;
  private String key;
  private List<String> documentTypes;
  private List<String> versionedTypes;

  public StorageConfiguration(String host, int port, String dbName, String user, String password, String type) {
    this.host = host;
    this.port = port;
    this.dbName = dbName;
    this.user = user;
    this.password = password;
    this.type = type;
  }

  public StorageConfiguration(Configuration conf) {
    type = conf.getSetting("database.type", "unknown");
    host = conf.getSetting("database.host", "localhost");
    port = conf.getIntSetting("database.port", 27017);
    dbName = conf.getSetting("database.name", conf.getSetting("project.internalname", "data"));
    user = conf.getSetting("database.user", null);
    password = conf.getSetting("database.password", null);
    key = Joiner.on(":").useForNull("null").join(host, port, dbName, user, password);
    String docTypes = conf.getSetting("doctypes");
    documentTypes = Lists.newArrayList(docTypes.split(","));
    String versionedDocTypes = conf.getSetting("versioneddoctypes", docTypes);
    versionedTypes = Lists.newArrayList(versionedDocTypes.split(","));
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

  public List<String> getDocumentTypes() {
    return documentTypes;
  }

  public List<String> getVersionedTypes() {
    return versionedTypes;
  }
}