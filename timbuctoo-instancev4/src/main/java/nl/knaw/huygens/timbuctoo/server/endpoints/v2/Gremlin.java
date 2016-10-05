package nl.knaw.huygens.timbuctoo.server.endpoints.v2;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import javaslang.control.Try;
import nl.knaw.huygens.timbuctoo.model.Datable;
import nl.knaw.huygens.timbuctoo.model.LocationNames;
import nl.knaw.huygens.timbuctoo.model.PersonNames;
import nl.knaw.huygens.timbuctoo.model.properties.ReadableProperty;
import nl.knaw.huygens.timbuctoo.model.vre.Vre;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import nl.knaw.huygens.timbuctoo.database.dto.dataset.Collection;
import nl.knaw.huygens.timbuctoo.search.EntityRef;
import nl.knaw.huygens.timbuctoo.search.description.PropertyDescriptor;
import nl.knaw.huygens.timbuctoo.search.description.property.PropertyDescriptorFactory;
import nl.knaw.huygens.timbuctoo.search.description.propertyparser.PropertyParserFactory;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import nl.knaw.huygens.timbuctoo.server.mediatypes.v2.gremlin.RootQuery;
import org.apache.tinkerpop.gremlin.groovy.DefaultImportCustomizerProvider;
import org.apache.tinkerpop.gremlin.groovy.jsr223.GremlinGroovyScriptEngine;
import org.apache.tinkerpop.gremlin.neo4j.process.traversal.LabelP;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.BulkSet;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Property;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Transaction;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;
import org.slf4j.Logger;

import javax.script.Bindings;
import javax.script.ScriptException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsn;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnA;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnO;
import static org.apache.tinkerpop.gremlin.structure.Direction.IN;
import static org.apache.tinkerpop.gremlin.structure.Direction.OUT;

@Path("/v2.1/gremlin")
public class Gremlin {
  private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(Gremlin.class);

  private final GremlinGroovyScriptEngine engine;
  private final GraphWrapper wrapper;
  private final Bindings bindings;
  private PropertyDescriptorFactory propertyDescriptorFactory;
  private PropertyParserFactory propertyParserFactory;
  private final ObjectMapper mapper;
  private Vres mappings;

  public Gremlin(GraphWrapper wrapper, Vres vres) {
    this.wrapper = wrapper;
    this.engine = new GremlinGroovyScriptEngine();

    final DefaultImportCustomizerProvider provider = new DefaultImportCustomizerProvider();
    final Set<String> allImports = provider.getAllImports();
    allImports.removeIf(path -> path.indexOf("groovy.") > -1);
    engine.addImports(allImports);
    engine.addImports(Sets.newHashSet("import org.apache.tinkerpop.gremlin.neo4j.process.traversal.LabelP"));

    this.bindings = engine.createBindings();
    propertyParserFactory = new PropertyParserFactory();
    propertyDescriptorFactory = new PropertyDescriptorFactory(propertyParserFactory);
    mapper = new ObjectMapper();
    this.mappings = vres;
  }

  @POST
  @Consumes("application/json")
  @Produces("application/json")
  public Response postJson2(RootQuery rootQuery) throws IOException {
    rootQuery.setVres(mappings);
    GraphTraversal result = wrapper.getLatestState().V().where(rootQuery.getTraversal(wrapper.getGraph().traversal()));
    LOG.info(result.toString());
    while (result.hasNext()) {
      Object item = result.next();
    }
    return Response.ok(rootQuery).build();
  }

  @POST
  @Consumes("text/plain")
  @Produces("text/plain")
  public Response post(String query, @QueryParam("timelimit") @DefaultValue("500") int timelimit) {
    return handlePlainQuery(query, timelimit);
  }

  @POST
  @Consumes("text/plain")
  @Produces("application/json")
  public Response postJson(String query) {
    bindings.put("g", wrapper.getGraph().traversal());
    bindings.put("maria", wrapper.getGraph().traversal().V().has("tim_id", "077bf0b5-6b7d-45aa-89ff-6ecf2cfc549c"));
    try {
      return Response.ok(evaluateQueryJson(query)).build();
    } catch (ScriptException e) {
      LOG.error(e.getMessage(), e);
      return Response.status(500).entity(e.getMessage()).build();
    }
  }

  @GET
  @Produces("text/html")
  public Response get() {
    return Response.temporaryRedirect(URI.create("/static/gremlin")).build();
  }

