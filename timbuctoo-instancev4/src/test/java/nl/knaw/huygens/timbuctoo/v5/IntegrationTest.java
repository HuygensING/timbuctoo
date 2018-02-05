package nl.knaw.huygens.timbuctoo.v5;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableMap;
import io.dropwizard.testing.junit.DropwizardAppRule;
import nl.knaw.huygens.timbuctoo.CleaningDropwizard;
import nl.knaw.huygens.timbuctoo.server.TimbuctooConfiguration;
import nl.knaw.huygens.timbuctoo.util.EvilEnvironmentVariableHacker;
import org.apache.activemq.util.ByteArrayInputStream;
import org.apache.commons.io.IOUtils;
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
import org.w3c.dom.NodeList;
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
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
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
    EvilEnvironmentVariableHacker.setEnv(
      "http://localhost",
      "9200",
      "elastic",
      "changeme",
      "http://127.0.0.1:0",
      resourceFilePath("integrationtest"),
      resourceFilePath("integrationtest"),
      "0",
      "0"
    );
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

    assertThat("Successful upload of rdf", uploadResponse.getStatus(), is(201));
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
        .get(createDataSetId(vreName))
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
        .get(createDataSetId(vreName))
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
        .get(createDataSetId(vreName))
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
  public void synchronousUnsuccessfulRdfUpload2X() throws Exception {
    String vreName = "clusius_" + UUID.randomUUID().toString().replace("-", "_");
    Response uploadResponse = multipartPost(
      "/v5/" + PREFIX + "/" + vreName + "/upload/rdf?forceCreation=true&async=false",
      new File(getResource(IntegrationTest.class, "error1_bia_clusius.ttl").toURI()),
      "text/turtle",
      ImmutableMap.of(
        "encoding", "UTF-8",
        "uri", "http://example.com/clusius"
      )
    );

    assertThat(uploadResponse.getStatus(), is(Response.Status.BAD_REQUEST.getStatusCode()));
    String content = IOUtils.toString((InputStream) uploadResponse.getEntity());
    assertThat(content, containsString("Namespace prefix 'wrong_in_1' used but not defined"));

    uploadResponse = multipartPost(
      "/v5/" + PREFIX + "/" + vreName + "/upload/rdf?forceCreation=true&async=false",
      new File(getResource(IntegrationTest.class, "error2_bia_clusius.ttl").toURI()),
      "text/turtle",
      ImmutableMap.of(
        "encoding", "UTF-8",
        "uri", "http://example.com/clusius"
      )
    );
    assertThat(uploadResponse.getStatus(), is(Response.Status.BAD_REQUEST.getStatusCode()));
    content = IOUtils.toString((InputStream) uploadResponse.getEntity());
    assertThat(content, containsString("Namespace prefix 'wrong_in_2' used but not defined"));
    assertThat(content.contains("Namespace prefix 'wrong_in_1' used but not defined"), is(false));
  }

  @Test
  public void synchronousUnsuccessfulRdfUploadWithGraphql() throws Exception {
    String vreName = "clusius_" + UUID.randomUUID().toString().replace("-", "_");
    Response uploadResponse = multipartPost(
      "/v5/" + PREFIX + "/" + vreName + "/upload/rdf?forceCreation=true",
      new File(getResource(IntegrationTest.class, "error1_bia_clusius.ttl").toURI()),
      "text/turtle",
      ImmutableMap.of(
        "encoding", "UTF-8",
        "uri", "http://example.com/clusius"
      )
    );

    Response graphqlCall = call("/v5/graphql")
      .accept(MediaType.APPLICATION_JSON)
      .post(Entity.entity(String.format("{\n" +
          "  dataSetMetadata(dataSetId: \"%1s__%2s\")\n" +
          "  {\n" +
          "    currentImportStatus {\n" +
          "      elapsedTime(unit: MILLISECONDS)\n" +
          "      status\n" +
          "    }\n" +
          "  }\n" +
          "}\n",
        PREFIX, vreName), MediaType.valueOf("application/graphql")));
    ObjectNode objectNode = graphqlCall.readEntity(ObjectNode.class);
    int elapsedTime = objectNode.get("data")
      .get("dataSetMetadata")
      .get(("currentImportStatus"))
      .get("elapsedTime").asInt();
    assertThat(elapsedTime > 0, is(true));
    
    graphqlCall = call("/v5/graphql")
      .accept(MediaType.APPLICATION_JSON)
      .post(Entity.entity(String.format("{\n" +
          "  dataSetMetadata(dataSetId: \"%1s__%2s\")\n" +
          "  {\n" +
          "    currentImportStatus {\n" +
          "      elapsedTime(unit: MILLISECONDS)\n" +
          "      status\n" +
          "    }\n" +
          "  }\n" +
          "}\n",
        PREFIX, vreName), MediaType.valueOf("application/graphql")));
    objectNode = graphqlCall.readEntity(ObjectNode.class);
    String status = objectNode.get("data")
                              .get("dataSetMetadata")
                              .get(("currentImportStatus"))
                              .get("status").asText();
    assertThat(status, status.contains("Finished import with 1 error"), is(true));
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

    assertThat("Successful upload of rdf", uploadResponse.getStatus(), is(201));

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
        .get(createDataSetId(vreName))
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
        .get(createDataSetId(vreName))
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

    assertThat("Successful upload of rdf", uploadResponse.getStatus(), is(201));

    Response sourceDescResponse = call("/v5/resourcesync/sourceDescription.xml").get();
    assertThat(sourceDescResponse.getStatus(), is(200));
    Node sourceDesc = streamToXml(sourceDescResponse.readEntity(InputStream.class));
    // TODO should be ends-with, but that is not supported in xpath v1
    assertThat(
      sourceDesc,
      hasXPath("//urlset/url/loc/text()[contains(. , '" + PREFIX + "/" + dataSetName + "/capabilitylist.xml')]")
    );

    Response capabilityListResp = call("/v5/resourcesync/" + PREFIX + "/" + dataSetName + "/capabilitylist.xml").get();
    assertThat(capabilityListResp.getStatus(), is(200));
    Node capabilityList = streamToXml(capabilityListResp.readEntity(InputStream.class));
    // TODO should be ends-with, but that is not supported in xpath v1
    assertThat(
      capabilityList,
      hasXPath("//urlset/url/loc/text()[contains(. , '" + PREFIX + "/" + dataSetName + "/resourcelist.xml')]")
    );

    Response resourceListResp = call("/v5/resourcesync/" + PREFIX + "/" + dataSetName + "/resourcelist.xml")
      .get();
    assertThat(resourceListResp.getStatus(), is(200));
    Node resourceList = streamToXml(resourceListResp.readEntity(InputStream.class));
    assertThat(
      resourceList,
      hasXPath("//urlset/url/loc/text()[contains(. , '" + PREFIX + "/" + dataSetName + "/files/')]")
    );

    // call resourceSync without authorization
    sourceDescResponse = callWithoutAuthentication("/v5/resourcesync/sourceDescription.xml").get();
    assertThat(sourceDescResponse.getStatus(), is(200));
    sourceDesc = streamToXml(sourceDescResponse.readEntity(InputStream.class));
    // TODO should be ends-with, but that is not supported in xpath v1
    assertThat(
      sourceDesc, not(
      hasXPath("//urlset/url/loc/text()[contains(. , '" + PREFIX + "/" + dataSetName + "/capabilitylist.xml')]"))
    );

    capabilityListResp =
      callWithoutAuthentication("/v5/resourcesync/" + PREFIX + "/" + dataSetName + "/capabilitylist.xml")
        .get();
    assertThat(capabilityListResp.getStatus(), is(Response.Status.UNAUTHORIZED.getStatusCode()));

    resourceListResp =
      callWithoutAuthentication("/v5/resourcesync/" + PREFIX + "/" + dataSetName + "/resourcelist.xml").get();
    assertThat(resourceListResp.getStatus(), is(Response.Status.UNAUTHORIZED.getStatusCode()));
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

    assertThat(response.getStatus(), Matchers.is(201));
    // assertThat(response.getHeaderString(HttpHeaders.LOCATION), Matchers.is(notNullValue()));
  }


  @Test
  public void checkJsonLdDeserialization() {
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
      "        \"predicate\": \"http://example.org/predicate/pred2\", \n" +
      "        \"value\": [\"multiple\", \"values\"]\n" +
      "      },\n" +
      "      {\n" +
      "        \"@type\": \"http://timbuctoo.huygens.knaw.nl/v5/vocabulary#mutation\", \n" +
      "        \"predicate\": \"http://www.w3.org/1999/02/22-rdf-syntax-ns#type\", \n" +
      "        \"value\": {\"@id\": \"http://example.org/collection/Person\"}\n" +
      "      }\n" +
      "    ]\n" +
      "  }]\n" +
      "}\n";

    Client client = ClientBuilder.newBuilder().build();

    String dataSetName = "ldtest" + UUID.randomUUID().toString().replace("-", "_");
    Response graphQlCall = call("/v5/graphql")
      .accept(MediaType.APPLICATION_JSON)
      .post(Entity.entity(jsnO(
        "query",
        jsn(
          "mutation CreateDataSet($dataSetName: String!) {" +
            "  createDataSet(dataSetName: $dataSetName) {" +
            "    dataSetId" +
            "  }" +
            "}"
        ),
        "variables",
        jsnO(
          "dataSetName", jsn(dataSetName)
        )
      ).toString(), MediaType.valueOf("application/json")));

    assertThat(graphQlCall.getStatus(), is(200));

    final WebTarget updateLoadJsonLdTarget =
      client.target(String.format(
        "http://localhost:%d/v5/" + PREFIX + "/" + dataSetName + "/upload/jsonld/",
        APP.getLocalPort()
      ));


    Response createResponse = updateLoadJsonLdTarget.request()
      .header(HttpHeaders.AUTHORIZATION, "fake")
      .put(Entity.json(testRdfReader));

    if (createResponse.getStatus() != 201) {
      System.out.println(createResponse.readEntity(String.class));
    }
    assertThat(createResponse.getStatus(), is(201));


    Response graphqlCall = call("/v5/graphql")
      .accept(MediaType.APPLICATION_JSON)
      .post(Entity.entity(String.format("{\n" +
        "  dataSets {\n" +
        "    %1s__%2s {\n" +
        "      local_col_Person(uri: \"http://example.com/the/actual/entity\") {\n" +
        "        tim_latestRevision {\n" +
        "          uri\n" +
        "        }\n" +
        "      }\n" +
        "    }\n" +
        "  }\n" +
        "}", PREFIX, dataSetName), MediaType.valueOf("application/graphql")));
    ObjectNode objectNode = graphqlCall.readEntity(ObjectNode.class);
    String revision = objectNode
      .get("data")
      .get("dataSets")
      .get(createDataSetId(dataSetName))
      .get("local_col_Person")
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
      "        \"predicate\": \"http://example.org/predicate/pred2\", \n" +
      "        \"value\": \"extra value\"\n" +
      "      }\n" +
      "    ],\n" +
      "    \"deletions\": [\n" +
      "      {\n" +
      "        \"@type\": \"http://timbuctoo.huygens.knaw.nl/v5/vocabulary#mutation\", \n" +
      "        \"predicate\": \"http://example.org/predicate/pred2\", \n" +
      "        \"value\": \"multiple\"\n" +
      "      }\n" +
      "    ]\n" +
      "  }]\n" +
      "}\n";

    final WebTarget createTarget2 =
      client.target(String.format(
        "http://localhost:%d/v5/" + PREFIX + "/" + dataSetName + "/upload/jsonld/",
        APP.getLocalPort()
      ));


    Response createResponse2 = createTarget2.request()
      .header(HttpHeaders.AUTHORIZATION, "fake")
      .put(Entity.json(testRdfReader2));

    if (createResponse2.getStatus() != 201) {
      System.out.println(createResponse2.readEntity(String.class));
    }
    assertThat(createResponse2.getStatus(), is(201));

    graphqlCall = call("/v5/graphql")
      .accept(MediaType.APPLICATION_JSON)
      .post(Entity.entity(String.format("{\n" +
        "  dataSets {\n" +
        "    %1s__%2s {\n" +
        "      local_col_Person(uri:\"http://example.com/the/actual/entity\"){\n" +
        "        local_pred_pred2List {\n" +
        "          items {\n" +
        "            value\n" +
        "          }\n" +
        "        }\n" +
        "      }\n" +
        "    }\n" +
        "  }\n" +
        "}", PREFIX, dataSetName), MediaType.valueOf("application/graphql")));
    objectNode = graphqlCall.readEntity(ObjectNode.class);

    assertThat(
      stream(objectNode
        .get("data")
        .get("dataSets")
        .get(createDataSetId(dataSetName))
        .get("local_col_Person")
        .get("local_pred_pred2List")
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
    final String dataSetId = createDataSetId(dataSetName);
    Response uploadResponse = multipartPost(
      "/v5/" + PREFIX + "/" + dataSetName + "/upload/rdf?forceCreation=true",
      new File(getResource(IntegrationTest.class, "bia_clusius.nqud").toURI()),
      "application/vnd.timbuctoo-rdf.nquads_unified_diff",
      ImmutableMap.of(
        "encoding", "UTF-8",
        "uri", "http://example.com/clusius.nqud"
      )
    );

    assertThat("Successful upload of rdf", uploadResponse.getStatus(), is(201));

    final String collectionUri = "http://timbuctoo.huygens.knaw.nl/datasets/clusius/Persons";
    final String type = "test3";
    final String value = "test4";
    Response graphQlCall = call("/v5/graphql")
      .accept(MediaType.APPLICATION_JSON)
      .post(Entity.entity(jsnO(
        "query",
        jsn(
          "mutation setViewConfig($dataSetId: String!, $collectionUri: String!, $type: String!, $value: String) " +
            "{setViewConfig(dataSet: $dataSetId, collectionUri: $collectionUri, viewConfig: [{type: $type, value: " +
            "$value, subComponents: [], formatter: []}]){   type    value}}"),
        "variables",
        jsnO(
          "dataSetId", jsn(dataSetId),
          "collectionUri", jsn(collectionUri),
          "type", jsn(type),
          "value", jsn(value)
        )
      ).toString(), MediaType.valueOf("application/json")));

    assertThat(graphQlCall.getStatus(), is(200));
    assertThat(graphQlCall.readEntity(ObjectNode.class), is(jsnO(
      "data",
      jsnO("setViewConfig", jsnA(
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
    assertThat(
      stream(metaData
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

  @Test
  public void schemaCanBeCustomizedWithGraphQl() throws Exception {
    final String dataSetName = "clusius_" + UUID.randomUUID().toString().replace("-", "_");
    final String dataSetId = createDataSetId(dataSetName);
    Response uploadResponse = multipartPost(
      "/v5/" + PREFIX + "/" + dataSetName + "/upload/rdf?forceCreation=true",
      new File(getResource(IntegrationTest.class, "bia_clusius.nqud").toURI()),
      "application/vnd.timbuctoo-rdf.nquads_unified_diff",
      ImmutableMap.of(
        "encoding", "UTF-8",
        "uri", "http://example.com/clusius.nqud"
      )
    );

    assertThat("Successful upload of rdf", uploadResponse.getStatus(), is(201));

    ObjectNode customSchemaField = jsnO(
      "name", jsn("test"),
      "uri", jsn(""),
      "shortenedUri", jsn(""),
      "isList", jsn(false),
      "values", jsnA(jsn("String")),
      "type", jsnO("name", jsn("String"))
    );

    ObjectNode customSchemaField2 = jsnO(
      "name", jsn("test2"),
      "uri", jsn(""),
      "shortenedUri", jsn(""),
      "isList", jsn(false),
      "values", jsnA(jsn("String")),
      "type", jsnO("name", jsn("http://timbuctoo.huygens.knaw.nl/datasets/clusius/Persons"))
    );

    ObjectNode customSchema = jsnO(
      "name", jsn("http://timbuctoo.huygens.knaw.nl/datasets/clusius/Persons"),
      "fields", jsnA(customSchemaField, customSchemaField2)
    );
    Response graphQlCall = call("/v5/graphql")
      .accept(MediaType.APPLICATION_JSON)
      .post(Entity.entity(jsnO(
        "query",
        jsn(
          "mutation extendSchema($dataSet: String!, $customSchema: [CustomSchemaTypeInput!]!) { " +
            "   extendSchema(dataSet:$dataSet,customSchema:$customSchema){\n" +
            "    message\n" +
            "   }" +
            "}"),
        "variables",
        jsnO(
          "dataSet", jsn(dataSetId),
          "customSchema", customSchema
        )
      ).toString(), MediaType.valueOf("application/json")));

    assertThat(graphQlCall.getStatus(), is(200));

    ObjectNode returnedData = graphQlCall.readEntity(ObjectNode.class);

    assertThat(returnedData.get("data").get("extendSchema").get("message").toString(),
      is("\"Schema extended successfully.\""));


    Response retrieveExtendedSchema = call("/v5/graphql")
      .accept(MediaType.APPLICATION_JSON)
      .post(Entity.entity(jsnO(
        "query",
        jsn(String.format(
          "query retrieveExtendedSchema  {\n" +
            "  dataSets {\n" +
            "    %s {\n" +
            "      http___timbuctoo_huygens_knaw_nl_datasets_clusius_PersonsList {\n" +
            "        items {\n" +
            "          test {\n" +
            "            type\n" +
            "          }\n" +
            "        }\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}", dataSetId))
      ).toString(), MediaType.valueOf("application/json")));

    assertThat(retrieveExtendedSchema.getStatus(), is(200));

    ObjectNode retrievedData = retrieveExtendedSchema.readEntity(ObjectNode.class);

    assertThat(retrievedData.get("data").get("dataSets").get(dataSetId)
        .get("http___timbuctoo_huygens_knaw_nl_datasets_clusius_PersonsList")
        .get("items").get(0).get("test").isNull(),
      is(true));
  }

  private String createDataSetId(String dataSetName) {
    return PREFIX + "__" + dataSetName;
  }

  @Test
  public void authorizationReadFromAuthorizationsFileIfVreFileNotPresent() throws Exception {
    // Create a dataset
    Client client = ClientBuilder.newBuilder().build();
    String dataSetName = "dataset" + UUID.randomUUID().toString().replace("-", "_");


    Response graphQlCall = call("/v5/graphql")
      .accept(MediaType.APPLICATION_JSON)
      .post(Entity.entity(jsnO(
        "query",
        jsn(
          "mutation CreateDataSet($dataSetName: String!) {" +
            "  createDataSet(dataSetName: $dataSetName) {" +
            "    dataSetId" +
            "  }" +
            "}"
        ),
        "variables",
        jsnO(
          "dataSetName", jsn(dataSetName)
        )
      ).toString(), MediaType.valueOf("application/json")));


    assertThat(graphQlCall.getStatus(), is(200));
    // check if the dataset is created
    List<String> dataSetNamesOfDummy = getDataSetNamesOfDummy();
    System.out.println("datasets: " + dataSetNamesOfDummy);
    assertThat(dataSetNamesOfDummy, hasItem(PREFIX + "__" + dataSetName));

  }

  @Test
  public void indexConfigCanBeChangedWithGraphQl() throws Exception {
    final String dataSetName = "clusius_" + UUID.randomUUID().toString().replace("-", "_");
    final String dataSetId = createDataSetId(dataSetName);
    Response uploadResponse = multipartPost(
      "/v5/" + PREFIX + "/" + dataSetName + "/upload/rdf?forceCreation=true",
      new File(getResource(IntegrationTest.class, "bia_clusius.nqud").toURI()),
      "application/vnd.timbuctoo-rdf.nquads_unified_diff",
      ImmutableMap.of(
        "encoding", "UTF-8",
        "uri", "http://example.com/clusius.nqud"
      )
    );

    assertThat("Successful upload of rdf", uploadResponse.getStatus(), is(201));

    final String collectionUri = "http://timbuctoo.huygens.knaw.nl/datasets/clusius/Persons";
    Response graphQlCall = call("/v5/graphql")
      .accept(MediaType.APPLICATION_JSON)
      .post(Entity.entity(jsnO(
        "query",
        jsn(
          "mutation SetIndexConfig($dataSetId:ID!, $collectionUri: String!, $indexConfig: IndexConfigInput!) {\n" +
            "  setIndexConfig(dataSet: $dataSetId, collectionUri:$collectionUri, indexConfig: $indexConfig) {\n" +
            "    facet {\n" +
            "      paths\n" +
            "      type\n" +
            "    }\n" +
            "  }\n" +
            "}"),
        "variables",
        jsnO(
          "dataSetId", jsn(dataSetId),
          "collectionUri", jsn(collectionUri),
          "indexConfig", jsnO(
            "facet", jsnA(
              jsnO(
                "paths", jsnA(jsn("tim_name.value")),
                "type", jsn("MultiSelect")
              )
            ),
            "fullText", jsnA()
          )
        )
      ).toString(), MediaType.valueOf("application/json")));

    assertThat(graphQlCall.getStatus(), is(200));
    assertThat(graphQlCall.readEntity(ObjectNode.class), is(jsnO(
      "data",
      jsnO(
        "setIndexConfig", jsnO(
          "facet", jsnA(
            jsnO(
              "paths", jsnA(jsn("tim_name.value")),
              "type", jsn("MultiSelect")
            )
          )
        )
      )
    )));

    graphQlCall = call("/v5/graphql")
      .accept(MediaType.APPLICATION_JSON)
      .post(
        Entity.entity(
          jsnO(
            "query", jsn(
              "query metadata($dataSetId: ID!) {\n" +
                "  dataSetMetadata(dataSetId: $dataSetId) {\n" +
                "    collectionList {\n" +
                "      items {\n" +
                "        uri\n" +
                "        indexConfig {\n" +
                "          facet {\n" +
                "            paths\n" +
                "            type\n" +
                "          }\n" +
                "          fullText {\n" +
                "            fields {\n" +
                "              path\n" +
                "            }\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}"
            ),
            "variables", jsnO(
              "dataSetId", jsn(dataSetId)
            )
          ),
          MediaType.valueOf("application/json")
        )
      );

    assertThat(graphQlCall.getStatus(), is(200));
    ObjectNode metaData = graphQlCall.readEntity(ObjectNode.class);
    assertThat(
      stream(metaData
        .get("data")
        .get("dataSetMetadata")
        .get("collectionList")
        .get("items").iterator()).collect(Collectors.toList()),
      hasItem(jsnO(
        "uri", jsn(collectionUri),
        "indexConfig", jsnO(
          "facet", jsnA(
            jsnO(
              "paths", jsnA(jsn("tim_name.value")),
              "type", jsn("MultiSelect")
            )
          ),
          "fullText", jsnA()
        )
      ))
    );

  }

  @Test
  public void dataSetCanBeCreatedWithGraphQl() {
    final String dataSetName = "clusius_" + UUID.randomUUID().toString().replace("-", "_");
    final String dataSetId = createDataSetId(dataSetName);

    Response graphQlCall = call("/v5/graphql")
      .accept(MediaType.APPLICATION_JSON)
      .post(Entity.entity(jsnO(
        "query",
        jsn(
          "mutation CreateDataSet($dataSetName: String!) {" +
            "  createDataSet(dataSetName: $dataSetName) {" +
            "    dataSetId" +
            "  }" +
            "}"
        ),
        "variables",
        jsnO(
          "dataSetName", jsn(dataSetName)
        )
      ).toString(), MediaType.valueOf("application/json")));

    assertThat(graphQlCall.getStatus(), is(200));
    assertThat(graphQlCall.readEntity(ObjectNode.class), is(jsnO(
      "data",
      jsnO(
        "createDataSet", jsnO(
          "dataSetId", jsn(dataSetId)
        )
      )
    )));

    graphQlCall = call("/v5/graphql")
      .accept(MediaType.APPLICATION_JSON)
      .post(
        Entity.entity(
          jsnO(
            "query", jsn(
              "query metadata($dataSetId: ID!) {\n" +
                "  dataSetMetadata(dataSetId: $dataSetId) {\n" +
                "    dataSetId" +
                "  }\n" +
                "}"
            ),
            "variables", jsnO(
              "dataSetId", jsn(dataSetId)
            )
          ),
          MediaType.valueOf("application/json")
        )
      );

    assertThat(graphQlCall.getStatus(), is(200));
    ObjectNode metaData = graphQlCall.readEntity(ObjectNode.class);
    assertThat(metaData, is(
      jsnO(
        "data", jsnO(
          "dataSetMetadata", jsnO(
            "dataSetId", jsn(dataSetId)
          )
        )
      )
    ));

  }

  @Test
  public void dataSetCanBeDeletedWithGraphQl() {
    final String dataSetName = "clusius_" + UUID.randomUUID().toString().replace("-", "_");
    final String dataSetId = createDataSetId(dataSetName);

    Response createCall = call("/v5/graphql")
      .accept(MediaType.APPLICATION_JSON)
      .post(Entity.entity(jsnO(
        "query",
        jsn(
          "mutation CreateDataSet($dataSetName: String!) {" +
            "  createDataSet(dataSetName: $dataSetName) {" +
            "    dataSetId" +
            "  }" +
            "}"
        ),
        "variables",
        jsnO(
          "dataSetName", jsn(dataSetName)
        )
      ).toString(), MediaType.valueOf("application/json")));

    assertThat(createCall.getStatus(), is(200));
    assertThat(createCall.readEntity(ObjectNode.class), is(jsnO(
      "data",
      jsnO(
        "createDataSet", jsnO(
          "dataSetId", jsn(dataSetId)
        )
      )
    )));

    Response retrieveBeforeDelete = call("/v5/graphql")
      .accept(MediaType.APPLICATION_JSON)
      .post(Entity.entity(jsnO(
        "query",
        jsn(
          "{\n" +
            "  allDataSets {\n" +
            "    dataSetId\n" +
            "  }\n" +
            "}"
        ),
        "variables",
        jsnO(
          "dataSetName", jsn(dataSetName)
        )
      ).toString(), MediaType.valueOf("application/json")));

    assertThat(retrieveBeforeDelete.getStatus(), is(200));
    ObjectNode dataSetsBeforeDelete = retrieveBeforeDelete.readEntity(ObjectNode.class);
    assertThat(stream(dataSetsBeforeDelete
        .get("data")
        .get("allDataSets").iterator()
      ).collect(Collectors.toList()),
      hasItem((JsonNode) jsnO("dataSetId", jsn(dataSetId)))
    );

    Response deleteCall = call("/v5/graphql")
      .accept(MediaType.APPLICATION_JSON)
      .post(Entity.entity(jsnO(
        "query",
        jsn(
          "mutation DeleteDataSet($dataSetId: String!) {" +
            "  deleteDataSet(dataSetId: $dataSetId) {" +
            "    dataSetId" +
            "  }" +
            "}"
        ),
        "variables",
        jsnO(
          "dataSetId", jsn(dataSetId)
        )
      ).toString(), MediaType.valueOf("application/json")));

    assertThat(deleteCall.getStatus(), is(200));
    assertThat(deleteCall.readEntity(ObjectNode.class), is(jsnO(
      "data",
      jsnO(
        "deleteDataSet", jsnO(
          "dataSetId", jsn(dataSetId)
        )
      )
    )));

    Response retrieveAfterDelete = call("/v5/graphql")
      .accept(MediaType.APPLICATION_JSON)
      .post(Entity.entity(jsnO(
        "query",
        jsn(
          "{\n" +
            "  allDataSets {\n" +
            "    dataSetId\n" +
            "  }\n" +
            "}"
        ),
        "variables",
        jsnO(
          "dataSetName", jsn(dataSetName)
        )
      ).toString(), MediaType.valueOf("application/json")));

    assertThat(retrieveAfterDelete.getStatus(), is(200));
    ObjectNode dataSetsAfterDelete = retrieveAfterDelete.readEntity(ObjectNode.class);
    assertThat(stream(dataSetsAfterDelete
        .get("data")
        .get("allDataSets").iterator()
      ).collect(Collectors.toList()),
      not(hasItem((JsonNode) jsnO("dataSetId", jsn(dataSetId))))
    );
  }

  @Test
  public void resourceSyncShowsAllThePublicDataSets() throws Exception {
    final String dataSetName = "clusius_" + UUID.randomUUID().toString().replace("-", "_");
    final String dataSetId = createDataSetId(dataSetName);
    Response uploadResponse = multipartPost(
      "/v5/" + PREFIX + "/" + dataSetName + "/upload/rdf?forceCreation=true",
      new File(getResource(IntegrationTest.class, "bia_clusius.nqud").toURI()),
      "application/vnd.timbuctoo-rdf.nquads_unified_diff",
      ImmutableMap.of(
        "encoding", "UTF-8",
        "uri", "http://example.com/clusius.nqud"
      )
    );
    assertThat(uploadResponse.getStatus(), is(201));

    call("/v5/graphql")
      .accept(MediaType.APPLICATION_JSON)
      .post(Entity.entity(jsnO(
        "query",
        jsn(
          "mutation Publish($dataSetId: String!) {" +
            "  publish(dataSet: $dataSetId) {" +
            "    dataSetId" +
            "  }" +
            "}"
        ),
        "variables",
        jsnO(
          "dataSetId", jsn(dataSetId)
        )
      ).toString(), MediaType.valueOf("application/json")));

    Response checkPublishedCall = callWithoutAuthentication("/v5/graphql")
      .accept(MediaType.APPLICATION_JSON)
      .post(Entity.entity(jsnO(
        "query",
        jsn(
          "query MetaData($dataSetId: ID!) {" +
            "  dataSetMetadata(dataSetId: $dataSetId) {" +
            "    dataSetId" +
            "  }" +
            "}"
        ),
        "variables",
        jsnO(
          "dataSetId", jsn(dataSetId)
        )
      ).toString(), MediaType.valueOf("application/json")));

    assertThat(checkPublishedCall.readEntity(ObjectNode.class), is(jsnO(
      "data",
      jsnO(
        "dataSetMetadata", jsnO(
          "dataSetId", jsn(dataSetId)
        )
      )
    )));

    Response resourceSyncCall = callWithoutAuthentication("/v5/resourcesync/sourceDescription.xml").get();

    ByteArrayInputStream value = new ByteArrayInputStream(resourceSyncCall.readEntity(String.class).getBytes());
    Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(value);
    XPath xpath = XPathFactory.newInstance().newXPath();

    NodeList uriNodes =  (NodeList) xpath.compile("/urlset/url/loc").evaluate(document, XPathConstants.NODESET);
    Set<String> dataSets = new HashSet<>();
    for (int i = 0; i < uriNodes.getLength(); i++) {
      dataSets.add(uriNodes.item(i).getTextContent());
    }

    assertThat(dataSets, hasItem(endsWith(dataSetName + "/capabilitylist.xml")));
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


  private Invocation.Builder callWithoutAuthentication(String path) {
    String server;
    if (path.startsWith("http://") || path.startsWith("https://")) {
      server = path;
    } else {
      int localPort = APP.getLocalPort();
      server = format("http://localhost:%d" + path, localPort);
    }
    return client
      .target(server)
      .register(MultiPartFeature.class)
      // .register(new LoggingFilter(Logger.getLogger(LoggingFilter.class.getName()), true))
      .request();
  }
}
