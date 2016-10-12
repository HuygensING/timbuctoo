package nl.knaw.huygens.timbuctoo.rml.rmldata.builders;

import nl.knaw.huygens.timbuctoo.rml.rmldata.RrTriplesMap;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;

class PromisedTriplesMap {
  private RrTriplesMap triplesMap;
  private Set<BiConsumer<RrTriplesMap, Boolean>> consumers = new HashSet<>();

  void setTriplesMap(RrTriplesMap triplesMap, boolean flippedEdge) {
    this.triplesMap = triplesMap;
    for (BiConsumer<RrTriplesMap, Boolean> consumer : this.consumers) {
      consumer.accept(triplesMap, flippedEdge);
    }
  }

  void onTriplesMapReceived(BiConsumer<RrTriplesMap, Boolean> consumer) {
    if (triplesMap == null) {
      this.consumers.add(consumer);
    } else {
      throw new RuntimeException("");
    }
  }
}
