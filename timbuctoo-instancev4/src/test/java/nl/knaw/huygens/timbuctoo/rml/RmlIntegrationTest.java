package nl.knaw.huygens.timbuctoo.rml;

import com.fasterxml.jackson.databind.node.ObjectNode;
import nl.knaw.huygens.timbuctoo.bulkupload.parsingstatemachine.ImportPropertyDescriptions;
import nl.knaw.huygens.timbuctoo.bulkupload.savers.TinkerpopSaver;
import nl.knaw.huygens.timbuctoo.crud.HandleAdder;
import nl.knaw.huygens.timbuctoo.database.ChangeListener;
import nl.knaw.huygens.timbuctoo.database.DataAccess;
import nl.knaw.huygens.timbuctoo.model.vre.vres.DatabaseConfiguredVres;
import nl.knaw.huygens.timbuctoo.rml.jena.JenaBasedReader;
import nl.knaw.huygens.timbuctoo.server.TinkerpopGraphManager;
import nl.knaw.huygens.timbuctoo.server.UriHelper;
import nl.knaw.huygens.timbuctoo.server.databasemigration.ScaffoldMigrator;
import nl.knaw.huygens.timbuctoo.server.endpoints.v2.bulkupload.DataSourceFactory;
import nl.knaw.huygens.timbuctoo.server.endpoints.v2.bulkupload.ExecuteRml;
import nl.knaw.huygens.timbuctoo.server.security.UserPermissionChecker;
import org.apache.jena.ext.com.google.common.collect.ImmutableList;
import org.apache.jena.ext.com.google.common.collect.ImmutableMap;
import org.apache.tinkerpop.gremlin.neo4j.process.traversal.LabelP;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Ignore;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsn;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnA;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnO;
import static nl.knaw.huygens.timbuctoo.util.TestGraphBuilder.newGraph;
import static nl.knaw.huygens.timbuctoo.util.Tuple.tuple;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;

public class RmlIntegrationTest {

