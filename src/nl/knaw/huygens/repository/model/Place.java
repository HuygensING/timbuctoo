package nl.knaw.huygens.repository.model;

import nl.knaw.huygens.repository.indexdata.IndexAnnotation;
import nl.knaw.huygens.repository.model.util.IDPrefix;

import com.fasterxml.jackson.annotation.JsonProperty;

@IDPrefix("PLA")
public class Place extends VariationDocument {

  public String name;
  public String latitude;
  public String longitude;
  public String currentVariation;

  @Override
  public String getDescription() {
    return name;
  };

  @IndexAnnotation(fieldName = "facet_t_name")
  public String getName() {
    return name;
  }

  @IndexAnnotation(fieldName = "facet_s_latitude")
  public String getLatitude() {
    return latitude;
  }

  @IndexAnnotation(fieldName = "facet_s_longitude")
  public String getLongitude() {
    return longitude;
  }

  @Override
  @JsonProperty("!currentVariation")
  public String getCurrentVariation() {
    return currentVariation;
  }

  @Override
  @JsonProperty("!currentVariation")
  public void setCurrentVariation(String currentVariation) {
    this.currentVariation = currentVariation;
  }

}
