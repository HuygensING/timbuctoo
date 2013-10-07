package nl.knaw.huygens.solr;

public class SolrFields {

  public static final String SORT_PREFIX = "sort_";

  public static final String UNKNOWN_VALUE = "unknown";
  public static final String DOC_ID = "id";

  /** Field used by Solr for refering to relevance of search results. */
  public static final String SCORE = "score";

  //  /** Field for storing letter texts. */
  //  public static final String TEXT = "text";

  static final String PROJECT_ID = "project_id";
  static final String ID = "id";
  static final String NAME = "name";
  static final String PUBLISHABLE = "publishable";

  public static final String METADATAFIELD_PREFIX = "metadata_";
  static final String TEXTLAYER_PREFIX = "textlayer_";
  static final String TEXTLAYERCS_PREFIX = "textlayercs_";
  static final String ANNOTATION_PREFIX = "annotations_";
  static final String ANNOTATIONCS_PREFIX = "annotationscs_";

  private SolrFields() {
    throw new AssertionError("Non-instantiable class");
  }

}
