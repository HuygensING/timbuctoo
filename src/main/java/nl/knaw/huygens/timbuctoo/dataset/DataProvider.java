package nl.knaw.huygens.timbuctoo.dataset;

public interface DataProvider {
  void subscribeToRdf(RdfProcessor processor);
}
