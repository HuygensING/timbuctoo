package nl.knaw.huygens.timbuctoo.model;

import java.util.Date;

import nl.knaw.huygens.timbuctoo.annotations.IDPrefix;
import nl.knaw.huygens.timbuctoo.facet.IndexAnnotation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;

@IDPrefix("TSTD")
public class TestSystemEntity extends SystemEntity {

  private String name;
  private String testValue1;
  private String testValue2;
  @JsonProperty("propAnnotated")
  private String annotatedProperty;
  private String propWithAnnotatedAccessors;
  private Date date;

  public TestSystemEntity() {}

  public TestSystemEntity(String id) {
    setId(id);
  }

  @Override
  @JsonIgnore
  @IndexAnnotation(fieldName = "desc")
  public String getDisplayName() {
    return name;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getTestValue1() {
    return testValue1;
  }

  public void setTestValue1(String testValue1) {
    this.testValue1 = testValue1;
  }

  public String getTestValue2() {
    return testValue2;
  }

  public void setTestValue2(String testValue2) {
    this.testValue2 = testValue2;
  }

  public String getAnnotatedProperty() {
    return annotatedProperty;
  }

  public void setAnnotatedProperty(String annotatedProperty) {
    this.annotatedProperty = annotatedProperty;
  }

  @JsonProperty("pwaa")
  public String getPropWithAnnotatedAccessors() {
    return propWithAnnotatedAccessors;
  }

  @JsonProperty("pwaa")
  public void setPropWithAnnotatedAccessors(String propWithAnnotatedAccessors) {
    this.propWithAnnotatedAccessors = propWithAnnotatedAccessors;
  }

  public Date getDate() {
    return date;
  }

  public void setDate(Date date) {
    this.date = date;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof TestSystemEntity)) {
      return false;
    }

    boolean isEqual = true;
    TestSystemEntity other = (TestSystemEntity) obj;
    isEqual &= Objects.equal(other.name, name);
    isEqual &= Objects.equal(other.testValue1, testValue1);
    isEqual &= Objects.equal(other.testValue2, testValue2);
    isEqual &= Objects.equal(other.annotatedProperty, annotatedProperty);
    isEqual &= Objects.equal(other.propWithAnnotatedAccessors, propWithAnnotatedAccessors);

    return isEqual;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(name, testValue1, testValue2, annotatedProperty, propWithAnnotatedAccessors);
  }

  @Override
  public String toString() {
    return "TestSystemEntity{\nname: " + name + "\ntestValue1: " + testValue1 + "\ntestValue2: " + testValue2 + "\nannotatedProperty: " + annotatedProperty + "propWithAnnotatedAccessors: "
        + propWithAnnotatedAccessors + "\n}";
  }
}
