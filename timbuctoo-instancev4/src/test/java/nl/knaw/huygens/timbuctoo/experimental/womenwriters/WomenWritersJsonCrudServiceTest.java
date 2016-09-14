package nl.knaw.huygens.timbuctoo.experimental.womenwriters;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Maps;
import nl.knaw.huygens.timbuctoo.crud.GremlinEntityFetcher;
import nl.knaw.huygens.timbuctoo.crud.InvalidCollectionException;
import nl.knaw.huygens.timbuctoo.crud.NotFoundException;
import nl.knaw.huygens.timbuctoo.model.vre.CollectionBuilder;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import nl.knaw.huygens.timbuctoo.model.vre.vres.VresBuilder;
import nl.knaw.huygens.timbuctoo.security.UserStore;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.junit.Test;

import java.net.URI;
import java.util.UUID;

import static nl.knaw.huygens.timbuctoo.model.properties.PropertyTypes.localProperty;
import static nl.knaw.huygens.timbuctoo.model.properties.converters.Converters.personNames;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsn;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnA;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnO;
import static nl.knaw.huygens.timbuctoo.util.TestGraphBuilder.newGraph;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs;


public class WomenWritersJsonCrudServiceTest {

  private final Vres vres;

  public WomenWritersJsonCrudServiceTest() {
    vres = new VresBuilder()
      .withVre("WomenWriters", "ww", vre -> vre
        .withCollection("wwdocuments", c -> c
          .withProperty("title", localProperty("wwdocument_title"))
          .withProperty("date", localProperty("wwdocument_date"))
        )
        .withCollection("wwkeywords", c -> c
          .withDisplayName(localProperty("displayName"))
          .withProperty("value", localProperty("wwkeyword_value"))
          .withProperty("type", localProperty("wwkeyword_type"))
        )
        .withCollection("wwrelations", CollectionBuilder::isRelationCollection)
        .withCollection("wwlanguages", c -> c
          .withDisplayName(localProperty("wwlanguage_name"))
          .withProperty("name", localProperty("wwlanguage_name"))
        )
        .withCollection("wwcollectives", c -> c
          .withDisplayName(localProperty("wwcollective_name"))
          .withProperty("name", localProperty("wwcollective_name"))
        )
        .withCollection("wwdisplaynames", c -> c
          .withDisplayName(localProperty("wwperson_displayName"))
        )
        .withCollection("wwpersons", c -> c
          .withProperty("name", localProperty("wwperson_name"))
          .withProperty("names", localProperty("wwperson_names", personNames))
          .withDisplayName(localProperty("displayName"))
        )
      )
      .build(Maps.newHashMap());
  }


  @Test
  public void getReturnsAJsonNodeWithARelationsPropertyWithTheGenderOfTheAuthors()
    throws InvalidCollectionException, NotFoundException {
    UUID workId = UUID.randomUUID();
    GraphWrapper graphWrapper = newGraph()
      .withVertex("work1", v -> {
          v.withOutgoingRelation("isCreatedBy", "pers1")
           .withOutgoingRelation("isCreatedBy", "pers2")
           .withVre("ww")
           .withVre("")
           .withType("document")
           .isLatest(true)
           .withTimId(workId.toString());
        }
      )
      .withVertex("pers1", v ->
        v.withVre("ww")
         .withVre("")
         .withType("person")
         .withProperty("displayName", "author1")
         .withProperty("wwperson_gender", "FEMALE")
         .isLatest(true)
         .withTimId(UUID.randomUUID().toString())
      )
      .withVertex("pers2", v ->
        v.withVre("ww")
         .withVre("")
         .withType("person")
         .withProperty("displayName", "author2")
         .withProperty("wwperson_gender", "FEMALE")
         .isLatest(true)
         .withTimId(UUID.randomUUID().toString())
      )
      .withVertex("relationType", v ->
        v.withType("relationType")
         .withVre("")
         .withProperty("relationtype_regularName", "isCreatedBy")
         .withProperty("relationtype_inverseName", "isCreatorOf")
      )
      .wrap();
    WomenWritersJsonCrudService instance = new WomenWritersJsonCrudService(
      graphWrapper,
      vres,
      mock(UserStore.class),
      (collection, id, rev) -> URI.create("http://example.com/"),
      new GremlinEntityFetcher()
    );

    JsonNode result = instance.get("wwdocuments", workId);

    assertThat(result.toString(), sameJSONAs(
      jsnO("@relations", jsnO("isCreatedBy", jsnA(
        jsnO("displayName", jsn("author1"), "gender", jsn("FEMALE")),
        jsnO("displayName", jsn("author2"), "gender", jsn("FEMALE"))
      ))).toString()
      ).allowingAnyArrayOrdering().allowingExtraUnexpectedFields()
    );
  }

