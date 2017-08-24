package nl.knaw.huygens.timbuctoo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableMap;
import io.dropwizard.testing.junit.DropwizardAppRule;
import nl.knaw.huygens.timbuctoo.server.TimbuctooConfiguration;
import nl.knaw.huygens.timbuctoo.server.TimbuctooV4;
import nl.knaw.huygens.timbuctoo.util.EvilEnvironmentVariableHacker;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.hamcrest.Matchers;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static com.google.common.io.Resources.asCharSource;
import static com.google.common.io.Resources.getResource;
import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static java.lang.String.format;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsn;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnA;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnO;
import static nl.knaw.huygens.timbuctoo.util.JsonContractMatcher.matchesContract;
import static nl.knaw.huygens.timbuctoo.util.StreamIterator.stream;
import static org.assertj.core.util.Lists.newArrayList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.hasXPath;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;

public class IntegrationTest {

  @ClassRule
  public static final DropwizardAppRule<TimbuctooConfiguration> APP = new DropwizardAppRule<>(
    TimbuctooV4.class,
    "example_config.yaml"
  );
  private static final String AUTH = "FAKE_AUTH_TOKEN";
  private static Client client;

  static {
    EvilEnvironmentVariableHacker.setEnv(ImmutableMap.of(
      "timbuctoo_dataPath", resourceFilePath("integrationtest"),
      "timbuctoo_port", "0",
      "timbuctoo_adminPort", "0"
    ));
  }

  @BeforeClass
  public static void beforeClass() throws IOException {
    // client = new JerseyClientBuilder(APP.getEnvironment()).build("test client");
    ClientConfig configuration = new ClientConfig();
    client = ClientBuilder.newClient(configuration);
  }

  @Test
  public void succeedingSmallUpload() throws Exception {
    String vreName = "demo-upload";
    String prefixedVreName = "DUMMY_" + vreName;
    Response uploadResponse = rawUpload(
      vreName,
      new File(getResource(IntegrationTest.class, "demo-upload.xlsx").toURI())
    );

    assertThat(uploadResponse.getStatus(), is(200));
    uploadResponse.readEntity(String.class); //needed for blocking side-effect

    Response rmlResponse = map(
      prefixedVreName,
      asCharSource(getResource(IntegrationTest.class, "demo-upload-rml.json"), Charset.forName("UTF8"))
        .read().replace(
        "{VRE_NAME}",
        prefixedVreName)
    );

    String output = rmlResponse.readEntity(String.class); //also needed for blocking side-effect
    String rmlResult = output.substring(output.lastIndexOf('\n') + 1);
    assertThat(rmlResult, is("success"));

    JsonNode metadata = call("/v2.1/metadata/" + prefixedVreName + "?withCollectionInfo=true").get(JsonNode.class);
    String personsType = prefixedVreName + "Persons";
    String placesType = prefixedVreName + "Places";
    String unknownType = prefixedVreName + "unknown";
    String personsCollection = personsType + "s";
    String placesCollection = placesType + "s";

    assertThat(metadata, matchesContract(jsnO(
      personsCollection, jsnO(),
      placesCollection, jsnO()
    )));

    JsonNode persons = call("/v2.1/domain/" + personsCollection + "/autocomplete?query=helmhol*").get(JsonNode.class);
    assertThat("No person found", persons.has(0), is(true));
    JsonNode personData = call(persons.get(0).get("key").asText()).get(JsonNode.class);
    assertThat(personData, matchesContract(jsnO(
      "@displayName", jsn("H.L.F. Helmholtz"),
      "@relations", jsnO(
        "hasBirthPlace", jsnA(
          jsnO(
            "displayName", jsn("Potsdam")
          )
        )
      ),
      "@variationRefs", jsnO(
        "custom-matcher", jsn("/*ALL_MATCH_ONE_OF*/"),
        "keyProp", jsn("type"),
        "possibilities", jsnO(
          personsType, jsnO(
            "type", jsn(personsType)
          ),
          "person", jsnO(
            "type", jsn("person")
          ),
          "concept", jsnO(
            "type", jsn("concept")
          ),
          unknownType, jsnO(
            "type", jsn(unknownType)
          )
        )
      )
    )));
  }

