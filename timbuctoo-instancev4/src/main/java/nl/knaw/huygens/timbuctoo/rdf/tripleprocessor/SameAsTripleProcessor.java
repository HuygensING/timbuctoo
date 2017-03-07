package nl.knaw.huygens.timbuctoo.rdf.tripleprocessor;

import nl.knaw.huygens.timbuctoo.model.properties.LocalProperty;
import nl.knaw.huygens.timbuctoo.rdf.Database;
import nl.knaw.huygens.timbuctoo.rdf.Entity;
import org.apache.commons.lang3.StringUtils;
import org.apache.tinkerpop.gremlin.structure.Property;
import org.slf4j.Logger;

import java.util.Optional;

import static org.slf4j.LoggerFactory.getLogger;

public class SameAsTripleProcessor extends AbstractReferenceTripleProcessor {
  private static final Logger LOG = getLogger(SameAsTripleProcessor.class);
  private final Database database;

  public SameAsTripleProcessor(Database database) {
    this.database = database;
  }

  @Override
  protected void processAssertion(String vreName, String subject, String predicate, String object) {
    if (StringUtils.isBlank(object)) {
      return;
    }

    Optional<Entity> objectEntityOpt = database.findEntity(vreName, object);
    Optional<Entity> subjectEntityOpt = database.findEntity(vreName, subject);

    if (objectEntityOpt.isPresent() && subjectEntityOpt.isPresent()) {
      final Entity objectEntity = objectEntityOpt.get();
      final Entity subjectEntity = subjectEntityOpt.get();

      if (LOG.isDebugEnabled()) {
        LOG.debug("Merging object entity into subject entity: {} <-- {}", subjectEntity.getProperties(),
          objectEntity.getProperties());
      }

      // First make sure all the edges are copied (especially edges pointing to collections only the object is part of)
      database.copyEdgesFromObjectIntoSubject(subjectEntity, objectEntity);

      // Reload subject entity with any new collection added to it from the object (vertex must still be present here)
      final Entity reloadedSubjectEntity = database.findEntity(vreName, subject).get();

      // Merge the properties of the object entity into the reloaded subject entity via Entity model
      mergeEntityProperties(reloadedSubjectEntity, objectEntity);

      if (LOG.isDebugEnabled()) {
        final Entity finalSubject = database.findEntity(vreName, subject).get();
        LOG.debug("Final subject properties: {}", finalSubject.getProperties());
      }

      // purge the object entity from the database and index
      database.purgeEntity(vreName, objectEntity);

      // add the object uri as a synonym to the subject entity
      database.addRdfSynonym(vreName, subjectEntity, object);
    } else if (objectEntityOpt.isPresent()) {
      database.addRdfSynonym(vreName, objectEntityOpt.get(), subject);
    } else if (subjectEntityOpt.isPresent()) {
      database.addRdfSynonym(vreName, subjectEntityOpt.get(), object);
    } else {
      Entity entity = database.findOrCreateEntity(vreName, object);
      database.addRdfSynonym(vreName, entity, subject);
    }
  }

  private void mergeEntityProperties(Entity subjectEntity, Entity objectEntity) {
    objectEntity.getProperties().iterator().forEachRemaining(propertyMap -> {
      final String unprefixedPropertyName = propertyMap.get(LocalProperty.CLIENT_PROPERTY_NAME);
      final String propertyValue = propertyMap.get("value");
      final Optional<Property> existingProperty = subjectEntity.getProperty(unprefixedPropertyName);

      if (!existingProperty.isPresent()) {
        final String propertyType = propertyMap.get(LocalProperty.PROPERTY_TYPE_NAME);

        // This is why the subject needs to be reloaded with all new collections:
        // addProperty adds to any existing collections linked to the subject entity
        subjectEntity.addProperty(unprefixedPropertyName, propertyValue, propertyType);
      } else if (!existingProperty.get().value().equals(propertyValue)) {
        LOG.warn("Property values differ when merging synonymous (<owl:sameAs>) entities: {}", unprefixedPropertyName);
      }
    });
  }

  @Override
  protected void processRetraction(String vreName, String subject, String predicate, String object) {
    LOG.error("No retraction implemented for 'same as' triples.");
  }
}
