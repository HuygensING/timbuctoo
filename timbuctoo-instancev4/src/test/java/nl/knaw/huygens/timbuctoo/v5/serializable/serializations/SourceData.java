package nl.knaw.huygens.timbuctoo.v5.serializable.serializations;

import nl.knaw.huygens.timbuctoo.v5.datastores.prefixstore.TypeNameStore;
import nl.knaw.huygens.timbuctoo.v5.serializable.Serializable;

import java.util.Collections;
import java.util.Map;

import static com.google.common.collect.ImmutableMap.of;
import static com.google.common.collect.Lists.newArrayList;
import static nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.TypedValue.create;

public class SourceData {
  public static Serializable simpleResult() {
    return new Serializable(of(
      "Persons", of(
        "items", newArrayList(
          of(
            "@id", "http://example.com/1",
            "@type", "http://example.com/Person",
            "a", create("1"),
            "b", newArrayList(
              of("@id", "http://example.com/11", "@type", "http://example.com/SubItem",
                "c", create("2"), "d", newArrayList( create("3"), create("4"), create("4a"))),
              of("@id", "http://example.com/12", "@type", "http://example.com/SubItem",
                "c", create("5"), "d", newArrayList( create("6"), create("7")))
            )
          ),
          of(
            "@id", "http://example.com/2",
            "@type", "http://example.com/Person",
            "a", create("8"),
            "b", newArrayList(
              of("@id", "http://example.com/11", "@type", "http://example.com/SubItem",
                "c", create("9"), "d", newArrayList( 10, "11")), //these are not a TypedValue
              of("@id", "http://example.com/12", "@type", "http://example.com/SubItem",
                "c", create("12"), "d", newArrayList( 13, "14")), //these are not a TypedValue
              of("@id", "http://example.com/13", "@type", "http://example.com/SubItem",
                "c", create("15"), "d", newArrayList( create("16"), create("17"), create("18")))
            )
          ),
          of(
            "@id", "http://example.com/3",
            "@type", "http://example.com/Person",
            "a", create("19"),
            "b", of("@id", "http://example.com/21", "@type", "http://example.com/OtherSubItem",
              "e", create("20"), "f", create("21"))
          )
        )
      )
    ), createTypeNameStore());
  }

  private static TypeNameStore createTypeNameStore() {
    return new TypeNameStore() {
      @Override
      public String makeGraphQlname(String uri) {
        return uri;
      }

      @Override
      public String makeUri(String graphQlName) {
        return graphQlName;
      }

      @Override
      public String shorten(String uri) {
        return uri;
      }

      @Override
      public Map<String, String> getMappings() {
        return Collections.emptyMap();
      }

      @Override
      public void close() throws Exception {

      }

    };
  }

}
