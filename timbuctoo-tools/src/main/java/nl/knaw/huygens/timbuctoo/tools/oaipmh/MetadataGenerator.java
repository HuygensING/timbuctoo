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
