package nl.knaw.huygens.repository.model;

import nl.knaw.huygens.repository.annotations.DocumentTypeName;
import nl.knaw.huygens.repository.annotations.IDPrefix;

@IDPrefix("AVR")
@DocumentTypeName("archiver")
public class Archiver extends DomainDocument {

  @Override
  public String getDisplayName() {
    return String.format("Archiver - %s", getId());
  }

}
