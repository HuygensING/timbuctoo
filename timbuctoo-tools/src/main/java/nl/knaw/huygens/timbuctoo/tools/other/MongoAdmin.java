package nl.knaw.huygens.timbuctoo.tools.other;

/*
 * #%L
 * Timbuctoo tools
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

import java.net.UnknownHostException;

import nl.knaw.huygens.timbuctoo.config.Configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.MongoOptions;
import com.mongodb.ServerAddress;

/**
 * Manages the Mongo database.
 */
public class MongoAdmin {

  private static final Logger LOG = LoggerFactory.getLogger(MongoAdmin.class);

  private Mongo mongo;
  private String dbName;
  private DB db;

  public MongoAdmin(Configuration config) throws UnknownHostException {
    MongoOptions options = new MongoOptions();
    options.safe = true;

    String host = config.getSetting("database.host", "localhost");
    int port = config.getIntSetting("database.port", 27017);
    mongo = new Mongo(new ServerAddress(host, port), options);

    dbName = config.getSetting("database.name");
    db = mongo.getDB(dbName);

    String user = config.getSetting("database.user");
    if (!user.isEmpty()) {
      String password = config.getSetting("database.password");
      db.authenticate(user, password.toCharArray());
    }
  }

  public void dropDatabase() {
    try {
      LOG.info("Dropping database '{}'", dbName);
      db.dropDatabase();
      LOG.info("Dropped database");
    } catch (MongoException e) {
      LOG.error(e.getMessage());
    } finally {
      mongo.close();
      LOG.info("Closed");
      db = null;
    }
  }

}