  private Response handlePlainQuery(String query, int timeLimit) {
    if (Strings.isNullOrEmpty(query)) {
      return Response.ok("").build();
    } else {
      bindings.put("g", wrapper.getGraph().traversal());
      bindings.put("maria", wrapper.getGraph().traversal().V().has("tim_id", "37981a95-e527-40a8-9528-7d32c5c5f360"));
      try {
        final String result = evaluateQuery(query);// + ".timeLimit(" + timeLimit + ")");
        wrapper.getGraph().tx().commit();
        return Response.ok(result).build();
      } catch (ScriptException e) {
        LOG.error(e.getMessage(), e);
        return Response.status(500).entity(e.getMessage()).build();
      }
    }
  }

  @GET
  @Path("datasets")
  @Produces("application/json")
  public Response getDatasets() {
    org.apache.tinkerpop.gremlin.structure.Graph graph = wrapper.getGraph();
    Transaction transaction = graph.tx();
    if (!transaction.isOpen()) {
      transaction.open();
    }
    List<Vertex> vreVertices = graph.traversal().V()
            .has(T.label, LabelP.of(Vre.DATABASE_LABEL))
            .where(__.not(__.has(Vre.VRE_NAME_PROPERTY_NAME, "Admin"))).toList();
    List<JsonNode> nodes = Lists.newArrayList();
    List<JsonNode> links = Lists.newArrayList();

    for (Vertex vreVertex : vreVertices) {
      int vreRef = nodes.size();
      nodes.add(jsnO(
        "label", jsn((String) vreVertex.property(Vre.VRE_NAME_PROPERTY_NAME).value()),
        "type", jsn("dataset")
      ));
      Iterator<Vertex> collectionVertices = vreVertex.vertices(Direction.OUT, Vre.HAS_COLLECTION_RELATION_NAME);
      while (collectionVertices.hasNext()) {
        Vertex collectionVertex = collectionVertices.next();
        final Collection collection = mappings
                .getCollection((String) collectionVertex.property(Collection.COLLECTION_NAME_PROPERTY_NAME).value())
                .get();
        if (collection.isRelationCollection() || collection.isUnknown()) {
          continue;
        }

        int collectionRef = nodes.size();
        nodes.add(jsnO(
          "label", jsn((String) collectionVertex.property(Collection.COLLECTION_LABEL_PROPERTY_NAME).value()),
          "type", jsn("collection")
        ));

        links.add(jsnO(
           "type", jsn("hasCollection"),
           "source", jsn(vreRef),
           "target", jsn(collectionRef)
        ));

        if (collectionVertex.vertices(Direction.OUT, Collection.HAS_ENTITY_NODE_RELATION_NAME).hasNext()) {
          Iterator<Vertex> entityVertices = collectionVertex
                  .vertices(Direction.OUT, Collection.HAS_ENTITY_NODE_RELATION_NAME)
                  .next().vertices(Direction.OUT, Collection.HAS_ENTITY_RELATION_NAME);

          while (entityVertices.hasNext()) {
            int entityRef = nodes.size();
            Vertex entityVertex = entityVertices.next();

            GraphTraversal<?, Try<JsonNode>> displayNameTraversal = collection.getDisplayName().traversalJson();
            GraphTraversal<Vertex, Try<JsonNode>> mapDisplayNameResult = graph.traversal()
                    .V(entityVertex.id()).map(displayNameTraversal);
            JsonNode displayName = mapDisplayNameResult.hasNext() ?
                    mapDisplayNameResult.next().get() : jsn("<no displayname>");

            nodes.add(jsnO(
              "label", displayName,
              "type", jsn(collection.getEntityTypeName())
            ));
            links.add(jsnO(
               "target", jsn(collectionRef),
               "source", jsn(entityRef),
               "type", jsn("isInCollection")
            ));
          }
        }
      }
    }

    transaction.close();
    ObjectNode d3Data = jsnO("nodes", jsnA(nodes.stream()), "links", jsnA(links.stream()));
    return Response.ok(d3Data).build();
  }

  private static class JsonResult {
    public Map<String, Long> counts;
    public Map<String, List<EntityRef>> results;

    public JsonResult(Map<String, Long> counts, Map<String, List<EntityRef>> results) {
      this.counts = counts;
      this.results = results;
    }
  }

