package nl.knaw.huygens.timbuctoo.dataset;

import nl.knaw.huygens.timbuctoo.dataset.exceptions.DataStoreCreationException;
import nl.knaw.huygens.timbuctoo.datastores.defaultresourcesstore.DefaultResourcesStore;
import nl.knaw.huygens.timbuctoo.datastores.graphstore.GraphStore;
import nl.knaw.huygens.timbuctoo.datastores.oldsubjecttypesstore.OldSubjectTypesStore;
import nl.knaw.huygens.timbuctoo.datastores.patchversionstore.PatchVersionStore;
import nl.knaw.huygens.timbuctoo.datastores.prefixstore.TypeNameStore;
import nl.knaw.huygens.timbuctoo.datastores.quadstore.QuadStore;
import nl.knaw.huygens.timbuctoo.datastores.schemastore.SchemaStore;
import nl.knaw.huygens.timbuctoo.datastores.updatedperpatchstore.UpdatedPerPatchStore;

public interface StoreProvider {
  QuadStore createQuadStore() throws DataStoreCreationException;

  GraphStore createGraphStore() throws DataStoreCreationException;

  DefaultResourcesStore createDefaultResourcesStore(ImportStatus importStatus) throws DataStoreCreationException;

  TypeNameStore createTypeNameStore(String rdfPrefix) throws DataStoreCreationException;

  SchemaStore createSchemaStore(ImportStatus importStatus) throws DataStoreCreationException;

  UpdatedPerPatchStore createUpdatePerPatchStore() throws DataStoreCreationException;

  PatchVersionStore createPatchVersionStore() throws DataStoreCreationException;

  OldSubjectTypesStore createOldSubjectTypesStore() throws DataStoreCreationException;
}