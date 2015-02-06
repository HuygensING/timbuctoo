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
import nl.knaw.huygens.timbuctoo.oaipmh.DublinCoreMetadataField;

import com.google.inject.Inject;

public class DublinCoreDynamicFieldFiller {

  private final DublinCoreValueConverter dcValueConverter;

  @Inject
  public DublinCoreDynamicFieldFiller(DublinCoreValueConverter dcValueConverter) {
    this.dcValueConverter = dcValueConverter;
  }

  public void addTo(DublinCoreRecord dcRecord, DublinCoreMetadataField field, Object value) {
    if (field == null || value == null) {
      return;
    }

    String convertedValue = dcValueConverter.convert(value);

    switch (field) {
      case CONTRIBUTOR:
        dcRecord.setContributor(convertedValue);
        break;
      case COVERAGE:
        dcRecord.setCoverage(convertedValue);
        break;
      case CREATOR:
        dcRecord.setCreator(convertedValue);
        break;
      case DATE:
        dcRecord.setDate(convertedValue);
        break;
      case DESCRIPTION:
        dcRecord.setDescription(convertedValue);
        break;
      case FORMAT:
        dcRecord.setFormat(convertedValue);
        break;
      case IDENTIFIER:
        dcRecord.setIdentifier(convertedValue);
        break;
      case LANGUAGE:
        dcRecord.setLanguage(convertedValue);
        break;
      case PUBLISHER:
        dcRecord.setPublisher(convertedValue);
        break;
      case RELATION:
        dcRecord.setRelation(convertedValue);
        break;
      case RIGHTS:
        dcRecord.setRights(convertedValue);
        break;
      case SOURCE:
        dcRecord.setSource(convertedValue);
        break;
      case SUBJECT:
        dcRecord.setSubject(convertedValue);
        break;
      case TITLE:
        dcRecord.setTitle(convertedValue);
        break;
      case TYPE:
        dcRecord.setType(convertedValue);
        break;
    }

  }
}
