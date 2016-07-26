package nl.knaw.huygens.timbuctoo.rml;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;

import java.util.Map;
import java.util.function.Function;

public class RmlMapper {
  public static void execute(Function<LogicalSource, Iterable<Map<String, Object>>> input, RmlMappingDocument map,
                             TripleConsumer consumer) {
    Model model = ModelFactory.createDefaultModel();

    for (TriplesMap triplesMap : map) {
      for (Map<String, Object> stringObjectMap : input.apply(triplesMap.logicalSource)) {
        Node subject = triplesMap.subjectMap.termMapContent.generateValue(stringObjectMap);

        if (triplesMap.subjectMap.className != null) {
          Resource object = model.createResource(triplesMap.subjectMap.className);
          Resource predicate = model.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
          consumer.accept(new Triple(subject, predicate.asNode(), object.asNode()));
        }
        // Create triples for the properties
        for (PredicateObjectMap predicateObjectMap : triplesMap.predicateObjectMaps) {
          Node predicate = model.createResource(predicateObjectMap.predicate).asNode();
          Node node = predicateObjectMap.objectMap.generateValue(stringObjectMap);
          consumer.accept(new Triple(subject, predicate, node));
        }

      }
    }
  }
}
