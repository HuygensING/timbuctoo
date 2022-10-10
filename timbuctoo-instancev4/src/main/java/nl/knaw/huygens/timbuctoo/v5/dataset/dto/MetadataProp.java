package nl.knaw.huygens.timbuctoo.v5.dataset.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "type")
@JsonSubTypes({
  @JsonSubTypes.Type(name = "UriMetadataProp", value = UriMetadataProp.class),
  @JsonSubTypes.Type(name = "SimpleMetadataProp", value = SimpleMetadataProp.class),
  @JsonSubTypes.Type(name = "EntityMetadataProp", value = EntityMetadataProp.class)})
public interface MetadataProp {
  String getPredicate();
}
