package nl.knaw.huygens.timbuctoo.tools.oaipmh;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.oaipmh.DublinCoreMetadataField;
import nl.knaw.huygens.timbuctoo.oaipmh.OAIDublinCoreField;

public class TestDomainEntity extends DomainEntity {

  private String title;

  @Override
  public String getDisplayName() {
    // TODO Auto-generated method stub
    return null;
  }

  @OAIDublinCoreField(dublinCoreField = DublinCoreMetadataField.TITLE)
  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

}
