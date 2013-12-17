package nl.knaw.huygens.timbuctoo.tools.other;

/*
 * #%L
 * Timbuctoo tools
 * =======
 * Copyright (C) 2012 - 2013 Huygens ING
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

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.util.Date;

import nl.knaw.huygens.timbuctoo.config.BasicInjectionModule;
import nl.knaw.huygens.timbuctoo.config.Configuration;
import nl.knaw.huygens.timbuctoo.model.SearchResult;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;
import nl.knaw.huygens.timbuctoo.storage.StorageManager;
import nl.knaw.huygens.timbuctoo.tools.util.UTCUtils;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.configuration.ConfigurationException;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Tool for managing stored search results. 
 * This class is mainly used for deleting {@code SearchResults}.
 */
public class SearchResultTool {

  public static void main(String[] args) {
    Options options = new Options();
    options.addOption("d", "delete", true, "delete search results");
    options.addOption("v", "verbose", false, "provide verbose output");

    try {
      CommandLineParser parser = new PosixParser();
      CommandLine cmd = parser.parse(options, args);
      boolean verbose = cmd.hasOption("verbose");
      boolean delete = cmd.hasOption("delete");
      Date threshold = delete ? getThreshold(cmd.getOptionValue("delete")) : null;

      SearchResultTool tool = new SearchResultTool();
      tool.execute(verbose, delete, threshold);
    } catch (ConfigurationException e) {
      System.out.printf("Configuration error. %s%n", e.getMessage());
    } catch (ParseException e) {
      System.out.printf("Parsing failed. %s.%n", e.getMessage());
      String syntax = "java " + SearchResultTool.class.getCanonicalName();
      new HelpFormatter().printHelp(100, syntax, null, options, null, true);
    }
  }

  private static Date getThreshold(String value) throws ParseException {
    Date threshold = null;
    if (!"all".equals(value)) {
      threshold = UTCUtils.stringToDate(value);
      if (threshold == null) {
        String message = String.format("Use ISO date format, e.g. '%s', or 'all'", UTCUtils.now());
        throw new ParseException(message);
      }
    }
    return threshold;
  }

  // -------------------------------------------------------------------

  private final StorageManager storageManager;

  public SearchResultTool() throws ConfigurationException {
    Configuration config = new Configuration("config.xml");
    Injector injector = Guice.createInjector(new BasicInjectionModule(config));
    storageManager = injector.getInstance(StorageManager.class);
  }

  private void execute(boolean verbose, boolean delete, Date date) {
    checkNotNull(storageManager);
    try {
      displayStatus(verbose);
      if (delete) {
        int n;
        if (date != null) {
          n = storageManager.deleteSearchResultsBefore(date);
        } else {
          n = storageManager.deleteAllSearchResults();
        }
        System.out.printf("Search results removed   : %5d%n", n);
        displayStatus(false);
      }
    } catch (IOException e) {
      System.out.printf("Error: %s%n", e.getMessage());
    } finally {
      storageManager.close();
    }
  }

  private void displayStatus(boolean verbose) {
    StorageIterator<SearchResult> iterator = storageManager.getAll(SearchResult.class);
    System.out.printf("Search results in storage: %5d%n", iterator.size());
    while (verbose && iterator.hasNext()) {
      SearchResult result = iterator.next();
      String text = UTCUtils.dateToString(result.getDate());
      System.out.printf(">> %s%n", text);
    }
  }

}
