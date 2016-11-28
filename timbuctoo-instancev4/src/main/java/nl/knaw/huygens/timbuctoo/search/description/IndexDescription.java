package nl.knaw.huygens.timbuctoo.search.description;


import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.neo4j.graphdb.GraphDatabaseService;

public interface IndexDescription {

  void addToFulltextIndex(Vertex vertex, GraphDatabaseService graphDatabase);

  void removeFromFulltextIndex(Vertex vertex, GraphDatabaseService graphDatabase);
}
