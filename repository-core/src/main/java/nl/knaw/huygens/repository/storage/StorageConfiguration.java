package nl.knaw.huygens.repository.storage;

import java.util.Set;

import nl.knaw.huygens.repository.config.Configuration;

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;
import com.google.inject.Inject;

public class StorageConfiguration {

  private String host;
  private int port;
  private String dbName;
  private String user;
  private String password;
  private String key;
  private Set<String> entityTypes;
  private Set<String> versionedTypes;

  public StorageConfiguration(String host, int port, String dbName, String user, String password, String type) {
    this.host = host;
    this.port = port;
    this.dbName = dbName;
    this.user = user;
    this.password = password;
  }

  @Inject
  public StorageConfiguration(Configuration conf) {
    host = conf.getSetting("database.host", "localhost");
    port = conf.getIntSetting("database.port", 27017);
    dbName = conf.getSetting("database.name", conf.getSetting("project.internalname", "data"));
    user = conf.getSetting("database.user", null);
    password = conf.getSetting("database.password", null);
    key = Joiner.on(":").useForNull("null").join(host, port, dbName, user, password);
    String docTypes = conf.getSetting("doctypes");
    entityTypes = Sets.newHashSet(docTypes.split(","));
    String versionedDocTypes = conf.getSetting("versioneddoctypes", docTypes);
    versionedTypes = Sets.newHashSet(versionedDocTypes.split(","));
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

  public String getKey() {
    return key;
  }

  public Set<String> getEntityTypes() {
    return entityTypes;
  }

  public Set<String> getVersionedTypes() {
    return versionedTypes;
  }

}
