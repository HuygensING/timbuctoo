package nl.knaw.huygens.timbuctoo.rml;

import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.rml.rmldata.RmlMappingDocument;
import nl.knaw.huygens.timbuctoo.rml.rmldata.termmaps.TermType;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Node_URI;

import java.util.Iterator;
import java.util.Map;

import static nl.knaw.huygens.timbuctoo.rml.rmldata.RmlMappingDocument.rmlMappingDocument;
import static nl.knaw.huygens.timbuctoo.rml.rmldata.RrPredicateObjectMap.rrPredicateObjectMap;
import static nl.knaw.huygens.timbuctoo.rml.rmldata.RrSubjectMap.rrSubjectMap;
import static nl.knaw.huygens.timbuctoo.rml.rmldata.RrTriplesMap.rrTriplesMap;
import static nl.knaw.huygens.timbuctoo.rml.rmldata.termmaps.referencingobjectmaps.RrRefObjectMap.rrRefObjectMap;

public class ExampleMapping {
  public static RmlMappingDocument createEmExampleMapping() {
    return rmlMappingDocument()
      .withTripleMap(rrTriplesMap()
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
        .withUri((Node_URI) NodeFactory.createURI("http://timbuctoo.com/mappings/locationmap"))
        .withSubjectMap(rrSubjectMap()
          .withClass((Node_URI) NodeFactory.createURI("http://timbuctoo.com/emlocations"))
          .withColumnTerm("http://timbuctoo.com/emlocations/{naam}")
        )
        .withPredicateObjectMap(rrPredicateObjectMap()
          .withPredicate((Node_URI) NodeFactory.createURI("http://timbuctoo.com/naam"))
          .withColumn("naam", TermType.Literal)
        )
      )
      .build(f -> new DataSource() {
        @Override
        public Iterator<Map<String, Object>> getItems() {
          return Lists.<Map<String, Object>>newArrayList().iterator();
        }

        @Override
        public void willBeJoinedOn(String fieldName, Object referenceJoinValue, String uri, String outputFieldName) {
          System.out.println("Does not do a thing");
        }
      });
  }
}
