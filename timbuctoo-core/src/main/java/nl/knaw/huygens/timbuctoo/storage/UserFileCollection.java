package nl.knaw.huygens.timbuctoo.storage;

import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.model.User;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class UserFileCollection extends FileCollection<User> {

  Map<String, User> idUserMap;
  Map<String, String> persistentIdIdMap;

  public UserFileCollection() {
    // I'm not sure if this is needed, better save than sorry.
    idUserMap = Maps.newConcurrentMap();
    persistentIdIdMap = Maps.newConcurrentMap();
  }

  @Override
  public String add(User user) {
    String id = createId(User.ID_PREFIX);
    user.setId(id);
    idUserMap.put(id, user);
    if (user.getPersistentId() != null) {
      persistentIdIdMap.put(user.getPersistentId(), id);
    }

    return id;
  }

  private String createId(String idPrefix) {
    return IdCreator.create(idPrefix, getNewNumber(idPrefix, 1));
  }

  private long getNewNumber(String idPrefix, int i) {
    LinkedList<String> ids = Lists.newLinkedList(idUserMap.keySet());

    Collections.sort(ids);

    int highestNumber = 0;

    if (!ids.isEmpty()) {
      String lastId = ids.getLast();
      highestNumber = Integer.parseInt(lastId.replace(idPrefix, ""));
    }

    return highestNumber + 1;
  }

  /**
   * Find the user by persistentId.
   * @param user the user that contains the persistentId
   * @return the user when found, null if the user has no persistent id.
   */
  @Override
  public User findItem(User user) {
    if (user == null || user.getPersistentId() == null) {
      return null;
    }
    String persistentId = user.getPersistentId();
    String id = persistentIdIdMap.get(persistentId);
    return idUserMap.get(id);
  }

  @Override
  public User get(String id) {
    return idUserMap.get(id);
  }

  @Override
  public StorageIterator<User> getAll() {
    return StorageIteratorStub.newInstance(Lists.newArrayList(idUserMap.values()));
  }

  @Override
  public void updateItem(User item) {
    if (item.getId() != null) {
      idUserMap.remove(item.getId());

      idUserMap.put(item.getId(), item);
    }
  }

  @Override
  public void deleteItem(User item) {
    if (item.getId() != null) {
      idUserMap.remove(item.getId());
    }

  }
}
