package nl.knaw.huygens.timbuctoo.rest.model;

import nl.knaw.huygens.timbuctoo.annotations.EntityTypeName;
import nl.knaw.huygens.timbuctoo.facet.IndexAnnotation;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;

import com.google.common.base.Objects;

@EntityTypeName("testdomainentities")
public class TestDomainEntity extends DomainEntity {

  public String name;

  public TestDomainEntity() {}

  public TestDomainEntity(String id) {
    setId(id);
  }

  public TestDomainEntity(String id, String name) {
    setId(id);
    this.name = name;
  }

  @IndexAnnotation(fieldName = "desc")
  public String getDisplayName() {
    return null;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof TestDomainEntity)) {
      return false;
    }

    TestDomainEntity other = (TestDomainEntity) obj;

    return Objects.equal(other.name, name) && Objects.equal(other.getId(), getId());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(name);
  }

}
