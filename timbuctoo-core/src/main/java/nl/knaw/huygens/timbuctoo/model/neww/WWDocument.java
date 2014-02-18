package nl.knaw.huygens.timbuctoo.model.neww;

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

import java.util.Map;

import nl.knaw.huygens.timbuctoo.model.Document;

public class WWDocument extends Document {

  public String notes;
  public String reference;
  public String origin;
  public String[] libraries;
  
  public String tempCreator;
  public String tempLanguage;
  
  public static class XDocument {
    public Map<String, XPrint> prints;
    public XSource source;
    public String[][] subject;
    public String[][] topoi;
    // public XUrl url;
    public String url_title;
  }

  public static class XPrint {
    public String edition;
    public String publisher;
    public String location;
    public String year;
  }

  public static class XSource {
    public String notes;
    public String type;
    public String full_name;
    public String short_name;
  }

}
