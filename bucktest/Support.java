public class Support {
  public org.neo4j.graphdb.GraphDatabaseService foo() {
    return new org.neo4j.test.TestGraphDatabaseFactory().newImpermanentDatabase();
  }
}
