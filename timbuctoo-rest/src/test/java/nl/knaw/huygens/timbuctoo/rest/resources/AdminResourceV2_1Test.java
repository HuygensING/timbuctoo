package nl.knaw.huygens.timbuctoo.rest.resources;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.index.IndexRequest;
import nl.knaw.huygens.timbuctoo.index.IndexRequestStatus;
import nl.knaw.huygens.timbuctoo.messages.ActionType;
import nl.knaw.huygens.timbuctoo.messages.Broker;
import nl.knaw.huygens.timbuctoo.messages.Producer;
import org.junit.Before;
import org.junit.Test;

import javax.jms.JMSException;
import javax.ws.rs.core.HttpHeaders;

import static com.sun.jersey.api.client.ClientResponse.Status.CREATED;
import static com.sun.jersey.api.client.ClientResponse.Status.INTERNAL_SERVER_ERROR;
import static nl.knaw.huygens.timbuctoo.config.Paths.ADMIN_PATH;
import static nl.knaw.huygens.timbuctoo.config.Paths.INDEX_REQUEST_PATH;
import static nl.knaw.huygens.timbuctoo.config.Paths.V2_1_PATH;
import static nl.knaw.huygens.timbuctoo.index.IndexRequest.INDEX_ALL;
import static nl.knaw.huygens.timbuctoo.messages.Broker.INDEX_QUEUE;
import static nl.knaw.huygens.timbuctoo.rest.resources.ActionMatcher.likeAction;
import static nl.knaw.huygens.timbuctoo.rest.resources.AdminResourceV2_1.INDEX_PRODUCER;
import static nl.knaw.huygens.timbuctoo.rest.resources.IndexRequestMatcher.likeIndexRequestMatcher;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AdminResourceV2_1Test extends WebServiceTestSetup {

  public static final String REQUEST_ID = "requestId";
  public static final String EXCEPTION_MESSAGE = "Exception message";
  private Broker broker;
  private Producer indexProducer;
  private int numberOfCollectionsToIndex;
  private IndexRequestStatus indexRequestStatus;

  @Before
  public void setup() throws JMSException {
    setupBroker();
    setupIndexRequestStatus();
    TypeRegistry typeRegistry = injector.getInstance(TypeRegistry.class);
    numberOfCollectionsToIndex = typeRegistry.getPrimitiveDomainEntityTypes().size();
  }

  private void setupIndexRequestStatus() {
    indexRequestStatus = injector.getInstance(IndexRequestStatus.class);
    when(indexRequestStatus.add(any(IndexRequest.class))).thenReturn(REQUEST_ID);
  }

  private void setupBroker() throws JMSException {
    broker = injector.getInstance(Broker.class);
    indexProducer = mock(Producer.class);
    when(broker.getProducer(INDEX_PRODUCER, INDEX_QUEUE)).thenReturn(indexProducer);
  }

  @Test
  public void postIndexRequestCreatesATemporaryRestResourceAndFiresAnIndexMessageForEachCollection() throws Exception {
    // action
    ClientResponse response = indexRequestResource().post(ClientResponse.class);

    String expectedLocationHeader = getExpectedLocationHeader(REQUEST_ID);

    // verify
    verifyResponseStatus(response, CREATED);
    String location = response.getHeaders().getFirst(HttpHeaders.LOCATION);
    assertThat(location, is(expectedLocationHeader));

    verify(indexProducer, times(numberOfCollectionsToIndex)).send(argThat( //
      likeAction() //
        .withActionType(ActionType.MOD) //
        .withForMultiEntitiesFlag(true)));
    verify(indexRequestStatus).add(argThat(likeIndexRequestMatcher().withDesc(INDEX_ALL)));
  }

  private String getExpectedLocationHeader(String requestId) {
    return indexRequestResource().getUriBuilder().path(requestId).build().toString();
  }

  private WebResource indexRequestResource() {
    return resource().path(V2_1_PATH).path(ADMIN_PATH).path(INDEX_REQUEST_PATH);
  }

  @Test
  public void postIndexRequestReturnsAInternalServerErrorWhenTheProducerCouldNotBeRetrieved() throws Exception {
    // setup
    when(broker.getProducer(INDEX_PRODUCER, INDEX_QUEUE)).thenThrow(new JMSException(EXCEPTION_MESSAGE));

    // action
    ClientResponse response = indexRequestResource().post(ClientResponse.class);

    // verify
    verifyResponseStatus(response, INTERNAL_SERVER_ERROR);
  }

}