  private JsonResult evaluateQueryJson(String query) throws ScriptException {
    String baseQuery = query.replaceAll("\\.select\\(.+\\)$", "");
    String[] selects = query.replaceAll(".+\\.select\\((.+)\\)$", "$1").split(",");

    List<GraphTraversal> traversals = new ArrayList<>();

    StringJoiner currentSelects = new StringJoiner(", ");
    for (int selectIndex = 0; selectIndex < selects.length; selectIndex++) {
      currentSelects.add(selects[selectIndex]);

      if ((selectIndex + 1) % 2 == 0) {
        LOG.info("Querying: .select(" + currentSelects.toString() + ")");
        traversals.add((GraphTraversal)
                engine.eval(baseQuery + ".select(" + currentSelects.toString() + ").dedup()", bindings));
        currentSelects = new StringJoiner(", ");
      }

    }

    if (currentSelects.toString().length() > 0) {
      LOG.info("Querying: .select(" + currentSelects.toString() + ")");
      traversals.add((GraphTraversal)
              engine.eval(baseQuery + ".select(" + currentSelects.toString() + ").dedup()", bindings));
    }


    Map<String, List<EntityRef>> results = new ConcurrentHashMap<>();
    Map<String, Set<String>> resultIds = new ConcurrentHashMap<>();
    Map<String, Long> resultCounts = new ConcurrentHashMap<>();

    for (GraphTraversal traversalResult : traversals) {
      while (traversalResult.hasNext()) {
        Object item = traversalResult.next();
        if (item instanceof LinkedHashMap) {
          LinkedHashMap lhm = (LinkedHashMap) item;
          lhm.forEach((key, obj) -> {
            if (obj instanceof Vertex) {
              loadVertex(results, key, (Vertex) obj, resultIds);
            }
          });
        } else if (item instanceof Vertex) {
          String key = currentSelects.toString().replace("\"", "");
          loadVertex(results, key, (Vertex) item, resultIds);
        }
      }
    }
    resultIds.forEach((key, obj) -> resultCounts.put(key, Long.valueOf(obj.size())));
    return new JsonResult(resultCounts, results);
  }

  private void loadVertex(Map<String, List<EntityRef>> results, Object key, Vertex obj,
                          Map<String, Set<String>> resultCounts) {

    if (!results.containsKey(key)) {
      results.put((String) key, new ArrayList<>());
      resultCounts.put((String) key, new HashSet<>());
    }

    String timId = (String) obj.property("tim_id").value();

    if (results.get(key).size() < 10 && !resultCounts.get(key).contains(timId)) {
      try {
        results.get(key).add(mapVertex(obj));
      } catch (IOException e) {
        LOG.error(e.getMessage(), e);
      }
    }
    resultCounts.get(key).add(timId);
  }


  private PropertyDescriptor createAuthorDescriptor() {
    PropertyDescriptor authorNameDescriptor = propertyDescriptorFactory.getDerivedWithSeparator(
            "isCreatedBy",
            "wwperson_names",
            propertyParserFactory.getParser(PersonNames.class),
            "; ");
    PropertyDescriptor authorTempNameDescriptor = propertyDescriptorFactory.getDerivedWithSeparator(
            "isCreatedBy",
            "wwperson_tempName",
            propertyParserFactory.getParser(String.class),
            "; ");

    return propertyDescriptorFactory
            .getComposite(authorNameDescriptor, authorTempNameDescriptor);
  }

  private EntityRef mapVertex(Vertex vertex) throws IOException {
    String id = (String) vertex.property("tim_id").value();
    String type = getVertexType(vertex);
    String displayName = "";
    Map<String, Object> data = Maps.newHashMap();

    if (type.equals("person")) {
      displayName = propertyDescriptorFactory.getComposite(
              propertyDescriptorFactory.getLocal("wwperson_names", PersonNames.class),
              propertyDescriptorFactory.getLocal("wwperson_tempName", String.class)).get(vertex);
    } else if (type.equals("location")) {
      displayName = propertyDescriptorFactory.getLocal("names", LocationNames.class).get(vertex);
    } else if (type.equals("document")) {
      PropertyDescriptor titleDescriptor = propertyDescriptorFactory.getLocal("wwdocument_title", String.class);
      PropertyDescriptor dateDescriptor = propertyDescriptorFactory
              .getLocal("wwdocument_date", Datable.class, "(", ")");

      PropertyDescriptor documentDescriptor = propertyDescriptorFactory
              .getAppender(titleDescriptor, dateDescriptor, " ");

      displayName = propertyDescriptorFactory.getAppender(createAuthorDescriptor(), documentDescriptor, " - ")
              .get(vertex);
    } else if (type.equals("keyword")) {
      PropertyDescriptor typeDescriptor = propertyDescriptorFactory.getLocal("keyword_type", String.class);
      PropertyDescriptor valueDescriptor = propertyDescriptorFactory.getLocal("keyword_value", String.class);
      displayName = propertyDescriptorFactory.getAppender(valueDescriptor, typeDescriptor, " - ")
              .get(vertex);
    } else if (type.equals("collective")) {
      PropertyDescriptor typeDescriptor = propertyDescriptorFactory.getLocal("collective_type", String.class);
      PropertyDescriptor valueDescriptor = propertyDescriptorFactory.getLocal("collective_name", String.class);
      displayName = propertyDescriptorFactory.getAppender(valueDescriptor, typeDescriptor, " - ")
              .get(vertex);
    } else if (type.equals("language")) {
      displayName = propertyDescriptorFactory.getLocal("language_name", String.class).get(vertex);
    }


    EntityRef ref = new EntityRef(type, id);
    ref.setDisplayName(displayName);
    ref.setData(data);

    return ref;
  }

