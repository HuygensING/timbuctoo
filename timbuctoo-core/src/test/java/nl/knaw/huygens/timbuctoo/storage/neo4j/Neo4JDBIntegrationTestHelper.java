package nl.knaw.huygens.timbuctoo.storage.neo4j;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.ModelException;
import nl.knaw.huygens.timbuctoo.storage.DBIntegrationTestHelper;
import nl.knaw.huygens.timbuctoo.storage.Storage;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.test.TestGraphDatabaseFactory;

public class Neo4JDBIntegrationTestHelper implements DBIntegrationTestHelper {

  private GraphDatabaseService db;
  private EntityConverterFactory objectWrapperFactory;
  private IdGenerator idGenerator;
  private EntityInstantiator entityInstantiator;
  private RelationConverter relationConverter;

  @Override
  public void startCleanDB() throws Exception {
    idGenerator = new IdGenerator();
    db = new TestGraphDatabaseFactory().newImpermanentDatabase();
    PropertyBusinessRules propertyBusinessRules = new PropertyBusinessRules();
    FieldConverterFactory fieldWrapperFactory = new FieldConverterFactory(propertyBusinessRules);
    objectWrapperFactory = new EntityConverterFactory(fieldWrapperFactory);
    entityInstantiator = new EntityInstantiator();
    relationConverter = new RelationConverter();
  }

  @Override
  public void stopDB() {
    db.shutdown();
  }

  @Override
  public Storage createStorage(TypeRegistry typeRegistry) throws ModelException {

    return new Neo4JStorage(db, objectWrapperFactory, entityInstantiator, idGenerator, typeRegistry, relationConverter);
  }

}
