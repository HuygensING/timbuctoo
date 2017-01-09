package nl.knaw.huygens.timbuctoo.rml;

import com.fasterxml.jackson.databind.node.ObjectNode;
import nl.knaw.huygens.contractdiff.diffresults.DiffResult;
import nl.knaw.huygens.contractdiff.jsondiff.JsonDiffer;
import nl.knaw.huygens.timbuctoo.bulkupload.parsingstatemachine.ImportPropertyDescriptions;
import nl.knaw.huygens.timbuctoo.bulkupload.savers.TinkerpopSaver;
import nl.knaw.huygens.timbuctoo.core.TransactionEnforcer;
import nl.knaw.huygens.timbuctoo.core.TransactionState;
import nl.knaw.huygens.timbuctoo.crud.InvalidCollectionException;
import nl.knaw.huygens.timbuctoo.crud.JsonCrudService;
import nl.knaw.huygens.timbuctoo.model.properties.JsonMetadata;
import nl.knaw.huygens.timbuctoo.model.vre.vres.DatabaseConfiguredVres;
import nl.knaw.huygens.timbuctoo.rml.jena.JenaBasedReader;
import nl.knaw.huygens.timbuctoo.security.UserStore;
import nl.knaw.huygens.timbuctoo.server.TinkerPopGraphManager;
import nl.knaw.huygens.timbuctoo.server.UriHelper;
import nl.knaw.huygens.timbuctoo.server.databasemigration.ScaffoldMigrator;
import nl.knaw.huygens.timbuctoo.server.endpoints.v2.bulkupload.DataSourceFactory;
import nl.knaw.huygens.timbuctoo.server.endpoints.v2.bulkupload.ExecuteRml;
import nl.knaw.huygens.timbuctoo.server.security.UserPermissionChecker;
import nl.knaw.huygens.timbuctoo.util.Neo4jHelper;
import org.apache.jena.ext.com.google.common.collect.ImmutableList;
import org.apache.jena.ext.com.google.common.collect.ImmutableMap;
import org.apache.tinkerpop.gremlin.neo4j.process.traversal.LabelP;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Ignore;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static nl.knaw.huygens.timbuctoo.core.TransactionEnforcerStubs.forGraphWrapper;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsn;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnA;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnO;
import static nl.knaw.huygens.timbuctoo.util.TestGraphBuilder.newSlowPrivateGraph;
import static nl.knaw.huygens.timbuctoo.util.Tuple.tuple;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class RmlIntegrationTest {

  public static final ObjectNode RML_JSON_LD_CONTEXT = jsnO(
    tuple("@vocab", jsn("http://www.w3.org/ns/r2rml#")),
    tuple("rml", jsn("http://semweb.mmlab.be/ns/rml#")),
    tuple("tim", jsn("http://timbuctoo.huygens.knaw.nl/mapping#")),
    tuple("http://www.w3.org/2000/01/rdf-schema#subClassOf", jsnO(
      "@type", jsn("@id")
    )),
    tuple("predicate", jsnO(
      "@type", jsn("@id")
    )),
    tuple("termType", jsnO(
      "@type", jsn("@id")
    )),
    tuple("parentTriplesMap", jsnO(
      "@type", jsn("@id")
    )),
    tuple("class", jsnO(
      "@type", jsn("@id")
    ))
  );

  @Test
  public void happyPath() throws Exception {
    IntegrationTester tester = new IntegrationTester();
    String p1id = "00000000-0000-0000-0000-000000000000";
    String p2id = "11111111-0000-0000-0000-000000000000";
    tester.executeRawUpload("someVre", "persons", ImmutableList.of(
      ImmutableMap.of(
        "id", p1id,
        "naam", "Karel",
        "geboorteplaats", "Rotterdam"
      ),
      ImmutableMap.of(
        "id", p2id,
        "naam", "Frits",
        "geboorteplaats", "Amsterdam"
      )
    ));
    Response response = tester.executeRml("someVre", jsnO(
      "@context", RML_JSON_LD_CONTEXT,
      "@graph", jsnA(
        jsnO(
          "@id", jsn("http://timbuctoo.huygens.knaw.nl/mapping/someVre/person"),
          "http://www.w3.org/2000/01/rdf-schema#subClassOf", jsn("http://timbuctoo.huygens.knaw.nl/person"),
          "rml:logicalSource", jsnO(
            "rml:source", jsnO(
              "tim:vreName", jsn("someVre"),
              "tim:rawCollection", jsn("persons")
            )
          ),
          "subjectMap", jsnO(
            "class", jsn("http://timbuctoo.huygens.knaw.nl/mapping/someVre/person"),
            "template", jsn("http://timbuctoo.huygens.knaw.nl/mapping/someVre/person/{id}")
          ),
          "predicateObjectMap", jsnA(
            jsnO(
              "predicate", jsn("http://timbuctoo.huygens.knaw.nl/id"),
              "objectMap", jsnO(
                "column", jsn("id")
              )
            ),
            jsnO(
              "predicate", jsn("http://timbuctoo.huygens.knaw.nl/hasBirthPlace"),
              "objectMap", jsnO(
                "column", jsn("geboorteplaats")
              )
            ),
            jsnO(
              "predicate", jsn("http://timbuctoo.huygens.knaw.nl/name"),
              "objectMap", jsnO(
                "column", jsn("naam")
              )
            )
          )
        )
      )
    ));
    assertThat(response.getStatus(), is(200));

    // try (Transaction tx = tester.graphManager.getGraph().tx()) {
    //   tx.open();
    //   tester.dumpDb();
    // }

    JsonDiffer differ = new JsonDiffer.JsonDifferBuilder().build();

    tester.getData(crudService -> {
      try {
        List<ObjectNode> persons = crudService.getCollection("someVrepersons", 10, 0, false);
        assertThat(persons, hasSize(2));
        String person1id = persons.get(0).get("id").asText();
        final int person2Index;
        final int person3Index;
        if (person1id.equals(p1id)) {
          person2Index = 0;
          person3Index = 1;
        } else {
          person2Index = 1;
          person3Index = 0;
        }

        DiffResult diffResult = differ.diff(
          jsnO(
            "2", persons.get(person2Index)
          ),
          jsnO(
            "2", jsnO(
              tuple("@type", jsn("someVreperson")),
              tuple("_id", jsn(p1id)),
              tuple("^rev", jsn(1)),
              tuple("^deleted", jsn(false)),
              tuple("^pid", jsn()),
              tuple("^rdfUri", jsn("http://timbuctoo.huygens.knaw.nl/mapping/someVre/person/" + p1id)),
              tuple("name", jsn("Karel")),
              tuple("hasBirthPlace", jsn("Rotterdam")),
              tuple("id", jsn(p1id)),
              tuple("@variationRefs", jsnO(
                "custom-matcher", jsn("/*ALL_MATCH_ONE_OF*/"),
                "keyProp", jsn("type"),
                "possibilities", jsnO(
                  "someVreunknown", jsnO(
                    tuple("id", jsn(p1id)),
                    tuple("type", jsn("someVreunknown"))
                  ),
                  "someVreperson", jsnO(
                    tuple("id", jsn(p1id)),
                    tuple("type", jsn("someVreperson"))
                  ),
                  "person", jsnO(
                    tuple("id", jsn(p1id)),
                    tuple("type", jsn("person"))
                  )
                )
              )),
              tuple("^rdfAlternatives", jsnA()),
              tuple("^modified", jsnO(
                tuple("timeStamp", jsn("/*NUMBER*/")),
                tuple("userId", jsn("rdf-importer")),
                tuple("vreId", jsn())
              )),
              tuple("^created", jsnO(
                tuple("timeStamp", jsn("/*NUMBER*/")),
                tuple("userId", jsn("rdf-importer")),
                tuple("vreId", jsn())
              ))
            )
          )
        );

        if (!diffResult.wasSuccess()) {
          System.out.println(diffResult.asConsole());
        }
        assertThat(diffResult.wasSuccess(), is(true));
      } catch (InvalidCollectionException e) {
        throw new RuntimeException(e);
      }
    });

    tester.getMetadata(metadataSupplier -> {
      ObjectNode metadata = metadataSupplier.getForVre("someVre", true);
      DiffResult diffResult = differ.diff(metadata, jsnO(
        "someVrerelations", jsnO(
          tuple("collectionName", jsn("someVrerelations")),
          tuple("collectionLabel", jsn("someVrerelations")),
          tuple("unknown", jsn(false)),
          tuple("relationCollection", jsn(true)),
          tuple("archetypeName", jsn("relation")),
          tuple("properties", jsnA())
        ),
        "someVrepersons", jsnO(
          tuple("collectionName", jsn("someVrepersons")),
          tuple("collectionLabel", jsn("person")),
          tuple("unknown", jsn(false)),
          tuple("relationCollection", jsn(false)),
          tuple("archetypeName", jsn("person")),
          tuple("properties", jsnO(
            "custom-matcher", jsn("/*ALL_MATCH_ONE_OF*/"),
            "possibilities", jsnO(
              tuple("id", jsnO("name", jsn("id"), "type", jsn("text"))),
              tuple("hasBirthPlace", jsnO("name", jsn("hasBirthPlace"), "type", jsn("text"))),
              tuple("name", jsnO("name", jsn("name"), "type", jsn("text"))),
              tuple("isFirstPersonInRelation", jsnO()),
              tuple("isSecondPersonInRelation", jsnO()),
              tuple("hasPersonState", jsnO()),
              tuple("hasDataLine", jsnO()),
              tuple("hasScientistBio", jsnO())
            ),
            "keyProp", jsn("name")
          ))
        ),
        "someVreunknowns", jsnO(
          tuple("archetypeName", jsn("concept")),
          tuple("relationCollection", jsn(false)),
          tuple("collectionLabel", jsn("unknown")),
          tuple("collectionName", jsn("someVreunknowns")),
          tuple("unknown", jsn(true))
        )
      ));
      if (!diffResult.wasSuccess()) {
        System.out.println(diffResult.asConsole());
      }
      assertThat(diffResult.wasSuccess(), is(true));
    });



  }

  @Test
  @Ignore
  public void handlesSameAsRelations() throws Exception {
    IntegrationTester tester = new IntegrationTester();
    tester.executeRawUpload("someVre", "persons", ImmutableList.of(
      ImmutableMap.of(
        "id", "2",
        "naam", "Karel",
        "geboorteplaats", "Rotterdam"
      )
    ));
    Response response = tester.executeRml("someVre", jsnO(
      "@context", RML_JSON_LD_CONTEXT,
      "@graph", jsnA(
        jsnO(
          "@id", jsn("http://timbuctoo.huygens.knaw.nl/mapping/someVre/persons"),
          "http://www.w3.org/2000/01/rdf-schema#subClassOf", jsn("http://timbuctoo.huygens.knaw.nl/person"),
          "rml:logicalSource", jsnO(
            "rml:source", jsnO(
              "tim:vreName", jsn("someVre"),
              "tim:rawCollection", jsn("persons")
            )
          ),
          "subjectMap", jsnO(
            "class", jsn("http://timbuctoo.huygens.knaw.nl/mapping/someVre/persons"),
            "template", jsn("http://timbuctoo.huygens.knaw.nl/mapping/someVre/persons/{tim_id}")
          ),
          "predicateObjectMap", jsnA(
            jsnO(
              "predicate", jsn("http://timbuctoo.huygens.knaw.nl/hasBirthPlace"),
              "objectMap", jsnO(
                "column", jsn("geboorteplaats")
              )
            ),
            jsnO(
              "predicate", jsn("http://www.w3.org/2002/07/owl#sameAs"),
              "objectMap", jsnO(
                "template", jsn("http://timbuctoo.huygens.knaw.nl/mapping/someVre/persons/local/{id}")
              )
            )
          )
        )
      )
    ));
    assertThat(response.getStatus(), is(200));
    GraphTraversal<Vertex, Vertex> karel = tester.graphManager.getGraph().traversal().V()
      .has(T.label, LabelP.of("someVrepersonss"));
    assertThat(karel.asAdmin().clone().count().next(), is(1L));
    String timId = (String) karel.asAdmin().clone().values("tim_id").next();
    assertThat(
      karel.asAdmin().clone().values("rdfUri").next(),
      is("http://timbuctoo.huygens.knaw.nl/mapping/someVre/persons/" + timId)
    );
    assertThat(karel.asAdmin().clone().values("rdfAlternatives").next(), is(new String[]{
      "http://timbuctoo.huygens.knaw.nl/mapping/someVre/persons/local/2"
    }));
  }


  public class IntegrationTester {
    private final TinkerPopGraphManager graphManager;
    private final TransactionEnforcer transactionEnforcer;
    private final DatabaseConfiguredVres vres;

    public IntegrationTester() {
      graphManager = newSlowPrivateGraph().wrap();
      transactionEnforcer = forGraphWrapper(graphManager);
      new ScaffoldMigrator(graphManager).execute();
      vres = new DatabaseConfiguredVres(transactionEnforcer);
    }


    public void executeRawUpload(String vreName, String collectionName, List<Map<String, String>> data) {
      try (TinkerpopSaver rawSaver = new TinkerpopSaver(vres, graphManager, vreName, vreName, 100, vreName)) {
        Vertex rawCollection = rawSaver.addCollection(collectionName);
        ImportPropertyDescriptions importPropertyDescriptions = new ImportPropertyDescriptions();
        int count = 0;
        for (String key : data.get(0).keySet()) {
          importPropertyDescriptions.getOrCreate(count++).setPropertyName(key);
        }
        rawSaver.addPropertyDescriptions(rawCollection, importPropertyDescriptions);
        for (Map<String, String> map : data) {
          rawSaver.addEntity(rawCollection, map);
        }
      }
    }

    public void dumpDb() {
      Neo4jHelper.dumpDb(graphManager.getGraphDatabase());
    }

    public Response executeRml(String vreName, ObjectNode mapping) throws InterruptedException {
      UserPermissionChecker alwaysAllowed = mock(UserPermissionChecker.class);
      given(alwaysAllowed.check(any(), any())).willReturn(UserPermissionChecker.UserPermission.ALLOWED_TO_WRITE);

      ExecuteRml executeRml = new ExecuteRml(
        new UriHelper("http://example.com"),
        graphManager,
        vres,
        new JenaBasedReader(),
        alwaysAllowed,
        new DataSourceFactory(graphManager),
        transactionEnforcer
      );

      Response result = executeRml.post(
        mapping.toString(),
        vreName,
        "blaat"
      );

      Thread.sleep(1000); //FIXME: improve
      return result;
    }

    public void getData(Consumer<JsonCrudService> consumer) {
      transactionEnforcer.execute(timbuctooActions -> {
        final JsonCrudService crudService = new JsonCrudService(
          vres,
          mock(UserStore.class),
          (collection, id, rev) -> null,
          timbuctooActions
        );
        consumer.accept(crudService);
        return TransactionState.commit();
      });
    }

    public void getMetadata(Consumer<JsonMetadata> consumer) {
      //will be wrapped in a transactionEnforcer call as well eventually
      final JsonMetadata metadata = new JsonMetadata(vres, graphManager);
      consumer.accept(metadata);
    }
  }
}
