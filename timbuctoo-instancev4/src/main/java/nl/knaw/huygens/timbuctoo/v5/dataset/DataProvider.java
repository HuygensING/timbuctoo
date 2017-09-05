package nl.knaw.huygens.timbuctoo.v5.dataset;

public interface DataProvider {
  void subscribeToRdf(RdfProcessor processor, int cursor);

  void subscribeToEntities(EntityProcessor processor, int cursor);
}
