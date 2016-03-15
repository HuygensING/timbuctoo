package nl.knaw.huygens.timbuctoo.model.dwc;

/*
 * #%L
 * Timbuctoo model
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

import nl.knaw.huygens.timbuctoo.model.Document;
import nl.knaw.huygens.timbuctoo.model.util.Datable;


public class DWCDocument extends Document {

  private String pageNumbers;
  private String canonicalUrl;
  private String filename;

  public void setDocumentType(String type) {
    setDocumentType(DocumentType.valueOf(type));
  }

  public String getYear() {
    return getDate().toString();
  }

  public void setYear(String year) {
    setDate(new Datable(year));
  }

  public String getPageNumbers() {
    return pageNumbers;
  }

  public void setPageNumbers(String page_numbers) {
    this.pageNumbers = page_numbers;
  }

  public String getCanonicalUrl() {
    return canonicalUrl;
  }

  public void setCanonicalUrl(String canonical_url) {
    this.canonicalUrl = canonical_url;
  }

  public String getFilename() {
    return filename;
  }

  public void setFilename(String filename) {
    this.filename = filename;
  }

}
