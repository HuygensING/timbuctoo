package nl.knaw.huygens.timbuctoo.tools.oaipmh;

import nl.knaw.huygens.oaipmh.metadata.CMDIDublinCoreRecord;
import nl.knaw.huygens.oaipmh.metadata.DublinCoreRecord;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;

import com.google.inject.Inject;

public class MetadataGenerator {

  private final DublinCoreRecordCreator dcRecordCreator;
  private final CMDIDublinCoreRecordCreator cmdiDCRecordCreator;

  @Inject
  public MetadataGenerator(DublinCoreRecordCreator dcRecordCreator, CMDIDublinCoreRecordCreator cmdiDCRecordCreator) {
    this.dcRecordCreator = dcRecordCreator;
    this.cmdiDCRecordCreator = cmdiDCRecordCreator;
  }

  public String generate(DomainEntity domainEntity, String identifier, String vreId, String baseURL, String oaiUrl) {

    DublinCoreRecord dcRecord = dcRecordCreator.fromDomainEntity(domainEntity, baseURL);

    CMDIDublinCoreRecord cmdiDCRecord = cmdiDCRecordCreator.create(dcRecord, domainEntity, identifier, vreId, oaiUrl);

    return String.format("<meta>%s%s</meta>", dcRecord.asXML(), cmdiDCRecord.asXML());
  }
}
