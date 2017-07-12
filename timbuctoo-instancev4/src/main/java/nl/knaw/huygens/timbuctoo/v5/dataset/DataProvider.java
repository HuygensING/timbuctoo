package nl.knaw.huygens.timbuctoo.v5.dataset;

public interface DataProvider {
  void subscribeToRdf(RdfProcessor processor, String cursor);

  void subscribeToEntities(EntityProcessor processor, String cursor);
}
