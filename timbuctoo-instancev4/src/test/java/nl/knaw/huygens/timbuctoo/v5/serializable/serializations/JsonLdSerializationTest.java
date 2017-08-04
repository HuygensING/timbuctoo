package nl.knaw.huygens.timbuctoo.v5.serializable.serializations;

import org.junit.Test;

import java.io.ByteArrayOutputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class JsonLdSerializationTest {

  @Test
  public void performSerialization() throws Exception {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    JsonLdSerialization cs = new JsonLdSerialization(out);

    cs.serialize(SourceData.simpleResult());

    assertThat(out.toString(), is("{\n" +
      "  \"data\" : {\n" +
      "    \"Persons\" : {\n" +
      "      \"items\" : [ {\n" +
      "        \"@id\" : \"http://example.com/1\",\n" +
      "        \"@type\" : \"http://example.com/Person\",\n" +
      "        \"a\" : 1,\n" +
      "        \"b\" : {\n" +
      "          \"items\" : [ {\n" +
      "            \"@id\" : \"http://example.com/11\",\n" +
      "            \"@type\" : \"http://example.com/SubItem\",\n" +
      "            \"c\" : \"2\",\n" +
      "            \"d\" : {\n" +
      "              \"items\" : [ \"3\", \"4\", null ]\n" +
      "            }\n" +
      "          }, {\n" +
      "            \"@id\" : \"http://example.com/12\",\n" +
      "            \"@type\" : \"http://example.com/SubItem\",\n" +
      "            \"c\" : \"5\",\n" +
      "            \"d\" : {\n" +
      "              \"items\" : [ \"6\", \"7\" ]\n" +
      "            }\n" +
      "          } ]\n" +
      "        }\n" +
      "      }, {\n" +
      "        \"@id\" : \"http://example.com/2\",\n" +
      "        \"@type\" : \"http://example.com/Person\",\n" +
      "        \"a\" : \"8\",\n" +
      "        \"b\" : {\n" +
      "          \"items\" : [ {\n" +
      "            \"@id\" : \"http://example.com/11\",\n" +
      "            \"@type\" : \"http://example.com/SubItem\",\n" +
      "            \"c\" : \"9\",\n" +
      "            \"d\" : {\n" +
      "              \"items\" : [ 10, \"11\" ]\n" +
      "            }\n" +
      "          }, {\n" +
      "            \"@id\" : \"http://example.com/12\",\n" +
      "            \"@type\" : \"http://example.com/SubItem\",\n" +
      "            \"c\" : \"12\",\n" +
      "            \"d\" : {\n" +
      "              \"items\" : [ 13.0, \"14\" ]\n" +
      "            }\n" +
      "          }, {\n" +
      "            \"@id\" : \"http://example.com/13\",\n" +
      "            \"@type\" : \"http://example.com/SubItem\",\n" +
      "            \"c\" : \"15\",\n" +
      "            \"d\" : {\n" +
      "              \"items\" : [ \"16\", \"17\", \"18\" ]\n" +
      "            }\n" +
      "          } ]\n" +
      "        }\n" +
      "      }, {\n" +
      "        \"@id\" : \"http://example.com/3\",\n" +
      "        \"@type\" : \"http://example.com/Person\",\n" +
      "        \"a\" : \"19\",\n" +
      "        \"b\" : {\n" +
      "          \"@id\" : \"http://example.com/21\",\n" +
      "          \"@type\" : \"http://example.com/OtherSubItem\",\n" +
      "          \"e\" : \"20\",\n" +
      "          \"f\" : \"21\"\n" +
      "        }\n" +
      "      } ]\n" +
      "    }\n" +
      "  },\n" +
      "  \"@context\" : {\n" +
      "    \"data\" : {\n" +
      "      \"@id\" : \"@graph\"\n" +
      "    },\n" +
      "    \"Persons\" : {\n" +
      "      \"@id\" : \"http://timbuctoo.huygens.knaw.nl/queryContainer#Persons\",\n" +
      "      \"@container\" : \"@index\"\n" +
      "    },\n" +
      "    \"a\" : \"http://example.org/b\",\n" +
      "    \"b\" : {\n" +
      "      \"@id\" : \"http://example.org/b\",\n" +
      "      \"@container\" : \"@index\"\n" +
      "    },\n" +
      "    \"a\" : \"http://example.org/a\",\n" +
      "    \"f\" : \"http://example.org/f\",\n" +
      "    \"e\" : \"http://example.org/e\",\n" +
      "    \"d\" : {\n" +
      "      \"@id\" : \"http://example.org/d\",\n" +
      "      \"@container\" : \"@index\"\n" +
      "    },\n" +
      "    \"c\" : \"http://example.org/c\"\n" +
      "  }\n" +
      "}"));
  }

}
