package nl.knaw.huygens.timbuctoo.tools.importer;

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

import static com.google.common.base.Preconditions.checkArgument;

import java.io.File;
import java.util.List;

import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.index.IndexManager;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Location;
import nl.knaw.huygens.timbuctoo.model.base.BaseLocation;
import nl.knaw.huygens.timbuctoo.tools.config.ToolsInjectionModule;
import nl.knaw.huygens.timbuctoo.tools.process.Progress;
import nl.knaw.huygens.timbuctoo.util.Files;

import org.apache.commons.io.LineIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Stopwatch;
import com.google.inject.Injector;

public class LocationUpdater extends DefaultImporter {

  private static final Logger LOG = LoggerFactory.getLogger(LocationUpdater.class);

  private static final String DEFAULT_VRE_ID = "base";
  private static final String IMPORT_DIRECTORY_NAME = "../../timbuctoo-testdata/src/main/resources/import/";

  public static void main(String[] args) throws Exception {
    Stopwatch stopWatch = Stopwatch.createStarted();

    String vreId = (args.length > 0) ? args[0] : DEFAULT_VRE_ID;
    LOG.info("VRE: {}", vreId);

    String directoryName = (args.length > 1) ? args[1] : IMPORT_DIRECTORY_NAME + vreId + "/";
    File directory = new File(directoryName);
    checkArgument(directory.isDirectory(), "Not a directory: %s", directory);

    Injector injector = ToolsInjectionModule.createInjector();
    Repository repository = injector.getInstance(Repository.class);
    IndexManager indexManager = injector.getInstance(IndexManager.class);

    try {
      LocationUpdater updater = new LocationUpdater(repository, indexManager, vreId);
      updater.handleFile(new File(directory, "baselocation.json"));
      updater.displayErrorSummary();
      updater.displayStatus();
    } finally {
      indexManager.close();
      repository.close();
      LOG.info("Time used: {}", stopWatch);
    }
  }

  // ---------------------------------------------------------------------------

  private final ObjectMapper mapper;

  public LocationUpdater(Repository repository, IndexManager indexManager, String vreId) {
    super(repository,  indexManager,  vreId);
    mapper = new ObjectMapper();
  }

  public <T extends DomainEntity> void handleFile(File file) throws Exception {
    printBoxedText("File: " + file.getName());

    Progress progress = new Progress();
    LineIterator iterator = Files.getLineIterator(file);
    try {
      while (iterator.hasNext()) {
        String line = iterator.nextLine().trim();
        if (!line.isEmpty()) {
          progress.step();
          handleEntity(line);
        }
      }
    } finally {
      LineIterator.closeQuietly(iterator);
      progress.done();
    }
  }

  private <T extends DomainEntity> void handleEntity(String line) throws Exception {
    BaseLocation location = mapper.readValue(line, BaseLocation.class);
    String urn = location.getUrn();
    List<Location> entities = repository.getEntitiesByProperty(Location.class, Location.URN, urn).getAll();
    if (entities.isEmpty()) {
      addDomainEntity(BaseLocation.class, location);
    } else if (entities.size() == 1) {
      Location entity = entities.get(0);
      location.setId(entity.getId());
      location.setRev(entity.getRev());
      updatePrimitiveDomainEntity(Location.class, location);
    } else {
      LOG.error("There are {} locations with urn {}", entities.size(), urn);
    }
  }

}
