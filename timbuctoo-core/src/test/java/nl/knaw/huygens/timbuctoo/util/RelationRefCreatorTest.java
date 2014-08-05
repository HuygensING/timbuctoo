package nl.knaw.huygens.timbuctoo.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import nl.knaw.huygens.timbuctoo.config.EntityMapper;
import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Reference;
import nl.knaw.huygens.timbuctoo.model.RelationRef;
import nl.knaw.huygens.timbuctoo.storage.Storage;
import nl.knaw.huygens.timbuctoo.storage.StorageException;

import org.junit.Before;
import org.junit.Test;

public class RelationRefCreatorTest {
  private static final Class<DomainEntity> MAPPED_TYPE = DomainEntity.class;
  private static final Class<DomainEntity> TYPE = DomainEntity.class;
  private static final String EXTERNAL_NAME = "xName";
  private static final String REFERENCE_TYPE_STRING = "type";
  private static final String REFERENCE_ID = "id";
  private static final int REVISION = 0;
  private static final boolean ACCEPTED = true;
  private static final String RELATION_ID = "relationId";
  private TypeRegistry registryMock;
  private Storage storageMock;
  private EntityMapper mapperMock;
  private RelationRefCreator relationRefCreator;
  private Reference reference;

  @Before
  public void setUp() {
    registryMock = mock(TypeRegistry.class);
    storageMock = mock(Storage.class);
    relationRefCreator = new RelationRefCreator(registryMock, storageMock);
    setupRegistryMock();
    setupMapperMock();
    reference = createReference();
  }

  @Test
  public void newRelationRefCreatesANewRelationRef() throws StorageException {
    // setup
    RelationRef expectedRef = createExpectedRelation(RELATION_ID, ACCEPTED, REVISION, REFERENCE_ID, MAPPED_TYPE, EXTERNAL_NAME);
    DomainEntity referencedEntity = createReferencedEntity();

    doReturn(referencedEntity).when(storageMock).getItem(MAPPED_TYPE, REFERENCE_ID);

    // action
    RelationRef actualRef = relationRefCreator.newRelationRef(mapperMock, reference, RELATION_ID, ACCEPTED, REVISION);

    // verify
    verify(storageMock).getItem(MAPPED_TYPE, REFERENCE_ID);
    assertThat(actualRef, equalTo(expectedRef));
  }

  @Test(expected = StorageException.class)
  public void newRelationRefThrowsAnExceptionWhenStorageThrowsAnException() throws StorageException {
    // setup
    doThrow(StorageException.class).when(storageMock).getItem(MAPPED_TYPE, REFERENCE_ID);

    relationRefCreator.newRelationRef(mapperMock, reference, RELATION_ID, ACCEPTED, REVISION);
  }

  private void setupMapperMock() {
    mapperMock = mock(EntityMapper.class);
    doReturn(MAPPED_TYPE).when(mapperMock).map(TYPE);
  }

  private void setupRegistryMock() {
    doReturn(TYPE).when(registryMock).getDomainEntityType(REFERENCE_TYPE_STRING);
    when(registryMock.getXNameForIName(anyString())).thenReturn(EXTERNAL_NAME);
  }

  private DomainEntity createReferencedEntity() {
    DomainEntity referencedEntity = new DomainEntity() {

      @Override
      public String getDisplayName() {
        return null;
      }
    };
    referencedEntity.setId(REFERENCE_ID);
    return referencedEntity;
  }

  private Reference createReference() {
    Reference reference = new Reference();
    reference.setId(REFERENCE_ID);
    reference.setType(REFERENCE_TYPE_STRING);
    return reference;
  }

  private RelationRef createExpectedRelation(String relationId, boolean accepted, int rev, String referenceId, Class<? extends DomainEntity> mappedType, String externalName) {
    return new RelationRef(TypeNames.getInternalName(mappedType), externalName, referenceId, null, relationId, accepted, rev);
  }

}
