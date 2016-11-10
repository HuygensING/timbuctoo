package nl.knaw.huygens.timbuctoo.rdf.tripleprocessor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.model.properties.converters.ArrayToEncodedArrayConverter;
import nl.knaw.huygens.timbuctoo.rdf.Database;
import nl.knaw.huygens.timbuctoo.rdf.Entity;
import nl.knaw.huygens.timbuctoo.util.JsonBuilder;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnA;
import static org.slf4j.LoggerFactory.getLogger;

public class AltLabelTripleProcessor implements TripleProcessor {
  private static final Logger LOG = getLogger(AltLabelTripleProcessor.class);

  private final Database database;
  private final ObjectMapper objectMapper;

  public AltLabelTripleProcessor(Database database) {
    this.database = database;
    this.objectMapper = new ObjectMapper();

  }

  @Override
  public void process(String vreName, boolean isAssertion, Triple triple) {

    final Node node = triple.getSubject();
    final Entity entity = database.findOrCreateEntity(vreName, node);
    final String propertyName = triple.getPredicate().getLocalName();
    final String value = triple.getObject().getLiteralLexicalForm();

    if (isAssertion) {
      addToListProperty(entity, propertyName, value);
    } else {
      removeFromListProperty(entity, propertyName, value);
    }
  }

  private void addToListProperty(Entity entity, String propertyName,  String newRawValue) {
    final Optional<String> currentRawValue = entity.getPropertyValue(propertyName);

    if (currentRawValue.isPresent()) {
      try {
        final List<String> currentValue = objectMapper.readValue(currentRawValue.get(),
          objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));

        currentValue.add(newRawValue);

        entity.addProperty(propertyName, jsnA(currentValue.stream().map(JsonBuilder::jsn)).toString(),
          new ArrayToEncodedArrayConverter().getUniqueTypeIdentifier());

      } catch (IOException e) {
        LOG.error("Failed to parse property '{}' as json array: {}", propertyName, currentRawValue.get());
      }
    } else {
      final List<String> newValue = Lists.newArrayList(newRawValue);
      entity.addProperty(propertyName, jsnA(newValue.stream().map(JsonBuilder::jsn)).toString(),
        new ArrayToEncodedArrayConverter().getUniqueTypeIdentifier());
    }
  }

  private void removeFromListProperty(Entity entity, String propertyName, String valueToRemove) {
    final Optional<String> currentRawValue = entity.getPropertyValue(propertyName);

    if (currentRawValue.isPresent()) {
      try {
        final List<String> currentValue = objectMapper.readValue(currentRawValue.get(),
          objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));

        currentValue.remove(valueToRemove);

        if (currentValue.size() == 0) {
          entity.removeProperty(propertyName);
        } else {
          entity.addProperty(propertyName, jsnA(currentValue.stream().map(JsonBuilder::jsn)).toString(),
            new ArrayToEncodedArrayConverter().getUniqueTypeIdentifier());
        }

      } catch (IOException e) {
        LOG.error("Failed to parse property '{}' as json array: {}", propertyName, currentRawValue.get());
      }
    }
  }


}
