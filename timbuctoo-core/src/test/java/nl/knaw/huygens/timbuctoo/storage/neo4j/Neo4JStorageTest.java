package nl.knaw.huygens.timbuctoo.storage.neo4j;

import static nl.knaw.huygens.timbuctoo.storage.neo4j.NodeMockBuilder.aNode;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.SearchResultBuilder.aSearchResult;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.SearchResultBuilder.anEmptySearchResult;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.SubADomainEntityBuilder.aDomainEntity;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.neo4j.conversion.PropertyContainerConverterFactory;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

import test.model.projecta.SubADomainEntity;

public class Neo4JStorageTest {

  private static final Class<SubADomainEntity> DOMAIN_ENTITY_TYPE = SubADomainEntity.class;
  private static final Label DOMAIN_ENTITY_LABEL = DynamicLabel.label(TypeNames.getInternalName(DOMAIN_ENTITY_TYPE));
  private static final int FIRST_REVISION = 1;
  private static final String ID = "id";
  private static final int SECOND_REVISION = 2;
  private Neo4JStorage instance;
  private PropertyContainerConverterFactory propertyContainerConverterFactoryMock;
  private Transaction transactionMock = mock(Transaction.class);
  private GraphDatabaseService dbMock;

  @Before
  public void setup() throws Exception {
    setupEntityConverterFactory();
    setupDBTransaction();
    instance = new Neo4JStorage(dbMock, propertyContainerConverterFactoryMock);
  }

  private void setupDBTransaction() {
    transactionMock = mock(Transaction.class);
    dbMock = mock(GraphDatabaseService.class);
    when(dbMock.beginTx()).thenReturn(transactionMock);
  }

  @Test
  public void getDomainEntityRevisionReturnsTheDomainEntityWithTheRequestedRevision() throws Exception {
    Node nodeWithSameRevision = aNode().withRevision(FIRST_REVISION).withAPID().build();
    aSearchResult().forLabel(DOMAIN_ENTITY_LABEL).andId(ID).withNode(nodeWithSameRevision).foundInDB(dbMock);

    NodeConverter<SubADomainEntity> converter = propertyContainerConverterFactoryHasANodeConverterTypeFor(DOMAIN_ENTITY_TYPE);
    when(converter.convertToEntity(nodeWithSameRevision)).thenReturn(aDomainEntity().withAPid().build());

    // action
    SubADomainEntity entity = instance.getDomainEntityRevision(DOMAIN_ENTITY_TYPE, ID, FIRST_REVISION);

    // verify
    assertThat(entity, is(instanceOf(SubADomainEntity.class)));
    verify(converter).convertToEntity(nodeWithSameRevision);
    verify(transactionMock).success();
  }

  @Test
  public void getDomainEntityRevisionReturnsNullIfTheFoundEntityHasNoPID() throws Exception {
    Node nodeWithSameRevision = aNode().withRevision(FIRST_REVISION).build();
    aSearchResult().forLabel(DOMAIN_ENTITY_LABEL).andId(ID).withNode(nodeWithSameRevision).foundInDB(dbMock);

    NodeConverter<SubADomainEntity> nodeConverter = propertyContainerConverterFactoryHasANodeConverterTypeFor(DOMAIN_ENTITY_TYPE);
    when(nodeConverter.convertToEntity(nodeWithSameRevision)).thenReturn(aDomainEntity().build());

    // action
    SubADomainEntity actualEntity = instance.getDomainEntityRevision(DOMAIN_ENTITY_TYPE, ID, FIRST_REVISION);

    // verify
    assertThat(actualEntity, is(nullValue()));
    verify(transactionMock).success();
  }

  @Test
  public void getDomainEntityRevisionReturnsNullIfTheEntityCannotBeFound() throws Exception {
    // setup
    anEmptySearchResult().forLabel(DOMAIN_ENTITY_LABEL).andId(ID).foundInDB(dbMock);

    // action
    SubADomainEntity entity = instance.getDomainEntityRevision(DOMAIN_ENTITY_TYPE, ID, FIRST_REVISION);

    // verify
    assertThat(entity, is(nullValue()));
    verify(transactionMock).success();
  }

  @Test
  public void getDomainEntityRevisionReturnsNullIfTheRevisionCannotBeFound() throws Exception {
    // setup
    Node nodeWithDifferentRevision = aNode().withRevision(SECOND_REVISION).withAPID().build();
    aSearchResult().forLabel(DOMAIN_ENTITY_LABEL).andId(ID).withNode(nodeWithDifferentRevision).foundInDB(dbMock);

    // action
    SubADomainEntity entity = instance.getDomainEntityRevision(DOMAIN_ENTITY_TYPE, ID, FIRST_REVISION);

    // verify
    assertThat(entity, is(nullValue()));
    verify(transactionMock).success();
  }

  @Test(expected = StorageException.class)
  public void getDomainEntityRevisionThrowsAStorageExceptionIfTheEntityCannotBeInstantiated() throws Exception {
    // setup
    Node nodeWithSameRevision = aNode().withRevision(FIRST_REVISION).withAPID().build();
    aSearchResult().forLabel(DOMAIN_ENTITY_LABEL).andId(ID).withNode(nodeWithSameRevision).foundInDB(dbMock);

    NodeConverter<SubADomainEntity> converter = propertyContainerConverterFactoryHasANodeConverterTypeFor(DOMAIN_ENTITY_TYPE);
    when(converter.convertToEntity(nodeWithSameRevision)).thenThrow(new InstantiationException());

    try {
      // action
      instance.getDomainEntityRevision(DOMAIN_ENTITY_TYPE, ID, FIRST_REVISION);
    } finally {
      // verify
      verify(transactionMock).failure();
    }
  }

  @Test(expected = ConversionException.class)
  public void getDomainEntityRevisionThrowsAConversionExceptionIfTheEntityCannotBeConverted() throws Exception {
    Node nodeWithSameRevision = aNode().withRevision(FIRST_REVISION).withAPID().build();
    aSearchResult().forLabel(DOMAIN_ENTITY_LABEL).andId(ID).withNode(nodeWithSameRevision).foundInDB(dbMock);

    NodeConverter<SubADomainEntity> converter = propertyContainerConverterFactoryHasANodeConverterTypeFor(DOMAIN_ENTITY_TYPE);
    when(converter.convertToEntity(nodeWithSameRevision)).thenThrow(new ConversionException());

    try {
      // action
      instance.getDomainEntityRevision(DOMAIN_ENTITY_TYPE, ID, FIRST_REVISION);
    } finally {
      // verify
      verify(converter).convertToEntity(nodeWithSameRevision);
      verify(transactionMock).failure();
    }
  }

  private void setupEntityConverterFactory() throws Exception {
    propertyContainerConverterFactoryMock = mock(PropertyContainerConverterFactory.class);
  }

  protected <T extends Entity> NodeConverter<T> propertyContainerConverterFactoryHasANodeConverterTypeFor(Class<T> type) {
    @SuppressWarnings("unchecked")
    NodeConverter<T> nodeConverter = mock(NodeConverter.class);
    when(propertyContainerConverterFactoryMock.createForType(argThat(equalTo(type)))).thenReturn(nodeConverter);
    return nodeConverter;
  }

}
