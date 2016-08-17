package nl.knaw.huygens.timbuctoo.rml.rmldata.builders;

import nl.knaw.huygens.timbuctoo.rml.rmldata.RrTriplesMap;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;

class PromisedTriplesMap {
  private RrTriplesMap triplesMap;
  private Set<BiConsumer<RrTriplesMap, Set<RrTriplesMap>>> consumers = new HashSet<>();

  void setTriplesMap(RrTriplesMap triplesMap, Set<RrTriplesMap> referedTripleMapIsEarlier) {
    this.triplesMap = triplesMap;
    for (BiConsumer<RrTriplesMap, Set<RrTriplesMap>> consumer : this.consumers) {
      consumer.accept(triplesMap, referedTripleMapIsEarlier);
    }
  }

  void onTriplesMapReceived(BiConsumer<RrTriplesMap, Set<RrTriplesMap>> consumer) {
    if (triplesMap == null) {
      this.consumers.add(consumer);
    } else {
      throw new RuntimeException("");
    }
  }

}
