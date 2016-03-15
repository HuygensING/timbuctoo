package nl.knaw.huygens.timbuctoo.model.util;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.Strings;

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

public class Period {

  private Datable startDate;
  private Datable endDate;

  public Period() {}

  public Period(Datable startDate, Datable endDate) {
    this.startDate = startDate;
    this.endDate = endDate;
  }

  public Period(String startDate, String endDate) {
    this(convert(startDate), convert(endDate));
  }

  private static Datable convert(String value) {
    String edtf = (value.length() < 4 && value.matches("\\d+")) ? Strings.padStart(value, 4, '0') : value;
    return new Datable(edtf);
  }

  public Datable getStartDate() {
    return startDate;
  }

  public Datable getEndDate() {
    return endDate;
  }

  @Override
  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj, false);
  }

  @Override
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this, false);
  }
}
