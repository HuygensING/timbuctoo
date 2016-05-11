package nl.knaw.huygens.timbuctoo.search;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
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
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsn;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnA;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnO;

public class AutocompleteService {

  private final TinkerpopGraphManager graphManager;
  private final Map<String, PropertyDescriptor> displayNameDescriptors;

  public AutocompleteService(TinkerpopGraphManager graphManager) {
    final PropertyDescriptorFactory propertyDescriptorFactory =
            new PropertyDescriptorFactory(new PropertyParserFactory());

    this.graphManager = graphManager;

    displayNameDescriptors = Maps.newHashMap();
    displayNameDescriptors.put("wwdocuments", new WwDocumentDisplayNameDescriptor());
    displayNameDescriptors.put("wwpersons", propertyDescriptorFactory.getComposite(
            propertyDescriptorFactory.getLocal("wwperson_names", PersonNames.class),
            propertyDescriptorFactory.getLocal("wwperson_tempName", TempName.class)));
  }

  public JsonNode search(String collectionName, Optional<String> query, Optional<String> type) {

    final GraphDatabaseService graphDatabase =   graphManager.getGraphDatabase();

    Transaction transaction = graphManager.getGraph().tx();

    // Apparently a lucene search needs a transaction to be open:
    // http://stackoverflow.com/questions/19428017
    if (!transaction.isOpen()) {
      transaction.open();
    }
    Lists.newArrayList();

    final Index<Node> index = graphDatabase.index().forNodes(collectionName);
    IndexHits<Node> hits = index.query("displayName", query.isPresent() ? query.get() : "*");


    List<ObjectNode> results = StreamSupport.stream(hits.spliterator(), false)
      .map(hit -> {
        Vertex vertex = graphManager.getGraph().traversal().V(hit.getId()).next();
        return jsnO(
              "key", jsn((String) vertex.property("tim_id").value()),
              "value", jsn(displayNameDescriptors.get(collectionName).get(vertex)));
      })
      .limit(50)
      .collect(Collectors.toList());

    hits.close();
    transaction.close();
    return jsnA(results.stream());
  }
}
