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

  private String address;
  private String settlement;
  private String country;
  private String zipcode;

  @Override
  public String getDisplayName() {
    return String.format("address [%s], settlement [%s], country [%s], zipcode [%s]", address, settlement, country, zipcode);
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public String getSettlement() {
    return settlement;
  }

  public void setSettlement(String settlement) {
    this.settlement = settlement;
  }

  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
  }

  public String getZipcode() {
    return zipcode;
  }

  public void setZipcode(String zipcode) {
    this.zipcode = zipcode;
  }

  @JsonIgnore
  public boolean isEmpty() {
    return address == null && settlement == null && country == null && zipcode == null;
  }

}
