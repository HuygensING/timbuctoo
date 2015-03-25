package nl.knaw.huygens.timbuctoo.storage.neo4j;

import org.neo4j.graphdb.RelationshipType;

enum SystemRelationshipType implements RelationshipType {
  VERSION_OF;
}
