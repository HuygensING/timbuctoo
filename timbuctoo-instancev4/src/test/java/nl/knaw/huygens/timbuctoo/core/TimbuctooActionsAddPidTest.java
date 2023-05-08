package nl.knaw.huygens.timbuctoo.core;

import nl.knaw.huygens.timbuctoo.core.dto.ImmutableEntityLookup;
import nl.knaw.huygens.timbuctoo.core.dto.dataset.Collection;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.UUID;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class TimbuctooActionsAddPidTest {
  private static final int REV = 1;
  private TimbuctooActions instance;
  private DataStoreOperations dataStoreOperations;
  private Collection collection;
  private UUID id;
  private URI pidUri;

  @BeforeEach
  public void setup() throws Exception {
    dataStoreOperations = mock(DataStoreOperations.class);
    instance = new TimbuctooActions(null, null, null, (coll, id, rev) -> URI.create("http://example.org/persistent"),
      dataStoreOperations, null);
    id = UUID.randomUUID();
    pidUri = new URI("http://example.com/pid");
  }

  @Test
  public void addPidLetsTheDataStoreOperationsAddAPidToTheEntity() throws Exception {
    instance.addPid(
      pidUri,
      ImmutableEntityLookup.builder().collection("someCollection").timId(id).rev(REV).build()
    );

    verify(dataStoreOperations).addPid(id, REV, pidUri);
  }

  @Test
  public void addPidThrowsANotFoundExceptionWhenTheEntityCannotBeFound() throws Exception {
    Assertions.assertThrows(NotFoundException.class, () -> {
      doThrow(new NotFoundException()).when(dataStoreOperations).addPid(id, REV, pidUri);

      instance.addPid(
          pidUri,
          ImmutableEntityLookup.builder().collection("someCollection").timId(id).rev(REV).build()
      );
    });
  }
}
