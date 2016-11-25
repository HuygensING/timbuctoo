package nl.knaw.huygens.timbuctoo.database.converters.json;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.database.dto.UpdateEntity;
import nl.knaw.huygens.timbuctoo.database.dto.dataset.Collection;
import nl.knaw.huygens.timbuctoo.database.dto.property.TimProperty;
import nl.knaw.huygens.timbuctoo.database.exceptions.UnknownPropertyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import static java.util.stream.Collectors.toSet;
import static nl.knaw.huygens.timbuctoo.util.StreamIterator.stream;

public class JsonToEntityMapper {

  public static final Logger LOG = LoggerFactory.getLogger(JsonToEntityMapper.class);

  public UpdateEntity newUpdateEntity(Collection collection, UUID id, ObjectNode data)
    throws IOException {
    if (data.get("^rev") == null) {
      throw new IOException("data object should have a ^rev property indicating the revision this update was based on");
    }
    int rev = data.get("^rev").asInt();

    List<TimProperty<?>> properties = getDataProperties(collection, data);

    return new UpdateEntity(id, properties, rev);
  }

  /**
   * Retrieve all the properties that contain client data.
   */
  public List<TimProperty<?>> getDataProperties(Collection collection, ObjectNode input) throws IOException {
    JsonPropertyConverter converter = new JsonPropertyConverter(collection);

    Set<String> fieldNames = getDataFields(input);
    List<TimProperty<?>> properties = Lists.newArrayList();
    for (String fieldName : fieldNames) {
      try {
        properties.add(converter.from(fieldName, input.get(fieldName)));
      } catch (UnknownPropertyException e) {
        LOG.error("Property with name '{}' is unknown for collection '{}'.", fieldName,
          collection.getCollectionName());
        throw new IOException(
          String.format("Items of %s have no property %s", collection.getCollectionName(), fieldName));
      } catch (IOException e) {
        LOG.error("Property '{}' with value '{}' could not be converted", fieldName, input.get(fieldName));
        throw new IOException(
          String.format("Property '%s' could not be converted. %s", fieldName, e.getMessage()),
          e
        );
      }
    }
    return properties;
  }

  private Set<String> getDataFields(ObjectNode data) {
    return stream(data.fieldNames())
      .filter(x -> !x.startsWith("@"))
      .filter(x -> !x.startsWith("^"))
      .filter(x -> !Objects.equals(x, "_id"))
      .collect(toSet());
  }


}
