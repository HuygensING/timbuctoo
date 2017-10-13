package nl.knaw.huygens.timbuctoo.v5.util;

public class RdfConstants {

  private static final String TIM = "http://timbuctoo.huygens.knaw.nl/static/v5/";
  public static final String TIM_VOCAB = TIM + "vocabulary#";
  public static final String TIM_PROP_DESC = TIM + "propertyDescription/";
  public static final String TIM_PROVENANCE_ENTITIES = TIM + "provenanceEntities/";
  public static final String TIM_TABULAR_FILE = TIM + "types#tabularFile";
  public static final String TIM_SKOLEMIZE = TIM + "skolemized/";
  public static final String TIM_USERS = TIM + "users/";

  public static final String TIM_JSONLD_UPLOAD_CONTEXT = TIM + "jsonLdUploadContext.json";
  // Vocabulary
  public static final String TIM_COLLECTION = TIM_VOCAB + "collection";
  public static final String TIM_LATEST_REVISION = TIM_VOCAB + "latestRevision";
  public static final String TIM_SPECIALIZATION_OF = TIM_VOCAB + "specialization";
  public static final String TIMBUCTOO_NEXT = TIM_VOCAB + "next";
  public static final String UNKNOWN = TIM_VOCAB + "unknown";
  public static final String TIM_PROP_NAME = TIM_VOCAB + "timpropname";
  public static final String TIM_HAS_ROW = TIM_VOCAB + "timhasrow";
  public static final String TIM_HAS_PROPERTY = TIM_VOCAB + "hasProperty";
  public static final String TIM_PROP_ID = TIM_VOCAB + "propertyId";
  public static final String TIM_MIMETYPE = TIM_VOCAB + "mimetype";
  public static final String TIM_HASCOLOR = TIM_VOCAB + "hasColor";
  public static final String TIM_EDITOR = TIM_VOCAB + "editor";
  public static final String TIM_HASINDEXERCONFIG = TIM_VOCAB + "hasIndexConfig";
  public static final String TIM_HASFULLTEXTSEARCH = TIM_VOCAB + "hasFullTextSearch";
  public static final String TIM_HASFACETPATH = TIM_VOCAB + "hasFacetPath";
  public static final String TIM_NEXTFACET = TIM_VOCAB + "nextFacet";
  public static final String TIM_SUMMARYTITLEPREDICATE = TIM_VOCAB + "summaryTitlePredicate";
  public static final String TIM_SUMMARYDESCRIPTIONPREDICATE = TIM_VOCAB + "summaryDescriptionPredicate";
  public static final String TIM_SUMMARYIMAGEPREDICATE = TIM_VOCAB + "summaryImagePredicate";

  public static final String PROV_DERIVED_FROM = "http://www.w3.org/ns/prov#wasDerivedFrom";
  public static final String RDF_TYPE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
  public static final String RDFS_LABEL = "http://www.w3.org/2000/01/rdf-schema#label";
  public static final String LANGSTRING = "http://www.w3.org/1999/02/22-rdf-syntax-ns#langString";
  public static final String STRING = "http://www.w3.org/2001/XMLSchema#string";
  public static final String XSD_BOOLEAN = "http://www.w3.org/2001/XMLSchema#boolean";
  public static final String INTEGER = "http://www.w3.org/2001/XMLSchema#integer";
}
