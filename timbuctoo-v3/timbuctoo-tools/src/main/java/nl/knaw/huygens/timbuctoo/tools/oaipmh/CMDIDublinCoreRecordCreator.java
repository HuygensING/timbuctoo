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
import nl.knaw.huygens.oaipmh.metadata.CMDIDublinCoreRecord.Builder;
import nl.knaw.huygens.oaipmh.metadata.DublinCoreRecord;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;

import org.apache.commons.lang3.StringUtils;

import com.google.inject.Inject;

public class CMDIDublinCoreRecordCreator {
  static final String MD_PROFILE = "clarin.eu:cr1:p_1288172614023"; //default for CMDI Dublin Core Records see: http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/clarin.eu:cr1:p_1288172614023
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
