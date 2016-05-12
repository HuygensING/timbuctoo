package nl.knaw.huygens.timbuctoo.search.description.indexes;


import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.model.Change;
import nl.knaw.huygens.timbuctoo.model.Datable;
import nl.knaw.huygens.timbuctoo.model.PersonNames;
import nl.knaw.huygens.timbuctoo.model.TempName;
import nl.knaw.huygens.timbuctoo.search.description.IndexDescription;
import nl.knaw.huygens.timbuctoo.search.description.PropertyDescriptor;
import nl.knaw.huygens.timbuctoo.search.description.PropertyParser;
import nl.knaw.huygens.timbuctoo.search.description.property.PropertyDescriptorFactory;
import nl.knaw.huygens.timbuctoo.search.description.propertyparser.PropertyParserFactory;
import nl.knaw.huygens.timbuctoo.search.description.propertyparser.TempNamePropertyParser;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.graphdb.index.IndexManager;
import org.neo4j.helpers.collection.MapUtil;

import java.util.List;
import java.util.Map;

class WwPersonIndexDescription implements IndexDescription {


  private static class WwPersonSortFieldDescription implements IndexerSortFieldDescription {
    private static final String PREFIX = "wwperson";
    private static final String POSTFIX = "sort";

    private PropertyParser parser;
    private Comparable<?> defaultValue;
    private String name;
    private Class<?> propertyType;

    public WwPersonSortFieldDescription(String name, Comparable<?> defaultValue, PropertyParser parser, Class<?> type) {
      this.parser = parser;
      this.defaultValue = defaultValue;
      this.name = name;
      this.propertyType = type;
    }

    @Override
    public String getSortPropertyName() {
      // FIXME: string concatenating methods like this should be delegated to a configuration class
      if (name.equals("modified")) {
        return "modified_sort";
      }
      return String.format("%s_%s_%s", PREFIX, name, POSTFIX);
    }

    @Override
    public String getPropertyName() {
      // FIXME: string concatenating methods like this should be delegated to a configuration class
      if (name.equals("modified")) {
        return name;
      }
      return String.format("%s_%s", PREFIX, name);
    }

    @Override
    public PropertyParser getParser() {
      return parser;
    }

    @Override
    public Comparable<?> getDefaultValue() {
      return defaultValue;
    }

    @Override
    public Class<?> getType() {
      return this.propertyType;
    }
  }

  private final List<IndexerSortFieldDescription> sortFieldDescriptions;

  private final PropertyDescriptor displayNameDescriptor;

  public WwPersonIndexDescription() {
    final PropertyParserFactory propertyParserFactory = new PropertyParserFactory();

    sortFieldDescriptions = Lists.newArrayList(
            new WwPersonSortFieldDescription(
                    "names", "", propertyParserFactory.getParser(PersonNames.class), String.class),
            new WwPersonSortFieldDescription(
                    "deathDate", 0, propertyParserFactory.getParser(Datable.class), Integer.class),
            new WwPersonSortFieldDescription(
                    "birthDate", 0, propertyParserFactory.getParser(Datable.class), Integer.class),
            new WwPersonSortFieldDescription(
                    "modified", 0L, propertyParserFactory.getParser(Change.class), Long.class)
    );

    final PropertyDescriptorFactory propertyDescriptorFactory =
            new PropertyDescriptorFactory(new PropertyParserFactory());

    displayNameDescriptor = propertyDescriptorFactory.getComposite(
            propertyDescriptorFactory.getLocal("wwperson_names", PersonNames.class),
            propertyDescriptorFactory.getLocal("wwperson_tempName", TempName.class));
  }


  @Override
  public List<IndexerSortFieldDescription> getSortFieldDescriptions() {
    return sortFieldDescriptions;
  }


  @Override
  public void addIndexedSortProperties(Vertex vertex) {
    for (IndexerSortFieldDescription description : sortFieldDescriptions) {
      PropertyParser parser = description.getParser();

      Comparable<?> parsed = vertex.property(description.getPropertyName()).isPresent() ?
              parser.parseForSort((String) vertex.property(description.getPropertyName()).value()) :
              parser.parseForSort(null);


      String sortPropertyName = description.getSortPropertyName();
      if (parsed == null) {
        if (description.getPropertyName().equals("wwperson_names") &&
                vertex.property("wwperson_tempName").isPresent()) {

          Comparable<?> tempName = new TempNamePropertyParser().parseForSort(
                  (String) vertex.property("wwperson_tempName").value());
          vertex.property(sortPropertyName, tempName);
        } else {
          vertex.property(sortPropertyName, description.getDefaultValue());
        }
      } else {
        vertex.property(sortPropertyName, parsed);
      }
    }
  }

  @Override
  public void addToFulltextIndex(Vertex vertex, GraphDatabaseService graphDatabase) {
    final IndexManager indexManager = graphDatabase.index();
    final Map<String, String> indexConfig = MapUtil.stringMap(IndexManager.PROVIDER, "lucene", "type", "fulltext");
    final Index<Node> index = indexManager.forNodes("wwpersons", indexConfig);
    final String displayName = displayNameDescriptor.get(vertex);
    final String timId = (String) vertex.property("tim_id").value();

    IndexHits<Node> hits = index.get("tim_id", timId);
    while (hits.hasNext()) {
      Node node = hits.next();
      index.remove(node);
    }

    long id = (long) vertex.id();
    Node neo4jNode = graphDatabase.getNodeById(id);
    index.add(neo4jNode, "displayName", displayName == null ? "" : displayName);
    index.add(neo4jNode, "tim_id", timId);
  }

}
