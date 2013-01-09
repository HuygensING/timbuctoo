package nl.knaw.huygens.repository.storage.mongo;

import java.util.List;

import nl.knaw.huygens.repository.storage.JsonViews;
import nl.knaw.huygens.repository.storage.RevisionChanges;

import org.bson.BSONObject;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.Lists;

public class MongoChanges implements RevisionChanges {
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