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

      mergeEntityProperties(objectEntity, subjectEntity);
      database.copyEdgesFromObjectIntoSubject(vreName, subjectEntity, objectEntity);
    } else if (object.isPresent()) {
      database.addRdfSynonym(vreName, object.get(), triple.getSubject());
    } else if (subject.isPresent()) {
      database.addRdfSynonym(vreName, subject.get(), triple.getObject());
    } else {
      Entity entity = database.findOrCreateEntity(vreName, triple.getObject());
      database.addRdfSynonym(vreName, entity, triple.getSubject());
    }
  }

  private void mergeEntityProperties(Entity objectEntity, Entity subjectEntity) {
    objectEntity.getProperties().iterator().forEachRemaining(propertyMap -> {
      final String unprefixedPropertyName = propertyMap.get(LocalProperty.CLIENT_PROPERTY_NAME);
      final String propertyValue = propertyMap.get("value");
      final Optional<Property> existingProperty = subjectEntity.getProperty(unprefixedPropertyName);

      if (!existingProperty.isPresent()) {
        final String propertyType = propertyMap.get(LocalProperty.PROPERTY_TYPE_NAME);
        subjectEntity.addProperty(unprefixedPropertyName, propertyValue, propertyType);
      } else if (!existingProperty.get().value().equals(propertyValue)) {
        LOG.warn("Property values differ when merging synonymous (<owl:sameAs>) entities: {}", unprefixedPropertyName);
      }
    });
  }
}
