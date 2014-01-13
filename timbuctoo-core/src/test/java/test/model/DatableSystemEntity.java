package test.model;

/*
 * #%L
 * Timbuctoo core
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

import nl.knaw.huygens.timbuctoo.model.SystemEntity;
import nl.knaw.huygens.timbuctoo.model.util.Datable;

import com.google.common.base.Objects;

public class DatableSystemEntity extends SystemEntity {

  private Datable testDatable;

  @Override
  public String getDisplayName() {
    return null;
  }

  public Datable getTestDatable() {
    return testDatable;
  }

  public void setTestDatable(Datable testDatable) {
    this.testDatable = testDatable;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof DatableSystemEntity)) {
      return false;
    }

    DatableSystemEntity other = (DatableSystemEntity) obj;

    boolean isEqual = true;
    isEqual &= Objects.equal(other.testDatable, testDatable);
    isEqual &= Objects.equal(other.getRev(), getRev());

    return isEqual;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("DatableSystemEntity{\ntestDatable: ");
    sb.append(testDatable);
    sb.append("\nrev: ");
    sb.append(getRev());
    sb.append("\n}");
    return sb.toString();
  }

}
