package nl.knaw.huygens.timbuctoo.v5;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableMap;
import io.dropwizard.testing.junit.DropwizardAppRule;
import nl.knaw.huygens.timbuctoo.CleaningDropwizard;
import nl.knaw.huygens.timbuctoo.server.TimbuctooConfiguration;
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
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.google.common.io.Resources.getResource;
import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static java.lang.String.format;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsn;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnA;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnO;
import static nl.knaw.huygens.timbuctoo.util.StreamIterator.stream;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasXPath;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;

public class IntegrationTest {

  @ClassRule
  public static final DropwizardAppRule<TimbuctooConfiguration> APP = new CleaningDropwizard(
    "example_config.yaml",
    Paths.get(resourceFilePath("integrationtest"), "datasets")
  );
  private static final String AUTH = "FAKE_AUTH_TOKEN";
  private static Client client;
  private static String PREFIX = "u33707283d426f900d4d33707283d426f900d4d0d";

  static {
    EvilEnvironmentVariableHacker.setEnv(ImmutableMap.of(
      "timbuctoo_dataPath", resourceFilePath("integrationtest"),
      "timbuctoo_port", "0",
      "timbuctoo_adminPort", "0"
    ));
  }

  @BeforeClass
  public static void beforeClass() throws IOException {
    ClientConfig configuration = new ClientConfig();
    client = ClientBuilder.newClient(configuration);
  }

