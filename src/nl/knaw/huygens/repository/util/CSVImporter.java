package nl.knaw.huygens.repository.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;

import org.apache.commons.lang.StringUtils;

import au.com.bytecode.opencsv.CSVReader;

/**
 * Base class for handling CSV files.
 */
public abstract class CSVImporter {

  public static final char SEPARATOR = ';';
  public static final char QUOTE = '"';

  private static final int LINES_TO_SKIP = 4;

  protected final PrintWriter out;

  public CSVImporter(PrintWriter out) {
    this.out = out;
  }

  public void handleFile(File file, int itemsPerLine, boolean verbose) throws IOException {
    handleFile(new FileInputStream(file), itemsPerLine, verbose);
  }

  public void handleFile(String filename, int itemsPerLine, boolean verbose) throws IOException {
    handleFile(new FileInputStream(filename), itemsPerLine, verbose);
  }

  private void handleFile(FileInputStream stream, int itemsPerLine, boolean verbose) throws IOException {
    initialize();
    CSVReader reader = null;
    try {
      Reader fileReader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
      reader = new CSVReader(fileReader, SEPARATOR, QUOTE, LINES_TO_SKIP);
      for (String[] line : reader.readAll()) {
        // allow lines to be empty
        if (line.length > 0) {
          validateLine(line, itemsPerLine, verbose);
          handleLine(line);
        }
      }
      handleEndOfFile();
    } finally {
      if (reader != null) {
        reader.close();
      }
      if (out != null) {
        out.flush();
      }
    }
  }

  /**
   * Performs initialization before handling of a file.
   */
  protected void initialize() {}

  /**
   * Handles a parsed input line.
   */
  protected abstract void handleLine(String[] items);

  /**
   * Performa actions after file has been handled.
   */
  protected void handleEndOfFile() {};

  private void validateLine(String[] line, int itemsPerLine, boolean verbose) {
    boolean error = (itemsPerLine != 0 && itemsPerLine != line.length);
    if (error || verbose) {
      out.println();
      for (String word : line) {
        out.println("[" + word + "]");
      }
      if (error) {
        out.println("## Number of items must be " + itemsPerLine);
        out.flush();
        throw new RuntimeException("Error on line '" + line[0] + "...'");
      }
    }
  }

  protected void displayError(String message, String[] line) {
    out.printf("%n## %s%n", message);
    out.printf("   [%s]%n", StringUtils.join(line, "] ["));
  }

}
