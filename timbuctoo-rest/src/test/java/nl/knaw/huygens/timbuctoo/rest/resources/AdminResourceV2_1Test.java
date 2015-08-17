package nl.knaw.huygens.timbuctoo.rest.resources;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.index.IndexRequest;
import nl.knaw.huygens.timbuctoo.index.IndexRequests;
import nl.knaw.huygens.timbuctoo.messages.ActionType;
import nl.knaw.huygens.timbuctoo.messages.Broker;
import nl.knaw.huygens.timbuctoo.messages.Producer;
import nl.knaw.huygens.timbuctoo.rest.util.ClientIndexRequest;
import org.junit.Before;
import org.junit.Test;
import test.model.projecta.SubADomainEntity;

import javax.jms.JMSException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import static com.sun.jersey.api.client.ClientResponse.Status.BAD_REQUEST;
import static com.sun.jersey.api.client.ClientResponse.Status.CREATED;
import static com.sun.jersey.api.client.ClientResponse.Status.INTERNAL_SERVER_ERROR;
import static com.sun.jersey.api.client.ClientResponse.Status.NOT_FOUND;
import static com.sun.jersey.api.client.ClientResponse.Status.OK;
import static nl.knaw.huygens.timbuctoo.config.Paths.ADMIN_PATH;
import static nl.knaw.huygens.timbuctoo.config.Paths.INDEX_REQUEST_PATH;
import static nl.knaw.huygens.timbuctoo.config.Paths.V2_1_PATH;
import static nl.knaw.huygens.timbuctoo.messages.Broker.INDEX_QUEUE;
import static nl.knaw.huygens.timbuctoo.rest.resources.ActionMatcher.likeAction;
import static nl.knaw.huygens.timbuctoo.rest.resources.AdminResourceV2_1.INDEX_PRODUCER;
import static nl.knaw.huygens.timbuctoo.rest.resources.IndexRequestMatcher.likeIndexRequest;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AdminResourceV2_1Test extends WebServiceTestSetup {

  public static final String REQUEST_ID = "7659943b-bdee-4ad3-b8fc-a0f1329d6e9f";
  public static final String EXCEPTION_MESSAGE = "Exception message";
  public static final Class<SubADomainEntity> TYPE = SubADomainEntity.class;
  private Broker broker;
  private Producer indexProducer;
  private int numberOfCollectionsToIndex;
  private IndexRequests indexRequestStatus;
  public static final ClientIndexRequest CLIENT_INDEX_REQUEST = new ClientIndexRequest(TYPE);

  @Before
  public void setup() throws JMSException {
    setupBroker();
    setupIndexRequestStatus();
    TypeRegistry typeRegistry = injector.getInstance(TypeRegistry.class);
    numberOfCollectionsToIndex = typeRegistry.getPrimitiveDomainEntityTypes().size();
  }

  private void setupIndexRequestStatus() {
    indexRequestStatus = injector.getInstance(IndexRequests.class);
    when(indexRequestStatus.add(any(IndexRequest.class))).thenReturn(REQUEST_ID);
  }

  private void setupBroker() throws JMSException {
    broker = injector.getInstance(Broker.class);
    indexProducer = mock(Producer.class);
    when(broker.getProducer(INDEX_PRODUCER, INDEX_QUEUE)).thenReturn(indexProducer);
  }

  @Test
  public void postIndexRequestReturnsAInternalServerErrorWhenTheProducerCouldNotBeRetrieved() throws Exception {
    // setup
    when(broker.getProducer(INDEX_PRODUCER, INDEX_QUEUE)).thenThrow(new JMSException(EXCEPTION_MESSAGE));

    // action
    ClientResponse response = indexRequestResource().type(MediaType.APPLICATION_JSON_TYPE).post(ClientResponse.class, CLIENT_INDEX_REQUEST);

    // verify
    verifyResponseStatus(response, INTERNAL_SERVER_ERROR);
  }

  @Test
  public void postIndexRequestCreatesAnIndexRequestForTheCollectionAndPostsItToTheBroker() throws Exception {
    // setup
    String expectedLocationHeader = getExpectedLocationHeader(REQUEST_ID);

    // action
    ClientResponse response = indexRequestResource().accept(MediaType.APPLICATION_JSON_TYPE).type(MediaType.APPLICATION_JSON_TYPE).post(ClientResponse.class, CLIENT_INDEX_REQUEST);

    // verify
    verifyResponseStatus(response, CREATED);

    String location = response.getHeaders().getFirst(HttpHeaders.LOCATION);
    assertThat(location, is(expectedLocationHeader));

    verify(indexRequestStatus).add(argThat(likeIndexRequest().withType(TYPE)));
    verify(indexProducer).send(argThat( //
      likeAction() //
        .withActionType(ActionType.MOD) //
        .withRequestId(REQUEST_ID)));
  }

  @Test
  public void postIndexRequestReturnsABadRequestStatusWhenTheClientIndexRequestTypeIsNotNull() throws Exception {
    // setup
    ClientIndexRequest clientIndexRequest = new ClientIndexRequest();

    // action
    ClientResponse response = indexRequestResource().accept(MediaType.APPLICATION_JSON_TYPE).type(MediaType.APPLICATION_JSON_TYPE).post(ClientResponse.class, clientIndexRequest);

    // verify
    verifyResponseStatus(response, BAD_REQUEST);
  }

  @Test
  public void getIndexRequestReturnsTheDescriptionOfTheIndexRequestAndThatItIsRunning() {
    // setup
    when(indexRequestStatus.get(REQUEST_ID)).thenReturn(IndexRequest.indexAll());

    // action
    ClientResponse response = indexRequestResource().path(REQUEST_ID).get(ClientResponse.class);

    // verify
    verifyResponseStatus(response, OK);
    assertThat(response.getEntity(String.class), is(IndexRequest.indexAll().toClientRep()));
  }

  @Test
  public void getIndexRequestReturnsNotFoundIfTheRequestCouldNotBeFound() {
    // setup
    when(indexRequestStatus.get(REQUEST_ID)).thenReturn(null);

    // action
    ClientResponse response = indexRequestResource().path(REQUEST_ID).get(ClientResponse.class);

    // verify
    verifyResponseStatus(response, NOT_FOUND);
    verify(indexRequestStatus).get(REQUEST_ID);
  }

  private String getExpectedLocationHeader(String requestId) {
    return indexRequestResource().getUriBuilder().path(requestId).build().toString();
  }

  private WebResource indexRequestResource() {
    return resource().path(V2_1_PATH).path(ADMIN_PATH).path(INDEX_REQUEST_PATH);
  }

}
