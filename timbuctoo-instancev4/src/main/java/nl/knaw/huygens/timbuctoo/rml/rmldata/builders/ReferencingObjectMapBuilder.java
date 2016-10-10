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
    getTriplesMap.apply(parentTriplesMapUri).onTriplesMapReceived((referencingTriplesMap, flippedEdge) -> {
      if (flippedEdge) {
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

/*  public void build(TermMapBuilder predicateMap, RrTriplesMap ownTriplesMap) {
    ownTriplesMap.addPredicateObjectMap(
      new RrPredicateObjectMapOfReferencingObjectMap(
        predicateMap.build(),
        false,
        new RrRefObjectMap(otherMap, rrJoinCondition, ownTriplesMap.getDataSource())
      )
    );
  }*/
}
