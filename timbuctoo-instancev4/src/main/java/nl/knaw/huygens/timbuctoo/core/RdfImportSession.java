package nl.knaw.huygens.timbuctoo.core;

import nl.knaw.huygens.timbuctoo.core.dto.CreateCollection;
import nl.knaw.huygens.timbuctoo.core.dto.rdf.PredicateInUse;
import nl.knaw.huygens.timbuctoo.core.dto.rdf.RdfProperty;
import nl.knaw.huygens.timbuctoo.core.rdf.PropertyFactory;
import nl.knaw.huygens.timbuctoo.model.vre.Vre;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class RdfImportSession {

  public static final Logger LOG = LoggerFactory.getLogger(RdfImportSession.class);
  private final DataStoreOperations dataStoreOperations;
  private final Vre vre;
  private final RdfImportErrorReporter errorReporter;
  private final PropertyFactory propertyFactory;
  private final EntityFinisherHelper entityFinisherHelper;
  private SessionState sessionState;

  private RdfImportSession(DataStoreOperations dataStoreOperations, Vre vre, RdfImportErrorReporter errorReporter,
                           PropertyFactory propertyFactory) {
    this.dataStoreOperations = dataStoreOperations;
    this.vre = vre;
    this.errorReporter = errorReporter;
    this.propertyFactory = propertyFactory;
    entityFinisherHelper = new EntityFinisherHelper();
  }

  public static RdfImportSession cleanImportSession(String vreName, DataStoreOperations dataStoreOperations) {
    LogRdfImportErrorReporter errorReporter = new LogRdfImportErrorReporter();
    return cleanImportSession(vreName, dataStoreOperations, errorReporter, new PropertyFactory(errorReporter));
  }

  static RdfImportSession cleanImportSession(String vreName, DataStoreOperations dataStoreOperations,
                                             RdfImportErrorReporter errorReporter,
                                             PropertyFactory propertyFactory) {
    Vre vre = dataStoreOperations.ensureVreExists(vreName);
    dataStoreOperations.clearMappingErrors(vre);
    dataStoreOperations.removeCollectionsAndEntities(vre);
    dataStoreOperations.addCollectionToVre(vre, CreateCollection.defaultCollection(vre.getVreName()));
    dataStoreOperations.addPredicateValueTypeVertexToVre(vre);
    Vre reloadedVre = dataStoreOperations.loadVres().getVre(vre.getVreName());

    return new RdfImportSession(dataStoreOperations, reloadedVre, errorReporter, propertyFactory);
  }

  public void close() {
    Vre reloadedVre = dataStoreOperations.loadVres().getVre(this.vre.getVreName());
    if (sessionState == SessionState.SUCCESS) {
      dataStoreOperations.getEntitiesWithUnknownType(reloadedVre)
                         .forEach(entityUri -> errorReporter.entityTypeUnknown(entityUri));
      dataStoreOperations.finishEntities(reloadedVre, entityFinisherHelper);
      reloadedVre.getCollections().values().forEach(col -> {
        List<PredicateInUse> predicatesFor = dataStoreOperations.getPredicatesFor(col);
        dataStoreOperations.addPropertiesToCollection(col, propertyFactory.fromPredicates(predicatesFor));
      });
    }
  }

  public void commit() {
    sessionState = SessionState.SUCCESS;
  }

  public void rollback() {
    sessionState = SessionState.ROLLBACK;
  }

  public void assertProperty(String rdfUri, RdfProperty property) {
    dataStoreOperations.assertProperty(vre, rdfUri, property);
  }

  public void retractProperty(String rdfUri, RdfProperty property) {
    dataStoreOperations.retractProperty(vre, rdfUri, property);
  }

  public RdfImportErrorReporter getErrorReporter() {
    return errorReporter;
  }

  enum SessionState {
    UNKNOWN,
    SUCCESS,
    ROLLBACK
  }

  private static class LogRdfImportErrorReporter implements RdfImportErrorReporter {
    @Override
    public void entityTypeUnknown(String rdfUri) {
      LOG.error("Entity with URI '{}' has no explicit type definition", rdfUri);
    }

    @Override
    public void entityHasWrongTypeForProperty(String entityRdfUri, String predicateUri, String expectedTypeUri,
                                              String actualTypeUri) {
      LOG.error("Entity with URI '{}' has wrong value type for predicate '{}'. Expected type '{}', but was '{}'.",
        entityRdfUri, predicateUri, expectedTypeUri, actualTypeUri);
    }

    @Override
    public void multipleRdfTypes(String subject, String object) {
      LOG.warn("Adding multiple rdf:types to a subject is not supported <{}> <rdf:type> <{}>.",
        subject,
        object
      );
    }
  }
}
