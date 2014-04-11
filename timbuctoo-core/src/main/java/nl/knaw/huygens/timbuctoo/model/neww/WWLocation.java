package nl.knaw.huygens.timbuctoo.model.neww;

import nl.knaw.huygens.timbuctoo.model.Location;

import com.fasterxml.jackson.annotation.JsonIgnore;

/*
 * #%L
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

public class WWLocation extends Location {

  // --- temporary fields ------------------------------------------------------

  public String tempAddress;
  public String tempSettlement;
  public String tempCountry;
  public String tempZipcode;

  // ---------------------------------------------------------------------------

  @JsonIgnore
  public boolean isValid() {
    return tempAddress != null || tempSettlement != null || tempCountry != null || tempZipcode != null;
  }

}
