package nl.knaw.huygens.timbuctoo.rml;

import nl.knaw.huygens.timbuctoo.rml.rmldata.DataSource;
import nl.knaw.huygens.timbuctoo.rml.rmldata.RmlMappingDocument;
import nl.knaw.huygens.timbuctoo.rml.rmldata.RrPredicateObjectMap;
import nl.knaw.huygens.timbuctoo.rml.rmldata.RrTriplesMap;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class RmlMapper {
  public static void execute(DataSource input, RmlMappingDocument map,
                             TripleConsumer consumer) {
    for (RrTriplesMap triplesMap : map) {
      List<ReferenceGetter> fieldsToJoinOn = triplesMap.getFieldsThatIamJoiningOn();
      List<ReferenceGetter> fieldsThatWillBeJoinedOn = triplesMap.getFieldsThatWillBeJoinedOn();

      final Iterator<Map<String, Object>> items = input.getItems(triplesMap.getLogicalSource(), fieldsToJoinOn);

      while (items.hasNext()) {
        final Map<String, Object> stringObjectMap = items.next();
        Node subject = triplesMap.getSubjectMap().getTermMap().generateValue(stringObjectMap);
        for (ReferenceGetter parentFieldFromJoin : fieldsThatWillBeJoinedOn) {
          input.willBeJoinedOn(
            triplesMap.getLogicalSource(),
            parentFieldFromJoin.targetFieldName,
            stringObjectMap.get(parentFieldFromJoin.targetFieldName),
            subject.getURI()
          );
        }

        if (triplesMap.getSubjectMap().getClassName() != null) {
          Node predicate = NodeFactory.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
          consumer.accept(new Triple(subject, predicate, triplesMap.getSubjectMap().getClassName()));
        }
        // Create triples for the properties
        for (RrPredicateObjectMap predicateObjectMap : triplesMap.getPredicateObjectMaps()) {
          Node node = predicateObjectMap.getObjectMap().generateValue(stringObjectMap);
          consumer.accept(new Triple(subject, predicateObjectMap.getPredicate(), node));
        }
      }
    }
  }
}
