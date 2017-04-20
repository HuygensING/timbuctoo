package nl.knaw.huygens.timbuctoo.v5.serializable.serializations;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import nl.knaw.huygens.timbuctoo.v5.datastores.prefixstore.TypeNameStore;
import nl.knaw.huygens.timbuctoo.v5.serializable.Serialization;
import nl.knaw.huygens.timbuctoo.v5.serializable.TocGenerator;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;

public class JsonLdSerialization implements Serialization {
  private final JsonGenerator generator;
  private TypeNameStore typeNameStore;
  private boolean contextWritten;

  public JsonLdSerialization(OutputStream outputStream) throws IOException {
    generator = new JsonFactory().createGenerator(outputStream, JsonEncoding.UTF8);
  }

  @Override
  public void initialize(TocGenerator tocGenerator, TypeNameStore typeNameStore) throws IOException {
    this.typeNameStore = typeNameStore;
  }

  @Override
  public void finish() throws IOException {
    generator.close();
  }

  @Override
  public void onStartEntity(String uri) throws IOException {
    generator.writeStartObject();
    if (!contextWritten) {
      contextWritten = true;
      generator.writeFieldName("@context");
      generator.writeStartObject();
      for (Map.Entry<String, String> mapping : typeNameStore.getMappings().entrySet()) {
        generator.writeStringField(mapping.getKey(), mapping.getValue());
      }
      generator.writeEndObject();
    }
    if (uri != null) {
      generator.writeStringField("@id", typeNameStore.shorten(uri));
    }
  }

  @Override
  public void onProperty(String propertyName) throws IOException {
    generator.writeFieldName(typeNameStore.shorten(propertyName));
  }

  @Override
  public void onCloseEntity() throws IOException {
    generator.writeEndObject();
  }

  @Override
  public void onStartList() throws IOException {
    generator.writeStartArray();
  }

  @Override
  public void onListItem(int index) {
  }

  @Override
  public void onCloseList() throws IOException {
    generator.writeEndArray();
  }

  @Override
  public void onRdfValue(Object value, String valueType) throws IOException {
    if (value == null) {
      generator.writeNull();
    } else if (valueType != null) {
      generator.writeStartObject();
      generator.writeStringField("@type", valueType);
      generator.writeFieldName("@value");
      writeValue(value);
      generator.writeEndObject();
    } else {
      writeValue(value);
    }
  }

  @Override
  public void onValue(Object value) throws IOException {
    onRdfValue(value, null);
  }

  private void writeValue(Object value) throws IOException {
    if (value instanceof Integer) {
      generator.writeNumber((Integer) value);
    } else if (value instanceof Long) {
      generator.writeNumber((Long) value);
    } else if (value instanceof Short) {
      generator.writeNumber((Short) value);
    } else if (value instanceof Byte) {
      generator.writeNumber((Byte) value);
    } else if (value instanceof Double) {
      generator.writeNumber((Double) value);
    } else if (value instanceof BigInteger) {
      generator.writeNumber((BigInteger) value);
    } else if (value instanceof BigDecimal) {
      generator.writeNumber((BigDecimal) value);
    } else if (value instanceof String) {
      generator.writeString((String) value);
    } else if (value instanceof Boolean) {
      generator.writeBoolean((Boolean) value);
    } else if (value instanceof Character) {
      generator.writeString(new char[]{(char) value}, 0, 1);
    } else {
      generator.writeString(value.toString());
    }
  }
}
