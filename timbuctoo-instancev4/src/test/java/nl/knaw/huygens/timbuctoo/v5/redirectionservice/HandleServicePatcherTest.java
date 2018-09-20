package nl.knaw.huygens.timbuctoo.v5.redirectionservice;

import nl.knaw.huygens.timbuctoo.v5.filestorage.exceptions.LogStorageFailedException;
import nl.knaw.huygens.timbuctoo.v5.rdfio.RdfPatchSerializer;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class HandleServicePatcherTest {
  @Test
  public void addsAQuadToTheSaver() throws LogStorageFailedException {
    HandleServicePatcher handleServicePatcher = new HandleServicePatcher("subject", "predicate", "object", "String");

    RdfPatchSerializer saver = mock(RdfPatchSerializer.class);
    handleServicePatcher.sendQuads(saver, null, null);

    verify(saver).onQuad("subject", "predicate", "object", "String", null, null);
  }

}
