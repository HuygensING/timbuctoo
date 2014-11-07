package nl.knaw.huygens.timbuctoo.storage.file;

/*
 * #%L
 * Timbuctoo core
 * =======
 * Copyright (C) 2012 - 2014 Huygens ING
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.model.VREAuthorization;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;
import nl.knaw.huygens.timbuctoo.storage.StorageIteratorStub;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@JsonSerialize(using = FileCollectionSerializer.class)
@JsonDeserialize(using = VREAuthorizationFileCollectionDeserializer.class)
public class VREAuthorizationFileCollection extends FileCollection<VREAuthorization> {

  private final Map<String, VREAuthorization> idAuthorizationMap;
  private final Map<String, String> vreIdUserIdIdMap;

  public VREAuthorizationFileCollection() {
    this(Lists.<VREAuthorization> newArrayList());
  }

  public VREAuthorizationFileCollection(List<VREAuthorization> authorizations) {
    idAuthorizationMap = Maps.newConcurrentMap();
    vreIdUserIdIdMap = Maps.newConcurrentMap();
    initialize(authorizations);
  }

  private void initialize(List<VREAuthorization> authorizations) {
    for (VREAuthorization authorization : authorizations) {
      String id = authorization.getId();
      idAuthorizationMap.put(id, authorization);
      vreIdUserIdIdMap.put(createVREIdUserIdIndexEntry(authorization), id);
    }
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
    return this.get(id);
  }

  @Override
  public VREAuthorization get(String id) {
    return id != null ? idAuthorizationMap.get(id) : null;
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
      idAuthorizationMap.put(id, item);
    }
  }

  private String findIdForAuthorization(VREAuthorization item) {
    String vreIdUserId = createVREIdUserIdIndexEntry(item);
    return vreIdUserIdIdMap.get(vreIdUserId);
  }

  @Override
  public void deleteItem(VREAuthorization item) {
    String vreIdUserId = createVREIdUserIdIndexEntry(item);
    String id = vreIdUserIdIdMap.remove(vreIdUserId);

    if (id != null) {
      idAuthorizationMap.remove(id);
    }
  }

  @Override
  protected LinkedList<String> getIds() {
    return Lists.newLinkedList(idAuthorizationMap.keySet());
  }

  @Override
  public VREAuthorization[] asArray() {
    return idAuthorizationMap.values().toArray(new VREAuthorization[] {});
  }

}
