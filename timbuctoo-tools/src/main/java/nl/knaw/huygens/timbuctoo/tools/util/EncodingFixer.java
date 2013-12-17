package nl.knaw.huygens.timbuctoo.tools.util;

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

import org.apache.commons.lang.StringUtils;

/**
 * Fixes latin-1 data that was wrongly converted to UTF-8.
 * Currently handles common lowercase accented characters.
 * See: http://www.weblogism.com/item/270/why-does-e-become-a
 *
 * Some texts may contain a 'Ã' followed by a regular space that was
 * orginally a nonbreaking space. You would need to check this.
 */
public class EncodingFixer {

  private static final int N = 22;
  private static final String[][] MAP = { //
  { "Ã ", "à" }, // Ã + nonbreaking space
      { "Ã¡", "á" }, //
      { "Ã¢", "â" }, //
      { "Ã¤", "ä" }, //
      { "Ã§", "ç" }, //
      { "Ã¨", "è" }, //
      { "Ã©", "é" }, //
      { "Ãª", "ê" }, //
      { "Ã«", "ë" }, //
      { "Ã¬", "ì" }, //
      { "Ã­", "í" }, // Ã + optional hyphen
      { "Ã®", "î" }, //
      { "Ã¯", "ï" }, //
      { "Ã±", "ñ" }, //
      { "Ã²", "ò" }, //
      { "Ã³", "ó" }, //
      { "Ã´", "ô" }, //
      { "Ã¶", "ö" }, //
      { "Ã¹", "ù" }, //
      { "Ãº", "ú" }, //
      { "Ã»", "û" }, //
      { "Ã¼", "ü" } //
  };

  private static final String[] SOURCE = new String[N];
  private static final String[] TARGET = new String[N];

  static {
    for (int i = 0; i < N; i++) {
      SOURCE[i] = MAP[i][0];
      TARGET[i] = MAP[i][1];
    }
  }

  public static String convert1(String text) {
    return StringUtils.replaceEach(text, SOURCE, TARGET);
  }

  public static String convert2(String text) {
    String conv1 = convert1(text);
    if (!conv1.equals(text)) {
      String conv2 = convert1(conv1);
      if (!conv2.equals(conv1)) {
        throw new RuntimeException("Instable conversion");
      }
    }
    return conv1;
  }

}
