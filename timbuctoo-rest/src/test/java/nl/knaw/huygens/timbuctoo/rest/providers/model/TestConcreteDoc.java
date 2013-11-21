package nl.knaw.huygens.timbuctoo.rest.providers.model;

import nl.knaw.huygens.timbuctoo.facet.IndexAnnotation;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;

import com.google.common.base.Objects;

public class TestConcreteDoc extends DomainEntity {

  public String name;

  public TestConcreteDoc() {}

  public TestConcreteDoc(String id) {
    setId(id);
  }

  public TestConcreteDoc(String id, String name) {
    setId(id);
    this.name = name;
  }

  @IndexAnnotation(fieldName = "desc")
  public String getDisplayName() {
    return null;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof TestConcreteDoc)) {
      return false;
    }

    TestConcreteDoc other = (TestConcreteDoc) obj;

    return Objects.equal(other.name, name) && Objects.equal(other.getId(), getId());
  }

  @Override
  public int hashCode() {
    // TODO Auto-generated method stub
    return Objects.hashCode(name);
  }

}