  @Test
  public void succeedingRdfUploadWithGraphql() throws Exception {
    String vreName = "clusius_" + UUID.randomUUID().toString().replace("-", "_");
    Response uploadResponse = multipartPost(
      "/v5/" + PREFIX + "/" + vreName + "/upload/rdf?forceCreation=true",
      new File(getResource(IntegrationTest.class, "bia_clusius.ttl").toURI()),
      "text/turtle",
      ImmutableMap.of(
        "encoding", "UTF-8",
        "uri", "http://example.com/clusius"
      )
    );

    assertThat("Successful upload of rdf", uploadResponse.getStatus(), is(204));
    uploadResponse.readEntity(String.class);

    Response graphqlCall = call("/v5/graphql")
      .accept(MediaType.APPLICATION_JSON)
      .post(Entity.entity(String.format("{\n" +
        "  dataSets {\n" +
        "    %1s__%2s {\n" +
        "      clusius_ResidenceList {\n" +
        "        items {\n" +
        "          uri\n" +
        "        }\n" +
        "      }\n" +
        "    }\n" +
        "  }\n" +
        "}", PREFIX, vreName), MediaType.valueOf("application/graphql")));
    ObjectNode objectNode = graphqlCall.readEntity(ObjectNode.class);
    assertThat(
      objectNode
        .get("data")
        .get("dataSets")
        .get(PREFIX + "__" + vreName)
        .get("clusius_ResidenceList")
        .get("items").size(),
      is(20)
    );

    graphqlCall = call("/v5/graphql")
      .accept(MediaType.APPLICATION_JSON)
      .post(Entity.entity(String.format("{\n" +
        "  dataSets {\n" +
        "    %1s__%2s {\n" +
        "      clusius_ResidenceList {\n" +
        "        items {\n" +
        "          tim_hasLocation {\n" +
        "            tim_name {value}\n" +
        "            _inverse_tim_hasBirthPlaceList {\n" +
        "              items {\n" +
        "                tim_gender {value}\n" +
        "              }\n" +
        "            }\n" +
        "          }\n" +
        "        }\n" +
        "      }\n" +
        "    }\n" +
        "  }\n" +
        "}", PREFIX, vreName), MediaType.valueOf("application/graphql")));
    objectNode = graphqlCall.readEntity(ObjectNode.class);
    assertThat( //every result has a value for name
      stream(objectNode
        .get("data")
        .get("dataSets")
        .get(PREFIX + "__" + vreName)
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
        .get("dataSets")
        .get(PREFIX + "__" + vreName)
        .get("clusius_ResidenceList")
        .get("items").iterator())
        .flatMap(item ->
          stream(item
            .get("tim_hasLocation")
            .get("_inverse_tim_hasBirthPlaceList")
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
    String vreName = "clusius_" + UUID.randomUUID().toString().replace("-", "_");
    Response uploadResponse = multipartPost(
      "/v5/" + PREFIX + "/" + vreName + "/upload/rdf?forceCreation=true",
      new File(getResource(IntegrationTest.class, "bia_clusius.nqud").toURI()),
      "application/vnd.timbuctoo-rdf.nquads_unified_diff",
      ImmutableMap.of(
        "encoding", "UTF-8",
        "uri", "http://example.com/clusius.nqud"
      )
    );

    assertThat("Successful upload of rdf", uploadResponse.getStatus(), is(204));

    Response graphqlCall = call("/v5/graphql")
      .accept(MediaType.APPLICATION_JSON)
      .post(Entity.entity(String.format("{\n" +
        "  dataSets {\n" +
        "    %1s__%2s {\n" +
        "      http___timbuctoo_huygens_knaw_nl_datasets_clusius_ResidenceList {\n" +
        "        items { uri }\n" +
        "      }\n" +
        "    }\n" +
        "  }\n" +
        "}", PREFIX, vreName), MediaType.valueOf("application/graphql")));
    ObjectNode objectNode = graphqlCall.readEntity(ObjectNode.class);
    assertThat(
      objectNode
        .get("data")
        .get("dataSets")
        .get(PREFIX + "__" + vreName)
        .get("http___timbuctoo_huygens_knaw_nl_datasets_clusius_ResidenceList")
        .get("items")
        .size(),
      is(20)
    );

    graphqlCall = call("/v5/graphql")
      .accept(MediaType.APPLICATION_JSON)
      .post(Entity.entity(String.format("{\n" +
        "  dataSets {\n" +
        "    %1s__%2s {\n" +
        "      http___timbuctoo_huygens_knaw_nl_datasets_clusius_ResidenceList {\n" +
        "        items {\n" +
        "          http___timbuctoo_huygens_knaw_nl_properties_hasLocation {\n" +
        "            http___timbuctoo_huygens_knaw_nl_properties_name {\n" +
        "              value\n" +
        "            }\n" +
        "          }\n" +
        "        }\n" +
        "      }\n" +
        "    }\n" +
        "  }\n" +
        "}", PREFIX, vreName), MediaType.valueOf("application/graphql")));
    objectNode = graphqlCall.readEntity(ObjectNode.class);
    assertThat(
      stream(objectNode
        .get("data")
        .get("dataSets")
        .get(PREFIX + "__" + vreName)
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
    String dataSetName = "clusius_" + UUID.randomUUID().toString().replace("-", "_");
    Response uploadResponse = multipartPost(
      "/v5/" + PREFIX + "/" + dataSetName + "/upload/rdf?forceCreation=true",
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
      hasXPath("//urlset/url/loc/text()[contains(. , '" + PREFIX + "/" + dataSetName + "/capabilityList.xml')]")
    );

    Response capabilityListResp = call("/v5/resourcesync/" + PREFIX + "/" + dataSetName + "/capabilityList.xml").get();
    assertThat(capabilityListResp.getStatus(), is(200));
    Node capabilityList = streamToXml(capabilityListResp.readEntity(InputStream.class));
    // TODO should be ends-with, but that is not supported in xpath v1
    assertThat(
      capabilityList,
      hasXPath("//urlset/url/loc/text()[contains(. , '" + PREFIX + "/" + dataSetName + "/resourceList.xml')]")
    );

    Response resourceListResp = call("/v5/resourcesync/" + PREFIX + "/" + dataSetName + "/resourceList.xml").get();
    assertThat(resourceListResp.getStatus(), is(200));
    Node resourceList = streamToXml(resourceListResp.readEntity(InputStream.class));
    // TODO should be ends-with, but that is not supported in xpath v1
    assertThat(
      resourceList,
      hasXPath("//urlset/url/loc/text()[contains(. , '" + PREFIX + "/" + dataSetName + "/files/')]")
    );

  }

  private Document streamToXml(InputStream is) throws ParserConfigurationException, IOException, SAXException {
    DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

    return documentBuilder.parse(is);
  }

  @Test
  public void tabularUpload() {
    Client client = ClientBuilder.newBuilder().register(MultiPartFeature.class).build();
    String dataSetId = "dataset" + UUID.randomUUID().toString().replace("-", "_");
    WebTarget target = client.target(
      format(
        "http://localhost:%d/v5/" + PREFIX + "/" + dataSetId + "/upload/table?forceCreation=true",
        APP.getLocalPort()
      )
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
    String dataSetId = "dataset" + UUID.randomUUID().toString().replace("-", "_");
    WebTarget createTarget =
      client
        .target(format("http://localhost:%d/v5/dataSets/" + PREFIX + "/" + dataSetId + "/create/", APP.getLocalPort()));

    Response createResponse = createTarget.request()
                                          .header(HttpHeaders.AUTHORIZATION, "fake")
                                          .post(Entity.json(jsnO()));
    assertThat(createResponse.getStatus(), is(201));
    // check if the dataset is created
    List<String> dataSetNamesOfDummy = getDataSetNamesOfDummy();
    System.out.println("datasets: " + dataSetNamesOfDummy);
    assertThat(dataSetNamesOfDummy, hasItem(PREFIX + "__" + dataSetId));

    // delete dataset
    WebTarget deleteTarget =
      client.target(format("http://localhost:%d/v5/" + PREFIX + "/" + dataSetId, APP.getLocalPort()));

    Response deleteResponse = deleteTarget.request()
                                          .header(HttpHeaders.AUTHORIZATION, "fake")
                                          .delete();

    assertThat(deleteResponse.getStatus(), is(204));

    // check if the dataset still exists
    assertThat(getDataSetNamesOfDummy(), not(hasItem(dataSetId)));
  }

  @Test
  public void checkJsonLdDeserialization() throws Exception {
    final String context = "{\n" +
      "    \"@vocab\": \"http://example.org/UNKNOWN#\",\n" +
      "    \"prov\": \"http://www.w3.org/ns/prov#\",\n" +
      "    \"tim\": \"http://timbuctoo.huygens.knaw.nl/v5/vocabulary#\",\n" +
      "    \"ex\": \"http://example.org/\",\n" +
      "    \"additions\": {\n" +
      "      \"@id\": \"http://timbuctoo.huygens.knaw.nl/v5/vocabulary#additions\"\n" +
      "    },\n" +
      "    \"deletions\": {\n" +
      "      \"@id\": \"http://timbuctoo.huygens.knaw.nl/v5/vocabulary#deletions\"\n" +
      "    },\n" +
      "    \"replacements\": {\n" +
      "      \"@id\": \"http://timbuctoo.huygens.knaw.nl/v5/vocabulary#replacements\"\n" +
      "    },\n" +
      "    \"latestRevision\": {\n" +
      "      \"@id\": \"http://timbuctoo.huygens.knaw.nl/v5/vocabulary#latestRevision\",\n" +
      "      \"@type\": \"@id\"\n" +
      "    },\n" +
      "    \"used\": {\n" +
      "      \"@id\": \"http://www.w3.org/ns/prov#used\"\n" +
      "    },\n" +
      "    \"generates\": {\n" +
      "      \"@id\": \"http://www.w3.org/ns/prov#generates\"\n" +
      "    },\n" +
      "    \"qualifiedAssociation\": {\n" +
      "      \"@id\": \"http://www.w3.org/ns/prov#qualifiedAssociation\"\n" +
      "    },\n" +
      "    \"agent\": {\n" +
      "      \"@id\": \"http://www.w3.org/ns/prov#agent\",\n" +
      "      \"@type\": \"@id\"\n" +
      "    },\n" +
      "    \"hadRole\": {\n" +
      "      \"@id\": \"http://www.w3.org/ns/prov#hadRole\",\n" +
      "      \"@type\": \"@id\"\n" +
      "    },\n" +
      "    \"specializationOf\": {\n" +
      "      \"@id\": \"http://www.w3.org/ns/prov#specializationOf\",\n" +
      "      \"@type\": \"@id\"\n" +
      "    },\n" +
      "    \"entityType\": {\n" +
      "      \"@id\": \"http://www.w3.org/ns/prov#entityType\",\n" +
      "      \"@type\": \"@id\"\n" +
      "    },\n" +
      "    \"entity\": {\n" +
      "      \"@id\": \"http://www.w3.org/ns/prov#entity\",\n" +
      "      \"@type\": \"@id\"\n" +
      "    },\n" +
      "    \"wasRevisionOf\": {\n" +
      "      \"@id\": \"http://www.w3.org/ns/prov#wasRevisionOf\",\n" +
      "      \"@type\": \"@id\"\n" +
      "    },\n" +
      "    \"predicate\": {\n" +
      "      \"@id\": \"http://timbuctoo.huygens.knaw.nl/v5/vocabulary#predicate\"\n" +
      "    },\n" +
      "    \"value\": {\n" +
      "      \"@id\": \"http://timbuctoo.huygens.knaw.nl/v5/vocabulary#value\"\n" +
      "    }\n" +
      "  }";
    final String testRdfReader = "{\n" +
      "  \"@context\":" + context + ",\n" +
      "  \"@type\": \"prov:Activity\",\n" +
      "  \"generates\": [{\n" +
      "    \"@type\": \"prov:Entity\",\n" +
      "    \"specializationOf\": \"http://example.com/the/actual/entity\",\n" +
      "    \"additions\": [\n" +
      "      {\n" +
      "        \"@type\": \"http://timbuctoo.huygens.knaw.nl/v5/vocabulary#mutation\", \n" +
      "        \"predicate\": \"http://example.org/pred2\", \n" +
      "        \"value\": [\"multiple\", \"values\"]\n" +
      "      },\n" +
      "      {\n" +
      "        \"@type\": \"http://timbuctoo.huygens.knaw.nl/v5/vocabulary#mutation\", \n" +
      "        \"predicate\": \"http://www.w3.org/1999/02/22-rdf-syntax-ns#type\", \n" +
      "        \"value\": {\"@id\": \"http://example.org/Person\"}\n" +
      "      }\n" +
      "    ]\n" +
      "  }]\n" +
      "}\n";

    Client client = ClientBuilder.newBuilder().build();

    String vreName = "ldtest" + UUID.randomUUID().toString().replace("-", "_");
    WebTarget createDataSet =
      client.target(String.format(
        "http://localhost:%d/v5/dataSets/" + PREFIX + "/" + vreName + "/create/",
        APP.getLocalPort()
      ));

    createDataSet.request()
                 .header(HttpHeaders.AUTHORIZATION, "fake")
                 .post(Entity.json(null));

    final WebTarget createTarget =
      client.target(String.format(
        "http://localhost:%d/v5/" + PREFIX + "/" + vreName + "/upload/jsonld/",
        APP.getLocalPort()
      ));


    Response createResponse = createTarget.request()
                                          .header(HttpHeaders.AUTHORIZATION, "fake")
                                          .put(Entity.json(testRdfReader));

    if (createResponse.getStatus() != 204) {
      System.out.println(createResponse.readEntity(String.class));
    }
    assertThat(createResponse.getStatus(), is(204));


    Response graphqlCall = call("/v5/graphql")
      .accept(MediaType.APPLICATION_JSON)
      .post(Entity.entity(String.format("{\n" +
        "  dataSets {\n" +
        "    %1s__%2s {\n" +
        "      local__Person(uri: \"http://example.com/the/actual/entity\") {\n" +
        "        tim_latestRevision {\n" +
        "          uri\n" +
        "        }\n" +
        "      }\n" +
        "    }\n" +
        "  }\n" +
        "}", PREFIX, vreName), MediaType.valueOf("application/graphql")));
    ObjectNode objectNode = graphqlCall.readEntity(ObjectNode.class);
    String revision = objectNode
      .get("data")
      .get("dataSets")
      .get(PREFIX + "__" + vreName)
      .get("local__Person")
      .get("tim_latestRevision")
      .get("uri")
      .asText();

    String testRdfReader2 = "{\n" +
      "  \"@context\":" + context + ",\n" +
      "  \"@type\": \"prov:Activity\",\n" +
      "  \"generates\": [{\n" +
      "    \"@type\": \"prov:Entity\",\n" +
      "    \"specializationOf\": \"http://example.com/the/actual/entity\",\n" +
      "    \"wasRevisionOf\": \"" + revision + "\",\n" +
      "    \"additions\": [\n" +
      "      {\n" +
      "        \"@type\": \"http://timbuctoo.huygens.knaw.nl/v5/vocabulary#mutation\", \n" +
      "        \"predicate\": \"http://example.org/pred2\", \n" +
      "        \"value\": \"extra value\"\n" +
      "      }\n" +
      "    ],\n" +
      "    \"deletions\": [\n" +
      "      {\n" +
      "        \"@type\": \"http://timbuctoo.huygens.knaw.nl/v5/vocabulary#mutation\", \n" +
      "        \"predicate\": \"http://example.org/pred2\", \n" +
      "        \"value\": \"multiple\"\n" +
      "      }\n" +
      "    ]\n" +
      "  }]\n" +
      "}\n";

    final WebTarget createTarget2 =
      client.target(String.format(
        "http://localhost:%d/v5/" + PREFIX + "/" + vreName + "/upload/jsonld/",
        APP.getLocalPort()
      ));


    Response createResponse2 = createTarget2.request()
                                            .header(HttpHeaders.AUTHORIZATION, "fake")
                                            .put(Entity.json(testRdfReader2));

    if (createResponse2.getStatus() != 204) {
      System.out.println(createResponse2.readEntity(String.class));
    }
    assertThat(createResponse2.getStatus(), is(204));

    graphqlCall = call("/v5/graphql")
      .accept(MediaType.APPLICATION_JSON)
      .post(Entity.entity(String.format("{\n" +
        "  dataSets {\n" +
        "    %1s__%2s {\n" +
        "      local__Person(uri:\"http://example.com/the/actual/entity\"){\n" +
        "        local__pred2List {\n" +
        "          items {\n" +
        "            value\n" +
        "          }\n" +
        "        }\n" +
        "      }\n" +
        "    }\n" +
        "  }\n" +
        "}", PREFIX, vreName), MediaType.valueOf("application/graphql")));
    objectNode = graphqlCall.readEntity(ObjectNode.class);

    assertThat(
      stream(objectNode
        .get("data")
        .get("dataSets")
        .get(PREFIX + "__" + vreName)
        .get("local__Person")
        .get("local__pred2List")
        .get("items").iterator())
        .map(x -> x.get("value").asText())
        .collect(Collectors.toList()),
      contains(
        "extra value",
        "values"
      )
    );
  }

  @Test
  public void viewConfigCanBeChangedWithGraphQl() throws Exception {
    final String dataSetName = "clusius_" + UUID.randomUUID().toString().replace("-", "_");
    final String dataSetId = PREFIX + "__" + dataSetName;
    Response uploadResponse = multipartPost(
      "/v5/" + PREFIX + "/" + dataSetName + "/upload/rdf?forceCreation=true",
      new File(getResource(IntegrationTest.class, "bia_clusius.nqud").toURI()),
      "application/vnd.timbuctoo-rdf.nquads_unified_diff",
      ImmutableMap.of(
        "encoding", "UTF-8",
        "uri", "http://example.com/clusius.nqud"
      )
    );

    assertThat("Successful upload of rdf", uploadResponse.getStatus(), is(204));

    final String collectionUri = "http://timbuctoo.huygens.knaw.nl/datasets/clusius/Persons";
    final String type = "test3";
    final String value = "test4";
    Response graphQlCall = call("/v5/graphql")
      .accept(MediaType.APPLICATION_JSON)
      .post(Entity.entity(String.format("{\n" +
        "\t\"query\": \"mutation CreateViewConfig {\\n  createViewConfig(dataSet: \\\"%s\\\", collectionUri: " +
        "\\\"%s\\\", viewConfig: [{type: \\\"%s\\\", value:\\\"%s\\\"}]){\\n    type\\n    value\\n  }\\n}\",\n" +
        "\t\"variables\": null,\n" +
        "\t\"operationName\": \"CreateViewConfig\"\n" +
        "}", dataSetId, collectionUri, type, value), MediaType.valueOf("application/json")));

    assertThat(graphQlCall.getStatus(), is(200));
    assertThat(graphQlCall.readEntity(ObjectNode.class), is(jsnO("data",
      jsnO("createViewConfig", jsnA(
          jsnO(
            "type", jsn("test3"),
            "value", jsn("test4")
          )
        )
      )
      )));


    graphQlCall = call("/v5/graphql")
      .accept(MediaType.APPLICATION_JSON)
      .post(
        Entity.entity(
          String.format("{\"query\":\"{\\n  dataSetMetadata" +
            "(dataSetId:\\\"%s\\\") {\\n    collectionList {\\n      " +
            "items {\\n        uri\\n        viewConfig {\\n          type\\n          value\\n        }\\n " +
            "  " +
            "  " +
            " }\\n    }\\n  }\\n}\",\"variables\":null,\"operationName\":null}", dataSetId),
          MediaType.valueOf("application/json")
        )
      );

    assertThat(graphQlCall.getStatus(), is(200));
    ObjectNode metaData = graphQlCall.readEntity(ObjectNode.class);
    assertThat(stream(metaData
      .get("data")
      .get("dataSetMetadata")
      .get("collectionList")
      .get("items").iterator()).collect(Collectors.toList()),
      hasItem(jsnO(
        "uri", jsn(collectionUri),
        "viewConfig", jsnA(
          jsnO(
            "type", jsn(type),
            "value", jsn(value)
          )
        )
      ))
    );

  }

  private List<String> getDataSetNamesOfDummy() {
    Response graphqlCall = call("/v5/graphql")
      .accept(MediaType.APPLICATION_JSON)
      .post(Entity.entity("{aboutMe{dataSets{dataSetId}}}", MediaType.valueOf("application/graphql")));
    ObjectNode objectNode = graphqlCall.readEntity(ObjectNode.class);

    assertThat(graphqlCall.getStatus(), is(200));
    return stream(
      objectNode
        .get("data")
        .get("aboutMe")
        .get("dataSets").iterator())
      .map(x -> x.get("dataSetId").asText())
      .collect(Collectors.toList());
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
