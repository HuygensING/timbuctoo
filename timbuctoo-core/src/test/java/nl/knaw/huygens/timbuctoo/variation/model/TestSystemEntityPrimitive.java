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

import nl.knaw.huygens.timbuctoo.model.SystemEntity;

public class TestSystemEntityPrimitive extends SystemEntity {

  private int testInt;
  private boolean testBoolean;
  private long testLong;
  private double testDouble;
  private short testShort;
  private float testFloat;
  private char testChar;

  public int getTestInt() {
    return testInt;
  }

  public void setTestInt(int testInt) {
    this.testInt = testInt;
  }

  public boolean isTestBoolean() {
    return testBoolean;
  }

  public void setTestBoolean(boolean testBoolean) {
    this.testBoolean = testBoolean;
  }

  public long getTestLong() {
    return testLong;
  }

  public void setTestLong(long testLong) {
    this.testLong = testLong;
  }

  public double getTestDouble() {
    return testDouble;
  }

  public void setTestDouble(double testDouble) {
    this.testDouble = testDouble;
  }

  public short getTestShort() {
    return testShort;
  }

  public void setTestShort(short testShort) {
    this.testShort = testShort;
  }

  public float getTestFloat() {
    return testFloat;
  }

  public void setTestFloat(float testFloat) {
    this.testFloat = testFloat;
  }

  public char getTestChar() {
    return testChar;
  }

  public void setTestChar(char testChar) {
    this.testChar = testChar;
  }

  @Override
  public String getDisplayName() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean equals(Object obj) {

    if (!(obj instanceof TestSystemEntityPrimitive)) {
      return false;
    }

    TestSystemEntityPrimitive other = (TestSystemEntityPrimitive) obj;

    boolean isEqual = (other.testBoolean == testBoolean);
    isEqual &= (other.testChar == testChar);
    isEqual &= (other.testDouble == testDouble);
    isEqual &= (other.testFloat == testFloat);
    isEqual &= (other.testInt == testInt);
    isEqual &= (other.testLong == testLong);
    isEqual &= (other.testShort == testShort);

    return isEqual;
  }

  @Override
  public String toString() {
    return "TestSystemEntityPrimitive{\ntestBoolean: " + testBoolean + "\ntestChar: " + testChar + "\ntestDouble: " + testDouble + "\ntestFloat: " + testFloat + "\ntestInt: " + testInt
        + "\ntestLong: " + testLong + "\ntestShort: " + testShort + "\n}";
  }
}
