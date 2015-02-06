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
