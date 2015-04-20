package nl.knaw.huygens.timbuctoo.storage.graph.neo4j;

import static nl.knaw.huygens.timbuctoo.model.Entity.ID_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.model.Relation.SOURCE_ID;
import static nl.knaw.huygens.timbuctoo.model.Relation.TARGET_ID;
import static nl.knaw.huygens.timbuctoo.model.Relation.TYPE_ID;
import static nl.knaw.huygens.timbuctoo.storage.graph.neo4j.PropertyContainerHelper.getRevisionProperty;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import nl.knaw.huygens.timbuctoo.model.Relation;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.graphdb.index.RelationshipIndex;

import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

class RelationshipIndexes {

  private final List<String> indexedProperties;
  private final GraphDatabaseService db;

  public RelationshipIndexes(GraphDatabaseService db) {
    this(db, Lists.newArrayList(SOURCE_ID, TARGET_ID, ID_PROPERTY_NAME));
  }

  public RelationshipIndexes(GraphDatabaseService db, List<String> indexedProperties) {
    this.db = db;
    this.indexedProperties = indexedProperties;
  }

  public boolean containsIndexFor(String propertyName) {
    return indexedProperties.contains(propertyName);
  }

  public List<Relationship> getRelationshipsBy(String propertyName, String propertyValue) {
    try (Transaction transaction = db.beginTx()) {

      IndexHits<Relationship> indexHits = getFromIndex(propertyName, propertyValue);

      transaction.success();
      return Lists.newArrayList(indexHits.iterator());
    }
  }

  private IndexHits<Relationship> getFromIndex(String propertyName, Object propertyValue) {
    return getRelationIndexshipFor(propertyName).get(propertyName, propertyValue);
  }

  public boolean isLatestVersion(Relationship relationship) {
    try (Transaction transaction = db.beginTx()) {
      IndexHits<Relationship> indexHits = getFromIndex(ID_PROPERTY_NAME, relationship.getProperty(ID_PROPERTY_NAME));

      int revisionToCheck = getRevisionProperty(relationship);

      boolean isLatest = true;
      for (ResourceIterator<Relationship> iterator = indexHits.iterator(); iterator.hasNext();) {
        Relationship next = iterator.next();

        int revisionProperty = getRevisionProperty(next);
        if (revisionProperty > revisionToCheck) {
          isLatest = false;
          break;
        }
      }
      transaction.success();
      return isLatest;
    }
  }

  private RelationshipIndex getRelationIndexshipFor(String propertyName) {
    return db.index().forRelationships(propertyName);
  }

  public void indexByField(Relationship relationship, String propertyName, Object value) {
    if (!containsIndexFor(propertyName)) {
      throw PropertyNotIndexedException.propertyHasNoIndex(relationship.getClass(), propertyName);
    }

    addToIndex(relationship, propertyName, value);
  }

  private void addToIndex(Relationship relationship, String propertyName, Object value) {
    getRelationIndexshipFor(propertyName).add(relationship, propertyName, value);
  }

  public Relationship getLatestRelationshipById(String id) {
    try (Transaction transaction = db.beginTx()) {
      ResourceIterator<Relationship> iterator = getFromIndexById(id);

      Relationship relationshipWithHighestRevision = getLatest(iterator);

      transaction.success();
      return relationshipWithHighestRevision;
    }
  }

  private Relationship getLatest(Iterator<Relationship> iterator) {
    Relationship relationshipWithHighestRevision = null;
    for (; iterator.hasNext();) {
      Relationship next = iterator.next();

      if (getRevisionProperty(next) > getRevisionProperty(relationshipWithHighestRevision)) {
        relationshipWithHighestRevision = next;
      }
    }
    return relationshipWithHighestRevision;
  }

  private ResourceIterator<Relationship> getFromIndexById(String id) {
    IndexHits<Relationship> indexHits = getFromIndex(ID_PROPERTY_NAME, id);

    ResourceIterator<Relationship> iterator = indexHits.iterator();
    return iterator;
  }

  public <T extends Relation> Relationship getRelationshipWithRevision(String id, int revision) {
    try (Transaction transaction = db.beginTx()) {
      ResourceIterator<Relationship> iterator = getFromIndexById(id);

      Relationship relationshipWithRevsion = null;

      for (; iterator.hasNext();) {
        Relationship next = iterator.next();
        if (getRevisionProperty(next) == revision) {

          relationshipWithRevsion = next;
          break;
        }
      }
      transaction.success();
      return relationshipWithRevsion;
    }
  }

  public Relationship findLatestRelationshipFor(String sourceId, final String targetId, final String relationTypeId) {
    try (Transaction transaction = db.beginTx()) {
      IndexHits<Relationship> foundInIndex = getFromIndex(SOURCE_ID, sourceId);

      Set<Relationship> foundRelationships = Sets.newHashSet(foundInIndex.iterator());

      Set<Relationship> matching = Sets.filter(foundRelationships, new SameTargetAndType(relationTypeId, targetId));

      Relationship latest = getLatest(matching.iterator());

      transaction.success();
      return latest;
    }
  }

  private static final class SameTargetAndType implements Predicate<Relationship> {
    private final String relationTypeId;
    private final String targetId;

    private SameTargetAndType(String relationTypeId, String targetId) {
      this.relationTypeId = relationTypeId;
      this.targetId = targetId;
    }

    @Override
    public boolean apply(Relationship input) {
      return hasSameTargetId(input, targetId) && hasSameTypeId(input, relationTypeId);
    }

    private boolean hasSameTypeId(Relationship input, String relationTypeId) {
      return hasPropertyWithValue(input, TYPE_ID, relationTypeId);
    }

    private boolean hasPropertyWithValue(PropertyContainer input, String key, String value) {
      return input.hasProperty(key) && Objects.equal(value, input.getProperty(key));
    }

    private boolean hasSameTargetId(Relationship input, final String targetId) {
      return hasPropertyWithValue(input.getEndNode(), ID_PROPERTY_NAME, targetId);
    }
  }

}
