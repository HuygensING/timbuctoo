package nl.knaw.huygens.repository.storage.mongo.variation;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.bson.BSONObject;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Sets;
import com.mongodb.DBObject;

public class DBJsonNode implements DBObject {
  private final JsonNode delegate;

  public DBJsonNode(JsonNode obj) {
    this.delegate = obj;
  }

  @Override
  public Object put(String key, Object v) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void putAll(BSONObject o) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void putAll(@SuppressWarnings("rawtypes") Map m) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object get(String key) {
    JsonNode rv;
    if (delegate.isArray()) {
      try {
        int i = Integer.parseInt(key);
        rv = delegate.get(i);
      } catch (NumberFormatException ex) {
        rv = null;
      }
    } else {
      rv = delegate.get(key);
    }
    return nodeToValue(rv);
  }

  @SuppressWarnings("rawtypes")
  @Override
  public Map toMap() {
    if (delegate.isObject()) {
      throw new UnsupportedOperationException();
    }
    return Collections.emptyMap();
  }

  @Override
  public Object removeField(String key) {
    if (!delegate.isObject()) {
      return null;
    }
    ObjectNode obj = (ObjectNode) delegate;
    return nodeToValue(obj.remove(key));
  }

  private Object nodeToValue(JsonNode rv) {
    if (rv == null || rv.isNull()) {
      return null;
    }
    if (rv.isValueNode()) {
      if (rv.isNumber()) {
        return rv.numberValue();
      }
      if (rv.isTextual()) {
        return rv.textValue();
      }
      if (rv.isBoolean()) {
        return rv.booleanValue();
      }
      if (rv.isBinary()) {
        try {
          return rv.binaryValue();
        } catch (IOException e) {
          e.printStackTrace();
          return null;
        }
      }
      throw new RuntimeException("Didn't understand JsonNode type of: " + rv.toString());
    }
    if (rv.isArray() || rv.isObject()) {
      return new DBJsonNode(rv);
    }
    throw new RuntimeException("Didn't understand JsonNode type of: " + rv.toString());
  }

  @Override
  @Deprecated
  public boolean containsKey(String s) {
    return containsField(s);
  }

  @Override
  public boolean containsField(String s) {
    return delegate.has(s);
  }

  @Override
  public Set<String> keySet() {
    if (delegate.isObject()) {
      ObjectNode obj = (ObjectNode) delegate;
      return Sets.newHashSet(obj.fieldNames());
    }
    return null;
  }

  @Override
  public void markAsPartialObject() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isPartialObject() {
    return false;
  }

  public JsonNode getDelegate() {
    return delegate;
  }
}
