package nl.knaw.huygens.timbuctoo.storage.file;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.model.Login;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;
import nl.knaw.huygens.timbuctoo.storage.StorageIteratorStub;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@JsonSerialize(using = FileCollectionSerializer.class)
@JsonDeserialize(using = LoginCollectionDeserializer.class)
public class LoginCollection extends FileCollection<Login> {

  Map<String, String> authStringIdMap;
  Map<String, Login> idLoginMap;

  public LoginCollection() {
    this(Lists.<Login> newArrayList());
  }

  public LoginCollection(List<Login> logins) {
    initialize(logins);
  }

  private void initialize(List<Login> logins) {
    authStringIdMap = Maps.newConcurrentMap();
    idLoginMap = Maps.newConcurrentMap();
    for (Login login : logins) {
      idLoginMap.put(login.getId(), login);
      authStringIdMap.put(login.getAuthString(), login.getId());
    }
  }

  @Override
  public String add(Login entity) {
    String authString = entity.getAuthString();
    if (StringUtils.isBlank(authString)) {
      throw new IllegalArgumentException("Authentication string cannot be empty.");
    }

    if (authStringIdMap.containsKey(authString)) {
      return authStringIdMap.get(authString);
    }

    String id = createId(Login.ID_PREFIX);
    idLoginMap.put(id, entity);
    entity.setId(id);

    authStringIdMap.put(authString, id);

    return id;
  }

  @Override
  public Login findItem(Login example) {
    String id = authStringIdMap.get(example.getAuthString());
    return id != null ? idLoginMap.get(id) : null;
  }

  @Override
  public Login get(String id) {
    return idLoginMap.get(id);
  }

  @Override
  public StorageIterator<Login> getAll() {
    return StorageIteratorStub.newInstance(Lists.newArrayList(idLoginMap.values()));
  }

  @Override
  public Login[] asArray() {
    return getItems().toArray(new Login[] {});
  }

  private Collection<Login> getItems() {
    return idLoginMap.values();
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
    return Lists.newLinkedList(idLoginMap.keySet());
  }

}
