package nl.knaw.huygens.timbuctoo.rdf.tripleprocessor;

import nl.knaw.huygens.timbuctoo.model.properties.LocalProperty;
import nl.knaw.huygens.timbuctoo.rdf.Database;
import nl.knaw.huygens.timbuctoo.rdf.Entity;
import org.apache.jena.graph.Triple;
import org.apache.tinkerpop.gremlin.structure.Property;
import org.slf4j.Logger;

import java.util.Optional;

import static org.slf4j.LoggerFactory.getLogger;

public class SameAsTripleProcessor {
  private final Database database;
  private static final Logger LOG = getLogger(SameAsTripleProcessor.class);

  public SameAsTripleProcessor(Database database) {
    this.database = database;
  }

  public void process(String vreName, boolean isAssertion, Triple triple) {
    Optional<Entity> object = database.findEntity(vreName, triple.getObject());
    Optional<Entity> subject = database.findEntity(vreName, triple.getSubject());

    if (object.isPresent() && subject.isPresent()) {
      final Entity objectEntity = object.get();
      final Entity subjectEntity = subject.get();

      if (LOG.isDebugEnabled()) {
        LOG.debug("Merging object entity into subject entity: {} <-- {}", subjectEntity.getProperties(),
          objectEntity.getProperties());
      }

      // First make sure all the edges are copied (especially edges pointing to collections only the object is part of)
      database.copyEdgesFromObjectIntoSubject(subjectEntity, objectEntity);

      // Reload subject entity with any new collection added to it from the object (vertex must still be present here)
      final Entity reloadedSubjectEntity = database.findEntity(vreName, triple.getSubject()).get();

      // Merge the properties of the object entity into the reloaded subject entity via Entity model
      mergeEntityProperties(reloadedSubjectEntity , objectEntity);

      if (LOG.isDebugEnabled()) {
        final Entity finalSubject = database.findEntity(vreName, triple.getSubject()).get();
        LOG.debug("Final subject properties: {}", finalSubject.getProperties());
      }

      // purge the object entity from the database and index
      database.purgeEntity(vreName, objectEntity);

      // add the object uri as a synonym to the subject entity
      database.addRdfSynonym(vreName, subjectEntity, triple.getObject());
    } else if (object.isPresent()) {
      database.addRdfSynonym(vreName, object.get(), triple.getSubject());
    } else if (subject.isPresent()) {
      database.addRdfSynonym(vreName, subject.get(), triple.getObject());
    } else {
      Entity entity = database.findOrCreateEntity(vreName, triple.getObject());
      database.addRdfSynonym(vreName, entity, triple.getSubject());
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
}
