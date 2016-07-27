package nl.knaw.huygens.timbuctoo.rml;

import nl.knaw.huygens.timbuctoo.rml.rmldata.DataSource;
import nl.knaw.huygens.timbuctoo.rml.rmldata.RrPredicateObjectMap;
import nl.knaw.huygens.timbuctoo.rml.rmldata.RrTriplesMap;
import nl.knaw.huygens.timbuctoo.util.Tuple;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RmlMapper {
  public static void execute(DataSource input, RmlMappingDocument map,
                             TripleConsumer consumer) {
    for (RrTriplesMap triplesMap : map) {
      List<ReferenceGetter> fieldsToJoinOn = triplesMap.getFieldsThatIamJoiningOn();
      List<ReferenceGetter> fieldsThatWillBeJoinedOn = triplesMap.getFieldsThatWillBeJoinedOn();

      for (Map<String, Object> stringObjectMap : input.getItems(triplesMap.logicalSource, fieldsToJoinOn)) {
        Node subject = triplesMap.subjectMap.termMap.generateValue(stringObjectMap);
        for (ReferenceGetter parentFieldFromJoin : fieldsThatWillBeJoinedOn) {
          input.willBeJoinedOn(triplesMap.logicalSource, parentFieldFromJoin.targetFieldName, stringObjectMap.get(parentFieldFromJoin.targetFieldName), subject.getURI());
        }

        if (triplesMap.subjectMap.className != null) {
          Node predicate = NodeFactory.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
          consumer.accept(new Triple(subject, predicate, triplesMap.subjectMap.className));
        }
        // Create triples for the properties
        for (RrPredicateObjectMap predicateObjectMap : triplesMap.predicateObjectMaps) {
          Node node = predicateObjectMap.objectMap.generateValue(stringObjectMap);
          consumer.accept(new Triple(subject, predicateObjectMap.predicate, node));
        }
      }
    }
  }
}