  @Test
  public void personNameVariantAreAddedToThePersonTheyBelongTo() throws Exception {
    String vreName = "demo-upload";
    String prefixedVreName = "DUMMY_" + vreName;
    Response uploadResponse = rawUpload(
      vreName,
      new File(getResource(IntegrationTest.class, "BIA_klein_ok.xlsx").toURI())
    );

    assertThat("Successful upload of excel", uploadResponse.getStatus(), is(200));
    uploadResponse.readEntity(String.class); //needed for blocking side-effect

    Response rmlResponse = map(
      prefixedVreName,
      asCharSource(getResource(
        IntegrationTest.class,
        "alternative-names-rml.json"),
        Charset.forName("UTF8")).read().replace("{VRE_NAME}", prefixedVreName)
    );

    String output = rmlResponse.readEntity(String.class); //also needed for blocking side-effect
    String rmlResult = output.substring(output.lastIndexOf('\n') + 1);
    assertThat("Succesful rml execution", rmlResult, is("success"));

    JsonNode metadata = call("/v2.1/metadata/" + prefixedVreName + "?withCollectionInfo=true").get(JsonNode.class);
    String personsType = prefixedVreName + "Persons";
    String personsCollection = personsType + "s";

    assertThat(metadata, matchesContract(jsnO(
      personsCollection, jsnO()
    )));

    JsonNode personsNode = call("/v2.1/domain/" + personsCollection).get(JsonNode.class);

    List<JsonNode> persons = newArrayList(personsNode.iterator());
    assertThat(persons, hasSize(2));
    int personWithTwoNamesIndex;
    int personWithOneNameIndex;
    if (persons.get(0).get("names").size() == 2) {
      personWithTwoNamesIndex = 0;
      personWithOneNameIndex = 1;
    } else {
      personWithTwoNamesIndex = 1;
      personWithOneNameIndex = 0;
    }
    assertThat(personsNode.get(personWithTwoNamesIndex).get("names"), containsInAnyOrder(jsnO(
      "components", jsnA(
        jsnO(
          "type", jsn("FORENAME"),
          "value", jsn("Jacques Henrij")
        ),
        jsnO(
          "type", jsn("SURNAME"),
          "value", jsn("Abendanon")
        )
      )
      ),
      jsnO(
        "components", jsnA(
          jsnO(
            "type", jsn("FORENAME"),
            "value", jsn("Christiaen")
          ),
          jsnO(
            "type", jsn("SURNAME"),
            "value", jsn("Christiaensen")
          )
        )
      )
    ));


    assertThat(personsNode.get(personWithOneNameIndex).get("names"), is(jsnA(
      jsnO(
        "components", jsnA(
          jsnO(
            "type", jsn("FORENAME"),
            "value", jsn("Miguel")
          ),
          jsnO(
            "type", jsn("SURNAME"),
            "value", jsn("Asin y Palacios")
          )
        )
      )
      )
    ));
  }


  @Test
  public void succeedingRdfUploadWithGraphql() throws Exception {
    String vreName = "clusius-" + UUID.randomUUID();
    Response uploadResponse = multipartPost(
      "/v5/DUMMY/" + vreName + "/upload/rdf?forceCreation=true",
      new File(getResource(IntegrationTest.class, "bia_clusius.ttl").toURI()),
      "text/turtle",
      ImmutableMap.of(
        "encoding", "UTF-8",
        "uri", "http://example.com/clusius"
      )
    );

    assertThat("Successful upload of rdf", uploadResponse.getStatus(), is(204));
    uploadResponse.readEntity(String.class);

    Response graphqlCall = call("/v5/DUMMY/" + vreName + "/graphql")
      .accept(MediaType.APPLICATION_JSON)
      .post(Entity.entity("{\n" +
        "  clusius_ResidenceList {\n" +
        "    items {\n" +
        "      uri\n" +
        "    }\n" +
        "  }\n" +
        "}", MediaType.valueOf("application/graphql")));
    ObjectNode objectNode = graphqlCall.readEntity(ObjectNode.class);
    assertThat(objectNode
        .get("data")
        .get("clusius_ResidenceList")
        .get("items").size(),
      is(20)
    );

    graphqlCall = call("/v5/DUMMY/" + vreName + "/graphql")
      .accept(MediaType.APPLICATION_JSON)
      .post(Entity.entity("{\n" +
        "  clusius_ResidenceList {\n" +
        "    items {\n" +
        "      tim_hasLocation {\n" +
        "        tim_name {value}\n" +
        "        _inverse_tim_hasBirthPlace {\n" +
        "          items {\n" +
        "            tim_gender {value}\n" +
        "          }\n" +
        "        }\n" +
        "      }\n" +
        "    }\n" +
        "  }\n" +
        "}", MediaType.valueOf("application/graphql")));
    objectNode = graphqlCall.readEntity(ObjectNode.class);
    assertThat( //every result has a value for name
      stream(objectNode
        .get("data")
        .get("clusius_ResidenceList")
        .get("items").iterator())
        .map(item -> item
          .get("tim_hasLocation")
          .get("tim_name"))
        .filter(Objects::nonNull)
        .count(),
      is(20L)
    );
    //the results link to people with genders (we can traverse inverse relations correctly)
    assertThat(
      stream(objectNode
        .get("data")
        .get("clusius_ResidenceList")
        .get("items").iterator())
        .flatMap(item ->
          stream(item
            .get("tim_hasLocation")
            .get("_inverse_tim_hasBirthPlace")
            .get("items").iterator())
            .map(person -> person
              .get("tim_gender"))
        )
        .filter(Objects::nonNull)
        .count(),
      is(37L)
    );

  }

