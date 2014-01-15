package nl.knaw.huygens.timbuctoo.storage.mongo;

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

import java.io.IOException;
import java.util.List;
import java.util.Set;

import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.storage.JsonViews;

import org.bson.BSONObject;
import org.bson.BasicBSONObject;
import org.mongojack.internal.object.BsonObjectGenerator;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.common.collect.Sets;
import com.mongodb.DBObject;

public class MongoDiff {

  private static ObjectWriter dbWriter;
  static {
    ObjectMapper mapper = new ObjectMapper();
    mapper.setSerializationInclusion(Include.NON_DEFAULT);
    dbWriter = mapper.writerWithView(JsonViews.DBView.class);
  }

  public static DBObject getObjectForDoc(Object doc) throws IOException {
    if (doc == null) {
      return null;
    }
    BsonObjectGenerator generator = new BsonObjectGenerator();
    dbWriter.writeValue(generator, doc);
    DBObject dbObject = generator.getDBObject();
    dbObject.removeField("@type");

    return generator.getDBObject();
  }

  public static <T extends Entity> BSONObject diffDocuments(T d1, T d2) throws IOException {
    return diff(getObjectForDoc(d1), getObjectForDoc(d2), false);
  }

  public static BSONObject diffToNewObject(BSONObject oldObj, BSONObject newObj) {
    return diff(oldObj, newObj, true);
  }

  private static BSONObject diff(BSONObject oldObj, BSONObject newObj, boolean createNewObj) {
    BSONObject rv = createNewObj ? new BasicBSONObject() : newObj;
    Set<String> allProps = Sets.newHashSet(Sets.union(newObj.keySet(), oldObj.keySet()));
    for (String k : allProps) {

      if (oldObj.containsField(k) && newObj.containsField(k)) {
        Object oldProp = oldObj.get(k);
        Object newProp = newObj.get(k);
        // Nothing should be null because of how the objectmapper serializes to JSON above.
        // If these are both objects, recurse:
        if (oldProp instanceof BSONObject) {
          BSONObject dProp = diff((BSONObject) oldProp, (BSONObject) newProp, createNewObj);
          if (dProp != null) {
            rv.put(k, dProp);
          } else {
            rv.removeField(k);
          }
          // If they are lists, recurse:
        } else if (oldProp instanceof List) {
          @SuppressWarnings("unchecked")
          List<Object> oldList = (List<Object>) oldProp;
          @SuppressWarnings("unchecked")
          List<Object> newList = (List<Object>) newProp;
          if (oldList != null && newList != null && oldList.size() == newList.size()) {
            boolean gotDiffs = false;
            for (Object oldListItem : oldList) {
              int newListIndex = newList.indexOf(oldListItem);
              Object newListItem = newList.get(newListIndex);

              if (oldListItem instanceof BSONObject && newListItem instanceof BSONObject) {
                Object diffObj = diff((BSONObject) oldListItem, (BSONObject) newListItem, createNewObj);
                gotDiffs = gotDiffs || (diffObj != null);
                newList.set(newListIndex, diffObj);
              } else if (oldListItem.equals(newListItem)) {
                newList.set(newListIndex, null);
              } else {
                gotDiffs = true;
              }
            }
            if (gotDiffs) {
              rv.put(k, newList);
            } else {
              rv.removeField(k);
            }
          } else {
            rv.put(k, newList);
          }
          // Otherwise, remove if equal:
        } else if (oldProp.equals(newProp)) {
          rv.removeField(k);
        }
      } else if (!newObj.containsField(k)) {
        rv.put(k, null);
      } else if (createNewObj) {
        // If oldObj didn't contain this field, and newObj did,
        // put it in the result (only necessary if we're constructing a new object).
        rv.put(k, newObj.get(k));
      }
    }
    if (rv.toMap().isEmpty()) {
      return null;
    }
    return rv;
  }

}
