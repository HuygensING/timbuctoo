package nl.knaw.huygens.timbuctoo.v5.datastores.rssource;

import nl.knaw.huygens.timbuctoo.v5.datastores.implementations.bdb.BdbTruePatchStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.CursorQuad;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.Direction;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ChangesRetriever {

  private BdbTruePatchStore bdbTruePatchStore;


  public ChangesRetriever(BdbTruePatchStore bdbTruePatchStore) {
    this.bdbTruePatchStore = bdbTruePatchStore;
  }

  public List<CursorQuad> retrieveChanges(Integer version, Supplier<List<String>> subjects) {
    List<CursorQuad> changes = new ArrayList<>();

    for (String subject : subjects.get()) {
      try (Stream<CursorQuad> quads = bdbTruePatchStore.getChanges(subject, version, true)) {
        // Filtering on direction is needed as the TruePatchStore contains the inverse relations as well.
        // The original relations are always in the "OUT" direction.
        // See StoreUpdater.addRelation
        changes.addAll(quads.filter(quad -> quad.getDirection().equals(Direction.OUT)).collect(Collectors.toList()));
      }
    }

    return changes;
  }
}

