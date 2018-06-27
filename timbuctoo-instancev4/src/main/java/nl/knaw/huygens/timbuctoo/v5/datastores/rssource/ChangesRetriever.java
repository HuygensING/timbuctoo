package nl.knaw.huygens.timbuctoo.v5.datastores.rssource;

import nl.knaw.huygens.timbuctoo.v5.datastores.implementations.bdb.BdbTruePatchStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.implementations.bdb.UpdatedPerPatchStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.CursorQuad;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.Direction;
import org.apache.jena.update.Update;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ChangesRetriever {

  private BdbTruePatchStore bdbTruePatchStore;
  private UpdatedPerPatchStore updatedPerPatchStore;

  public ChangesRetriever(BdbTruePatchStore bdbTruePatchStore, UpdatedPerPatchStore updatedPerPatchStore) {
    this.bdbTruePatchStore = bdbTruePatchStore;
    this.updatedPerPatchStore = updatedPerPatchStore;
  }

  public Supplier<List<Integer>> getVersions() {
    return () -> {
      try (Stream<Integer> versions = updatedPerPatchStore.getVersions()) {
        return versions.collect(Collectors.toList());
      }
    };
  }

  public Supplier<List<String>> getSubjects(Integer version) {
    return () -> {
      try (Stream<String> subjects = updatedPerPatchStore.ofVersion(version)) {
        return subjects.collect(Collectors.toList());
      }
    };
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

