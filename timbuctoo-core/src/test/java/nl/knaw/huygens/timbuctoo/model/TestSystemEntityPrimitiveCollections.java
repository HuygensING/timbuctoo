package nl.knaw.huygens.timbuctoo.model;

import java.util.List;
import java.util.Set;

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
