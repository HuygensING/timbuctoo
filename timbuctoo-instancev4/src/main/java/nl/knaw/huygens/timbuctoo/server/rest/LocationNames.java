package nl.knaw.huygens.timbuctoo.server.rest;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Map;

// Container class, for entity reducer
class LocationNames {
  @JsonProperty("defLang")
  private String defLang;
  @JsonProperty("map")
  private Map<String, PlaceName> map;

  public LocationNames(String defLang) {
    this.defLang = defLang;
    map = Maps.newHashMap();
  }

  public LocationNames(){

  }

  @Override
  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj, false);
  }

  @Override
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this, false);
  }

  public void addCountryName(String lang, String name) {
    map.put(lang, new PlaceName().setCountry(name));
  }

  public String getDefaultName(LocationType locationType) {
    return map.get(defLang).getDisplayName(locationType);
  }

  public enum LocationType {
    UNKNOWN, DISTRICT, SETTLEMENT, REGION, COUNTRY, BLOC
  }

  public static class PlaceName {

    private String district;
    private String settlement;
    private String region;
    private String country;
    private String countryCode;
    private String bloc;

    public PlaceName() {
    }

    @JsonIgnore
    public String getDisplayName(LocationType type) {
      StringBuilder builder = new StringBuilder();
      switch (type) {
        case DISTRICT:
          handleDistrict(builder);
          break;
        case SETTLEMENT:
          handleSettlement(builder);
          break;
        case REGION:
          handleRegionName(builder);
          break;
        case COUNTRY:
          handleCountry(builder);
          break;
        case BLOC:
          handleBloc(builder);
          break;
        default:
          handleMediumName(builder);
          break;
      }
      return builder.toString();
    }

    @JsonIgnore
    public String getMediumName() {
      StringBuilder builder = new StringBuilder();
      handleMediumName(builder);
      return builder.toString();
    }

    private void handleDistrict(StringBuilder builder) {
      if (district != null) {
        builder.append(district);
        if (settlement != null) {
          builder.append(", ");
          handleSettlement(builder);
        }
      }
    }

    private void handleSettlement(StringBuilder builder) {
      if (settlement != null) {
        builder.append(settlement).append(" (");
        if (region != null) {
          builder.append(region).append(", ");
        }
        if (countryCode != null) {
          builder.append(countryCode);
        } else if (country != null) {
          builder.append(country);
        }
        builder.append(")");
      }
    }

    private void handleRegionName(StringBuilder builder) {
      if (region != null) {
        builder.append(region).append(" (region");
        if (countryCode != null) {
          builder.append(", ").append(countryCode);
        } else if (country != null) {
          builder.append(", ").append(country);
        }
        builder.append(")");
      }
    }

    private void handleCountry(StringBuilder builder) {
      if (country != null) {
        builder.append(country);
      }
    }

    private void handleBloc(StringBuilder builder) {
      if (bloc != null) {
        builder.append(bloc);
      }
    }

    private void handleMediumName(StringBuilder builder) {
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
    }

    // ---------------------------------------------------------------------------

    public String getDistrict() {
      return district;
    }

    public PlaceName setDistrict(String district) {
      this.district = district;
      return this;
    }

    public String getSettlement() {
      return settlement;
    }

    public PlaceName setSettlement(String settlement) {
      this.settlement = settlement;
      return this;
    }

    public String getRegion() {
      return region;
    }

    public PlaceName setRegion(String region) {
      this.region = region;
      return this;
    }

    public String getCountry() {
      return country;
    }

    public PlaceName setCountry(String country) {
      this.country = country;
      return this;
    }

    public String getCountryCode() {
      return countryCode;
    }

    public PlaceName setCountryCode(String countryCode) {
      this.countryCode = countryCode;
      return this;
    }

    public String getBloc() {
      return bloc;
    }

    public PlaceName setBloc(String bloc) {
      this.bloc = bloc;
      return this;
    }

    @Override
    public boolean equals(Object obj) {
      return EqualsBuilder.reflectionEquals(this, obj, false);
    }

    @Override
    public int hashCode() {
      return HashCodeBuilder.reflectionHashCode(this, false);
    }

  }
}
