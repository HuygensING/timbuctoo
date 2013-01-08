package nl.knaw.huygens.repository.storage.mongo;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.vz.mongodb.jackson.internal.object.BsonObjectGenerator;
import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.model.storage.JsonViews;

import org.bson.BSONObject;
import org.bson.BasicBSONObject;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.UntypedObjectDeserializer;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mongodb.DBObject;

public class MongoUtils {
  public static class BSONDeserializer extends JsonDeserializer<BSONObject> {
    UntypedObjectDeserializer nestedSer = new UntypedObjectDeserializer();
    @Override
    public BSONObject deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
      @SuppressWarnings("unchecked")
      Map<Object, Object> x = (Map<Object, Object>) nestedSer.deserialize(jp, ctxt);
      return new BasicBSONObject(x);
    }
  }
  public static class MongoChanges implements RevisionChanges {
    public static class MongoRev implements RevisionChanges.Rev {
      private BSONObject obj;
      public MongoRev(BSONObject obj) {
        this.obj = obj;
      }
      @Override
    public BSONObject fromNext() {
        return (BSONObject) obj.get("fromnext");
      }
      @Override
    public BSONObject fromPrev() {
        return (BSONObject) obj.get("frompast");
      }
    }

    protected MongoChanges() {
      // do nothing, used for Jackson
    }

    public MongoChanges(String id, BSONObject origObj) {
      _id = id;
      original = origObj;
      changes = Lists.newArrayList();
      Object origRev = original.get("^rev");
      try {
        lastRev = (Integer) origRev;
      } catch (Exception ex) {
        System.err.println("invalid revision value: " + origRev.toString());
        lastRev = -1;
      }
    }

    public String _id;
    public int lastRev;

    @JsonDeserialize(contentUsing=BSONDeserializer.class)
    public List<BSONObject> changes;
    @JsonDeserialize(using=BSONDeserializer.class)
    public BSONObject original;

    @Override
    @JsonIgnore
    public String getId() {
      return _id;
    }

    @JsonView(JsonViews.WebView.class)
    @Override
    public List<Rev> getRevisions() {
      List<Rev> revs = Lists.newArrayListWithCapacity(changes.size());
      for (BSONObject obj : changes) {
        revs.add(new MongoRev(obj));
      }
      return revs;
    }
    @Override
    public int getLastRev() {
      return lastRev;
    }

    @Override
    public BSONObject getOriginal() {
      return original;
    }
  }

  private static ObjectWriter dbWriter;
  static {
    ObjectMapper mapper = new ObjectMapper();
    mapper.setSerializationInclusion(Include.NON_DEFAULT);
    dbWriter = mapper.writerWithView(JsonViews.DBView.class);
  }


  public static String getVersioningCollectionName(Class<?> cls) {
    return getCollectionName(cls) + "-versions";
  }

  public static String getCollectionName(Class<?> cls) {
    return cls.getSimpleName().toLowerCase();
  }

  public static <T extends Document> BSONObject diff(T d1, T d2) throws IOException {
    return diff(getObjectForDoc(d1), getObjectForDoc(d2), false);
  }

  public static DBObject getObjectForDoc(Object doc) throws IOException {
    BsonObjectGenerator generator = new BsonObjectGenerator();
    dbWriter.writeValue(generator, doc);
    return generator.getDBObject();
  }

  public static BSONObject bsondiff(BSONObject oldObj, BSONObject newObj) {
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
          List<Object> newList = Lists.newArrayList((List<Object>) newProp);
          if (oldList != null && newList != null && oldList.size() == newList.size()) {
            int listSize = oldList.size();
            boolean gotDiffs = false;
            for (int i = 0; i < listSize; i++) {
              Object oldListItem = oldList.get(i);
              Object newListItem = newList.get(i);
              if (oldListItem instanceof BSONObject && newListItem instanceof BSONObject) {
                Object diffObj = diff((BSONObject) oldListItem, (BSONObject) newListItem, createNewObj);
                gotDiffs = gotDiffs || (diffObj != null);
                newList.set(i, diffObj);
              } else if (oldListItem.equals(newListItem)) {
                newList.set(i, null);
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
