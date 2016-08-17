package nl.knaw.huygens.timbuctoo.rml.rmldata.builders;

import nl.knaw.huygens.timbuctoo.rml.rmldata.RrPredicateObjectMapOfReferencingObjectMap;
import nl.knaw.huygens.timbuctoo.rml.rmldata.RrTriplesMap;
import nl.knaw.huygens.timbuctoo.rml.rmldata.termmaps.RrJoinCondition;
import nl.knaw.huygens.timbuctoo.rml.rmldata.termmaps.RrRefObjectMap;

import java.util.function.Function;

public class ReferencingObjectMapBuilder {

  private String parentTriplesMapUri;
  private RrJoinCondition rrJoinCondition;

  ReferencingObjectMapBuilder() {
  }

  public ReferencingObjectMapBuilder withParentTriplesMap(String rrTriplesMapUri) {
    this.parentTriplesMapUri = rrTriplesMapUri;
    return this;
  }

  public ReferencingObjectMapBuilder withJoinCondition(String child, String parent) {
    this.rrJoinCondition = new RrJoinCondition(child, parent);
    return this;
  }

  public void build(TermMapBuilder predicateMap,
                    Function<String, PromisedTriplesMap> getTriplesMap, RrTriplesMap ownTriplesMap) {
    getTriplesMap.apply(parentTriplesMapUri).onTriplesMapReceived((referencingTriplesMap, seenTriplesMaps) -> {
      if (seenTriplesMaps.contains(ownTriplesMap)) { //Then I'm earlier then the one I'm refering to
        //We need to give the datasource that we're going to ask for Subject urls time to gather them. so we're
        // inverting the link
        referencingTriplesMap.addPredicateObjectMap(
          new RrPredicateObjectMapOfReferencingObjectMap(
            predicateMap.build(),
            true,
            new RrRefObjectMap(ownTriplesMap, rrJoinCondition.flipped(), referencingTriplesMap.getDataSource())
          )
        );
      } else {
        ownTriplesMap.addPredicateObjectMap(
          new RrPredicateObjectMapOfReferencingObjectMap(
            predicateMap.build(),
            false,
            new RrRefObjectMap(referencingTriplesMap, rrJoinCondition, ownTriplesMap.getDataSource())
          )
        );
      }
    });
  }
}
