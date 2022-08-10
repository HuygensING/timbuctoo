package nl.knaw.huygens.timbuctoo.v5.datastores.implementations.bdb;

import nl.knaw.huygens.timbuctoo.v5.dataset.ChangeFetcher;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.ChangeType;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.Direction;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.QuadGraphs;

import java.util.stream.Stream;

import static com.google.common.collect.Streams.stream;

class ChangeFetcherImpl implements ChangeFetcher {
  private final BdbPatchVersionStore patchVersionStore;
  private final BdbQuadStore quadStore;

  public ChangeFetcherImpl(BdbPatchVersionStore patchVersionStore, BdbQuadStore quadStore) {
    this.patchVersionStore = patchVersionStore;
    this.quadStore = quadStore;
  }

  public ChangeFetcherImpl(BdbQuadStore quadStore) {
    this.patchVersionStore = null;
    this.quadStore = quadStore;
  }

  @Override
  public Stream<QuadGraphs> getPredicates(String subject, boolean getRetracted, boolean getUnchanged,
                                          boolean getAsserted) {
    return getPredicates(subject, null, null, getRetracted, getUnchanged, getAsserted);
  }

  @Override
  public Stream<QuadGraphs> getPredicates(String subject, String predicate, Direction direction,
                                          boolean getRetracted, boolean getUnchanged, boolean getAsserted) {
    final Stream<QuadGraphs> result;
    if (patchVersionStore == null && getAsserted) {
      result = QuadGraphs.mapToQuadGraphs(predicate != null ?
          quadStore.getQuads(subject, predicate, direction, "") :
          quadStore.getQuads(subject)
      ).map(q -> QuadGraphs.create(
          q.getSubject(), q.getPredicate(), q.getDirection(), q.getObject(),
          q.getValuetype(), q.getLanguage(), q.getGraphs(), ChangeType.ASSERTED
      ));
    } else if (patchVersionStore == null) {
      result = Stream.empty();
    } else if (getUnchanged) {
      final Stream<QuadGraphs> assertions = QuadGraphs.mapToQuadGraphs(predicate != null ?
          patchVersionStore.getChanges(subject, predicate, direction, true) :
          patchVersionStore.getChanges(subject, true)
      );

      final Stream<QuadGraphs> currentState = QuadGraphs.mapToQuadGraphs(predicate != null ?
          quadStore.getQuads(subject, predicate, direction, "") :
          quadStore.getQuads(subject)
      );

      result = stream(new IterationState(assertions, currentState, getAsserted)).onClose(() -> {
        assertions.close();
        currentState.close();
      });
    } else if (getAsserted) {
      result = QuadGraphs.mapToQuadGraphs((predicate == null ?
          patchVersionStore.getChanges(subject, true) :
          patchVersionStore.getChanges(subject, predicate, direction, true)
      ));
    } else {
      result = Stream.empty();
    }

    if (getRetracted && patchVersionStore != null) {
      final Stream<QuadGraphs> retractions = QuadGraphs.mapToQuadGraphs(predicate != null ?
          patchVersionStore.getChanges(subject, predicate, direction, false) :
          patchVersionStore.getChanges(subject, false)
      );

      return stream(new RetractionMerger(result, retractions)).onClose(() -> {
        result.close();
        retractions.close();
      });
    } else {
      return result;
    }
  }
}