  @Test
  public void succeedingNQuadUdUploadWithGraphql() throws Exception {
    String vreName = "clusius-" + UUID.randomUUID();
    Response uploadResponse = multipartPost(
      "/v5/DUMMY/" + vreName + "/upload/rdf?forceCreation=true",
      new File(getResource(IntegrationTest.class, "bia_clusius.nqud").toURI()),
      "application/vnd.timbuctoo-rdf.nquads_unified_diff",
      ImmutableMap.of(
        "encoding", "UTF-8",
        "uri", "http://example.com/clusius.nqud"
      )
    );

    assertThat("Successful upload of rdf", uploadResponse.getStatus(), is(204));

    Response graphqlCall = call("/v5/DUMMY/" + vreName + "/graphql")
      .accept(MediaType.APPLICATION_JSON)
      .post(Entity.entity("{\n" +
        "  http___timbuctoo_huygens_knaw_nl_datasets_clusius_ResidenceList {\n" +
        "    items { uri }\n" +
        "  }\n" +
        "}", MediaType.valueOf("application/graphql")));
    ObjectNode objectNode = graphqlCall.readEntity(ObjectNode.class);
    assertThat(objectNode
        .get("data")
        .get("http___timbuctoo_huygens_knaw_nl_datasets_clusius_ResidenceList")
        .get("items")
        .size(),
      is(20)
    );

    graphqlCall = call("/v5/DUMMY/" + vreName + "/graphql")
      .accept(MediaType.APPLICATION_JSON)
      .post(Entity.entity("{\n" +
        "  http___timbuctoo_huygens_knaw_nl_datasets_clusius_ResidenceList {\n" +
        "    items {\n" +
        "      http___timbuctoo_huygens_knaw_nl_properties_hasLocation {\n" +
        "        http___timbuctoo_huygens_knaw_nl_properties_name {\n" +
        "          value\n" +
        "        }\n" +
        "      }\n" +
        "    }\n" +
        "  }\n" +
        "}", MediaType.valueOf("application/graphql")));
    objectNode = graphqlCall.readEntity(ObjectNode.class);
    assertThat(
      stream(objectNode
        .get("data")
        .get("http___timbuctoo_huygens_knaw_nl_datasets_clusius_ResidenceList")
        .get("items").iterator())
        .map(item -> item
          .get("http___timbuctoo_huygens_knaw_nl_properties_hasLocation")
          .get("http___timbuctoo_huygens_knaw_nl_properties_name"))
        .filter(Objects::nonNull)
        .count(),
      is(20L)
    );
  }

