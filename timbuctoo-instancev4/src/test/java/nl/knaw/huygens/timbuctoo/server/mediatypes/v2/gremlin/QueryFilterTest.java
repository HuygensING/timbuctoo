package nl.knaw.huygens.timbuctoo.server.mediatypes.v2.gremlin;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class QueryFilterTest {

  @Test
  public void mapsJsonToQuery() throws IOException {
    String json = "{" +
            "\"or\": [{" +
            "\"type\": \"entity\"," +
            "\"domain\": \"wwdocument\"," +
            "\"and\": [{" +
            "\"name\": \"hasTranslation\"," +
            "\"type\": \"relation\"," +
            "\"direction\": \"in\"," +
            "\"or\": [{" +
            "\"type\": \"entity\"," +
            "\"domain\": \"wwdocument\"," +
            "\"and\": [{" +
            "\"name\": \"hasPublishLocation\"," +
            "\"type\": \"relation\"," +
            "\"direction\": \"out\"," +
            "\"or\": [{" +
            "\"type\": \"entity\"," +
            "\"domain\": \"wwlocation\"," +
            "\"and\": []" +
            "}]," +
            "\"targetDomain\": \"wwlocation\"" +
            "}]" +
            "}]," +
            "\"targetDomain\": \"wwdocument\"" +
            "}, {" +
            "\"type\": \"property\"," +
            "\"name\": \"tim_id\"," +
            "\"or\": [{" +
            "\"type\": \"value\"," +
            "\"value\": \"c65b9d97-11d6-4325-a522-276ea48eee77\"," +
            "\"label\": \"Johnston, Charlotte C. - The Master Mosaic Workers (1895)\"" +
            "}]" +
            "}, {" +
            "\"type\": \"property\"," +
            "\"name\": \"date\"," +
            "\"or\": [{" +
            "\"type\": \"between\"," +
            "\"values\": [" +
            "\"1800\"," +
            "\"1900\"" +
            "]," +
            "\"label\": \"between(1800, 1900)\"" +
            "}]" +
            "}]" +
            "}]" +
            "}";

    ObjectMapper mapper = new ObjectMapper();
    RootQuery underTest = mapper.readValue(json, RootQuery.class);
    RelationFilter relationFilter = (RelationFilter) underTest.getOr().get(0).getAnd().get(0);
    CollectionQuery collectionQuery = (CollectionQuery) relationFilter.getOr().get(0);
    PropertyFilter propertyFilter = (PropertyFilter) underTest.getOr().get(0).getAnd().get(2);
    PropertyValueFilter propertyValueFilter = propertyFilter.getOr().get(0);

    assertThat(underTest.getOr().get(0).getDomain()).isEqualTo("wwdocument");
    assertThat(relationFilter.getName()).isEqualTo("hasTranslation");
    assertThat(collectionQuery.getDomain()).isEqualTo("wwdocument");
    assertThat(propertyFilter.getType()).isEqualTo("property");
    assertThat(propertyValueFilter.getType()).isEqualTo("between");
  }
}
