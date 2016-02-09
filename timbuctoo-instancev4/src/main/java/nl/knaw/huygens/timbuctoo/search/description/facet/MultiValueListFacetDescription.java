package nl.knaw.huygens.timbuctoo.search.description.facet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import nl.knaw.huygens.timbuctoo.search.description.FacetDescription;
import nl.knaw.huygens.timbuctoo.search.description.facet.Facet.DefaultOption;
import nl.knaw.huygens.timbuctoo.search.description.facet.Facet.Option;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.toList;

public class MultiValueListFacetDescription implements FacetDescription {
  public static final Logger LOG = LoggerFactory.getLogger(MultiValueListFacetDescription.class);
  private final String facetName;
  private final String propertyName;
  private final ObjectMapper mapper;

  public MultiValueListFacetDescription(String facetName, String propertyName) {
    this.facetName = facetName;
    this.propertyName = propertyName;
    mapper = new ObjectMapper();
  }

  @Override
  public Facet getFacet(GraphTraversal<Vertex, Vertex> searchResult) {
    List<List<?>> vertexValues = searchResult.has(propertyName).map(v -> {
      String value = v.get().value(propertyName);

      try {
        List<?> list = mapper.readValue(value, List.class);

        return list;
      } catch (IOException e) {
        LOG.error("'{}' is not a valid multi valued field", value);
      }
      return Lists.newArrayList();
    }).toList();


    List<Option> options =
      vertexValues.stream().flatMap(Collection::stream)
                  .collect(Collectors.groupingBy(v -> v, counting()))
                  .entrySet().stream()
                  .map(entry -> new DefaultOption(entry.getKey().toString(), entry.getValue()))
                  .collect(toList());

    return new Facet(facetName, options, "LIST");
  }
}
