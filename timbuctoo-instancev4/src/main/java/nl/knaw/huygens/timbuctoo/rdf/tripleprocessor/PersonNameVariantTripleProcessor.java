package nl.knaw.huygens.timbuctoo.rdf.tripleprocessor;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.knaw.huygens.timbuctoo.model.properties.converters.PersonNamesConverter;
import nl.knaw.huygens.timbuctoo.rdf.Database;
import nl.knaw.huygens.timbuctoo.rdf.Entity;
import nl.knaw.huygens.timbuctoo.rdf.UriBearingPersonNames;
import org.apache.jena.graph.Triple;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import static org.slf4j.LoggerFactory.getLogger;

public class PersonNameVariantTripleProcessor implements TripleProcessor {
  private static final Logger LOG = getLogger(PersonNameVariantTripleProcessor.class);
  private static final String NAMES_TYPE_ID = new PersonNamesConverter().getUniqueTypeIdentifier();
  private static final String NAMES_PROPERTY_NAME = "names";

  private Database database;
  private ObjectMapper objectMapper;

  public PersonNameVariantTripleProcessor(Database database) {
    this.database = database;
    this.objectMapper = new ObjectMapper();
  }

  @Override
  public void process(String vreName, boolean isAssertion, Triple triple) {
    final Optional<Entity> subjectEntity = database.findEntity(vreName, triple.getSubject());
    final Optional<Entity> objectEntity = database.findEntity(vreName, triple.getObject());

    if (subjectEntity.isPresent() && objectEntity.isPresent()) {
      final Optional<String> myRawValue = subjectEntity.get().getPropertyValue(NAMES_PROPERTY_NAME);
      if (myRawValue.isPresent()) {
        final Optional<String> theirRawValue = objectEntity.get().getPropertyValue(NAMES_PROPERTY_NAME);
        if (theirRawValue.isPresent()) {
          try {
            final UriBearingPersonNames mergedNames = mergeNames(myRawValue.get(), theirRawValue.get());
            objectEntity.get().addProperty(NAMES_PROPERTY_NAME, objectMapper.writeValueAsString(mergedNames),
              NAMES_TYPE_ID);
          } catch (IOException e) {
            LOG.error("Failed to read/write personNames json", e);
            return;
          }
        } else {
          objectEntity.get().addProperty(NAMES_PROPERTY_NAME, myRawValue.get(), NAMES_TYPE_ID);
        }
      }
      database.addRdfSynonym(vreName, objectEntity.get(), triple.getObject());
      database.purgeEntity(vreName, subjectEntity.get());
    } else {
      LOG.error("entity not found. subject: {}, object: {}", triple.getSubject().getLocalName(),
        triple.getObject().getLocalName());
    }
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
}
