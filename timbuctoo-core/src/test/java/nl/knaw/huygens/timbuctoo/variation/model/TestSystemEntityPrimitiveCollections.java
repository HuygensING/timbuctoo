package nl.knaw.huygens.timbuctoo.variation.model;

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

import java.util.List;
import java.util.Set;

import nl.knaw.huygens.timbuctoo.model.SystemEntity;

import com.google.common.base.Objects;
import com.google.common.collect.Sets;

public class TestSystemEntityPrimitiveCollections extends SystemEntity {

  private List<String> testStringList;
  private List<Integer> testIntegerList;

  public List<String> getTestStringList() {
    return testStringList;
  }

  public void setTestStringList(List<String> testStringList) {
    this.testStringList = testStringList;
  }

  public List<Integer> getTestIntegerList() {
    return testIntegerList;
  }

  public void setTestIntegerList(List<Integer> testIntegerList) {
    this.testIntegerList = testIntegerList;
  }

  @Override
  public String getDisplayName() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof TestSystemEntityPrimitiveCollections)) {
      return false;
    }

    TestSystemEntityPrimitiveCollections other = (TestSystemEntityPrimitiveCollections) obj;

    boolean isEqual = true;
    isEqual &= Objects.equal(createSet(other.testIntegerList), createSet(testIntegerList));
    isEqual &= Objects.equal(createSet(other.testStringList), createSet(testStringList));

    return isEqual;

  }

  private <T> Set<T> createSet(List<T> list) {
    if (list == null) {
      return null;
    }
    return Sets.newHashSet(list);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();

    sb.append("TestSystemEntityPrimitiveCollections{\ntestIntegerList: ");
    sb.append(testIntegerList);
    sb.append("\ntestStringList: ");
    sb.append(testStringList);
    sb.append("\n}");

    return sb.toString();
  }
}
