package nl.knaw.huygens.timbuctoo.v5.dataset;

public interface OptimizedPatchListener {

  void start();

  void onChangedSubject(String subject, ChangeFetcher changeFetcher);

  void notifyUpdate();

  void finish();
}
