package nl.knaw.huygens.timbuctoo.tools.oaipmh;

import nl.knaw.huygens.oaipmh.metadata.DublinCoreRecord;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;

import com.google.inject.Inject;

public class DublinCoreRecordCreator {

  static final String TYPE = "InteractiveResource";
  private final DublinCoreDynamicFieldRetriever dcDynamicFieldRetriever;
  private final DublinCoreIdentifierCreator dcIdentifierCreator;

  @Inject
  public DublinCoreRecordCreator(DublinCoreDynamicFieldRetriever dublinCoreMetadataRetriever, DublinCoreIdentifierCreator dcIdentifierCreator) {
    this.dcDynamicFieldRetriever = dublinCoreMetadataRetriever;
    this.dcIdentifierCreator = dcIdentifierCreator;
  }

  public DublinCoreRecord fromDomainEntity(DomainEntity domainEntity, String baseURL) {
    DublinCoreRecord dublinCoreRecord = createDublinCoreRecord();

    dublinCoreRecord.setType(TYPE);
    dublinCoreRecord.setIdentifier(dcIdentifierCreator.create(domainEntity, baseURL));

    dcDynamicFieldRetriever.retrieve(dublinCoreRecord, domainEntity);

    return dublinCoreRecord;
  }

  protected DublinCoreRecord createDublinCoreRecord() {
    DublinCoreRecord record = new DublinCoreRecord();
    return record;
  }

}
