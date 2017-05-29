package nl.knaw.huygens.timbuctoo.v5.dataset;

public class ValuePredicate extends PredicateData {
  private final String value;
  private final String dataType;

  public ValuePredicate(String uri, String value, String dataType) {
    super(uri);

    this.value = value;
    this.dataType = dataType;
  }

  @Override
  public void handle(PredicateHandler handler) {
    handler.onValue(value, dataType);
  }
}
