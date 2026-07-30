package nl.knaw.huygens.timbuctoo.dataset;

import nl.knaw.huygens.timbuctoo.datastores.quadstore.QuadStore;
import nl.knaw.huygens.timbuctoo.datastores.quadstore.dto.CursorQuad;
import nl.knaw.huygens.timbuctoo.datastores.quadstore.dto.Direction;

import java.util.stream.Stream;

public class  CurrentStateRetriever {
  private final QuadStore quadStore;

  public CurrentStateRetriever(QuadStore bdbQuadStore) {
    this.quadStore = bdbQuadStore;
  }

  /**
   *
   * @return all the outgoing quads stored in the data set
   */
  public Stream<CursorQuad> retrieveData() {
    return quadStore.getAllQuads().filter(quad -> quad.getDirection() == Direction.OUT);
  }
}
