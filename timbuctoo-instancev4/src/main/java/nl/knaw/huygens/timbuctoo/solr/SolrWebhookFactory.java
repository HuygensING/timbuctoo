package nl.knaw.huygens.timbuctoo.solr;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Optional;

public class SolrWebhookFactory {

  @JsonProperty
  private boolean isEnabled;

  @JsonProperty
  private String url;

  public Optional<SolrWebhook> getWebHook() {
    return isEnabled ? Optional.of(new SolrWebhookImpl(url)) : Optional.empty();
  }
}
