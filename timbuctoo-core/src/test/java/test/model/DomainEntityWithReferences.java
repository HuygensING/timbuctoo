package test.model;

import nl.knaw.huygens.timbuctoo.model.Reference;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Used for testing inducing an reducing of non-primitive properties.
 */
public class DomainEntityWithReferences extends BaseDomainEntity {

  private Reference sharedReference;
  private Reference uniqueReference;

  public DomainEntityWithReferences() {}

  public DomainEntityWithReferences(String id) {
    setId(id);
  }

  public Reference getSharedReference() {
    return sharedReference;
  }

  public void setSharedReference(Reference sharedReference) {
    this.sharedReference = sharedReference;
  }

  @JsonProperty("^uniqueReference")
  public Reference getUniqueReference() {
    return uniqueReference;
  }

  @JsonProperty("^uniqueReference")
  public void setUniqueReference(Reference uniqueReference) {
    this.uniqueReference = uniqueReference;
  }

}
