package nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers;

import nl.knaw.huygens.timbuctoo.v5.datastores.exceptions.DataStoreCreationException;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.DataFetcherFactory;

public interface DataFetcherFactoryFactory {
  DataFetcherFactory createDataFetcherFactory(String userId, String dataSetId) throws DataStoreCreationException;
}
