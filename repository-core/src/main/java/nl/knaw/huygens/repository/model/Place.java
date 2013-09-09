package nl.knaw.huygens.repository.model;

import nl.knaw.huygens.repository.annotations.IDPrefix;
import nl.knaw.huygens.repository.facet.IndexAnnotation;

@IDPrefix("PLA")
public class Place extends DomainDocument {

  public String name;
  public String latitude;
  public String longitude;

  @Override
  public String getDisplayName() {
    return name;
  };

  @IndexAnnotation(fieldName = "dynamic_t_name", isFaceted = false)
  public String getName() {
    return name;
  }

  @IndexAnnotation(fieldName = "dynamic_s_latitude")
  public String getLatitude() {
    return latitude;
  }

  @IndexAnnotation(fieldName = "dynamic_s_longitude")
  public String getLongitude() {
    return longitude;
  }

}
