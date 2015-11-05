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

import com.fasterxml.jackson.annotation.JsonProperty;
import nl.knaw.huygens.timbuctoo.annotations.DBProperty;
import nl.knaw.huygens.timbuctoo.annotations.IDPrefix;
import nl.knaw.huygens.timbuctoo.model.SystemEntity;
import nl.knaw.huygens.timbuctoo.model.util.Change;
import nl.knaw.huygens.timbuctoo.model.util.Datable;

import java.util.List;
import java.util.Map;

import static nl.knaw.huygens.timbuctoo.storage.graph.FieldType.VIRTUAL;

/**
 * A system entity for test purposes, in particular for handling cases
 * where properties are set and modified. The type of these properties
 * is not relevant for that purpose, so we simply use strings.
 */
@IDPrefix(TestSystemEntityWrapper.ID_PREFIX)
public class TestSystemEntityWrapper extends SystemEntity {

  public static final String ID_PREFIX = "TSYW";
  public static final String ANNOTED_GETTER_NAME = "annotedGetter";
  public static final String ANOTATED_PROPERTY_NAME = "something";
  public static final String DB_PROPERTY_ANNOTATED = "dbProperty";

  private static String staticField;

  private String stringValue;

  private Long primitiveWrapperValue;
  private int primitiveValue;
  private List<Integer> primitiveCollection;
  private List<String> stringCollection;
  private List<Change> objectCollection;
  private Change objectValue;
  private Map<String, String> map;

  @JsonProperty(ANOTATED_PROPERTY_NAME)
  private String annotatedProperty;

  @DBProperty(value = DB_PROPERTY_ANNOTATED, type = VIRTUAL)
  private String dbPropertyAnnotatedWithTypeVirtual;

  private String propertyWithAnnotatedGetter;


  // Values used fo MongoProperties Test
  private Boolean booleanObject;
  private boolean booleanPrimitive;
  private short shortValue;
  private Short shortObject;
  private int intPrimitive;
  private Integer intObject;
  private long longPrimitive;
  private Long longObject;
  private float floatPrimitive;
  private Float floatObject;
  private double doublePrimitive;
  private Double doubleObject;
  private char charPrimitive;
  private Character charObject;
  private String stringObject;
  private Datable datableObject;


  public TestSystemEntityWrapper() {}

  public TestSystemEntityWrapper(String id) {
    setId(id);
  }

  public TestSystemEntityWrapper(String id, String value1) {
    setId(id);
    setStringValue(value1);
  }

  @Override
  public String getIdentificationName() {
    return null;
  }

  public String getStringValue() {
    return stringValue;
  }

  public void setStringValue(String stringValue) {
    this.stringValue = stringValue;
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

  @JsonProperty(ANNOTED_GETTER_NAME)
  public String getPropertyWithAnnotatedGetter() {
    return propertyWithAnnotatedGetter;
  }

  public void setPropertyWithAnnotatedGetter(String propertyWithAnnotatedGetter) {
    this.propertyWithAnnotatedGetter = propertyWithAnnotatedGetter;
  }

  public static String getStaticField() {
    return staticField;
  }

  public static void setStaticField(String staticField) {
    TestSystemEntityWrapper.staticField = staticField;
  }

  private short getShortValue() {
    return shortValue;
  }

  private void setShortValue(short shortValue) {
    this.shortValue = shortValue;
  }

  private Short getShortObject() {
    return shortObject;
  }

  private void setShortObject(Short shortObject) {
    this.shortObject = shortObject;
  }

  private Boolean getBooleanObject() {
    return booleanObject;
  }

  private void setBooleanObject(Boolean booleanObject) {
    this.booleanObject = booleanObject;
  }

  private boolean isBooleanPrimitive() {
    return booleanPrimitive;
  }

  private void setBooleanPrimitive(boolean booleanPrimitive) {
    this.booleanPrimitive = booleanPrimitive;
  }

  private int getIntPrimitive() {
    return intPrimitive;
  }

  private void setIntPrimitive(int intPrimitive) {
    this.intPrimitive = intPrimitive;
  }

  private Integer getIntObject() {
    return intObject;
  }

  private void setIntObject(Integer intObject) {
    this.intObject = intObject;
  }

  private long getLongPrimitive() {
    return longPrimitive;
  }

  private void setLongPrimitive(long longPrimitive) {
    this.longPrimitive = longPrimitive;
  }

  private Long getLongObject() {
    return longObject;
  }

  private void setLongObject(Long longObject) {
    this.longObject = longObject;
  }

  private float getFloatPrimitive() {
    return floatPrimitive;
  }

  private void setFloatPrimitive(float floatPrimitive) {
    this.floatPrimitive = floatPrimitive;
  }

  private Float getFloatObject() {
    return floatObject;
  }

  private void setFloatObject(Float floatObject) {
    this.floatObject = floatObject;
  }

  private double getDoublePrimitive() {
    return doublePrimitive;
  }

  private void setDoublePrimitive(double doublePrimitive) {
    this.doublePrimitive = doublePrimitive;
  }

  private Double getDoubleObject() {
    return doubleObject;
  }

  private void setDoubleObject(Double doubleObject) {
    this.doubleObject = doubleObject;
  }

  private char getCharPrimitive() {
    return charPrimitive;
  }

  private void setCharPrimitive(char charPrimitive) {
    this.charPrimitive = charPrimitive;
  }

  private Character getCharObject() {
    return charObject;
  }

  private void setCharObject(Character charObject) {
    this.charObject = charObject;
  }

  private String getStringObject() {
    return stringObject;
  }

  private void setStringObject(String stringObject) {
    this.stringObject = stringObject;
  }

  private Datable getDatableObject() {
    return datableObject;
  }

  private void setDatableObject(Datable datableObject) {
    this.datableObject = datableObject;
  }
}
