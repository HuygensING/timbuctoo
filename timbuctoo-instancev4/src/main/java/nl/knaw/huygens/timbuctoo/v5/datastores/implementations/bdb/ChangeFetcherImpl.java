package nl.knaw.huygens.timbuctoo.v5.datastores.implementations.bdb;

import nl.knaw.huygens.timbuctoo.v5.dataset.ChangeFetcher;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.CursorQuad;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.Direction;

import java.util.stream.Stream;

import static com.google.common.collect.Streams.stream;

class ChangeFetcherImpl implements ChangeFetcher {
  private final BdbTruePatchStore truePatchStore;
  private final BdbQuadStore quadStore;
  private final int currentversion;

  public ChangeFetcherImpl(BdbTruePatchStore truePatchStore, BdbQuadStore quadStore, int currentversion) {
    this.truePatchStore = truePatchStore;
    this.quadStore = quadStore;
    this.currentversion = currentversion;
  }

  @Override
  public Stream<CursorQuad> getPredicates(String subject, boolean getRetracted, boolean getUnchanged,
                                          boolean getAsserted) {
    return getPredicates(subject, null, null, getRetracted, getUnchanged, getAsserted);
  }

  @Override
  public Stream<CursorQuad> getPredicates(String subject, String predicate, Direction direction,
                                          boolean getRetracted, boolean getUnchanged, boolean getAsserted) {
    final Stream<CursorQuad> result;
    if (getUnchanged) {
      final Stream<CursorQuad> assertions;
      final Stream<CursorQuad> currentState;
      if (predicate != null) {
        assertions = truePatchStore.getChanges(subject, predicate, direction, currentversion, true);
        currentState = quadStore.getQuads(subject, predicate, direction, "");
      } else {
        assertions = truePatchStore.getChanges(subject, currentversion, true);
        currentState = quadStore.getQuads(subject);
      }
      //if (!assertions.findAny().isPresent()) {
      //  result = currentState;
      //} else {
      result = stream(new IterationState(assertions, currentState, getAsserted)).onClose(() -> {
        assertions.close();
        currentState.close();
      });
      //}
    } else {
      if (getAsserted) {
        if (predicate == null) {
          result = truePatchStore.getChanges(subject, currentversion, true);
        } else {
          result = truePatchStore.getChanges(subject, predicate, direction, currentversion, true);
        }
      } else {
        result = Stream.empty();
      }
    }
    if (getRetracted) {
      final Stream<CursorQuad> retractions;
      if (predicate != null) {
        retractions = truePatchStore.getChanges(subject, predicate, direction, currentversion, false);
      } else {
        retractions = truePatchStore.getChanges(subject, currentversion, false);
      }
      return stream(new RetractionMerger(result, retractions, quadStore, currentversion)).onClose(() -> {
        result.close();
        retractions.close();
      });
    } else {
      return result;
    }
  }
}
