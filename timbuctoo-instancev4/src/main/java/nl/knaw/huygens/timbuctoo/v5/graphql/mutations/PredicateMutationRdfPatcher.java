package nl.knaw.huygens.timbuctoo.v5.graphql.mutations;

import nl.knaw.huygens.timbuctoo.v5.dataset.PatchRdfCreator;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.QuadStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.CursorQuad;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.Direction;
import nl.knaw.huygens.timbuctoo.v5.filestorage.exceptions.LogStorageFailedException;
import nl.knaw.huygens.timbuctoo.v5.graphql.mutations.dto.PredicateMutation;
import nl.knaw.huygens.timbuctoo.v5.rdfio.RdfPatchSerializer;

import java.util.function.Consumer;
import java.util.stream.Stream;

public class PredicateMutationRdfPatcher implements PatchRdfCreator {
  private final PredicateMutation mutation;

  public PredicateMutationRdfPatcher(PredicateMutation mutation) {

    this.mutation = mutation;
  }

  @Override
  public void sendQuads(RdfPatchSerializer saver, Consumer<String> importStatusConsumer, DataSet dataSet)
    throws LogStorageFailedException {

    final QuadStore quadStore = dataSet.getQuadStore();
    for (CursorQuad newValue : mutation.getReplacements()) {
      final String subject = newValue.getSubject();
      final String predicate = newValue.getPredicate();
      final Direction direction = newValue.getDirection();

      try (Stream<CursorQuad> quads = quadStore.getQuads(subject, predicate, direction, "")) {
        for (CursorQuad oldValue : (Iterable<CursorQuad>) quads::iterator) {
          saver.delQuad(
            oldValue.getSubject(),
            oldValue.getPredicate(),
            oldValue.getObject(),
            oldValue.getValuetype().orElse(null),
            oldValue.getLanguage().orElse(null),
            null
          );
        }
      }
      saver.onQuad(
        newValue.getSubject(),
        newValue.getPredicate(),
        newValue.getObject(),
        newValue.getValuetype().orElse(null),
        newValue.getLanguage().orElse(null),
        null
      );
    }

    for (CursorQuad oldValue : mutation.getRetractions()) {
      saver.delQuad(
        oldValue.getSubject(),
        oldValue.getPredicate(),
        oldValue.getObject(),
        oldValue.getValuetype().orElse(null),
        oldValue.getLanguage().orElse(null),
        null
      );
    }

    for (CursorQuad oldValue : mutation.getAdditions()) {
      saver.onQuad(
        oldValue.getSubject(),
        oldValue.getPredicate(),
        oldValue.getObject(),
        oldValue.getValuetype().orElse(null),
        oldValue.getLanguage().orElse(null),
        null
      );
    }
  }
}
