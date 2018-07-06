package nl.knaw.huygens.timbuctoo.v5.dataset;

import nl.knaw.huygens.timbuctoo.v5.datastores.implementations.bdb.BdbTripleStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.CursorQuad;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class  CurrentStateRetriever {

  private final BdbTripleStore bdbTripleStore;

  public CurrentStateRetriever(BdbTripleStore bdbTripleStore) {
    this.bdbTripleStore = bdbTripleStore;
  }

  public List<CursorQuad> retrieveData() {
    try (Stream<CursorQuad> quads = bdbTripleStore.getAllQuads()) {
      return quads.collect(Collectors.toList());
    }
  }
}
