package nl.knaw.huygens.timbuctoo.rml;

import nl.knaw.huygens.timbuctoo.rml.rmldata.rmlsources.TimbuctooRawCollectionSource;
import nl.knaw.huygens.timbuctoo.rml.rmldata.termmaps.TermType;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Node_URI;
import org.apache.jena.graph.Triple;

import java.util.stream.Stream;

import static nl.knaw.huygens.timbuctoo.rml.rmldata.RmlLogicalSource.rrLogicalSource;
import static nl.knaw.huygens.timbuctoo.rml.rmldata.RmlMappingDocument.rmlMappingDocument;
import static nl.knaw.huygens.timbuctoo.rml.rmldata.RrPredicateObjectMap.rrPredicateObjectMap;
import static nl.knaw.huygens.timbuctoo.rml.rmldata.RrSubjectMap.rrSubjectMap;
import static nl.knaw.huygens.timbuctoo.rml.rmldata.RrTriplesMap.rrTriplesMap;
import static nl.knaw.huygens.timbuctoo.rml.rmldata.termmaps.RrRefObjectMap.rrRefObjectMap;

public class ExampleMapping {
  public static Stream<Triple> executeEmExampleMapping(GraphWrapper graphWrapper, String vre) {
    return rmlMappingDocument()
      .withTripleMap(rrTriplesMap()
        .withLogicalSource(rrLogicalSource()
          .withSource(new TimbuctooRawCollectionSource("emmigrantunits", vre))
        )
        .withUri((Node_URI) NodeFactory.createURI("http://timbuctoo.com/mappings/personmap"))
        .withSubjectMap(rrSubjectMap()
          .withClass((Node_URI) NodeFactory.createURI("http://timbuctoo.com/emmigrantunits"))
          .withTemplateTerm("http://timbuctoo.com/emmigrantunits/{persoonsId}")
        )
        .withPredicateObjectMap(rrPredicateObjectMap()
          .withPredicate((Node_URI) NodeFactory.createURI("http://timbuctoo.com/id"))
          .withColumn("persoonsId", TermType.Literal)
        )
        .withPredicateObjectMap(rrPredicateObjectMap()
          .withPredicate((Node_URI) NodeFactory.createURI("http://timbuctoo.com/achternaam"))
          .withColumn("achternaam", TermType.Literal)
        )
        .withPredicateObjectMap(rrPredicateObjectMap()
          .withPredicate((Node_URI) NodeFactory.createURI("http://timbuctoo.com/hasResidenceLocation"))
          .withReference(rrRefObjectMap()
            .withParentTriplesMap("http://timbuctoo.com/mappings/locationmap")
            .withJoinCondition("hasResidenceLocation", "naam")
          )
        )
      )
      .withTripleMap(rrTriplesMap()
        .withLogicalSource(rrLogicalSource()
          .withSource(new TimbuctooRawCollectionSource("emlocations", vre))
        )
        .withUri((Node_URI) NodeFactory.createURI("http://timbuctoo.com/mappings/locationmap"))
        .withSubjectMap(rrSubjectMap()
          .withClass((Node_URI) NodeFactory.createURI("http://timbuctoo.com/emlocations"))
          .withTemplateTerm("http://timbuctoo.com/emlocations/{naam}")
        )
        .withPredicateObjectMap(rrPredicateObjectMap()
          .withPredicate((Node_URI) NodeFactory.createURI("http://timbuctoo.com/naam"))
          .withColumn("naam", TermType.Literal)
        )
      )
      .build(new DataSourceFactory(graphWrapper))
      .execute();
  }
}
