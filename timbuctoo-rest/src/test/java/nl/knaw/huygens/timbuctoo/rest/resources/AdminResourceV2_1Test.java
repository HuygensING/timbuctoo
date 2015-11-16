package nl.knaw.huygens.timbuctoo.rest.resources;

import com.google.common.collect.Maps;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.index.request.IndexRequest;
import nl.knaw.huygens.timbuctoo.index.request.IndexRequestFactory;
import nl.knaw.huygens.timbuctoo.messages.ActionType;
import nl.knaw.huygens.timbuctoo.messages.Broker;
import nl.knaw.huygens.timbuctoo.messages.Producer;
import nl.knaw.huygens.timbuctoo.model.ModelException;
import nl.knaw.huygens.timbuctoo.rest.util.ClientIndexRequest;
import nl.knaw.huygens.timbuctoo.rest.util.CustomHeaders;
import nl.knaw.huygens.timbuctoo.security.UserRoles;
import nl.knaw.huygens.timbuctoo.vre.VRE;
import org.junit.Before;
import org.junit.Test;
import test.model.projecta.SubADomainEntity;

import javax.jms.JMSException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.util.Map;

import static com.sun.jersey.api.client.ClientResponse.Status.BAD_REQUEST;
import static com.sun.jersey.api.client.ClientResponse.Status.FORBIDDEN;
import static com.sun.jersey.api.client.ClientResponse.Status.INTERNAL_SERVER_ERROR;
import static com.sun.jersey.api.client.ClientResponse.Status.OK;
import static com.sun.jersey.api.client.ClientResponse.Status.UNAUTHORIZED;
import static nl.knaw.huygens.timbuctoo.config.Paths.ADMIN_PATH;
import static nl.knaw.huygens.timbuctoo.config.Paths.INDEX_REQUEST_PATH;
import static nl.knaw.huygens.timbuctoo.config.Paths.V2_1_PATH;
import static nl.knaw.huygens.timbuctoo.messages.Broker.INDEX_QUEUE;
import static nl.knaw.huygens.timbuctoo.rest.resources.ActionMatcher.likeAction;
import static nl.knaw.huygens.timbuctoo.rest.resources.AdminResourceV2_1.INDEX_PRODUCER;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AdminResourceV2_1Test extends WebServiceTestSetup {

  public static final String EXCEPTION_MESSAGE = "Exception message";
  public static final Class<SubADomainEntity> TYPE = SubADomainEntity.class;
  public static final Map<String, Object> CLIENT_REP = Maps.newHashMap();
  private Broker broker;
  private Producer indexProducer;
  public static final ClientIndexRequest CLIENT_INDEX_REQUEST = new ClientIndexRequest(TypeNames.getExternalName(TYPE));
  private VRE vre;
  private IndexRequestFactory indexRequestFactory;
  private IndexRequest indexRequest;

  @Before
  public void setup() throws JMSException, ModelException {
    setupBroker();
    setupIndexRequestFactory();
    TypeRegistry typeRegistry = injector.getInstance(TypeRegistry.class);
    typeRegistry.init(TYPE.getPackage().getName());
    vre = mock(VRE.class);
    makeVREAvailable(vre, VRE_ID);
  }

  private void setupIndexRequestFactory() {
    indexRequestFactory = injector.getInstance(IndexRequestFactory.class);
    when(indexRequestFactory.forCollectionOf(TYPE)).thenReturn(indexRequest);
  }

  private void setupBroker() throws JMSException {
    broker = injector.getInstance(Broker.class);
    indexProducer = mock(Producer.class);
    when(broker.getProducer(INDEX_PRODUCER, INDEX_QUEUE)).thenReturn(indexProducer);
  }

  @Test
  public void postIndexRequestInitiatesAIndexationOfACollection() throws Exception {
    // setup
    setupUserWithRoles(VRE_ID, USER_ID, UserRoles.ADMIN_ROLE);
    when(vre.inScope(TYPE)).thenReturn(true);

    // action
    ClientResponse response = withHeaders(asJsonRequest(indexRequestResource())).post(ClientResponse.class, CLIENT_INDEX_REQUEST);

    // verify
    verifyResponseStatus(response, OK);

    verify(indexRequestFactory).forCollectionOf(TYPE);
    verify(indexProducer).send(argThat( //
      likeAction() //
        .withActionType(ActionType.MOD) //
        .withType(TYPE)
        .withForMultiEntitiesFlag(true)));
  }

  @Test
  public void postIndexRequestReturnsAInternalServerErrorWhenTheProducerCouldNotBeRetrieved() throws Exception {
    // setup
    setupUserWithRoles(VRE_ID, USER_ID, UserRoles.ADMIN_ROLE);
    when(vre.inScope(TYPE)).thenReturn(true);
    when(broker.getProducer(INDEX_PRODUCER, INDEX_QUEUE)).thenThrow(new JMSException(EXCEPTION_MESSAGE));

    // action
    ClientResponse response = withHeaders(asJsonRequest(indexRequestResource())).post(ClientResponse.class, CLIENT_INDEX_REQUEST);

    // verify
    verifyResponseStatus(response, INTERNAL_SERVER_ERROR);
  }


  @Test
  public void postIndexRequestReturnsABadRequestStatusWhenTheClientIndexRequestCollectionNameIsNull() throws Exception {
    // setup
    setupUserWithRoles(VRE_ID, USER_ID, UserRoles.ADMIN_ROLE);
    when(vre.inScope(TYPE)).thenReturn(true);
    ClientIndexRequest clientIndexRequest = new ClientIndexRequest();

    // action
    ClientResponse response = withHeaders(asJsonRequest(indexRequestResource())).post(ClientResponse.class, clientIndexRequest);

    // verify
    verifyResponseStatus(response, BAD_REQUEST);
  }

  @Test
  public void postIndexRequestReturnsABadRequestStatusWhenTheClientIndexRequestCollectionNameIsNotValid() throws Exception {
    // setup
    setupUserWithRoles(VRE_ID, USER_ID, UserRoles.ADMIN_ROLE);
    when(vre.inScope(TYPE)).thenReturn(true);
    ClientIndexRequest clientIndexRequest = new ClientIndexRequest("invalid collection");

    // action
    ClientResponse response = withHeaders(asJsonRequest(indexRequestResource())).post(ClientResponse.class, clientIndexRequest);

    // verify
    verifyResponseStatus(response, BAD_REQUEST);
  }

  @Test
  public void postWithoutVREAndAuthorizationHandlersReturnsAUnauthorized() {
    // action
    ClientResponse response = withHeaders(asJsonRequest(indexRequestResource())).post(ClientResponse.class, CLIENT_INDEX_REQUEST);

    // verify
    verifyResponseStatus(response, UNAUTHORIZED);
  }

  @Test
  public void postWithVREAndAuthorizationReturnsForbiddenIfTheUserHasTheUserRole() {
    // setup
    setupUserWithRoles(VRE_ID, USER_ID, UserRoles.USER_ROLE);
    when(vre.inScope(TYPE)).thenReturn(true);

    // action
    ClientResponse response = withHeaders(asJsonRequest(indexRequestResource())).post(ClientResponse.class, CLIENT_INDEX_REQUEST);

    // verify
    verifyResponseStatus(response, FORBIDDEN);
  }

  @Test
  public void postWithVREAndAuthorizationReturnsForbiddenIfTheUserHasTheUnverifiedUserRole() {
    // setup
    setupUserWithRoles(VRE_ID, USER_ID, UserRoles.UNVERIFIED_USER_ROLE);
    when(vre.inScope(TYPE)).thenReturn(true);

    // action
    ClientResponse response = withHeaders(asJsonRequest(indexRequestResource())).post(ClientResponse.class, CLIENT_INDEX_REQUEST);

    // verify
    verifyResponseStatus(response, FORBIDDEN);
  }

  @Test
  public void postWithVREAndAuthorizationReturnsForbiddenIfTheVREDoesNotContainTheCollection() {
    // setup
    setupUserWithRoles(VRE_ID, USER_ID, UserRoles.ADMIN_ROLE);
    when(vre.inScope(TYPE)).thenReturn(false);

    // action
    ClientResponse response = withHeaders(asJsonRequest(indexRequestResource())).post(ClientResponse.class, CLIENT_INDEX_REQUEST);

    // verify
    verifyResponseStatus(response, FORBIDDEN);
  }

  //--------------------------------------------------------------------------------------------------------------------


  private WebResource.Builder withHeaders(WebResource.Builder resource) {
    return resource.header(CustomHeaders.VRE_ID_KEY, VRE_ID).header(HttpHeaders.AUTHORIZATION, USER_ID);
  }

  private WebResource.Builder asJsonRequest(WebResource webResource) {
    return webResource.accept(MediaType.APPLICATION_JSON_TYPE).type(MediaType.APPLICATION_JSON_TYPE);
  }

  private WebResource indexRequestResource() {
    return resource().path(V2_1_PATH).path(ADMIN_PATH).path(INDEX_REQUEST_PATH);
  }

}
