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

import nl.knaw.huygens.timbuctoo.tools.importer.CSVImporter;

import org.apache.commons.lang.StringUtils;

import com.google.common.collect.Maps;

/**
 * Normalizes names of publishers.
 */
public class PublisherNormalizer extends CSVImporter {

  private final Map<String, String> map = Maps.newHashMap();

  public PublisherNormalizer(File file) throws Exception {
    super(new PrintWriter(System.err));
    if (file != null) {
      handleFile(file, 2, false);
    }
  }

  @Override
  protected void handleLine(String[] items) {
    if (map.containsKey(items[0])) {
      throw new RuntimeException("Duplicate entry for key " + items[0]);
    }
    map.put(items[0], items[1]);
  }

  @Override
  protected void handleEndOfFile() {
    System.out.printf("Publisher concordance size : %d%n", map.size());
  };

  public String preprocess(String text) {
    // remove suspect entries
    if (text.contains("(") || text.contains("[") || text.contains("etc.") || text.startsWith("in ")) {
      return "";
    }

    text = StringUtils.capitalize(text);
    text = text.replaceAll("/", " / ");
    text = text.replaceAll("\\band\\b", "&");
    text = text.replaceAll("\\bco\\b", "Co");
    text = text.replaceAll("\\ben\\b", "&");
    text = text.replaceAll("\\bet\\b", "&");

    text = text.replaceAll("\\b([A-Z])\\s+", "$1. ");
    text = text.replaceAll("\\b([A-Z]\\.)(\\w\\w)", "$1 $2");
    text = text.replaceAll("\\b([A-Z]\\.)\\s+([A-Z]\\.)", "$1$2");

    text = text.replaceAll("[\\s\\u00A0]+", " ").trim();

    text = text.replaceAll("& [Cc]omp\\.", "& Co.");
    text = text.replaceAll("& [Cc]ompany", "& Co.");
    text = text.replaceAll("& [Cc]o\\.?$", "& Co.");
    text = text.replaceAll("& son", "& Son");
    text = text.replaceAll("& de", "& De");
    text = text.replaceAll("& van", "& Van");
    text = text.replaceAll("& Zn\\.?", "& Zoon");
    text = text.replaceAll("& zoon", "& Zoon");

    if (text.contains("etc.")) {
      text = "";
    }

    return (text.length() > 50 || text.matches(".*?\\d.*?")) ? "" : text;
  }

  public String normalize(String text) {
    text = preprocess(text);
    String mapped = map.get(text);
    if (mapped == null) {
      return text;
    } else if (mapped.equals("IGNORE")) {
      return "";
    } else {
      return mapped;
    }
  }

}
