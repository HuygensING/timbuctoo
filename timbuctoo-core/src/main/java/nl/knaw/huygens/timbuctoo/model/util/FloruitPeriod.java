package nl.knaw.huygens.timbuctoo.model.util;

/*
 * #%L
 * Timbuctoo core
 * =======
 * Copyright (C) 2012 - 2015 Huygens ING
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

public class FloruitPeriod extends Period {

  public FloruitPeriod(String date) {
    super(date, date);
  }

  public FloruitPeriod() {}

  public FloruitPeriod(String startDate, String endDate) {
    super(startDate, endDate);
  }

  public FloruitPeriod(Datable date) {
    super(date, date);
  }

  public FloruitPeriod(Datable startDate, Datable endDate) {
    super(startDate, endDate);
  }

  @Override
  public String toString() {
    int yearLo = getStartDate().getFromYear();
    int yearHi = getEndDate().getToYear();

    StringBuilder builder = new StringBuilder("fl. ");
    builder.append(yearLo);
    if (yearLo != yearHi) {
      builder.append('-').append(yearHi);
    }

    return builder.toString();
  }

  @Override
  public boolean equals(Object obj) {
    return super.equals(obj);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

}
