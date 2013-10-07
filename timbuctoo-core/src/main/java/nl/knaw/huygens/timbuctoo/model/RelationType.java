package nl.knaw.huygens.timbuctoo.model;

import nl.knaw.huygens.timbuctoo.annotations.IDPrefix;

/**
 * The type of a relation between domain entities.
 *
 * (This resembles an RDF predicate.)
 */
@IDPrefix("RELT")
public class RelationType extends SystemEntity {

  /** The name that uniquely identifies this relation type. */
  private String relTypeName;
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

  public RelationType(String typeName, Class<? extends DomainEntity> sourceDocType, Class<? extends DomainEntity> targetDocType) {
    this.relTypeName = typeName;
    this.sourceDocType = sourceDocType;
    this.targetDocType = targetDocType;
    this.reflexive = sourceDocType.equals(targetDocType);
    this.symmetric = false;
  }

  public RelationType(String typeName, Class<? extends DomainEntity> docType) {
    this(typeName, docType, docType);
  }

  @Override
  public String getDisplayName() {
    return relTypeName;
  }

  public String getRelTypeName() {
    return relTypeName;
  }

  public void setRelTypeName(String typeName) {
    this.relTypeName = typeName;
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
