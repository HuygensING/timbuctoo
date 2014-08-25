package nl.knaw.huygens.timbuctoo.storage;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.model.User;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@JsonSerialize(using = FileCollectionSerializer.class)
@JsonDeserialize(using = UserFileCollectionDeserializer.class)
public class UserFileCollection extends FileCollection<User> {

	Map<String, User> idUserMap;
	Map<String, String> persistentIdIdMap;

	public UserFileCollection() {
		this(Lists.<User> newArrayList());
	}

	public UserFileCollection(List<User> users) {
		// I'm not sure if this is needed, better save than sorry.
		idUserMap = Maps.newConcurrentMap();
		persistentIdIdMap = Maps.newConcurrentMap();
		initialize(users);
	}

	private void initialize(List<User> users) {
		for (User user : users) {
			String id = user.getId();
			idUserMap.put(id, user);
			persistentIdIdMap.put(id, user.getPersistentId());
		}

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

	@Override
	protected LinkedList<String> getIds() {
		LinkedList<String> ids = Lists.newLinkedList(idUserMap.keySet());
		return ids;
	}

	/**
	 * Find the user by persistentId.
	 * 
	 * @param user
	 *        the user that contains the persistentId
	 * @return the user when found, null if the user has no persistent id.
	 */
	@Override
	public User findItem(User user) {
		if (user == null || user.getPersistentId() == null) {
			return null;
		}
		String persistentId = user.getPersistentId();
		String id = persistentIdIdMap.get(persistentId);
		return this.get(id);
	}

	@Override
	public User get(String id) {
		return id != null ? idUserMap.get(id) : null;
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
