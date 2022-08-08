package nl.knaw.huygens.timbuctoo.v5.datastores.implementations.bdb;

import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.QuadGraphs;
import org.slf4j.Logger;

import java.util.Iterator;
import java.util.stream.Stream;

import static org.slf4j.LoggerFactory.getLogger;

class RetractionMerger implements Iterator<QuadGraphs> {
  private static final Logger LOG = getLogger(RetractionMerger.class);

  private final PeekingIterator<QuadGraphs> state;
  private final PeekingIterator<QuadGraphs> retractions;
  private final int version;

  public RetractionMerger(Stream<QuadGraphs> state, Stream<QuadGraphs> retractions, int version) {
    this.state = Iterators.peekingIterator(state.iterator());
    this.retractions = Iterators.peekingIterator(retractions.iterator());
    this.version = version;
  }

  @Override
  public boolean hasNext() {
    boolean allChecked = false;
    boolean stateHasNext = false;
    boolean retractionsHasNext = false;

    // Get retractions out of the way which only removed some graphs
    while (!allChecked) {
      stateHasNext = state.hasNext();
      retractionsHasNext = retractions.hasNext();

      if (stateHasNext && retractionsHasNext) {
        QuadGraphs leftQ = state.peek();
        QuadGraphs rightQ = retractions.peek();
        int compareResult = BdbQuadStore.compare(leftQ, rightQ);

        // The current state and retraction do not match or they match exactly (including graphs)
        // If not, (the quads match, but some graphs are missing from the current state) we just move to the next
        if (compareResult != 0 || leftQ.getGraphs().equals(rightQ.getGraphs())) {
          allChecked = true;
        } else {
          state.next();
          retractions.next();
        }
      } else {
        allChecked = true;
      }
    }

    return stateHasNext || retractionsHasNext;
  }

  /*
  There's currentState/additions and retractions:

  e.g.

  currentState:     retractions:
   S,P1,O           S,P3,O
  +S,P2,O           S,P5,O2
   S,P4,O           S,P8,O2
   S,P5,O
  +S,P6,O
   S,P7,O
  +S,P8,O


  This iterator will peek at the next item of currentState and at the next item of retractions.

  If the currentstate is sorted before the retraction it emits the currentstate, otherwise the retraction

  We als handle the case where a retraction is present in currentState (emit the retraction and move currentstate
  forward as well though that should never happen.
  */
  @Override
  public QuadGraphs next() {
    if (state.hasNext() && retractions.hasNext()) {
      QuadGraphs leftQ = state.peek();
      QuadGraphs rightQ = retractions.peek();
      int compareResult = BdbQuadStore.compare(leftQ, rightQ);

      if (compareResult == 0) {
        //Huh? we have a retraction, but the thing is also part of the state?
        LOG.error("in {} {} was retracted, but it is still part of the store as {}", version, rightQ, leftQ);
        //oh well...
        state.next();
        //back to business
        return retractions.next();
      } else if (compareResult < 0) {
        return state.next();
      } else {
        return retractions.next();
      }
    } else {
      if (state.hasNext()) {
        return state.next();
      } else {
        return retractions.next();
      }
    }
  }
}
