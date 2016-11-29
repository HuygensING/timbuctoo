package nl.knaw.huygens.timbuctoo.rdf.tripleprocessor;

import nl.knaw.huygens.timbuctoo.rdf.Database;
import nl.knaw.huygens.timbuctoo.rdf.Entity;
import nl.knaw.huygens.timbuctoo.rdf.Relation;
import nl.knaw.huygens.timbuctoo.rdf.RelationType;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.slf4j.LoggerFactory.getLogger;

class RelationTripleProcessor {
  private static final Logger LOG = getLogger(RelationTripleProcessor.class);
  private final Database database;
  private final Map<String, String> mappings;

  public RelationTripleProcessor(Database database) {
    this(database, new HashMap<>());
  }

  public RelationTripleProcessor(Database database, Map<String, String> mappings) {
    this.database = database;
    this.mappings = mappings;
  }

  public void process(String vreName, boolean isAssertion, Triple triple) {
    final Entity subject = database.findOrCreateEntity(vreName, triple.getSubject());
    Node predicate = triple.getPredicate();
    final RelationType relationType = database.findOrCreateRelationType(predicate);
    final Entity object;
    if (mappings.containsKey(predicate.getURI())) {
      Optional<Entity> entity = database.findEntity(mappings.get(predicate.getURI()), triple.getObject());
      if (entity.isPresent()) {
        object = entity.get();
      } else {
        LOG.error("Could not find " + triple.getObject() + " in " + vreName);
        return;
      }
    } else {
      object = database.findOrCreateEntity(vreName, triple.getObject());
    }

    if (isAssertion) {
      if (relationType.isInverted()) {
        final Relation relation = object.addRelation(relationType, subject);
        relation.setCommonVreProperties(vreName);
      } else {
        final Relation relation = subject.addRelation(relationType, object);
        relation.setCommonVreProperties(vreName);
      }
    } else {
      subject.removeRelation(relationType, object);
    }
  }
}
