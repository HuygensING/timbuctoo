package nl.knaw.huygens.timbuctoo.tools.importer.neww;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.google.common.collect.Maps;

import nl.knaw.huygens.timbuctoo.storage.ValidationException;
import nl.knaw.huygens.timbuctoo.tools.importer.CSVImporter;

/**
 * Normalizes names of publishers.
 */
public class PublisherNormalizer extends CSVImporter {

  private final Map<String, String> map = Maps.newHashMap();

  public PublisherNormalizer(File file) throws IOException, ValidationException {
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