  @Test
  public void succeedingRdfUploadResourceSync() throws Exception {
    String dataSetName = "clusius-" + UUID.randomUUID();
    Response uploadResponse = multipartPost(
      "/v5/DUMMY/" + dataSetName + "/upload/rdf?forceCreation=true",
      new File(getResource(IntegrationTest.class, "bia_clusius.ttl").toURI()),
      "text/turtle",
      ImmutableMap.of(
        "encoding", "UTF-8",
        "uri", "http://example.com/clusius.rdfp"
      )
    );

    assertThat("Successful upload of rdf", uploadResponse.getStatus(), is(204));

    Response sourceDescResponse = call("/v5/resourcesync/sourceDescription.xml").get();
    assertThat(sourceDescResponse.getStatus(), is(200));
    Node sourceDesc = streamToXml(sourceDescResponse.readEntity(InputStream.class));
    // TODO should be ends-with, but that is not supported in xpath v1
    assertThat(
      sourceDesc,
      hasXPath("//urlset/url/loc/text()[contains(. , 'DUMMY/" + dataSetName + "/capabilityList.xml')]")
    );

    Response capabilityListResp = call("/v5/resourcesync/DUMMY/" + dataSetName + "/capabilityList.xml").get();
    assertThat(capabilityListResp.getStatus(), is(200));
    Node capabilityList = streamToXml(capabilityListResp.readEntity(InputStream.class));
    // TODO should be ends-with, but that is not supported in xpath v1
    assertThat(
      capabilityList,
      hasXPath("//urlset/url/loc/text()[contains(. , 'DUMMY/" + dataSetName + "/resourceList.xml')]")
    );

    Response resourceListResp = call("/v5/resourcesync/DUMMY/" + dataSetName + "/resourceList.xml").get();
    assertThat(resourceListResp.getStatus(), is(200));
    Node resourceList = streamToXml(resourceListResp.readEntity(InputStream.class));
    // TODO should be ends-with, but that is not supported in xpath v1
    assertThat(
      resourceList,
      hasXPath("//urlset/url/loc/text()[contains(. , 'DUMMY/" + dataSetName + "/files/')]")
    );

  }

  private Document streamToXml(InputStream is) throws ParserConfigurationException, IOException, SAXException {
    DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

    return documentBuilder.parse(is);
  }