  private String getVertexType(Vertex vertex) throws IOException {
    String types = (String) vertex.property("types").value();
    List<String> typeList = mapper.readValue(types,
            mapper.getTypeFactory().constructCollectionType(List.class, String.class));
    typeList.sort((o1, o2) -> o1.length() - o2.length());
    return typeList.get(0);
  }

  private void dumpVertex(Vertex vertex, StringBuilder result) {
    result.append(String.format("Vertex [%s]:\n", vertex.id()));
    ArrayList<VertexProperty<Object>> properties = Lists.newArrayList(vertex.properties());
    properties.sort((o1, o2) -> java.text.Collator.getInstance().compare(o1.label(), o2.label()));
    for (VertexProperty<Object> property : properties) {
      String stringified;
      try {
        stringified = mapper.writeValueAsString(property.value());
      } catch (JsonProcessingException e) {
        stringified = String.format("%s", mapper);
      }
      result.append(String.format("  %s: %s\n", property.label(), stringified));
    }
    for (Edge edge : ImmutableList.copyOf(vertex.edges(IN))) {
      result.append(String.format("  <--[%s]-- v[%s]\n", edge.label(), edge.outVertex().id()));
    }
    for (Edge edge : ImmutableList.copyOf(vertex.edges(OUT))) {
      result.append(String.format("  --[%s]--> v[%s]\n", edge.label(), edge.inVertex().id()));
    }
  }

  private void dumpEdge(Edge edge, StringBuilder result) {
    result.append(String.format("v[%s] --[%s]--> v[%s]\n",
            edge.outVertex().id(), edge.label(), edge.inVertex().id()));
    ArrayList<Property<Object>> properties = Lists.newArrayList(edge.properties());
    properties.sort((o1, o2) -> java.text.Collator.getInstance().compare(o1.key(), o2.key()));
    for (Property<Object> property : properties) {
      String stringified;
      try {
        stringified = mapper.writeValueAsString(property.value());
      } catch (JsonProcessingException e) {
        stringified = String.format("%s", mapper);
      }
      result.append(String.format("  %s: %s\n", property.key(), stringified));
    }
  }

  private void dumpItem(Object item, StringBuilder result) {
    if (item instanceof Vertex) {
      dumpVertex((Vertex) item, result);
    } else if (item instanceof Edge) {
      dumpEdge((Edge) item, result);
    } else if (item instanceof BulkSet) {
      BulkSet bulkSet = (BulkSet<Object>) item;
      bulkSet.forEach((obj, amount) -> {
        StringBuilder stringBuilder = new StringBuilder();
        dumpItem(obj, stringBuilder);
        result.append(stringBuilder.toString() + "\n");
      });

    } else if (item instanceof LinkedHashMap) {
      Map<String, String> map = new HashMap<>();

      LinkedHashMap lhm = (LinkedHashMap<String, Object>) item;
      lhm.forEach((key, obj) -> {
        StringBuilder stringBuilder = new StringBuilder();
        dumpItem(obj, stringBuilder);
        map.put((String)key, stringBuilder.toString());
      });
      try {
        result.append(mapper.writeValueAsString(map));
      } catch (JsonProcessingException e) {
        LOG.error(e.getMessage(), e);
      }
    } else {
      result.append(item);
      result.append(item).append("\n");
    }
  }

  private String evaluateQuery(String query) throws ScriptException {
    GraphTraversal traversalResult = (GraphTraversal) engine.eval(query, bindings);
    StringBuilder result = new StringBuilder();
    while (traversalResult.hasNext()) {

      dumpItem(traversalResult.next(), result);
    }
    if (result.length() > 0) {
      return result.toString();
    } else {
      return "No results...";
    }
  }

}
