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

import nl.knaw.huygens.timbuctoo.config.Configuration;
import nl.knaw.huygens.timbuctoo.index.IndexManager;
import nl.knaw.huygens.timbuctoo.storage.mongo.MongoDB;
import nl.knaw.huygens.timbuctoo.tools.config.ToolsInjectionModule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;
import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * A class to clean up the database and the index.
 */
public class EnvironmentCleaner {

  private static final Logger LOG = LoggerFactory.getLogger(EnvironmentCleaner.class);

  public static void main(String[] args) throws Exception {
    Stopwatch stopWatch = Stopwatch.createStarted();

    Configuration config = new Configuration("config.xml");
    Injector injector = Guice.createInjector(new ToolsInjectionModule(config));

    // clean the database
    MongoDB mongoDB = new MongoDB(config);
    mongoDB.dropDatabase();
    mongoDB.close();

    // clean the index
    IndexManager indexManager = injector.getInstance(IndexManager.class);
    indexManager.deleteAllEntities();
    indexManager.close();

    LOG.info("Time used: {}", stopWatch);
  }

}
