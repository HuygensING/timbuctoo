package nl.knaw.huygens.timbuctoo.tools.importer.neww;

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

import java.io.File;
import java.io.PrintWriter;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.index.IndexManager;
import nl.knaw.huygens.timbuctoo.model.neww.WWKeyword;
import nl.knaw.huygens.timbuctoo.model.util.Change;
import nl.knaw.huygens.timbuctoo.storage.ValidationException;
import nl.knaw.huygens.timbuctoo.tools.config.ToolsInjectionModule;
import nl.knaw.huygens.timbuctoo.tools.importer.CSVImporter;
import nl.knaw.huygens.timbuctoo.tools.importer.Constants;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Maps;
import com.google.inject.Injector;

/**
 * Handles updates and additions for NEWW genre keywords.
 */
public class GenreUpdater extends CSVImporter {

  private static final Logger LOG = LoggerFactory.getLogger(GenreUpdater.class);

  public static void main(String[] args) throws Exception {
    Stopwatch stopWatch = Stopwatch.createStarted();

    // Handle commandline arguments
    String directory = (args.length > 0) ? args[0] : "../../timbuctoo-testdata/src/main/resources/neww/";

    Repository repository = null;
    IndexManager indexManager = null;
    try {
      Injector injector = ToolsInjectionModule.createInjector();
      repository = injector.getInstance(Repository.class);
      indexManager = injector.getInstance(IndexManager.class);
      Change change = new Change(Constants.IMPORT_USER, "neww");

      GenreUpdater importer = new GenreUpdater(repository, change);
      importer.handleFile(new File(directory, "genre-update.csv"), 2, false);
    } finally {
      if (indexManager != null) {
        indexManager.close();
      }
      if (repository != null) {
        repository.close();
      }
      LOG.info("Time used: {}", stopWatch);
    }
  }

  // -------------------------------------------------------------------

  private static final String TYPE = "genre";

  private final Repository repository;
  private final Change change;

  private final Map<String, WWKeyword> map = Maps.newHashMap();

  public GenreUpdater(Repository repository, Change change) {
    super(new PrintWriter(System.err));
    this.repository = repository;
    this.change = change;
  }

  @Override
  protected void initialize() throws Exception {
    for (WWKeyword keyword : repository.getEntitiesByProperty(WWKeyword.class, "type", TYPE).getAll()) {
      if (map.put(keyword.getValue(), keyword) != null) {
        LOG.warn("Storage contains a duplicate keyword '{}'", keyword.getValue());
      }
    }
  }

  @Override
  protected void handleLine(String[] items) throws Exception {
    if (items.length < 2) {
      throw new ValidationException("Lines must have at least 2 items");
    }

    String oldValue = StringUtils.trimToEmpty(items[0]);
    String newValue = StringUtils.trimToEmpty(items[1]);
    if (oldValue.equals(newValue)) {
      LOG.info("Ignored '{}' (identical)", oldValue);
    } else if (oldValue.equals("?")) {
      // add keyword
      if (map.containsKey(newValue)) {
        LOG.error("Skipped '{}' (duplicate)", newValue);
      } else {
        WWKeyword keyword = new WWKeyword();
        keyword.setType(TYPE);
        keyword.setValue(newValue);
        repository.addDomainEntity(WWKeyword.class, keyword, change);
        LOG.info("Added   '{}'", newValue);
      }
    } else {
      // update keyword
      WWKeyword keyword = map.get(oldValue);
      if (keyword == null) {
        LOG.error("Skipped '{}' (missing)", oldValue);
      } else if (map.containsKey(newValue)) {
        LOG.error("Skipped '{}' (duplicate)", newValue);
      } else {
        keyword.setValue(newValue);
        repository.updateDomainEntity(WWKeyword.class, keyword, change);
        LOG.info("Updated '{}' to '{}'", oldValue, newValue);
      }
    }
  }

}
