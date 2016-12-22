package nl.knaw.huygens.timbuctoo.core;

import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.core.dto.CreateCollection;
import nl.knaw.huygens.timbuctoo.core.dto.dataset.Collection;
import nl.knaw.huygens.timbuctoo.core.dto.rdf.CreateProperty;
import nl.knaw.huygens.timbuctoo.core.dto.rdf.PredicateInUse;
import nl.knaw.huygens.timbuctoo.core.rdf.PropertyFactory;
import nl.knaw.huygens.timbuctoo.model.vre.Vre;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import nl.knaw.huygens.timbuctoo.model.vre.vres.VresBuilder;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import java.util.ArrayList;
import java.util.List;

import static nl.knaw.huygens.timbuctoo.core.RdfImportSessionStubs.rdfImportSessionWithErrorReporter;
import static nl.knaw.huygens.timbuctoo.core.RdfImportSessionStubs.rdfImportSessionWithPropertyFactory;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;

public class RdfImportSessionTest {

  public static final String VRE_NAME = "VRE";
  private DataStoreOperations dataStoreOperations;
  private Vre vre;

  @Before
  public void setUp() throws Exception {
    dataStoreOperations = mock(DataStoreOperations.class);
    Vres vres = new VresBuilder().withVre(VRE_NAME, "prefix", vre -> vre
      .withCollection("prefixanyCols")
    ).build();
    vre = vres.getVre(VRE_NAME);
    given(dataStoreOperations.ensureVreExists(VRE_NAME)).willReturn(vre);
    given(dataStoreOperations.loadVres()).willReturn(vres);
  }

  @Test
  public void cleanImportSessionEnsuresTheVreIsAvailable() {
    RdfImportSession.cleanImportSession(VRE_NAME, dataStoreOperations);

    verify(dataStoreOperations).ensureVreExists(VRE_NAME);
  }

  @Test
  public void cleanImportSessionEnsuresTheVreHasADefaultCollection() {
    RdfImportSession.cleanImportSession(VRE_NAME, dataStoreOperations);

    verify(dataStoreOperations).addCollectionToVre(
      argThat(hasProperty("vreName", equalTo(VRE_NAME))),
      eq(CreateCollection.defaultCollection())
    );
  }

  @Test
  public void cleanImportSessionEnsuresNoDataFromAPreviousSessionIsLeftBeforeTheDefaultCollectionIsAdded() {
    RdfImportSession.cleanImportSession(VRE_NAME, dataStoreOperations);

    InOrder inOrder = inOrder(dataStoreOperations);
    inOrder.verify(dataStoreOperations).clearMappingErrors(
      argThat(hasProperty("vreName", equalTo(VRE_NAME)))
    );
    inOrder.verify(dataStoreOperations).removeCollectionsAndEntities(
      argThat(hasProperty("vreName", equalTo(VRE_NAME)))
    );
    inOrder.verify(dataStoreOperations).addCollectionToVre(
      argThat(hasProperty("vreName", equalTo(VRE_NAME))),
      eq(CreateCollection.defaultCollection())
    );
  }

  @Test
  public void closeDoesNothingWhenRollbackIsCalled() {
    RdfImportErrorReporter errorReporter = mock(RdfImportErrorReporter.class);
    RdfImportSession instance = rdfImportSessionWithErrorReporter(VRE_NAME, dataStoreOperations, errorReporter);
    instance.rollback();

    instance.close();

    verifyZeroInteractions(errorReporter);
    verify(dataStoreOperations, never()).getEntitiesWithUnknownType(any(Vre.class));
  }

  @Test
  public void closeReportsEntitiesInTheDefaultCollectionWhenCommitIsCalled() {
    RdfImportErrorReporter errorReporter = mock(RdfImportErrorReporter.class);
    String uri1 = "http://example.com/entity1";
    String uri2 = "http://example.com/entity2";
    given(dataStoreOperations.getEntitiesWithUnknownType(vre)).willReturn(Lists.newArrayList(uri1, uri2));
    RdfImportSession instance = rdfImportSessionWithErrorReporter(VRE_NAME, dataStoreOperations, errorReporter);
    instance.commit();

    instance.close();

    verify(errorReporter).entityTypeUnknown(uri1);
    verify(errorReporter).entityTypeUnknown(uri2);
  }

  @Test
  public void closeAddsAdministrativePropertiesToTheCreatedEntities() {
    RdfImportSession instance = RdfImportSession.cleanImportSession(VRE_NAME, dataStoreOperations);
    instance.commit();
    instance.close();

    verify(dataStoreOperations).finishEntities(eq(vre), any(EntityFinisherHelper.class));
  }

  @Test
  public void closeCreatesPropertyDescriptionsForEachPredicate() {
    List<PredicateInUse> predicates = Lists.newArrayList();
    given(dataStoreOperations.getPredicatesFor(any(Collection.class))).willReturn(predicates);
    PropertyFactory propertyFactory = mock(PropertyFactory.class);
    ArrayList<CreateProperty> createProperties = Lists.newArrayList();
    given(propertyFactory.fromPredicates(predicates)).willReturn(createProperties);
    RdfImportSession instance = rdfImportSessionWithPropertyFactory(VRE_NAME, dataStoreOperations, propertyFactory);
    instance.commit();

    instance.close();

    verify(propertyFactory).fromPredicates(predicates);
    verify(dataStoreOperations).addPropertiesToCollection(any(Collection.class), eq(createProperties));
  }

}
