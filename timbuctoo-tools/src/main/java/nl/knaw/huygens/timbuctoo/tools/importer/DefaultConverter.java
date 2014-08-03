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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;

import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.tools.config.ToolsInjectionModule;
import nl.knaw.huygens.timbuctoo.tools.process.Task;
import nl.knaw.huygens.timbuctoo.tools.util.EntityToJsonConverter;
import nl.knaw.huygens.timbuctoo.util.Files;

import org.apache.commons.lang.StringUtils;

import com.google.common.base.Strings;
import com.google.inject.Injector;

/**
 * Contains functionality needed in most converters.
 */
public abstract class DefaultConverter implements Task {

  protected final Injector injector;
  protected final File outputDirectory;
  protected final EntityToJsonConverter jsonConverter;

  public DefaultConverter(String vreId) {
    try {
      injector = ToolsInjectionModule.createInjector();
      outputDirectory = new File(new File("import"), vreId);
      outputDirectory.mkdirs();
      jsonConverter = new EntityToJsonConverter();
    } catch (Exception e) {
      throw new IllegalStateException("Failed to initialize convereter: " + e.getMessage());
    }
  }

  protected <T extends DomainEntity> PrintWriter createPrintWriter(Class<T> type) {
    String filename = TypeNames.getInternalName(type) + ".json";
    return Files.createPrintWriter(outputDirectory, filename);
  }

  // --- Error handling --------------------------------------------------------

  private int errors = 0;
  private String prevMessage = "";

  protected void handleError(String format, Object... args) {
    errors++;
    String message = String.format(format, args);
    if (!message.equals(prevMessage)) {
      System.out.print("## ");
      System.out.print(message);
      System.out.println();
      prevMessage = message;
    }
  }

  protected void displayErrorSummary() {
    if (errors > 0) {
      System.out.printf("%n## Error count = %d%n", errors);
    }
  }

  // --- Conversion log --------------------------------------------------------

  private Writer importLog;
  private String sourceName;

  protected void openLog(String fileName) throws IOException {
    File file = new File(fileName);
    FileOutputStream fos = new FileOutputStream(file);
    OutputStreamWriter out = new OutputStreamWriter(fos, "UTF-8");
    importLog = new BufferedWriter(out);
  }

  protected void closeLog() throws IOException {
    if (importLog != null) {
      importLog.close();
    }
  }

  protected void logSourceName(String sourceName) {
    this.sourceName = sourceName;
  }

  protected void log(String format, Object... args) {
    String text = String.format(format, args);
    if (importLog != null) {
      try {
        if (sourceName != null) {
          importLog.write(String.format("-- %s%n", sourceName));
        }
        importLog.write(text);
        return;
      } catch (IOException e) {
        // ignore
      }
    }
    System.out.println(text);
  }

  /**
   * Displays a text in a formatted box.
   */
  protected void printBoxedText(String text) {
    String line = Strings.repeat("-", text.length() + 8);
    System.out.println();
    System.out.println(line);
    System.out.print("--  ");
    System.out.print(text);
    System.out.println("  --");
    System.out.println(line);
  }

  /**
   * Filters a text field by collapsing whitespace and removing leading and trailing whitespace.
   * Returns {@code null} if the remaining text is empty.
   */
  protected String filterField(String text) {
    if (text == null) {
      return null;
    }
    if (text.contains("\\")) {
      text = text.replaceAll("\\\\r", " ");
      text = text.replaceAll("\\\\n", " ");
    }
    text = text.replaceAll("[\\s\\u00A0]+", " ");
    return StringUtils.stripToNull(text);
  }

  /** Line separator in note fields */
  public static final String NEWLINE = "\n";

  /**
   * Filters a notes text field by collapsing whitespace and removing leading and trailing whitespace.
   * Newlines are  retained
   * Returns {@code null} if the remaining text is empty.
   */
  protected String filterNotesField(String text) {
    if (text == null) {
      return null;
    }
    if (text.contains("\\")) {
      text = text.replaceAll("\\\\r", NEWLINE);
      text = text.replaceAll("\\\\n", NEWLINE);
    }
    text = text.replaceAll("[ \\u00A0]+", " ");
    return StringUtils.stripToNull(text);
  }

}
