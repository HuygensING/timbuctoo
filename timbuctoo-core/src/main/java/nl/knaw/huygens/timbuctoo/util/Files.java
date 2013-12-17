package nl.knaw.huygens.timbuctoo.util;

/*
 * #%L
 * Timbuctoo core
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

public class Files {

  /** Default encoding for text files. */
  public static final String ENCODING = "UTF-8";

  /** Standard header of XML files. */
  public static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n";

  /**
   * Returns a <code>File</code> with the specfied name in the user's home directory.
   * This may be a file or a directory; it may exist or not.
   */
  public static File fileInHomeDirectory(String filename) {
    return new File(System.getProperty("user.home"), filename);
  }

  public static void writeTextToFile(String text, File file, boolean append) {
    FileOutputStream stream = null;
    try {
      stream = new FileOutputStream(file, append);
      IOUtils.write(text, stream, ENCODING);
    } catch (IOException e) {
      System.err.println(">> " + e.getMessage());
      IOUtils.closeQuietly(stream);
    }
  }

  public static void writeTextToFile(String text, File file) {
    writeTextToFile(text, file, false);
  }

  public static void writeTextToFile(String text, String filename) {
    writeTextToFile(text, new File(filename));
  }

  public static String readTextFromFile(File file) {
    try {
      return FileUtils.readFileToString(file, ENCODING);
    } catch (IOException e) {
      throw new RuntimeException("Failed to read from " + file.getAbsolutePath());
    }
  }

  /**
   * Write standard header to CSV file.
   */
  public static void writeHeader(PrintWriter out, String description) {
    out.println("--");
    out.println("-- " + description);
    out.println("-- " + new Date());
    out.println("--");
  }

  private Files() {
    throw new AssertionError("Non-instantiable class");
  }

}
