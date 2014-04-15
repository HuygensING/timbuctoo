package nl.knaw.huygens.timbuctoo.model.util;

import org.apache.commons.lang.StringUtils;

/*
 * #%L
 * Timbuctoo core
 * =======
 * Copyright (C) 2012 - 2014 Huygens ING
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

public class Link {

  private String url;
  private String label;

  public Link() {}

  public Link(String url) {
    setUrl(url);
    setLabel("");
  }

  public Link(String url, String label) {
    setUrl(url);
    setLabel(label);
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = StringUtils.stripToEmpty(url);
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = StringUtils.stripToEmpty(label);
  }

}
