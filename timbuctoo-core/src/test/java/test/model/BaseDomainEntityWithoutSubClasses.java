package test.model;

import nl.knaw.huygens.timbuctoo.annotations.IDPrefix;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;

@IDPrefix("BDEWSC")
public final class BaseDomainEntityWithoutSubClasses extends DomainEntity{
  @Override
  public String getIdentificationName() {
    throw new UnsupportedOperationException("Not implemented yet");
  }
}