  @Test
  public void tabularUpload() {
    Client client = ClientBuilder.newBuilder().register(MultiPartFeature.class).build();
    String dataSetId = "dataset" + UUID.randomUUID();
    WebTarget target = client.target(
      format("http://localhost:%d/v5/DUMMY/" + dataSetId + "/upload/table?forceCreation=true", APP.getLocalPort())
    );

    FormDataMultiPart multiPart = new FormDataMultiPart();
    multiPart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);
    FileDataBodyPart fileDataBodyPart = new FileDataBodyPart(
      "file",
      new File(getResource(IntegrationTest.class, "2017_04_17_BIA_Clusius.xlsx").getFile()),
      new MediaType("application", "vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    );
    multiPart.bodyPart(fileDataBodyPart);
    multiPart.field("type", "xlsx");


    Response response = target.request()
                              .header(HttpHeaders.AUTHORIZATION, "fake")
                              .post(Entity.entity(multiPart, multiPart.getMediaType()));

    assertThat(response.getStatus(), Matchers.is(204));
    // assertThat(response.getHeaderString(HttpHeaders.LOCATION), Matchers.is(notNullValue()));
  }

  @Test
  public void deleteDataSet() throws Exception {
    // Create a dataset
    Client client = ClientBuilder.newBuilder().build();
    String dataSetId = "dataset" + UUID.randomUUID();
    WebTarget createTarget =
      client
        .target(format("http://localhost:%d/v5/dataSets/DUMMY/" + dataSetId + "/create/", APP.getLocalPort()));

    Response createResponse = createTarget.request()
                                          .header(HttpHeaders.AUTHORIZATION, "fake")
                                          .post(Entity.json(jsnO()));
    assertThat(createResponse.getStatus(), is(201));
    // check if the dataset is created
    List<String> dataSetNamesOfDummy = getDataSetNamesOfDummy(client);
    System.out.println("datasets: " + dataSetNamesOfDummy);
    assertThat(dataSetNamesOfDummy, hasItem(dataSetId));

    // delete dataset
    WebTarget deleteTarget =
      client.target(format("http://localhost:%d/v5/DUMMY/" + dataSetId, APP.getLocalPort()));

    Response deleteResponse = deleteTarget.request()
                                          .header(HttpHeaders.AUTHORIZATION, "fake")
                                          .delete();

    assertThat(deleteResponse.getStatus(), is(204));

    // check if the dataset still exists
    assertThat(getDataSetNamesOfDummy(client), not(hasItem(dataSetId)));
  }

  @Ignore("Checkrevisionof currently fails. Need to decide how we are adding 'revisionof' info to the database" +
    "in the first place.")
  @Test
  public void checkJsonLdDeserialization() throws Exception {
    String testRdfReader = "{\t\"prov:generates\": [{\n" +
      "\t\t\"entityType\": \"Entity\",\n" +
      "\t\t\"specializationOf\": \"http://example.com/the/actual/entity\",\n" +
      "\t\t\"wasRevisionOf\": {\n" +
      "\t\t\t\"@id\": \"http://previous/mutation\"\n" +
      "\t\t},\n" +
      "\t\t\"tim:additions\": {\n" +
      "\t\t\t\"name\": \"extra name\",\n" +
      "\t\t\t\"pred2\": [\"multiple\", \"values\"]\n" +
      "\t\t},\n" +
      "\t\t\"tim:delet  ions\": {\n" +
      "\t\t\t\"name\": \"extra name\",\n" +
      "\t\t\t\"pred2\": [\"multiple\", \"values\"]\n" +
      "\t\t},\n" +
      "\t\t\"tim:replacements\": {\n" +
      "\t\t\t\"name\": \"extra name\",\n" +
      "\t\t\t\"pred2\": [\"multiple\", \"values\"]\n" +
      "\t\t}\n" +
      "\t}]\n" +
      "}";

    ObjectMapper objectMapper = new ObjectMapper();

    Client client = ClientBuilder.newBuilder().build();
    WebTarget createTarget =
      client.target(String.format("http://localhost:%d/v5/DUMMY/testset/upload/jsonld/", APP.getLocalPort()));

    Response createResponse = createTarget.request()
                                          .header(HttpHeaders.AUTHORIZATION, "fake")
                                          .put(Entity.json(testRdfReader));

    assertThat(createResponse.getStatus(), is(204));

  }

  private List<String> getDataSetNamesOfDummy(Client client) {
    WebTarget allDataSetsTarget =
      client.target(format("http://localhost:%d/v5/dataSets/DUMMY/", APP.getLocalPort()));

    Response allDataSetsResponse = allDataSetsTarget.request().get();

    assertThat(allDataSetsResponse.getStatus(), is(200));
    return newArrayList(allDataSetsResponse.readEntity(ObjectNode.class).fieldNames());
  }


  private Response rawUpload(String vrename, File resource) throws ParseException {
    Map<String, String> arguments = ImmutableMap.of(
      "vreName", vrename
    );
    String path = "/v2.1/bulk-upload";

    return multipartPost(
      path,
      resource,
      "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
      arguments
    );
  }

  private Response multipartPost(String path, File resource, String mediaType, Map<String, String> arguments)
    throws ParseException {
    MultiPart formdata = new MultiPart();
    arguments.forEach((key, value) -> formdata.bodyPart(new FormDataBodyPart(key, value)));

    formdata.bodyPart(new FormDataBodyPart(
      new FormDataContentDisposition(
        "form-data; name=\"file\"; filename=\"" + resource.getName().replace("\"", "") + "\""
      ),
      resource,
      MediaType.valueOf(mediaType)
    ));

    Response result = call(path)
      .post(Entity.entity(formdata, "multipart/form-data; boundary=Boundary_1_498293219_1483974344746"));
    return result;
  }

  private Response map(String vrename, String rmlMapping) {
    Response response = call("/v2.1/bulk-upload/" + vrename + "/rml/execute")
      .post(Entity.entity(rmlMapping, new MediaType("application", "ld+json")));
    return response;
  }

  private Invocation.Builder call(String path) {
    String server;
    if (path.startsWith("http://") || path.startsWith("https://")) {
      server = path;
    } else {
      int localPort = APP.getLocalPort();
      // int localPort = 8080;
      server = format("http://localhost:%d" + path, localPort);
    }
    return client
      .target(server)
      .register(MultiPartFeature.class)
      // .register(new LoggingFilter(Logger.getLogger(LoggingFilter.class.getName()), true))
      .request()
      .header("Authorization", AUTH);
  }
}
