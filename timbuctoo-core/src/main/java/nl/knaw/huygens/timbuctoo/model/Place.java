package nl.knaw.huygens.timbuctoo.model;

import nl.knaw.huygens.timbuctoo.annotations.IDPrefix;
import nl.knaw.huygens.timbuctoo.facet.IndexAnnotation;

@IDPrefix("PLAC")
public class Place extends DomainEntity {

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
