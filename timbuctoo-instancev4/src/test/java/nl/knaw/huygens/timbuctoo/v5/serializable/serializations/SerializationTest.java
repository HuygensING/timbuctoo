package nl.knaw.huygens.timbuctoo.v5.serializable.serializations;

import nl.knaw.huygens.timbuctoo.v5.datastores.implementations.json.JsonTypeNameStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.prefixstore.TypeNameStore;
import nl.knaw.huygens.timbuctoo.v5.serializable.Serializable;
import nl.knaw.huygens.timbuctoo.v5.serializable.SerializableList;
import nl.knaw.huygens.timbuctoo.v5.serializable.SerializableObject;
import nl.knaw.huygens.timbuctoo.v5.serializable.SerializableUntypedValue;
import nl.knaw.huygens.timbuctoo.v5.serializable.SerializableValue;
import nl.knaw.huygens.timbuctoo.v5.serializable.serializations.base.BaseSerialization;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created on 2017-06-07 11:06.
 */
public abstract class SerializationTest {

  @Test
  public void basicSerialization() throws Exception {
    BaseSerialization bs = new BaseSerialization();
    SerializableObject graph = createGraph_01(createTypeNameStore());

    graph.performSerialization(bs);
  }


  SerializableObject createGraph_01(TypeNameStore typeNameStore) {
    LinkedHashMap<String, Serializable> data4 = new LinkedHashMap<>();
    SerializableObject so4 = createSerializableObject(data4, 4, typeNameStore);

    LinkedHashMap<String, Serializable> data3 = new LinkedHashMap<>();
    SerializableObject so3 = createSerializableObject(data3, 3, typeNameStore);
    data3.put("fooBar", so4);

    LinkedHashMap<String, Serializable> data2 = new LinkedHashMap<>();
    SerializableObject so2 = createSerializableObject(data2, 2, typeNameStore);
    data2.put("hasSibling", so3);

    List<Serializable> listData = new ArrayList<>();
    listData.add(so2);
    listData.add(so3);
    SerializableList sl = new SerializableList(listData);

    LinkedHashMap<String, Serializable> data1 = new LinkedHashMap<>();
    SerializableObject so1 = createSerializableObject(data1, 1, typeNameStore);
    data1.put("hasChild", sl);

    LinkedHashMap<String, Serializable> data0 = new LinkedHashMap<>();
    SerializableObject so0 = createSerializableObject(data0, 0, typeNameStore);
    data0.put("hasBeer", so1);
    return so0;
  }

  SerializableObject createSerializableObject(
    LinkedHashMap<String, Serializable> data, int ix, TypeNameStore typeNameStore) {
    String uri = "uri" + ix;
    data.put("foo", new SerializableUntypedValue("foo" + ix));
    data.put("name", new SerializableValue("name" + ix, "string"));
    data.put("uri", new SerializableValue(uri, "string"));
    return new SerializableObject(data, uri, typeNameStore);
  }

  TypeNameStore createJsonTypeNameStore() throws IOException {
    return new JsonTypeNameStore(new File("nop"), null);
  }

  TypeNameStore createTypeNameStore() {
    return new TypeNameStore() {
      @Override
      public String makeGraphQlname(String uri) {
        return "nameFromUri=" + uri;
      }

      @Override
      public String makeUri(String graphQlName) {
        return "uriFromName=" + graphQlName;
      }

      @Override
      public String shorten(String uri) {
        return "shortened=" + uri;
      }

      @Override
      public Map<String, String> getMappings() {
        return Collections.emptyMap();
      }

      // @Override
      // public void addPrefix(String prefix, String iri) {
      //
      // }

      @Override
      public void close() throws Exception {

      }

      // @Override
      // public StoreStatus getStatus() {
      //   return null;
      // }
      //
      // @Override
      // public void process(QuadLoader source, long version) throws ProcessingFailedException {
      //
      // }
    };
  }
}
