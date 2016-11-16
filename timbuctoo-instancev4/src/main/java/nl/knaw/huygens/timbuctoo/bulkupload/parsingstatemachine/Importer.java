package nl.knaw.huygens.timbuctoo.bulkupload.parsingstatemachine;

public class Importer {

  private final StateMachine importer;
  private final ResultReporter resultReporter;

  public Importer(StateMachine importer, ResultReporter resultReporter) {
    this.importer = importer;
    this.resultReporter = resultReporter;
  }

  public void startCollection(String collectionName) {
    resultReporter.startCollection(collectionName, importer.startCollection(collectionName));
  }

  public void registerPropertyName(int id, String name) {
    resultReporter.registerPropertyName(id, name, importer.registerPropertyName(id, name));
  }

  public void startEntity() {
    resultReporter.startEntity();
    importer.startEntity();
  }

  public void setValue(int id, String value) {
    resultReporter.setValue(id, value, importer.setValue(id, value));
  }

  public void finishEntity() {
    resultReporter.finishEntity(importer.finishEntity());
  }

  public void finishCollection() {
    resultReporter.finishCollection();
    importer.finishCollection();
  }

}
