package nl.knaw.huygens.timbuctoo.v5.serializable.serializations.base;

import nl.knaw.huygens.timbuctoo.v5.datastores.implementations.json.JsonTypeNameStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.prefixstore.TypeNameStore;
import nl.knaw.huygens.timbuctoo.v5.serializable.Serializable;
import nl.knaw.huygens.timbuctoo.v5.serializable.SerializableList;
import nl.knaw.huygens.timbuctoo.v5.serializable.SerializableObject;
import nl.knaw.huygens.timbuctoo.v5.serializable.SerializableUntypedValue;
import nl.knaw.huygens.timbuctoo.v5.serializable.SerializableValue;
import nl.knaw.huygens.timbuctoo.v5.serializable.serializations.base.BaseSerialization;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created on 2017-06-07 11:06.
 */
public abstract class SerializationTest {

  protected void saveAs(ByteArrayOutputStream bout, String filename) throws IOException {
    File dir = new File("target/test-output/serializations");
    dir.mkdirs();
    FileOutputStream fout = new FileOutputStream(new File(dir, filename));
    bout.writeTo(fout);
    fout.close();
  }


  protected SerializableObject createGraph_01(TypeNameStore typeNameStore) {
    List<Serializable> listFor2 = new ArrayList<>();
    listFor2.add(new SerializableValue("La Possibilité d'une île", "string"));
    listFor2.add(new SerializableUntypedValue("Les Particules élémentaires"));
    listFor2.add(new SerializableValue("J'ai un rêve", "string"));
    
    LinkedHashMap<String, Serializable> data4 = new LinkedHashMap<>();
    SerializableObject so4 = createSerializableObject(data4, 4, typeNameStore);

    LinkedHashMap<String, Serializable> data3 = new LinkedHashMap<>();
    SerializableObject so3 = createSerializableObject(data3, 3, typeNameStore);
    data3.put("fooBar", so4);

    LinkedHashMap<String, Serializable> data2 = new LinkedHashMap<>();
    SerializableObject so2 = createSerializableObject(data2, 2, typeNameStore);
    data2.put("hasSibling", so3);
    data2.put("wroteBook", new SerializableList(listFor2));

    List<Serializable> listFor1 = new ArrayList<>();
    listFor1.add(so2);
    listFor1.add(so3);
    SerializableList slForSo1 = new SerializableList(listFor1);

    LinkedHashMap<String, Serializable> data1 = new LinkedHashMap<>();
    SerializableObject so1 = createSerializableObject(data1, 1, typeNameStore);
    data1.put("hasChild", slForSo1);

    LinkedHashMap<String, Serializable> data0 = new LinkedHashMap<>();
    SerializableObject so0 = createSerializableObject(data0, 0, typeNameStore);
    data0.put("hasBeer", so1);
    return so0;
  }

  protected SerializableObject createSerializableObject(
    LinkedHashMap<String, Serializable> data, int ix, TypeNameStore typeNameStore) {
    String uri = "uri" + ix;
    data.put("foo", new SerializableUntypedValue("foo" + ix));
    data.put("name", new SerializableValue("name" + ix, "string"));
    data.put("uri", new SerializableValue(uri, "string"));
    return new SerializableObject(data, uri, typeNameStore);
  }

  protected TypeNameStore createJsonTypeNameStore() throws IOException {
    return new JsonTypeNameStore(new File("nop"), null);
  }

  protected TypeNameStore createTypeNameStore() {
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
