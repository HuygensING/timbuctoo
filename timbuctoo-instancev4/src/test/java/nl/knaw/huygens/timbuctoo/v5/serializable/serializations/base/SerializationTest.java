package nl.knaw.huygens.timbuctoo.v5.serializable.serializations.base;

import nl.knaw.huygens.timbuctoo.v5.datastores.implementations.json.JsonTypeNameStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.prefixstore.TypeNameStore;
import nl.knaw.huygens.timbuctoo.v5.serializable.Serializable;
import nl.knaw.huygens.timbuctoo.v5.serializable.SerializableList;
import nl.knaw.huygens.timbuctoo.v5.serializable.SerializableObject;
import nl.knaw.huygens.timbuctoo.v5.serializable.SerializableUntypedValue;
import nl.knaw.huygens.timbuctoo.v5.serializable.SerializableValue;
import nl.knaw.huygens.timbuctoo.v5.serializable.serializations.base.BaseSerialization;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
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
    List<Serializable> listFor1 = new ArrayList<>();
    listFor1.add(so2);
    listFor1.add(so3);
    LinkedHashMap<String, Serializable> data101 = new LinkedHashMap<>();
    SerializableObject so101 = createSerializableObject(data101, 101, typeNameStore);
    data101.put("items", new SerializableList(listFor2));
    data2.put("wroteBook", so101);

    LinkedHashMap<String, Serializable> data102 = new LinkedHashMap<>();
    SerializableObject so102 = createSerializableObject(data102, 102, typeNameStore);
    data102.put("items", new SerializableList(listFor1));

    LinkedHashMap<String, Serializable> data1 = new LinkedHashMap<>();
    SerializableObject so1 = createSerializableObject(data1, 1, typeNameStore);
    data1.put("hasChild", so102);

    LinkedHashMap<String, Serializable> data0 = new LinkedHashMap<>();
    SerializableObject so0 = createSerializableObject(data0, 0, typeNameStore);
    data0.put("hasBeer", so1);
    return so0;
  }

  protected SerializableObject createSerializableObject(
    LinkedHashMap<String, Serializable> data, int ix, TypeNameStore typeNameStore) {
    String uri = "uri" + ix;
    if (ix > 100) { // this is a list object
      uri = null;
    } else { // this is a 'normal' object
      data.put("foo", new SerializableUntypedValue("foo" + ix));
      data.put("name", new SerializableValue("name" + ix, "string"));
      data.put("uri", new SerializableValue(uri, "string"));
    }
    return new SerializableObject(data, uri, typeNameStore);
  }

  protected TypeNameStore createJsonTypeNameStore() throws IOException {
    return new JsonTypeNameStore(new File("nop"), null);
  }

  protected TypeNameStore createTypeNameStore() {
    return new TypeNameStore() {
      @Override
      public String makeGraphQlname(String uri) {
        return uri;
      }

      @Override
      public String makeUri(String graphQlName) {
        return graphQlName;
      }

      @Override
      public String shorten(String uri) {
        return uri;
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

  protected void validate(String schemaLocation, String result) throws Exception {
    InputStream in = IOUtils.toInputStream(result, "UTF-8");
    Source source = new StreamSource(in);

    SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    Schema schema = sf.newSchema(new URL(schemaLocation));

    Validator validator = schema.newValidator();
    validator.setErrorHandler(new GraphErrorHandler());
    validator.validate(source);
  }

  protected static class GraphErrorHandler implements ErrorHandler {

    @Override
    public void warning(SAXParseException exception) throws SAXException {
      throw exception;
    }

    @Override
    public void error(SAXParseException exception) throws SAXException {
      throw exception;
    }

    @Override
    public void fatalError(SAXParseException exception) throws SAXException {
      throw exception;
    }
  }
}
