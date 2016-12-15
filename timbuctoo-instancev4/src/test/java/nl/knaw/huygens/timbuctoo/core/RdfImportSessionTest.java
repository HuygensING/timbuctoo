package nl.knaw.huygens.timbuctoo.core;

import nl.knaw.huygens.timbuctoo.core.dto.CreateCollection;
import nl.knaw.huygens.timbuctoo.model.vre.Vre;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import nl.knaw.huygens.timbuctoo.model.vre.vres.VresBuilder;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;

public class RdfImportSessionTest {

  public static final String VRE_NAME = "VRE";

  @Test
  public void cleanImportSessionEnsuresTheVreIsAvailable() {
    DataStoreOperations dataStoreOperations = mock(DataStoreOperations.class);

    RdfImportSession.cleanImportSession(VRE_NAME, dataStoreOperations);

    verify(dataStoreOperations).ensureVreExists(VRE_NAME);
  }

  @Test
  public void cleanImportSessionEnsuresTheVreHasADefaultCollection() {
    Vres vres = new VresBuilder().withVre(VRE_NAME, "prefix").build();
    Vre vre = vres.getVre(VRE_NAME);
    DataStoreOperations dataStoreOperations = mock(DataStoreOperations.class);
    given(dataStoreOperations.ensureVreExists(VRE_NAME)).willReturn(vre);

    RdfImportSession.cleanImportSession(VRE_NAME, dataStoreOperations);

    verify(dataStoreOperations).addCollectionToVre(
      argThat(hasProperty("vreName", equalTo(VRE_NAME))),
      eq(CreateCollection.defaultCollection())
    );
  }

  @Test
  public void cleanImportSessionEnsuresNoDataFromAPreviousSessionIsLeft() {
    Vres vres = new VresBuilder().withVre(VRE_NAME, "prefix").build();
    Vre vre = vres.getVre(VRE_NAME);
    DataStoreOperations dataStoreOperations = mock(DataStoreOperations.class);
    given(dataStoreOperations.ensureVreExists(VRE_NAME)).willReturn(vre);

    RdfImportSession.cleanImportSession(VRE_NAME, dataStoreOperations);

    verify(dataStoreOperations).clearMappingErrors(
      argThat(hasProperty("vreName", equalTo(VRE_NAME)))
    );
    verify(dataStoreOperations).removeCollectionsAndEntities(
      argThat(hasProperty("vreName", equalTo(VRE_NAME)))
    );
  }
}
