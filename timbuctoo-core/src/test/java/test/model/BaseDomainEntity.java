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

import nl.knaw.huygens.timbuctoo.annotations.DBProperty;
import nl.knaw.huygens.timbuctoo.annotations.IDPrefix;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;

import com.fasterxml.jackson.annotation.JsonProperty;
import nl.knaw.huygens.timbuctoo.storage.graph.FieldType;

/**
 * A domain entity for test purposes, in particular for handling cases
 * where properties are set and modified. The type of these properties
 * is not relevant for that purpose, so we simply use strings.
 */
@IDPrefix(BaseDomainEntity.ID_PREFIX)
public class BaseDomainEntity extends DomainEntity {

  public static final String ID_PREFIX = "TDOM";

  @DBProperty(value = "sharedValue", type = FieldType.ADMINISTRATIVE)
  @JsonProperty("^sharedValue")
  private String sharedValue;

  private String value1;
  private String value2;

  public BaseDomainEntity() {}

  public BaseDomainEntity(String id) {
    setId(id);
  }

  public BaseDomainEntity(String id, String pid, String value1, String value2) {
    setId(id);
    setPid(pid);
    setValue1(value1);
    setValue2(value2);
  }

  @Override
  public String getIdentificationName() {
    return null;
  }

  public String getSharedValue() {
    return sharedValue;
  }

  public void setSharedValue(String value) {
    sharedValue = value;
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

}
