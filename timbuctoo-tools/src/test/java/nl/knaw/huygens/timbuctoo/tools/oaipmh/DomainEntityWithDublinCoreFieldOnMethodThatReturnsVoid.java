package nl.knaw.huygens.timbuctoo.tools.oaipmh;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.oaipmh.DublinCoreMetadataField;
import nl.knaw.huygens.timbuctoo.oaipmh.OAIDublinCoreField;

public class DomainEntityWithDublinCoreFieldOnMethodThatReturnsVoid extends DomainEntity {

  @Override
  public String getDisplayName() {
    // TODO Auto-generated method stub
    return null;
  }

  @OAIDublinCoreField(dublinCoreField = DublinCoreMetadataField.CONTRIBUTOR)
  public void contributor() {

  }

}
