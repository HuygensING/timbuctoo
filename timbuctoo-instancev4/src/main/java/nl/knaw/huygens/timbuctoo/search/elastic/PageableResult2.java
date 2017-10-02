package nl.knaw.huygens.timbuctoo.search.elastic;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created on 2017-09-28 12:56.
 */
public class PageableResult2 {

  private List<String> idFields = new ArrayList<>();
  private String token;

  public List<String> getIdFields() {
    return idFields;
  }

  PageableResult2 setIdFields(List<String> idFields) {
    this.idFields = idFields;
    return this;
  }

  public Optional<String> getToken() {
    if (token == null) {
      return Optional.empty();
    } else {
      return Optional.of(token);
    }
  }

  PageableResult2 setToken(String token) {
    this.token = token;
    return this;
  }


}
