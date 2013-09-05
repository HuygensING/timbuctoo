package nl.knaw.huygens.repository.rest.resources;

import nl.knaw.huygens.repository.rest.security.apis.ApisAuthorizationServerResourceFilter;

import org.surfnet.oaaas.model.VerifyTokenResponse;

public class MockApisAuthorizationServerResourceFilter extends ApisAuthorizationServerResourceFilter {
  private VerifyTokenResponse verifyTokenResponse;

  public MockApisAuthorizationServerResourceFilter() {
    super("123", "123", "123", false);
  }

  @Override
  protected VerifyTokenResponse getVerifyTokenResponse(String accessToken) {
    return this.verifyTokenResponse;
  }

  public void setVerifyTokenResponse(VerifyTokenResponse verifyTokenResponse) {
    this.verifyTokenResponse = verifyTokenResponse;
  }

}
