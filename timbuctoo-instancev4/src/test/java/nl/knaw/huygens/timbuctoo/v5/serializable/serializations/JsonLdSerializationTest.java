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
      "    \"Persons\" : [ { }, {\n" +
      "      \"@id\" : \"http://example.com/1\",\n" +
      "      \"@type\" : \"http://example.com/Person\",\n" +
      "      \"a\" : 1,\n" +
      "      \"b\" : [ {\n" +
      "        \"prevCursor\" : \"next\"\n" +
      "      }, {\n" +
      "        \"@id\" : \"http://example.com/11\",\n" +
      "        \"@type\" : \"http://example.com/SubItem\",\n" +
      "        \"c\" : \"2\",\n" +
      "        \"d\" : [ { }, \"3\", \"4\", null ]\n" +
      "      }, {\n" +
      "        \"@id\" : \"http://example.com/12\",\n" +
      "        \"@type\" : \"http://example.com/SubItem\",\n" +
      "        \"c\" : \"5\",\n" +
      "        \"d\" : [ { }, \"6\", \"7\" ]\n" +
      "      } ]\n" +
      "    }, {\n" +
      "      \"@id\" : \"http://example.com/2\",\n" +
      "      \"@type\" : \"http://example.com/Person\",\n" +
      "      \"a\" : \"8\",\n" +
      "      \"b\" : [ { }, {\n" +
      "        \"@id\" : \"http://example.com/11\",\n" +
      "        \"@type\" : \"http://example.com/SubItem\",\n" +
      "        \"c\" : \"9\",\n" +
      "        \"d\" : [ { }, 10, \"11\" ]\n" +
      "      }, {\n" +
      "        \"@id\" : \"http://example.com/12\",\n" +
      "        \"@type\" : \"http://example.com/SubItem\",\n" +
      "        \"c\" : \"12\",\n" +
      "        \"d\" : [ { }, 13.0, \"14\" ]\n" +
      "      }, {\n" +
      "        \"@id\" : \"http://example.com/13\",\n" +
      "        \"@type\" : \"http://example.com/SubItem\",\n" +
      "        \"c\" : \"15\",\n" +
      "        \"d\" : [ { }, \"16\", \"17\", \"18\" ]\n" +
      "      } ]\n" +
      "    }, {\n" +
      "      \"@id\" : \"http://example.com/3\",\n" +
      "      \"@type\" : \"http://example.com/Person\",\n" +
      "      \"a\" : \"19\",\n" +
      "      \"b\" : {\n" +
      "        \"@id\" : \"http://example.com/21\",\n" +
      "        \"@type\" : \"http://example.com/OtherSubItem\",\n" +
      "        \"e\" : \"20\",\n" +
      "        \"f\" : \"21\"\n" +
      "      }\n" +
      "    } ]\n" +
      "  },\n" +
      "  \"@context\" : {\n" +
      "    \"data\" : {\n" +
      "      \"@id\" : \"@graph\",\n" +
      "      \"@container\" : \"@index\"\n" +
      "    },\n" +
      "    \"a\" : \"http://example.org/b\",\n" +
      "    \"b\" : \"http://example.org/b\",\n" +
      "    \"c\" : \"http://example.org/c\",\n" +
      "    \"d\" : {\n" +
      "      \"@reverse\" : \"http://example.org/d\"\n" +
      "    },\n" +
      "    \"d\" : \"http://example.org/d\",\n" +
      "    \"a\" : \"http://example.org/a\",\n" +
      "    \"e\" : \"http://example.org/e\",\n" +
      "    \"f\" : \"http://example.org/f\"\n" +
      "  }\n" +
      "}"));
  }

}
