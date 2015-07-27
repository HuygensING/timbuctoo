package nl.knaw.huygens.timbuctoo.storage.mongo;

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

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.ModelException;
import nl.knaw.huygens.timbuctoo.storage.EntityInducer;
import nl.knaw.huygens.timbuctoo.storage.EntityReducer;

import org.junit.After;
import org.junit.Before;

import com.mongodb.MongoClient;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;

public class DBIntegrationTest {

  private static final int DB_PORT = 12345;
  private static final MongodStarter starter = MongodStarter.getDefaultInstance();
  private MongodExecutable mongodExe;
  private MongodProcess mongod;
  private MongoClient mongo;
  private MongoDB mongoDB;

  public DBIntegrationTest() {
    super();
  }

  @Before
  public void setUp() throws Exception {
    mongodExe = starter.prepare(new MongodConfigBuilder().version(Version.Main.PRODUCTION)//
        .net(new Net(DB_PORT, Network.localhostIsIPv6()))//
        .build());

    mongod = mongodExe.start();
    mongo = new MongoClient("localhost", DB_PORT);

    mongoDB = new MongoDB(mongo, mongo.getDB("test"));
  }

  @After
  public void tearDown() {
    mongod.stop();
    mongodExe.stop();
  }

  protected MongoStorage createMongoStorage(TypeRegistry typeRegistry) throws ModelException {
    return new MongoStorage(mongoDB, new EntityIds(typeRegistry, mongoDB), new EntityInducer(), new EntityReducer(typeRegistry));
  }

}
