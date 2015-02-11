package test.model;

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

import java.util.List;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.annotations.IDPrefix;
import nl.knaw.huygens.timbuctoo.model.SystemEntity;
import nl.knaw.huygens.timbuctoo.model.util.Change;

/**
 * A system entity for test purposes, in particular for handling cases
 * where properties are set and modified. The type of these properties
 * is not relevant for that purpose, so we simply use strings.
 */
@IDPrefix("TSYW")
public class TestSystemEntityWrapper extends SystemEntity {

  private String value1;
  private String value2;
  private String value3;

  private Long primitiveWrapperValue;
  private int primitiveValue;
  private List<Integer> primitiveCollection;
  private List<String> stringCollection;
  private List<Change> objectCollection;
  private Change objectValue;
  private Map<String, String> map;

  public TestSystemEntityWrapper() {}

  public TestSystemEntityWrapper(String id) {
    setId(id);
  }

  public TestSystemEntityWrapper(String id, String value1, String value2) {
    setId(id);
    setValue1(value1);
    setValue2(value2);
  }

  public TestSystemEntityWrapper(String id, String value1, String value2, String value3) {
    setId(id);
    setValue1(value1);
    setValue2(value2);
    setValue3(value3);
  }

  @Override
  public String getIdentificationName() {
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

  public Long getLongWrapperValue() {
    return primitiveWrapperValue;
  }

  public void setLongWrapperValue(Long longWrapperValue) {
    this.primitiveWrapperValue = longWrapperValue;
  }

  public int getIntValue() {
    return primitiveValue;
  }

  public void setIntValue(int intValue) {
    this.primitiveValue = intValue;
  }

  public List<Integer> getPrimitiveCollection() {
    return primitiveCollection;
  }

  public void setPrimitiveCollection(List<Integer> primitiveCollection) {
    this.primitiveCollection = primitiveCollection;
  }

  public List<String> getStringCollection() {
    return stringCollection;
  }

  public void setStringCollection(List<String> stringCollection) {
    this.stringCollection = stringCollection;
  }

  public List<Change> getObjectCollection() {
    return objectCollection;
  }

  public void setObjectCollection(List<Change> objectCollection) {
    this.objectCollection = objectCollection;
  }

  public Change getObjectValue() {
    return objectValue;
  }

  public void setObjectValue(Change objectValue) {
    this.objectValue = objectValue;
  }

  public Map<String, String> getMap() {
    return map;
  }

  public void setMap(Map<String, String> map) {
    this.map = map;
  }

}
