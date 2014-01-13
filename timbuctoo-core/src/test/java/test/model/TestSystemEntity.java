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

import nl.knaw.huygens.timbuctoo.annotations.IDPrefix;
import nl.knaw.huygens.timbuctoo.model.SystemEntity;

/**
 * A system entity for test purposes, in particular for handling cases
 * where properties are set and modified. The type of these properties
 * is not relevant for that purpose, so we simply use strings.
 */
@IDPrefix("TSYS")
public class TestSystemEntity extends SystemEntity {

  private String value1;
  private String value2;
  private String value3;

  public TestSystemEntity() {}

  public TestSystemEntity(String id) {
    setId(id);
  }

  public TestSystemEntity(String id, String value1, String value2) {
    setId(id);
    setValue1(value1);
    setValue2(value2);
  }

  public TestSystemEntity(String id, String value1, String value2, String value3) {
    setId(id);
    setValue1(value1);
    setValue2(value2);
    setValue3(value3);
  }

  @Override
  public String getDisplayName() {
    return null;
  }

  public String getValue1() {
    return value1;
  }

  public void setValue1(String value) {
    value1 = value;
  }

  public String getValue2() {
    return value2;
  }

  public void setValue2(String value) {
    value2 = value;
  }

  public String getValue3() {
    return value3;
  }

  public void setValue3(String value) {
    value3 = value;
  }

}
