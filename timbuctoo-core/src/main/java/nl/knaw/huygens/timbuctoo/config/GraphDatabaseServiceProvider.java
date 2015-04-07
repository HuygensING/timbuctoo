package nl.knaw.huygens.timbuctoo.config;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.rest.graphdb.CypherRestGraphDatabase;
import org.neo4j.rest.graphdb.RestAPI;
import org.neo4j.rest.graphdb.RestAPIImpl;

import com.google.inject.Provider;
import com.google.inject.Singleton;

@Singleton
public class GraphDatabaseServiceProvider implements Provider<GraphDatabaseService> {

  @Override
  public GraphDatabaseService get() {
    RestAPI restApi = new RestAPIImpl("http://localhost:7474", "neo4j", "test123");
    /* It is a wonder this works mapping a CypherRestGraphDatabase, that implements 
     * CypherRestGraphDatabase of Neo4J 2.1.7, to a CypherRestGraphDatabase of Neo4J 2.2.0.
     */
    CypherRestGraphDatabase graphDb = new CypherRestGraphDatabase(restApi);
    return graphDb;
  }

}
