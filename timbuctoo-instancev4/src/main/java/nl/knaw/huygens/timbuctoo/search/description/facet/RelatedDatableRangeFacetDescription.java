package nl.knaw.huygens.timbuctoo.search.description.facet;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import nl.knaw.huygens.timbuctoo.model.Datable;
import nl.knaw.huygens.timbuctoo.search.FacetValue;
import nl.knaw.huygens.timbuctoo.search.description.FacetDescription;
import nl.knaw.huygens.timbuctoo.server.mediatypes.v2.search.DateRangeFacetValue;
import org.apache.commons.lang.StringUtils;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;


public class RelatedDatableRangeFacetDescription implements FacetDescription {
  public static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyyMMdd");
  public static final SimpleDateFormat FILTER_FORMAT = new SimpleDateFormat("yyyy");
  public static final Logger LOG = LoggerFactory.getLogger(DatableRangeFacetDescription.class);
  private final String facetName;
  private final String propertyName;
  private final String[] relations;

  public RelatedDatableRangeFacetDescription(String facetName, String propertyName, String... relations) {
    this.facetName = facetName;
    this.propertyName = propertyName;
    this.relations = relations;
  }

  private Datable getDatable(String datableAsString) {
    String value = StringUtils.strip(datableAsString, "\"");

    return new Datable(value);
  }

  @Override
  public String getName() {
    return facetName;
  }

  @Override
  public Facet getFacet(Map<String, Set<Vertex>> values) {
    long lowerLimit = 0;
    long upperLimit = 0;

    for (String key : values.keySet()) {
      Datable datable = getDatable(key);
      if (datable.isValid()) {
        long fromDate = Long.valueOf(FORMAT.format(datable.getFromDate()));
        long toDate = Long.valueOf(FORMAT.format(datable.getFromDate()));
        if (toDate > upperLimit) {
          upperLimit = toDate;
        }
        if (lowerLimit == 0 || fromDate < lowerLimit) {
          lowerLimit = fromDate;
        }
      }
    }

    return new Facet(facetName, Lists.newArrayList(new Facet.RangeOption(lowerLimit, upperLimit)), "RANGE");
  }

  @Override
  public void filter(GraphTraversal<Vertex, Vertex> graphTraversal, List<FacetValue> facets) {
    Optional<FacetValue> first = facets.stream()
            .filter(facetValue -> Objects.equals(facetValue.getName(), facetName))
            .findFirst();

    if (!first.isPresent()) {
      return;
    }
    FacetValue facetValue = first.get();

    if (!(facetValue instanceof DateRangeFacetValue)) {
      return;
    }

    // pad the strings to make them parsable
    String lowerLimitString =
            Strings.padStart("" + ((DateRangeFacetValue) facetValue).getLowerLimit(), 8, '0').substring(0, 4);
    String upperLimitString =
            Strings.padStart("" + ((DateRangeFacetValue) facetValue).getUpperLimit(), 8, '0').substring(0, 4);

    try {
      Range<Date> range = Range.closed(FILTER_FORMAT.parse(lowerLimitString), FILTER_FORMAT.parse(upperLimitString));

      graphTraversal.where(__.bothE(relations).otherV().has(propertyName, P.test((o1, o2) -> {
        Datable datable = getDatable("" + o1);
        if (!datable.isValid()) {
          return false;
        }

        Range<Date> range1 = (Range<Date>) o2;
        return range1.contains(datable.getFromDate()) || range1.contains(datable.getToDate());
      }, range)));
    } catch (ParseException e) {
      LOG.error("Cannot parse date", e);
    }

  }

  @Override
  public List<String> getValues(Vertex vertex) {
    List<String> result = new ArrayList<>();
    vertex.vertices(Direction.BOTH, relations).forEachRemaining(targetVertex -> {
      if (targetVertex.property(propertyName).isPresent()) {
        result.add((String) targetVertex.property(propertyName).value());
      }
    });
    return result;
  }
}
