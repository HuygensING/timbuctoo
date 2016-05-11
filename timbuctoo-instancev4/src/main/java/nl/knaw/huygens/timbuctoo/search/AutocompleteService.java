package nl.knaw.huygens.timbuctoo.search;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Maps;
import nl.knaw.huygens.timbuctoo.crud.InvalidCollectionException;
import nl.knaw.huygens.timbuctoo.crud.UrlGenerator;
import nl.knaw.huygens.timbuctoo.model.PersonNames;
import nl.knaw.huygens.timbuctoo.model.TempName;
import nl.knaw.huygens.timbuctoo.search.description.PropertyDescriptor;
import nl.knaw.huygens.timbuctoo.search.description.property.PropertyDescriptorFactory;
import nl.knaw.huygens.timbuctoo.search.description.property.WwDocumentDisplayNameDescriptor;
import nl.knaw.huygens.timbuctoo.search.description.propertyparser.PropertyParserFactory;
import nl.knaw.huygens.timbuctoo.server.TinkerpopGraphManager;
import org.apache.tinkerpop.gremlin.structure.Transaction;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsn;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnA;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnO;

public class AutocompleteService {

  private final TinkerpopGraphManager graphManager;
  private final Map<String, PropertyDescriptor> displayNameDescriptors;
  private final UrlGenerator autoCompleteUrlFor;

  public AutocompleteService(TinkerpopGraphManager graphManager, UrlGenerator autoCompleteUrlFor) {
    final PropertyDescriptorFactory propertyDescriptorFactory =
            new PropertyDescriptorFactory(new PropertyParserFactory());

    this.graphManager = graphManager;
    this.autoCompleteUrlFor = autoCompleteUrlFor;

    displayNameDescriptors = Maps.newHashMap();
    displayNameDescriptors.put("wwdocuments", new WwDocumentDisplayNameDescriptor());
    displayNameDescriptors.put("wwpersons", propertyDescriptorFactory.getComposite(
            propertyDescriptorFactory.getLocal("wwperson_names", PersonNames.class),
            propertyDescriptorFactory.getLocal("wwperson_tempName", TempName.class)));
    displayNameDescriptors.put("wwkeywords", propertyDescriptorFactory.getLocal("wwkeyword_value", String.class));
  }

  public JsonNode search(String collectionName, Optional<String> query, Optional<String> type)
          throws InvalidCollectionException {

    final GraphDatabaseService graphDatabase =   graphManager.getGraphDatabase();

    Transaction transaction = graphManager.getGraph().tx();

    // Apparently a lucene search needs a transaction to be open:
    // http://stackoverflow.com/questions/19428017
    if (!transaction.isOpen()) {
      transaction.open();
    }

    if (!graphDatabase.index().existsForNodes(collectionName)) {
      transaction.close();
      throw new InvalidCollectionException("Collection does not have autocomplete index: " + collectionName);
    }

    final Index<Node> index = graphDatabase.index().forNodes(collectionName);
    String parsedQuery = query.isPresent() ? query.get() : "*";
    IndexHits<Node> hits;
    if (type.isPresent()) {
      hits = index.query(String.format("displayName:%s AND type:%s", parsedQuery, type.get()));
    } else {
      hits = index.query("displayName", parsedQuery);
    }

    List<ObjectNode> results = StreamSupport.stream(hits.spliterator(), false)
      .map(hit -> {
        Vertex vertex = graphManager.getGraph().traversal().V(hit.getId()).next();
        String timId = (String) vertex.property("tim_id").value();
        int rev = (Integer) vertex.property("rev").value();

        return jsnO(
              "key", jsn(autoCompleteUrlFor.apply(collectionName, UUID.fromString(timId), rev).toString()),
              "value", jsn(displayNameDescriptors.get(collectionName).get(vertex)));
      })
      .limit(collectionName.equals("wwkeywords") ? 1000 : 50) // FIXME: expose param to client again
      .collect(Collectors.toList());

    hits.close();
    transaction.close();
    return jsnA(results.stream());
  }
}
