package nl.knaw.huygens.timbuctoo.storage.neo4j;

import static nl.knaw.huygens.timbuctoo.model.Entity.ID_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.model.Entity.REVISION_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.model.Relation.SOURCE_ID;
import static nl.knaw.huygens.timbuctoo.model.Relation.TARGET_ID;

import java.util.List;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.IndexHits;

import com.google.common.collect.Lists;

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

      IndexHits<Relationship> indexHits = db.index().forRelationships(propertyName).get(propertyName, propertyValue);

      transaction.success();
      return Lists.newArrayList(indexHits.iterator());
    }
  }

  public boolean isLatestVersion(Relationship relationship) {
    try (Transaction transaction = db.beginTx()) {
      IndexHits<Relationship> indexHits = db.index().forRelationships(ID_PROPERTY_NAME).get(ID_PROPERTY_NAME, relationship.getProperty(ID_PROPERTY_NAME));

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

  public static int getRevisionProperty(PropertyContainer propertyContainer) {
    return propertyContainer != null && propertyContainer.hasProperty(REVISION_PROPERTY_NAME) ? //
    (int) propertyContainer.getProperty(REVISION_PROPERTY_NAME)
        : 0;
  }

}
