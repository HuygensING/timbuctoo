package nl.knaw.huygens.timbuctoo.rml;

public interface DataSourceWithJoiningSupport extends DataSource {
  void willBeJoinedOn(String fieldName, Object referenceJoinValue, String uri, String outputFieldName);
}
