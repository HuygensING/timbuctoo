package nl.knaw.huygens.repository.storage.mongo.model;

import java.util.List;

import nl.knaw.huygens.repository.annotations.IDPrefix;
import nl.knaw.huygens.repository.facet.IndexAnnotation;
import nl.knaw.huygens.repository.model.SystemDocument;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@IDPrefix("TSTD")
public class MongoObjectMapperDocument extends SystemDocument {
  
  private String name;
  private String testValue1;
  private String testValue2;
  @JsonProperty("propAnnotated")
  private String annotatedProperty;
  private String propWithAnnotatedAccessors;

  private List<String> primitiveTestCollection;
  private List<? extends SystemDocument> nonPrimitiveTestCollection;

  @Override
  @JsonProperty("!currentVariation")
  public String getCurrentVariation() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  @JsonProperty("!currentVariation")
  public void setCurrentVariation(String currentVariation) {
    // TODO Auto-generated method stub
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

  public List<String> getPrimitiveTestCollection() {
    return primitiveTestCollection;
  }

  public void setPrimitiveTestCollection(List<String> primitiveTestCollection) {
    this.primitiveTestCollection = primitiveTestCollection;
  }

  public List<? extends SystemDocument> getNonPrimitiveTestCollection() {
    return nonPrimitiveTestCollection;
  }

  public void setNonPrimitiveTestCollection(List<? extends SystemDocument> nonPrimitiveTestCollection) {
    this.nonPrimitiveTestCollection = nonPrimitiveTestCollection;
  }

}
