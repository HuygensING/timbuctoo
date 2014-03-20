package nl.knaw.huygens.timbuctoo.tools.util;

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

/**
 * Displays progress on console.
 */
public class Progress {

  private int count;

  public Progress() {
    count = 0;
  }

  public void step() {
    if (count % 10 == 0) {
      if (count % 1000 == 0) {
        System.out.printf("%n%05d ", count);
      }
      System.out.print(".");
    }
    count++;
  }

  public void done() {
    System.out.printf("%n%05d%n%n", count);
  }

}
