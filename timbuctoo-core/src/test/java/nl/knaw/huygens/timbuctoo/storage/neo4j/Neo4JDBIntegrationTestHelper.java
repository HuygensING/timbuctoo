package nl.knaw.huygens.timbuctoo.storage.neo4j;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.ModelException;
import nl.knaw.huygens.timbuctoo.storage.DBIntegrationTestHelper;
import nl.knaw.huygens.timbuctoo.storage.Storage;
import nl.knaw.huygens.timbuctoo.storage.neo4j.conversion.PropertyContainerConverterFactory;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.test.TestGraphDatabaseFactory;

public class Neo4JDBIntegrationTestHelper implements DBIntegrationTestHelper {

  private GraphDatabaseService db;
  private PropertyContainerConverterFactory propertyContainerConverterFactory;
  private IdGenerator idGenerator;
  private EntityInstantiator entityInstantiator;

  @Override
  public void startCleanDB() throws Exception {
    idGenerator = new IdGenerator();
    db = new TestGraphDatabaseFactory().newImpermanentDatabase();
    entityInstantiator = new EntityInstantiator();
  }

  @Override
  public void stopDB() {
    db.shutdown();
  }

  @Override
  public Storage createStorage(TypeRegistry typeRegistry) throws ModelException {
    propertyContainerConverterFactory = new PropertyContainerConverterFactory(typeRegistry);
    return new Neo4JStorage(db, propertyContainerConverterFactory, entityInstantiator, idGenerator, typeRegistry, new NodeDuplicator(db), new RelationshipDuplicator(db));
  }

}
