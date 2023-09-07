package nl.knaw.huygens.timbuctoo.webhook;

public class NoOpWebhooks implements Webhooks {
  @Override
  public void dataSetUpdated(String dataSetId)  {
    //ignore
  }
}
