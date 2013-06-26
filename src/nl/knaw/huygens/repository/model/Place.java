package nl.knaw.huygens.repository.model;

import nl.knaw.huygens.repository.annotations.DocumentTypeName;
import nl.knaw.huygens.repository.annotations.IDPrefix;
import nl.knaw.huygens.repository.annotations.IndexAnnotation;

@IDPrefix("PLA")
@DocumentTypeName("place")
public class Place extends DomainDocument {

  public String name;
  public String latitude;
  public String longitude;

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

}
