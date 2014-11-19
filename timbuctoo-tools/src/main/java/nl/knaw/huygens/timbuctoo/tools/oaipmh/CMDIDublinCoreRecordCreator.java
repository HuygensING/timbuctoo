package nl.knaw.huygens.timbuctoo.tools.oaipmh;

import nl.knaw.huygens.oaipmh.metadata.CMDIDublinCoreRecord;
import nl.knaw.huygens.oaipmh.metadata.CMDIDublinCoreRecord.Builder;
import nl.knaw.huygens.oaipmh.metadata.DublinCoreRecord;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;

import org.apache.commons.lang3.StringUtils;

import com.google.inject.Inject;

public class CMDIDublinCoreRecordCreator {
  static final String MD_PROFILE = "clarin.eu:cr1:p_1345561703673"; //default for CMDI Dublin Core Records see: http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/clarin.eu:cr1:p_1288172614023
  private final CMDICollectionNameCreator collectionNameCreator;

  @Inject
  public CMDIDublinCoreRecordCreator(CMDICollectionNameCreator collectionNameCreator) {
    this.collectionNameCreator = collectionNameCreator;
  }

  public CMDIDublinCoreRecord create(DublinCoreRecord dcRecord, DomainEntity domainEntity, String identifier, String vreId, String oaiUrl) {
    if (StringUtils.isBlank(identifier)) {
      throw new IllegalArgumentException("identifier should have a value, but was \"" + identifier + "\"");
    }

    Builder builder = newBuilder();
    builder.setDublinCoreRecord(dcRecord);
    builder.setMdCollectionDisplayName(collectionNameCreator.create(domainEntity, vreId));
    builder.setMdProfile(MD_PROFILE);
    builder.setMdCreator(vreId);
    builder.setMdSelfLink(String.format("%s/cmdi/%s", oaiUrl, identifier));

    return builder.build();
  }

  protected Builder newBuilder() {
    Builder builder = new Builder();
    return builder;
  }
}
