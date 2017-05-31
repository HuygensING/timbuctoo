package nl.knaw.huygens.timbuctoo.v5.dataset;

public class DummyDataProvider implements DataProvider {

  @Override
  public void subscribeToRdf(RdfProcessor processor, String cursor) { }

  @Override
  public void subscribeToEntities(EntityProcessor processor, String cursor) { }
}
