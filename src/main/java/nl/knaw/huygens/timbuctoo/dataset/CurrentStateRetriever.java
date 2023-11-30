package nl.knaw.huygens.timbuctoo.dataset;

import nl.knaw.huygens.timbuctoo.datastores.implementations.bdb.BdbQuadStore;
import nl.knaw.huygens.timbuctoo.datastores.quadstore.dto.CursorQuad;
import nl.knaw.huygens.timbuctoo.datastores.quadstore.dto.Direction;

import java.util.stream.Stream;

public class  CurrentStateRetriever {

  private final BdbQuadStore bdbQuadStore;

  public CurrentStateRetriever(BdbQuadStore bdbQuadStore) {
    this.bdbQuadStore = bdbQuadStore;
  }

  /**
   *
   * @return all the outgoing quads stored in the data set
   */
  public Stream<CursorQuad> retrieveData() {
    return bdbQuadStore.getAllQuads().filter(quad -> quad.getDirection() == Direction.OUT);
  }
}
