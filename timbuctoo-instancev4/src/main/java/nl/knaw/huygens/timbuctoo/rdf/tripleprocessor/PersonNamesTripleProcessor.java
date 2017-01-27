package nl.knaw.huygens.timbuctoo.rdf.tripleprocessor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.knaw.huygens.timbuctoo.model.PersonNameComponent;
import nl.knaw.huygens.timbuctoo.model.properties.converters.PersonNamesConverter;
import nl.knaw.huygens.timbuctoo.rdf.Database;
import nl.knaw.huygens.timbuctoo.rdf.Entity;
import nl.knaw.huygens.timbuctoo.rdf.UriBearingPersonNames;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.impl.LiteralLabel;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.Optional;

import static nl.knaw.huygens.timbuctoo.rdf.tripleprocessor.RdfNameHelper.getEntityTypeName;
import static org.slf4j.LoggerFactory.getLogger;

class PersonNamesTripleProcessor extends AbstractValueTripleProcessor {
  private static final Logger LOG = getLogger(PersonNamesTripleProcessor.class);
  private static final String NAMES_PROPERTY_NAME = "names";
  private static final String NAMES_TYPE_ID = new PersonNamesConverter().getUniqueTypeIdentifier();

  private final Database database;
  private ObjectMapper objectMapper = new ObjectMapper();

  PersonNamesTripleProcessor(Database database) {
    this.database = database;
  }

  public void process(String vreName, boolean isAssertion, Triple triple) {
    process(vreName, triple.getSubject().getURI(), triple.getPredicate().getURI(), triple.getObject().getLiteral(),
      isAssertion);
  }

  @Override
  protected void processAssertion(String vreName, String subject, String predicate, LiteralLabel object) {
    final Entity entity = database.findOrCreateEntity(vreName, subject);
    final String value = object.getLexicalForm();
    final String nameTypePredicate = getEntityTypeName(predicate);

    try {
      addNameComponentToEntity(entity, nameTypePredicate, value, subject);
    } catch (JsonProcessingException e) {
      LOG.error("Failed to write personNames json for {}.", entity);
      LOG.error("Error thrown", e);
    } catch (IOException e) {
      LOG.error("Failed to read personNames json for {}", entity);
      LOG.error("Error thrown", e);
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
    saveNamesProperty(entity, names);
  }

  @Override
  protected void processRetraction(String vreName, String subject, String predicate, LiteralLabel object) {
    final Entity entity = database.findOrCreateEntity(vreName, subject);
    final String value = object.getLexicalForm();
    final String nameTypePredicate = getEntityTypeName(predicate);

    try {
      removeNameComponent(entity, nameTypePredicate, value, subject);
    } catch (IOException e) {
      LOG.error("Failed to update personNames for {}", entity);
    }
  }

  private void removeNameComponent(Entity entity, String nameTypePredicate, String value, String subjectUri)
    throws IOException {
    final PersonNameComponent.Type nameType = PersonNameComponent.Type.getInstance(nameTypePredicate);
    final Optional<String> currentRawValue = entity.getPropertyValue("names");

    if (!currentRawValue.isPresent()) {
      LOG.warn("'{}' has no 'names' property", entity);
    }

    UriBearingPersonNames names = objectMapper.readValue(currentRawValue.get(), UriBearingPersonNames.class);

    names.removeComponent(subjectUri, nameType, value);
    saveNamesProperty(entity, names);
  }

  private void saveNamesProperty(Entity entity, UriBearingPersonNames personNames) throws JsonProcessingException {
    entity.addProperty(NAMES_PROPERTY_NAME, objectMapper.writeValueAsString(personNames), NAMES_TYPE_ID);
  }
}
