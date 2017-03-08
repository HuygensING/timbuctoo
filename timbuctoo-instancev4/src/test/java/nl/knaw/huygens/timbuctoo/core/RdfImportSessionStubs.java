package nl.knaw.huygens.timbuctoo.core;

import nl.knaw.huygens.timbuctoo.core.rdf.PropertyFactory;

import static org.mockito.Mockito.mock;

public class RdfImportSessionStubs {
  public static RdfImportSession rdfImportSessionWithErrorReporter(String vreName,
                                                                   DataStoreOperations dataStoreOperations,
                                                                   RdfImportErrorReporter errorReporter) {
    return RdfImportSession.cleanImportSession(
      vreName,
      dataStoreOperations,
      errorReporter,
      mock(PropertyFactory.class),
      new EntityFinisherHelper()
    );
  }

  public static RdfImportSession rdfImportSessionWithPropertyFactory(String vreName,
                                                                     DataStoreOperations dataStoreOperations,
                                                                     PropertyFactory propertyFactory) {
    return RdfImportSession.cleanImportSession(
      vreName,
      dataStoreOperations,
      mock(RdfImportErrorReporter.class),
      propertyFactory,
      new EntityFinisherHelper()
    );
  }
}
