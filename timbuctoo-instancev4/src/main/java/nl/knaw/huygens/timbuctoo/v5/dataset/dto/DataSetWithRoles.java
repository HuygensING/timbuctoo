package nl.knaw.huygens.timbuctoo.v5.dataset.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;
import java.util.List;

public class DataSetWithRoles {
  String name;
  Boolean promoted;
  List<String> roles;
  URI uri;

  public DataSetWithRoles() {

  }

  public DataSetWithRoles(String name, Boolean promoted, List<String> roles, URI uri) {
    this.name = name;
    this.promoted = promoted;
    this.roles = roles;
    this.uri = uri;
  }

  @JsonProperty
  public String getName() {
    return name;
  }

  @JsonProperty
  public Boolean getPromoted() {
    return promoted;
  }

  @JsonProperty
  public List<String> getRoles() {
    return roles;
  }

  @JsonProperty
  public URI getUri() {
    return uri;
  }

  public void setUri(URI uri) {
    this.uri = uri;
  }
}
