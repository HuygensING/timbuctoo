package nl.knaw.huygens.timbuctoo.variation.model;

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