  @Test
  public void getReturnsAJsonNodeWithARelationsPropertyWithAuthorsWithTheirGenderOfEachCreatedWork()
    throws Exception {
    UUID pers1Id = UUID.randomUUID();

    GraphWrapper graphWrapper = newGraph()
      .withVertex("work1", v ->
        v.withOutgoingRelation("isCreatedBy", "pers1", r -> r.withIsLatest(true).withAccepted("wwrelation", true))
         .withVre("ww")
         .withVre("")
         .withType("document")
         .isLatest(true)
         .withTimId(UUID.randomUUID().toString())
      )
      .withVertex("work2", v ->
        v.withOutgoingRelation("isCreatedBy", "pers1", r -> r.withIsLatest(true).withAccepted("wwrelation", true))
         .withOutgoingRelation("isCreatedBy", "pers2", r -> r.withIsLatest(true).withAccepted("wwrelation", true))
         .withVre("ww")
         .withVre("")
         .withType("document")
         .isLatest(true)
         .withTimId(UUID.randomUUID().toString())
      )
      .withVertex("pers1", v ->
        v.withVre("ww")
         .withVre("")
         .withType("person")
         .withProperty("displayName", "author1")
         .withProperty("wwperson_gender", "FEMALE")
         .isLatest(true)
         .withTimId(pers1Id.toString())
      )
      .withVertex("pers2", v ->
        v.withVre("ww")
         .withVre("")
         .withType("person")
         .withProperty("displayName", "author2")
         .withProperty("wwperson_gender", "FEMALE")
         .isLatest(true)
         .withTimId(UUID.randomUUID().toString())
      )
      .withVertex("relationType", v ->
        v.withType("relationType")
         .withVre("")
         .withProperty("relationtype_regularName", "isCreatedBy")
         .withProperty("relationtype_inverseName", "isCreatorOf")
      )
      .wrap();
    WomenWritersJsonCrudService instance = new WomenWritersJsonCrudService(
      graphWrapper,
      vres,
      mock(UserStore.class),
      (collection, id, rev) -> URI.create("http://example.com/"),
      new GremlinEntityFetcher()
    );

    JsonNode result = instance.get("wwpersons", pers1Id);

    assertThat(result.toString(), sameJSONAs(
      jsnO("@relations", jsnO("isCreatorOf", jsnA(
        jsnO("authors", jsnA(
          jsnO("displayName", jsn("author1"), "gender", jsn("FEMALE"))
        )),
        jsnO("authors", jsnA(
          jsnO("displayName", jsn("author1"), "gender", jsn("FEMALE")),
          jsnO("displayName", jsn("author2"), "gender", jsn("FEMALE"))
        ))))).toString()
      ).allowingExtraUnexpectedFields().allowingAnyArrayOrdering()
    );
  }

  @Test
  public void getReturnsTheLanguagesOfTheDocumentsWrittenByAPerson()
    throws InvalidCollectionException, NotFoundException {
    UUID pers1Id = UUID.randomUUID();

    GraphWrapper graphWrapper = newGraph()
      .withVertex("work1", v ->
        v.withOutgoingRelation("isCreatedBy", "pers1", r -> r.withIsLatest(true).withAccepted("wwrelation", true))
        .withOutgoingRelation("hasWorkLanguage", "lang1", r -> r.withIsLatest(true).withAccepted("wwrelation", true))
         .withVre("ww")
         .withVre("")
         .withType("document")
         .isLatest(true)
         .withTimId(UUID.randomUUID().toString())
      )
      .withVertex("work2", v ->
        v.withOutgoingRelation("isCreatedBy", "pers1", r -> r.withIsLatest(true).withAccepted("wwrelation", true))
         .withOutgoingRelation("hasWorkLanguage", "lang2", r -> r.withIsLatest(true).withAccepted("wwrelation", true))
         .withVre("ww")
         .withVre("")
         .withType("document")
         .isLatest(true)
         .withTimId(UUID.randomUUID().toString())
      )
      .withVertex("pers1", v ->
        v.withVre("ww")
         .withVre("")
         .withType("person")
         .withProperty("displayName", "author1")
         .withProperty("wwperson_gender", "FEMALE")
         .isLatest(true)
         .withTimId(pers1Id.toString())
      )
      .withVertex("lang1", v ->
        v.withVre("ww")
         .withVre("")
         .withType("language")
         .withProperty("wwlanguage_name", "French")
         .isLatest(true)
         .withTimId(UUID.randomUUID().toString())
      )
      .withVertex("lang2", v ->
        v.withVre("ww")
         .withVre("")
         .withType("language")
         .withProperty("wwlanguage_name", "Dutch")
         .isLatest(true)
         .withTimId(UUID.randomUUID().toString())
      )
      .withVertex("relationType1", v ->
        v.withType("relationType")
         .withVre("")
         .withProperty("relationtype_regularName", "isCreatedBy")
         .withProperty("relationtype_inverseName", "isCreatorOf")
      )
      .withVertex("relationType2", v ->
        v.withType("relationType")
         .withVre("")
         .withProperty("relationtype_regularName", "hasWorkLanguage")
         .withProperty("relationtype_inverseName", "isWorkLanguageOf")
      )
      .wrap();
    WomenWritersJsonCrudService instance = new WomenWritersJsonCrudService(
      graphWrapper,
      vres,
      mock(UserStore.class),
      (collection, id, rev) -> URI.create("http://example.com/"),
      new GremlinEntityFetcher()
    );

    JsonNode result = instance.get("wwpersons", pers1Id);

    assertThat(result.toString(), sameJSONAs(
      jsnO("@authorLanguages", jsnA(jsn("French"), jsn("Dutch"))).toString()
    ).allowingAnyArrayOrdering().allowingExtraUnexpectedFields());
  }

}