  public static final ObjectNode RML_JSON_LD_CONTEXT = jsnO(
    tuple("@vocab", jsn("http://www.w3.org/ns/r2rml#")),
    tuple("rml", jsn("http://semweb.mmlab.be/ns/rml#")),
    tuple("tim", jsn("http://timbuctoo.com/mapping#")),
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
  public void canLinkToExternalCollections() throws Exception {
    IntegrationTester tester = new IntegrationTester();

    tester.executeRawUpload("locaties", "locaties", ImmutableList.of(
      ImmutableMap.of("naam", "Vlissingen")
    ));
    Response response = tester.executeRml("locaties", jsnO(
      "@context", RML_JSON_LD_CONTEXT,
      "@graph", jsnA(
        jsnO(
          "@id", jsn("http://timbuctoo.com/mapping/locaties/locaties"),
          "http://www.w3.org/2000/01/rdf-schema#subClassOf", jsn("http://timbuctoo.com/location"),
          "rml:logicalSource", jsnO(
            "rml:source", jsnO(
              "tim:rawCollection", jsn("locaties"),
              "tim:vreName", jsn("locaties")
            )
          ),
          "subjectMap", jsnO(
            "class", jsn("http://timbuctoo.com/mapping/locaties/locaties"),
            "template", jsn("http://timbuctoo.com/mapping/locaties/locaties/{tim_id}")
          ),
          "predicateObjectMap", jsnA(
            jsnO(
              "objectMap", jsnO(
                "column", jsn("naam")
              ),
              "predicate", jsn("http://timbuctoo.com/name")
            )
          )
        )
      )
    ));
    assertThat(response.getStatus(), is(200));

    //one location should have been created
    List<Vertex> locaties = tester.traversalSource.V().has(T.label, LabelP.of("locaties")).toList();
    assertThat(locaties.size(), is(1));
    Vertex locatie = locaties.get(0);

    //with an rdf url
    String rdfUri = locatie.value("rdfUri");

    //Both the own property and the abstract property should be set
    String locatiesName = locatie.value("locaties_name");
    String locationName = locatie.value("location_name");
    assertThat(locatiesName, is(locationName));

    tester.executeRawUpload("migraties", "migranten", ImmutableList.of(
      ImmutableMap.of(
        "naam", "Karel",
        "geboorteplaats", rdfUri
      )
    ));
    response = tester.executeRml("migraties", jsnO(
      "@context", RML_JSON_LD_CONTEXT,
      "@graph", jsnA(
        jsnO(
          "@id", jsn("http://timbuctoo.com/mapping/migraties/migranten"),
          "http://www.w3.org/2000/01/rdf-schema#subClassOf", jsn("http://timbuctoo.com/person"),
          "rml:logicalSource", jsnO(
            "rml:source", jsnO(
              "tim:rawCollection", jsn("migranten"),
              "tim:vreName", jsn("migraties")
            )
          ),
          "subjectMap", jsnO(
            "class", jsn("http://timbuctoo.com/mapping/migraties/migranten"),
            "template", jsn("http://timbuctoo.com/mapping/migraties/migranten/{tim_id}")
          ),
          "predicateObjectMap", jsnA(
            jsnO(
              "objectMap", jsnO(
                "column", jsn("geboorteplaats"),
                "termType", jsn("http://www.w3.org/ns/r2rml#IRI")
              ),
              "predicate", jsn("http://timbuctoo.com/hasBirthPlace"),
              "http://timbuctoo.com/mapping/existingTimbuctooVre", jsn("locaties")
            )
          )
        )
      )
    ));
    assertThat(response.getStatus(), is(200));

    //one migrant should have been created
    List<Vertex> migrants = tester.traversalSource.V().has(T.label, LabelP.of("migratiesmigranten")).toList();
    assertThat(migrants.size(), is(1));
    Vertex migrant = migrants.get(0);

    //the migrant should have a link to the location
    assertThat(migrant.vertices(Direction.OUT, "hasBirthPlace").next().id(), is(locatie.id()));
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
          "@id", jsn("http://timbuctoo.com/mapping/someVre/persons"),
          "http://www.w3.org/2000/01/rdf-schema#subClassOf", jsn("http://timbuctoo.com/person"),
          "rml:logicalSource", jsnO(
            "rml:source", jsnO(
              "tim:vreName", jsn("someVre"),
              "tim:rawCollection", jsn("persons")
            )
          ),
          "subjectMap", jsnO(
            "class", jsn("http://timbuctoo.com/mapping/someVre/persons"),
            "template", jsn("http://timbuctoo.com/mapping/someVre/persons/{tim_id}")
          ),
          "predicateObjectMap", jsnA(
            jsnO(
              "predicate", jsn("http://timbuctoo.com/hasBirthPlace"),
              "objectMap", jsnO(
                "column", jsn("geboorteplaats")
              )
            ),
            jsnO(
              "predicate", jsn("http://www.w3.org/2002/07/owl#sameAs"),
              "objectMap", jsnO(
                "template", jsn("http://timbuctoo.com/mapping/someVre/persons/local/{id}")
              )
            )
          )
        )
      )
    ));
    assertThat(response.getStatus(), is(200));
    GraphTraversal<Vertex, Vertex> karel = tester.traversalSource.V().has(T.label, LabelP.of("someVrepersons"));
    assertThat(karel.asAdmin().clone().count().next(), is(1L));
    String timId = (String) karel.asAdmin().clone().values("tim_id").next();
    assertThat(karel.asAdmin().clone().values("rdfUri").next(), is(new String[] {
      "http://timbuctoo.com/mapping/someVre/persons/" + timId,
      "http://timbuctoo.com/mapping/someVre/persons/local/2"
    }));
  }



  public class IntegrationTester {
    private final TinkerpopGraphManager graphManager;
    private final DataAccess dataAccess;
    private final DatabaseConfiguredVres vres;
    public final GraphTraversalSource traversalSource;

    public IntegrationTester() {
      graphManager = newGraph().wrap();
      traversalSource = graphManager.getGraph().traversal();
      dataAccess = new DataAccess(graphManager, null, mock(ChangeListener.class), mock(HandleAdder.class));
      new ScaffoldMigrator(dataAccess).execute();

      vres = new DatabaseConfiguredVres(dataAccess);
    }

    public void executeRawUpload(String vreName, String collectionName, List<Map<String, String>> data) {
      try (TinkerpopSaver rawSaver = new TinkerpopSaver(vres, graphManager, vreName, 100)) {
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

    public Response executeRml(String vreName, ObjectNode mapping) {
      UserPermissionChecker alwaysAllowed = mock(UserPermissionChecker.class);
      given(alwaysAllowed.check(any(), any())).willReturn(UserPermissionChecker.UserPermission.ALLOWED_TO_WRITE);

      ExecuteRml executeRml = new ExecuteRml(
        new UriHelper("http://example.com"),
        graphManager,
        vres,
        new JenaBasedReader(),
        alwaysAllowed,
        new DataSourceFactory(graphManager),
        dataAccess
      );

      return executeRml.post(
        mapping.toString(),
        vreName,
        "blaat"
      );
    }

  }
}
