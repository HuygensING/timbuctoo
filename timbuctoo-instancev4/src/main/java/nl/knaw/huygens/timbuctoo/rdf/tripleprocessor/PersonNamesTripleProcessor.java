package nl.knaw.huygens.timbuctoo.rdf.tripleprocessor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.model.PersonName;
import nl.knaw.huygens.timbuctoo.model.PersonNameComponent;
import nl.knaw.huygens.timbuctoo.model.properties.converters.PersonNamesConverter;
import nl.knaw.huygens.timbuctoo.rdf.Database;
import nl.knaw.huygens.timbuctoo.rdf.Entity;
import nl.knaw.huygens.timbuctoo.rdf.UriBearingPersonNames;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.slf4j.LoggerFactory.getLogger;

class PersonNamesTripleProcessor implements TripleProcessor {
  private static final Logger LOG = getLogger(TripleProcessorImpl.class);
  private static final String NAMES_PROPERTY_NAME = "names";
  private static final String NAMES_TYPE_ID = new PersonNamesConverter().getUniqueTypeIdentifier();

  private final Database database;
  private ObjectMapper objectMapper = new ObjectMapper();

  private final List<PersonNameComponent.Type> naturalOrder = Lists.newArrayList(
    PersonNameComponent.Type.ROLE_NAME,
    PersonNameComponent.Type.FORENAME,
    PersonNameComponent.Type.NAME_LINK,
    PersonNameComponent.Type.SURNAME,
    PersonNameComponent.Type.GEN_NAME,
    PersonNameComponent.Type.ADD_NAME
  );


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

    if (currentRawValue.isPresent()) {
      final UriBearingPersonNames current = objectMapper.readValue(currentRawValue.get(), UriBearingPersonNames.class);
      addNamesProperty(entity, appendToPersonNames(value, nameType, current, subjectUri));
    } else {
      addNamesProperty(entity, makeNewPersonNames(value, nameType, subjectUri));
    }
  }

  private UriBearingPersonNames makeNewPersonNames(String value, PersonNameComponent.Type nameType, String subjectUri) {
    final UriBearingPersonNames personNames = new UriBearingPersonNames();
    final PersonName personName = new PersonName();
    personName.addNameComponent(nameType, value);
    personNames.list.add(personName);
    personNames.nameUris.put(subjectUri, 0);
    return personNames;
  }

  private UriBearingPersonNames appendToPersonNames(String value, PersonNameComponent.Type nameType,
                                                    UriBearingPersonNames current, String subjectUri) {

    // FIXME: this will somehow need to become a lookup by URI for alternative names
    final Integer nameIndex = current.nameUris.getOrDefault(subjectUri, -1);
    LOG.debug("Processing name uri {} found? {} to -> {}", subjectUri, current.nameUris.containsKey(subjectUri),
      nameIndex);

    final PersonName newOrUpdatedPersonName = nameIndex > -1 && current.list.size() > nameIndex ?
      current.list.get(nameIndex) : new PersonName();


    insertNameComponentAtNaturalPosition(nameType, value, newOrUpdatedPersonName);

    if (nameIndex < 0) {
      current.list.add(newOrUpdatedPersonName);
      current.nameUris.put(subjectUri, current.list.size() - 1);
    }

    return current;
  }


  private void insertNameComponentAtNaturalPosition(PersonNameComponent.Type nameType, String value,
                                                    PersonName currentPersonName) {
    final List<PersonNameComponent> currentPersonNameComponents = currentPersonName.getComponents();
    int currentIndex = getNaturalComponentPos(nameType, currentPersonNameComponents);
    final PersonNameComponent newNameComponent = new PersonNameComponent(nameType, value);
    currentPersonNameComponents.add(currentIndex, newNameComponent);
  }

  private int getNaturalComponentPos(PersonNameComponent.Type nameType,
                                     List<PersonNameComponent> currentPersonNameComponents) {

    for (int  currentIndex = 0; currentIndex < currentPersonNameComponents.size(); currentIndex++) {
      final int currentOrderIndex = naturalOrder.indexOf(currentPersonNameComponents.get(currentIndex).getType());
      final int newOrderIndex = naturalOrder.indexOf(nameType);
      if (currentOrderIndex > newOrderIndex) {
        return currentIndex;
      }
    }

    return currentPersonNameComponents.size();
  }

  private void addNamesProperty(Entity entity, UriBearingPersonNames personNames) throws JsonProcessingException {
    entity.addProperty(NAMES_PROPERTY_NAME, objectMapper.writeValueAsString(personNames), NAMES_TYPE_ID);
  }
}
