package nl.knaw.huygens.timbuctoo.tools.oaipmh;

import java.util.Date;
import java.util.List;

import nl.knaw.huygens.oaipmh.OAIRecord;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.util.Change;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

public class OAIRecordCreator {

  static final List<String> METADATA_PREFIXES = ImmutableList.of("oai_dc", "cmdi");
  private final CMDIOAIIdentifierGenerator identifierGenerator;
  private final MetadataGenerator metadataGenerator;
  private final SetSpecGenerator setSpecGenerator;
  private final OaiPmhRestClient oaiPmhRestClient;

  /**
   * Creates a new instance of the OAIRecordCreator.
   * @param identifierGenerator object that generates the id for the OAIPMH record and CMDI metadata.
   * @param metadataGenerator object that generates the CMDI and DublinCore metadata. 
   * @param setSpecGenerator object that generates the OAIPMH set specifications.
   * @param oaiPmhRestClient object that posts the new OAIPMH record on the server.
   */
  @Inject
  public OAIRecordCreator(CMDIOAIIdentifierGenerator identifierGenerator, MetadataGenerator metadataGenerator, SetSpecGenerator setSpecGenerator, OaiPmhRestClient oaiPmhRestClient) {
    this.identifierGenerator = identifierGenerator;
    this.metadataGenerator = metadataGenerator;
    this.setSpecGenerator = setSpecGenerator;
    this.oaiPmhRestClient = oaiPmhRestClient;
  }

  public void create(DomainEntity domainEntity, String vreId, String baseURL, String oaiUrl) {
    String identifier = identifierGenerator.generate(domainEntity, vreId);
    OAIRecord record = new OAIRecord() //
        .setMetadata(metadataGenerator.generate(domainEntity, identifier, vreId, baseURL, oaiUrl)) //
        .setMetadataPrefixes(METADATA_PREFIXES) //
        .setIdentifier(identifier) //
        .setSetSpecs(setSpecGenerator.generate(domainEntity, vreId)) //
        .setDatestamp(getDatestamp(domainEntity));

    oaiPmhRestClient.deleteRecord(identifier); // try delete first to be sure, the record with the identifier does not exist.
    oaiPmhRestClient.postRecord(record);
  }

  private Date getDatestamp(DomainEntity domainEntity) {
    Change lastModified = domainEntity.getModified();
    return new Date(lastModified.getTimeStamp());
  }
}
