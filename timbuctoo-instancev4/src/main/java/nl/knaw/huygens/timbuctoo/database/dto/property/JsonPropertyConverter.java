package nl.knaw.huygens.timbuctoo.database.dto.property;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.knaw.huygens.timbuctoo.model.vre.Collection;
import nl.knaw.huygens.timbuctoo.util.Tuple;

import java.io.IOException;

import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsn;

public class JsonPropertyConverter extends PropertyConverter<JsonNode> {

  private final ObjectMapper objectMapper;

  public JsonPropertyConverter(Collection collection) {
    super(collection);
    objectMapper = new ObjectMapper();
  }

  @Override
  protected AltNamesProperty createAltNamesProperty(String propertyName, JsonNode value) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  protected DatableProperty createDatableProperty(String propertyName, JsonNode value) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  protected DefaultFullPersonNameProperty createDefaultFullPersonNameProperty(String propertyName, JsonNode value) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  protected DefaultLocationNameProperty createDefaultLocationNameProperty(String propertyName, JsonNode value) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  protected HyperLinksProperty createHyperLinksProperty(String propertyName, JsonNode value) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  protected PersonNamesProperty createPersonNamesProperty(String propertyName, JsonNode value) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  protected ArrayOfLimitedValuesProperty createArrayOfLimitedValuesProperty(String propertyName, JsonNode value) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  protected EncodedStringOfLimitedValuesProperty createEncodedStringOfLimitedValuesProperty(String propertyName,
                                                                                            JsonNode value) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  protected StringProperty createStringProperty(String propertyName, JsonNode value) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  protected StringOfLimitedValuesProperty createStringOfLimitedValues(String propertyName, JsonNode value) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  protected Tuple<String, JsonNode> to(AltNamesProperty property) throws IOException {
    try {
      return new Tuple<>(property.getName(), objectMapper.valueToTree(property.getValue().list));
    } catch (Exception e) {
      throw new IOException(e);
    }
  }

  @Override
  protected Tuple<String, JsonNode> to(DatableProperty property) throws IOException {
    return new Tuple<>(property.getName(), new ObjectMapper().readTree(property.getValue()));
  }

  @Override
  protected Tuple<String, JsonNode> to(DefaultFullPersonNameProperty property) throws IOException {
    return new Tuple<>(property.getName(), jsn(property.getValue().defaultName().getFullName()));
  }

  @Override
  protected Tuple<String, JsonNode> to(DefaultLocationNameProperty property) throws IOException {
    return new Tuple<>(property.getName(), jsn(property.getValue().getDefaultName()));
  }

  @Override
  protected Tuple<String, JsonNode> to(HyperLinksProperty property) throws IOException {
    return new Tuple<>(property.getName(), objectMapper.readTree(property.getValue()));
  }

  @Override
  protected Tuple<String, JsonNode> to(PersonNamesProperty property) throws IOException {
    return new Tuple<>(property.getName(), objectMapper.valueToTree(property.getValue().list));
  }

  @Override
  protected Tuple<String, JsonNode> to(ArrayOfLimitedValuesProperty property) throws IOException {
    return new Tuple<>(property.getName(), objectMapper.readTree(property.getValue()));
  }

  @Override
  protected Tuple<String, JsonNode> to(EncodedStringOfLimitedValuesProperty property) throws IOException {
    return new Tuple<>(property.getName(), objectMapper.readTree(property.getValue()));
  }

  @Override
  protected Tuple<String, JsonNode> to(StringProperty property) throws IOException {
    return new Tuple<>(property.getName(), jsn(property.getValue()));
  }

  @Override
  protected Tuple<String, JsonNode> to(StringOfLimitedValuesProperty property) throws IOException {
    return new Tuple<>(property.getName(), jsn(property.getValue()));
  }

}
