package nl.knaw.huygens.timbuctoo.v5.datastores.implementations.bdb;

import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.QuadGraphs;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

class IterationState implements Iterator<QuadGraphs> {
  private final PeekingIterator<QuadGraphs> assertions;
  private final PeekingIterator<QuadGraphs> currentState;
  private final boolean getAsserted;
  private QuadGraphs currentQuad;

  public IterationState(Stream<QuadGraphs> assertions, Stream<QuadGraphs> currentState, boolean getAsserted) {
    this.assertions = Iterators.peekingIterator(assertions.iterator());
    this.currentState = Iterators.peekingIterator(currentState.iterator());
    this.getAsserted = getAsserted;
    prepNext();
  }

  /*
  There's currentState and additions:

  e.g.

  currentState:     additions:
  S,P1,O            S,P2,O
  S,P2,O            S,P5,O
  S,P3,O            S,P7,O
  S,P4,O
  S,P5,O
  S,P6,O
  S,P7,O


  This iterator will loop over currentState and additions. An addition might be kept for a while until we find a
  currentState item that matches. Each addition will always be in currentState.

  When we come past an item that's also in additions we either emit the addition (if the user requested additions)
  or we skip this item (if the user requested unchanged only)
   */
  private void prepNext() {
    //if we want to exclude the asserted quads then
    // skip all quads from currentState that are equal to the assertion
    while (!getAsserted &&
        currentState.hasNext() && assertions.hasNext() &&
        assertions.peek().equals(currentState.peek())) {
      currentState.next();
      assertions.next();
    }

    //either the iterator is empty, currentState.peek()!=assertions.peek(),
    // the assertion only added a new graph, or we want assertions
    if (currentState.hasNext()) {
      currentQuad = currentState.next();
      if (getAsserted && assertions.hasNext() && assertions.peek().equals(currentQuad)) {
        currentQuad = assertions.next();
      } else if (getAsserted && assertions.hasNext() && assertions.peek().equalsIgnoreGraphs(currentQuad)) {
        assertions.next();
      }
    } else {
      currentQuad = null;
    }
  }

  @Override
  public boolean hasNext() {
    return currentQuad != null;
  }

  @Override
  public QuadGraphs next() {
    if (currentQuad == null) {
      throw new NoSuchElementException();
    }
    QuadGraphs result = currentQuad;
    prepNext();
    return result;
  }
}
