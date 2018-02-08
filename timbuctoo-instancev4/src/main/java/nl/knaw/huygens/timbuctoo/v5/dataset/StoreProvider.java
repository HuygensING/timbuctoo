package nl.knaw.huygens.timbuctoo.v5.dataset;

import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.DataStoreCreationException;
import nl.knaw.huygens.timbuctoo.v5.datastores.prefixstore.TypeNameStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.QuadStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.rmldatasource.RmlDataSourceStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.schemastore.SchemaStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.truepatch.TruePatchStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.updatedperpatchstore.UpdatedPerPatchStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.versionstore.VersionStore;

public interface StoreProvider {
  QuadStore createTripleStore()
    throws DataStoreCreationException;

  TypeNameStore createTypeNameStore(String rdfPrefix)
      throws DataStoreCreationException;

  SchemaStore createSchemaStore(ImportStatus importStatus)
        throws DataStoreCreationException;

  TruePatchStore createTruePatchStore() throws DataStoreCreationException;

  UpdatedPerPatchStore createUpdatePerPatchStore()
          throws DataStoreCreationException;

  RmlDataSourceStore createRmlDataSourceStore(ImportStatus importStatus)
            throws DataStoreCreationException;

  VersionStore createVersionStore() throws DataStoreCreationException;
}
