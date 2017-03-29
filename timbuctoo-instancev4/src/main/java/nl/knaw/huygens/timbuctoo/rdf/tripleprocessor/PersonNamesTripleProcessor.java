package nl.knaw.huygens.timbuctoo.rdf.tripleprocessor;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.knaw.huygens.timbuctoo.core.RdfImportSession;
import nl.knaw.huygens.timbuctoo.core.dto.rdf.RdfProperty;
import nl.knaw.huygens.timbuctoo.core.dto.rdf.RdfReadProperty;
import nl.knaw.huygens.timbuctoo.model.PersonName;
import nl.knaw.huygens.timbuctoo.model.PersonNames;
import nl.knaw.huygens.timbuctoo.model.properties.converters.PersonNamesConverter;
import nl.knaw.huygens.timbuctoo.rdf.Database;
import nl.knaw.huygens.timbuctoo.rdf.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;

import static nl.knaw.huygens.timbuctoo.rdf.tripleprocessor.RdfNameHelper.getLocalName;

public class PersonNamesTripleProcessor extends AbstractValueTripleProcessor {

  static final String PERSON_NAMES_TYPE_URI = "http://timbuctoo.huygens.knaw.nl/datatypes/person-names";
  static final String NAMES_TYPE_ID = new PersonNamesConverter().getUniqueTypeIdentifier();
  private static final Logger LOG = LoggerFactory.getLogger(PropertyTripleProcessor.class);
  private final RdfImportSession rdfImportSession;
  private final Database database;
  private final ObjectMapper objectMapper;

  public PersonNamesTripleProcessor(RdfImportSession rdfImportSession, Database database) {
    this.rdfImportSession = rdfImportSession;
    this.database = database;
    objectMapper = new ObjectMapper();
  }

  @Override
  protected void processAssertion(String vreName, String subject, String predicate, String lexicalValue,
                                  String typeUri) {
    LOG.debug("Process PersonNames triple for subject '{}' with value '{}'", subject, lexicalValue);

    Entity entity = database.findOrCreateEntity(vreName, subject);
    String propertyName = getLocalName(predicate);
    Optional<String> propertyValue = entity.getPropertyValue(propertyName);

    try {
      PersonName personName = objectMapper.readValue(lexicalValue, PersonName.class);
      PersonNames personNames = getPersonNames(objectMapper, propertyValue);
      personNames.list.add(personName);
      String names = objectMapper.writeValueAsString(personNames);
      // Because the person names is wrapped in a person names type, the type changes
      entity.addProperty(propertyName, names, NAMES_TYPE_ID);
    } catch (IOException e) {
      LOG.error("Could not convert '{}' to PersonName", lexicalValue);
    }
  }

  private PersonNames getPersonNames(ObjectMapper objectMapper, Optional<String> propertyValue)
    throws IOException {
    if (propertyValue.isPresent()) {
      LOG.debug("Update existing person names: {}", propertyValue.get());
      return objectMapper.readValue(propertyValue.get(), PersonNames.class);
    } else {
      return new PersonNames();
    }
  }

  @Override
  protected void processRetraction(String vreName, String subject, String predicate, String lexicalValue,
                                   String typeUri) {
    throw new UnsupportedOperationException("Not implemented yet");
  }
}
