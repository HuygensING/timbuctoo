package nl.knaw.huygens.timbuctoo.storage.graph.neo4j;

import nl.knaw.huygens.timbuctoo.model.Relation;

import org.neo4j.graphdb.Relationship;

public interface RelationshipConverter<T extends Relation> extends PropertyContainerConverter<Relationship, T> {

}