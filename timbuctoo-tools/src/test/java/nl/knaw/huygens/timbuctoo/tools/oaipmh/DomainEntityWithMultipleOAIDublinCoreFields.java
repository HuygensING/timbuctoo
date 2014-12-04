package nl.knaw.huygens.timbuctoo.tools.oaipmh;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.oaipmh.DublinCoreMetadataField;
import nl.knaw.huygens.timbuctoo.oaipmh.OAIDublinCoreField;

public class DomainEntityWithMultipleOAIDublinCoreFields extends DomainEntity {

  private String title;
  private String subject;

  @Override
  public String getIdentificationName() {
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

  @OAIDublinCoreField(dublinCoreField = DublinCoreMetadataField.SUBJECT)
  public String getSubject() {
    return subject;
  }

  public void setSubject(String subject) {
    this.subject = subject;
  }

}
