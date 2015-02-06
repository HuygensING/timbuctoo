package nl.knaw.huygens.timbuctoo.tools.oaipmh;

/*
 * #%L
 * Timbuctoo tools
 * =======
 * Copyright (C) 2012 - 2015 Huygens ING
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

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
