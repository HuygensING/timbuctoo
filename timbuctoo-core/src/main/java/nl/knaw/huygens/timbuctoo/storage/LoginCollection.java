package nl.knaw.huygens.timbuctoo.storage;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.model.Login;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.Maps;

@JsonSerialize(using = FileCollectionSerializer.class)
@JsonDeserialize(using = LoginCollectionDeserializer.class)
public class LoginCollection extends FileCollection<Login> {

  Map<String, Login> authStringLoginMap;

  public LoginCollection(List<Login> logins) {
    initialize(logins);
  }

  private void initialize(List<Login> logins) {
    authStringLoginMap = Maps.newConcurrentMap();
    for (Login login : logins) {
      authStringLoginMap.put(login.getAuthString(), login);
    }
  }

  @Override
  public String add(Login entity) {
    throw new UnsupportedOperationException("Not yet implemented");
  }

  @Override
  public Login findItem(Login example) {
    return authStringLoginMap.get(example.getAuthString());
  }

  @Override
  public Login get(String id) {
    throw new UnsupportedOperationException("Not yet implemented");
  }

  @Override
  public StorageIterator<Login> getAll() {
    throw new UnsupportedOperationException("Not yet implemented");
  }

  @Override
  public Login[] asArray() {
    return getItems().toArray(new Login[] {});
  }

  private Collection<Login> getItems() {
    return authStringLoginMap.values();
  }

  @Override
  public void updateItem(Login item) {
    throw new UnsupportedOperationException("Not yet implemented");

  }

  @Override
  public void deleteItem(Login item) {
    throw new UnsupportedOperationException("Not yet implemented");

  }

  @Override
  protected LinkedList<String> getIds() {
    throw new UnsupportedOperationException("Not yet implemented");
  }

}
