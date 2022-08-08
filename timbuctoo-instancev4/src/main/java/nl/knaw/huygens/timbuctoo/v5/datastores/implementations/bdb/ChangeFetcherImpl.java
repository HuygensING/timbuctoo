package nl.knaw.huygens.timbuctoo.v5.datastores.implementations.bdb;

import nl.knaw.huygens.timbuctoo.v5.dataset.ChangeFetcher;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.ChangeType;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.Direction;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.QuadGraphs;

import java.util.stream.Stream;

import static com.google.common.collect.Streams.stream;

class ChangeFetcherImpl implements ChangeFetcher {
  private final BdbTruePatchStore truePatchStore;
  private final BdbQuadStore quadStore;
  private final int version;

  public ChangeFetcherImpl(BdbTruePatchStore truePatchStore, BdbQuadStore quadStore, int version) {
    this.truePatchStore = truePatchStore;
    this.quadStore = quadStore;
    this.version = version;
  }

  public ChangeFetcherImpl(BdbQuadStore quadStore) {
    this.truePatchStore = null;
    this.quadStore = quadStore;
    this.version = -1;
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
    if (truePatchStore == null && getAsserted) {
      result = QuadGraphs.mapToQuadGraphs(predicate != null ?
          quadStore.getQuads(subject, predicate, direction, "") :
          quadStore.getQuads(subject)
      ).map(q -> QuadGraphs.create(
          q.getSubject(), q.getPredicate(), q.getDirection(), q.getObject(),
          q.getValuetype(), q.getLanguage(), q.getGraphs(), ChangeType.ASSERTED
      ));
    } else if (truePatchStore == null) {
      result = Stream.empty();
    } else if (getUnchanged) {
      final Stream<QuadGraphs> assertions = QuadGraphs.mapToQuadGraphs(predicate != null ?
          truePatchStore.getChanges(subject, predicate, direction, version, true) :
          truePatchStore.getChanges(subject, version, true)
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
          truePatchStore.getChanges(subject, version, true) :
          truePatchStore.getChanges(subject, predicate, direction, version, true)
      ));
    } else {
      result = Stream.empty();
    }

    if (getRetracted && truePatchStore != null) {
      final Stream<QuadGraphs> retractions = QuadGraphs.mapToQuadGraphs(predicate != null ?
          truePatchStore.getChanges(subject, predicate, direction, version, false) :
          truePatchStore.getChanges(subject, version, false)
      );

      return stream(new RetractionMerger(result, retractions, version)).onClose(() -> {
        result.close();
        retractions.close();
      });
    } else {
      return result;
    }
  }
}
