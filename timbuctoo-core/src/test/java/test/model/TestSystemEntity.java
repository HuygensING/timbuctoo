package test.model;

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

  public TestSystemEntity() {}

  public TestSystemEntity(String id) {
    setId(id);
  }

  public TestSystemEntity(String id, String value1, String value2) {
    setId(id);
    setValue1(value1);
    setValue2(value2);
  }

  @Override
  public String getDisplayName() {
    return null;
  }

  public String getValue1() {
    return value1;
  }

  public void setValue1(String value1) {
    this.value1 = value1;
  }

  public String getValue2() {
    return value2;
  }

  public void setValue2(String value2) {
    this.value2 = value2;
  }

}
