package nl.knaw.huygens.timbuctoo.model;

import nl.knaw.huygens.timbuctoo.annotations.IDPrefix;

/**
 * The type of a relation between domain entities.
 *
 * (This resembles an RDF predicate.)
 */
@IDPrefix("RELT")
public class RelationType extends SystemEntity {

  /** The name of this relation type. */
  private String regularName;
  /** The name of this relation type when source and target are interchanged. */
  private String inverseName;
  /** The type token of the 'active' participant of the relation. */
  private Class<? extends DomainEntity> sourceDocType;
  /** The type token of the 'passive' participant of the relation. */
  private Class<? extends DomainEntity> targetDocType;
  /** If source and target doc types are the same, is relation(A,A) allowed? */
  private boolean reflexive;
  /** If source and target doc types are the same, does relation(A,B) imply relation(B,A)? */
  private boolean symmetric;

  // For deserialization...
  public RelationType() {}

  public RelationType(String regularName, String inverseName, Class<? extends DomainEntity> sourceDocType, Class<? extends DomainEntity> targetDocType) {
    this.regularName = regularName;
    this.inverseName = inverseName;
    this.sourceDocType = sourceDocType;
    this.targetDocType = targetDocType;
    this.reflexive = sourceDocType.equals(targetDocType);
    this.symmetric = false;
  }

  public RelationType(String name, Class<? extends DomainEntity> docType) {
    this(name, name, docType, docType);
  }

  @Override
  public String getDisplayName() {
    return regularName;
  }

  public String getRegularName() {
    return regularName;
  }

  public void setRegularName(String name) {
    regularName = name;
  }

  public String getInverseName() {
    return inverseName;
  }

  public void setInverseName(String inverseName) {
    this.inverseName = inverseName;
  }

  public Class<? extends DomainEntity> getSourceDocType() {
    return sourceDocType;
  }

  public void setSourceDocType(Class<? extends DomainEntity> sourceDocType) {
    this.sourceDocType = sourceDocType;
  }

  public Class<? extends DomainEntity> getTargetDocType() {
    return targetDocType;
  }

  public void setTargetDocType(Class<? extends DomainEntity> targetDocType) {
    this.targetDocType = targetDocType;
  }

  public boolean isReflexive() {
    return reflexive;
  }

  public void setReflexive(boolean reflexive) {
    this.reflexive = reflexive;
  }

  public boolean isSymmetric() {
    return symmetric;
  }

  public void setSymmetric(boolean symmetric) {
    this.symmetric = symmetric;
  }

}
