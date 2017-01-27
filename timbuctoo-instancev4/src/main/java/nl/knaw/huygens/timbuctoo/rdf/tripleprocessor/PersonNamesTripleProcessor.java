package nl.knaw.huygens.timbuctoo.rdf.tripleprocessor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.knaw.huygens.timbuctoo.model.PersonNameComponent;
import nl.knaw.huygens.timbuctoo.model.properties.converters.PersonNamesConverter;
import nl.knaw.huygens.timbuctoo.rdf.Database;
import nl.knaw.huygens.timbuctoo.rdf.Entity;
import nl.knaw.huygens.timbuctoo.rdf.UriBearingPersonNames;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.Optional;

import static org.slf4j.LoggerFactory.getLogger;

class PersonNamesTripleProcessor implements TripleProcessor {
  private static final Logger LOG = getLogger(PersonNamesTripleProcessor.class);
  private static final String NAMES_PROPERTY_NAME = "names";
  private static final String NAMES_TYPE_ID = new PersonNamesConverter().getUniqueTypeIdentifier();

  private final Database database;
  private ObjectMapper objectMapper = new ObjectMapper();

  PersonNamesTripleProcessor(Database database) {
    this.database = database;
  }

  @Override
  public void process(String vreName, boolean isAssertion, Triple triple) {

    final Node node = triple.getSubject();
    final Entity entity = database.findOrCreateEntity(vreName, node);
    final String value = triple.getObject().getLiteralLexicalForm();
    final String nameTypePredicate = triple.getPredicate().getLocalName();

    if (isAssertion && value.length() > 0) {
      try {
        addNameComponentToEntity(entity, nameTypePredicate, value, node.getURI());
      } catch (JsonProcessingException e) {
        LOG.error("Failed to write personNames json for {}.", entity, e);
      } catch (IOException e) {
        LOG.error("Failed to read personNames json for {}", entity, e);
      }
    }
  }

  private void addNameComponentToEntity(Entity entity, String nameTypePredicate, String value, String subjectUri)
    throws IOException {

    final PersonNameComponent.Type nameType = PersonNameComponent.Type.getInstance(nameTypePredicate);
    final Optional<String> currentRawValue = entity.getPropertyValue("names");

    UriBearingPersonNames names;
    if (currentRawValue.isPresent()) {
      names = objectMapper.readValue(currentRawValue.get(), UriBearingPersonNames.class);
    } else {
      names = new UriBearingPersonNames();
    }

    names.addNameComponent(subjectUri, nameType, value);
    addNamesProperty(entity, names);
  }

  private void addNamesProperty(Entity entity, UriBearingPersonNames personNames) throws JsonProcessingException {
    entity.addProperty(NAMES_PROPERTY_NAME, objectMapper.writeValueAsString(personNames), NAMES_TYPE_ID);
  }
}
