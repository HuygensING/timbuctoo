package nl.knaw.huygens.timbuctoo.storage;

import java.util.LinkedList;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.model.VREAuthorization;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class VREAuthorizationFileCollection extends FileCollection<VREAuthorization> {
	private Map<String, VREAuthorization> idAuthorizationMap;
	private Map<String, String> vreIdUserIdIdMap;

	public VREAuthorizationFileCollection() {
		idAuthorizationMap = Maps.newConcurrentMap();
		vreIdUserIdIdMap = Maps.newConcurrentMap();
	}

	@Override
	public String add(VREAuthorization entity) {
		String vreIdUserId = createVREIdUserIdIndexEntry(entity);

		if (vreIdUserIdIdMap.containsKey(vreIdUserId)) {
			return vreIdUserIdIdMap.get(vreIdUserId);
		}

		String id = createId(VREAuthorization.ID_PREFIX);
		entity.setId(id);

		idAuthorizationMap.put(id, entity);
		vreIdUserIdIdMap.put(vreIdUserId, id);

		return id;
	}

	private String createVREIdUserIdIndexEntry(VREAuthorization entity) {
		return String.format("%s%s", entity.getVreId(), entity.getUserId());
	}

	@Override
	public VREAuthorization findItem(VREAuthorization example) {
		String id = findIdForAuthorization(example);

		return id != null ? idAuthorizationMap.get(id) : null;
	}

	@Override
	public VREAuthorization get(String id) {
		return idAuthorizationMap.get(id);
	}

	@Override
	public StorageIterator<VREAuthorization> getAll() {
		return StorageIteratorStub.newInstance(Lists.newArrayList(idAuthorizationMap.values()));
	}

	@Override
	public void updateItem(VREAuthorization item) {
		String id = findIdForAuthorization(item);

		if (id != null) {
			item.setId(id); // make sure the item has the right id
			idAuthorizationMap.remove(id);
			idAuthorizationMap.put(id, item);
		}

	}

	private String findIdForAuthorization(VREAuthorization item) {
		String vreIdUserId = createVREIdUserIdIndexEntry(item);
		String id = vreIdUserIdIdMap.get(vreIdUserId);
		return id;
	}

	@Override
	public void deleteItem(VREAuthorization item) {
		String vreIdUserId = createVREIdUserIdIndexEntry(item);
		String id = vreIdUserIdIdMap.remove(vreIdUserId);

		if (id != null) {
			idAuthorizationMap.remove(id);
		}

	}

	protected LinkedList<String> getIds() {
		return Lists.newLinkedList(idAuthorizationMap.keySet());
	}

}
