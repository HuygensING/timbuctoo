package nl.knaw.huygens.timbuctoo.search.description.facet;

import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import nl.knaw.huygens.timbuctoo.model.Change;
import nl.knaw.huygens.timbuctoo.search.FacetValue;
import nl.knaw.huygens.timbuctoo.search.description.FacetDescription;
import nl.knaw.huygens.timbuctoo.server.mediatypes.v2.search.DateRangeFacetValue;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.shaded.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

class ChangeRangeFacetDescription implements FacetDescription {

  public static final DateTimeFormatter FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;
  public static final Logger LOG = LoggerFactory.getLogger(ChangeRangeFacetDescription.class);
  private final String facetName;
  private final String propertyName;
  private final ObjectMapper objectMapper;

  public ChangeRangeFacetDescription(String facetName, String propertyName) {
    this.facetName = facetName;
    this.propertyName = propertyName;
    objectMapper = new ObjectMapper();
  }

  @Override
  public String getName() {
    return facetName;
  }

  @Override
  public Facet getFacet(GraphTraversal<Vertex, Vertex> searchResult) {
    List<Long> dateStamps = Lists.newArrayList();
    searchResult.has(propertyName).map(vertexTraverser -> vertexTraverser.get().<String>value(propertyName))
      .<String>forEachRemaining(prop -> {
        try {
          LocalDate localDate = getChangeLocalDate(prop);

          dateStamps.add(Long.valueOf(FORMATTER.format(localDate)));
        } catch (IOException e) {
          LOG.error("'{}' is not a valid change.", prop);
        }
      });

    dateStamps.sort(Long::compareTo);

    long lowerLimit = 0;
    long upperLimit = 0;
    if (!dateStamps.isEmpty()) {
      lowerLimit = dateStamps.get(0);
      upperLimit = dateStamps.get(dateStamps.size() - 1);
    }

    return new Facet(facetName, Lists.newArrayList(new Facet.RangeOption(lowerLimit, upperLimit)), "RANGE");
  }

  @Override
  @SuppressWarnings("unchecked")
  public void filter(GraphTraversal<Vertex, Vertex> graphTraversal, List<FacetValue> facets) {
    Optional<FacetValue> first =
      facets.stream().filter(facetValue -> Objects.equals(facetValue.getName(), facetName)).findFirst();

    if (!first.isPresent()) {
      return;
    }
    FacetValue facetValue = first.get();

    if (!(facetValue instanceof DateRangeFacetValue)) {
      return;
    }

    long lowerLimit = ((DateRangeFacetValue) facetValue).getLowerLimit();
    LocalDate lowerLimitDate = LocalDate.parse("" + lowerLimit, FORMATTER);
    long upperLimit = ((DateRangeFacetValue) facetValue).getUpperLimit();
    LocalDate upperLimitDate = LocalDate.parse(("" + upperLimit), FORMATTER);
    // Use range because the java.time.Period has no way to determine if a date falls in that Period.
    Range<LocalDate> period = Range.closed(lowerLimitDate, upperLimitDate);

    graphTraversal.where(__.has(propertyName, P.test((o1, o2) -> {
      try {
        LocalDate localDate = getChangeLocalDate(o1);

        return ((Range<LocalDate>) o2).contains(localDate);
      } catch (IOException e) {
        LOG.error("Date {} cannot be parsed.", o1);
      }
      return false;
    }, period)));

  }

  @Override
  public List<String> getValues(Vertex vertex) {
    if(vertex.property(propertyName).isPresent()) {
      return Lists.newArrayList((String) vertex.property(propertyName).value());
    }
    return null;
  }

  @Override
  public Facet getFacet(Map<String, Set<Vertex>> values) {
    return null;
  }

  private LocalDate getChangeLocalDate(Object changeObjectString) throws IOException {
    Change change = objectMapper.readValue("" + changeObjectString, Change.class);
    return Instant.ofEpochMilli(change.getTimeStamp()).atZone(ZoneId.systemDefault()).toLocalDate();
  }
}
