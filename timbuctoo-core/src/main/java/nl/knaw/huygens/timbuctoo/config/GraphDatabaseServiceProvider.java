package nl.knaw.huygens.timbuctoo.config;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.rest.graphdb.CypherRestGraphDatabase;
import org.neo4j.rest.graphdb.RestAPI;
import org.neo4j.rest.graphdb.RestAPIImpl;

import com.google.inject.Provider;
import com.google.inject.Singleton;

@Singleton
public class GraphDatabaseServiceProvider implements Provider<GraphDatabaseService> {

  private static final String PASSWORD = "test123";
  private static final String USER = "neo4j";
  private static final String URI = "http://localhost:7474/db/data/";

  @Override
  public GraphDatabaseService get() {
    RestAPI restApi = new RestAPIImpl(URI, USER, PASSWORD);
    /* It is a wonder this works mapping a CypherRestGraphDatabase, that implements 
     * CypherRestGraphDatabase of Neo4J 2.1.7, to a CypherRestGraphDatabase of Neo4J 2.2.0.
     */
    CypherRestGraphDatabase graphDb = new CypherRestGraphDatabase(restApi);

    if (!graphDb.isAvailable(5000)) {
      throw new RuntimeException("Database is not available on: " + URI);
    }

    return graphDb;
  }
}
