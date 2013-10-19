package nl.knaw.huygens.timbuctoo.model.dcar;

/**
 * Defines names of relation types. These names must correspond with
 * the ones in {@code RelationType} entities in the data store.
 */
public enum RelTypeNames {

  IS_CREATOR_OF("is_creator_of", "is_created_by"), //
  HAS_KEYWORD("has_keyword", "is_keyword_of"), //
  HAS_PERSON("has_person", "is_person_of"), //
  HAS_PLACE("has_place", "is_place_of"), //
  HAS_PARENT_ARCHIVE("has_parent_archive", "has_child_archive"), //
  HAS_SIBLING_ARCHIVE("has_sibling_archive", "has_sibling_archive"), //
  HAS_SIBLING_ARCHIVER("has_sibling_archiver", "has_sibling_archiver");

  public final String regular;
  public final String inverse;

  private RelTypeNames(String regular, String inverse) {
    this.regular = regular;
    this.inverse = inverse;
  }

}
