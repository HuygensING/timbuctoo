package nl.knaw.huygens.timbuctoo.rdf.tripleprocessor;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.knaw.huygens.timbuctoo.model.properties.converters.PersonNamesConverter;
import nl.knaw.huygens.timbuctoo.rdf.Database;
import nl.knaw.huygens.timbuctoo.rdf.Entity;
import nl.knaw.huygens.timbuctoo.rdf.UriBearingPersonNames;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import static org.slf4j.LoggerFactory.getLogger;

public class PersonNameVariantTripleProcessor extends AbstractReferenceTripleProcessor {
  private static final Logger LOG = getLogger(PersonNameVariantTripleProcessor.class);
  private static final String NAMES_TYPE_ID = new PersonNamesConverter().getUniqueTypeIdentifier();
  private static final String NAMES_PROPERTY_NAME = "names";

  private Database database;
  private ObjectMapper objectMapper;

  public PersonNameVariantTripleProcessor(Database database) {
    this.database = database;
    this.objectMapper = new ObjectMapper();
  }

  private UriBearingPersonNames mergeNames(String myRawValue, String theirRawValue) throws IOException {
    final UriBearingPersonNames theirs = objectMapper.readValue(theirRawValue, UriBearingPersonNames.class);
    final UriBearingPersonNames mine = objectMapper.readValue(myRawValue, UriBearingPersonNames.class);

    int startIndex = theirs.list.size();
    for (Map.Entry<String, Integer> entry : mine.nameUris.entrySet()) {
      theirs.list.add(mine.list.get(entry.getValue()));
      theirs.nameUris.put(entry.getKey(), entry.getValue() + startIndex);
    }

    return theirs;
  }

  @Override
  protected void processAssertion(String vreName, String subject, String predicate, String object) {
    final Optional<Entity> subjectEntity = database.findEntity(vreName, subject);
    final Optional<Entity> objectEntity = database.findEntity(vreName, object);

    if (!subjectEntity.isPresent()) {
      LOG.error("Entity with rdf uri '{}' not found", subject);
      return;
    }

    if (!objectEntity.isPresent()) {
      LOG.error("Entity with rdf uri '{}' not found", object);
    }

    final Optional<String> subjectRawNames = subjectEntity.get().getPropertyValue(NAMES_PROPERTY_NAME);
    if (subjectRawNames.isPresent()) {
      final Optional<String> objectRawNames = objectEntity.get().getPropertyValue(NAMES_PROPERTY_NAME);
      if (objectRawNames.isPresent()) {
        try {
          final UriBearingPersonNames mergedNames = mergeNames(subjectRawNames.get(), objectRawNames.get());
          objectEntity.get().addProperty(NAMES_PROPERTY_NAME, objectMapper.writeValueAsString(mergedNames),
            NAMES_TYPE_ID);
        } catch (IOException e) {
          LOG.error("Failed to read/write personNames json", e);
          return;
        }
      } else {
        objectEntity.get().addProperty(NAMES_PROPERTY_NAME, subjectRawNames.get(), NAMES_TYPE_ID);
      }
    }
    // FIXME looks like a bug, but seems to work in the front-end, somewhere else this is corrected
    database.addRdfSynonym(vreName, objectEntity.get(), object);
    database.purgeEntity(vreName, subjectEntity.get());
  }

  @Override
  protected void processRetraction(String vreName, String subject, String predicate, String object) {
    LOG.error("No retraction implemented for person name variant triples.");
  }
}
