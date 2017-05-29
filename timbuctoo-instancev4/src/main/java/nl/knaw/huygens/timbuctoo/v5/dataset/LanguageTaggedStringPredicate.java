package nl.knaw.huygens.timbuctoo.v5.dataset;

public class LanguageTaggedStringPredicate extends PredicateData {
  private final String value;
  private final String language;

  protected LanguageTaggedStringPredicate(String uri, String value, String language) {
    super(uri);
    this.value = value;
    this.language = language;
  }

  @Override
  public void handle(PredicateHandler handler) {
    handler.onLanguageTaggedString(value, language);
  }
}
