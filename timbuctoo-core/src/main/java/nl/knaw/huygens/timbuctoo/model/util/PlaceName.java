package nl.knaw.huygens.timbuctoo.model.util;

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

import com.fasterxml.jackson.annotation.JsonIgnore;

public class PlaceName {

  private String district;
  private String settlement;
  private String region;
  private String country;
  private String countryCode;
  private String bloc;

  public PlaceName() {}

  @JsonIgnore
  public String getLongName() {
    StringBuilder builder = new StringBuilder();
    if (district != null) {
      builder.append(district).append(", ");
    }
    if (settlement != null) {
      builder.append(settlement);
    }
    if (builder.length() == 0 && region != null) {
      builder.append(region);
    }
    if (builder.length() == 0 && country != null) {
      builder.append(country);
    } else if (countryCode != null) {
      builder.append(" (").append(countryCode).append(")");
    } else if (country != null) {
      builder.append(" (").append(country).append(")");
    }
    if (builder.length() == 0 && bloc != null) {
      builder.append(bloc);
    }
    return builder.toString();
  }

  public String getDistrict() {
    return district;
  }

  public void setDistrict(String district) {
    this.district = district;
  }

  public String getSettlement() {
    return settlement;
  }

  public void setSettlement(String settlement) {
    this.settlement = settlement;
  }

  public String getRegion() {
    return region;
  }

  public void setRegion(String region) {
    this.region = region;
  }

  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
  }

  public String getCountryCode() {
    return countryCode;
  }

  public void setCountryCode(String countryCode) {
    this.countryCode = countryCode;
  }

  public String getBloc() {
    return bloc;
  }

  public void setBloc(String bloc) {
    this.bloc = bloc;
  }

}
