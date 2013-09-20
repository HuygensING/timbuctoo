package nl.knaw.huygens.repository.model;

import nl.knaw.huygens.repository.annotations.IDPrefix;

/**
 * The type of a relation between domain documents.
 *
 * (This resembles an RDF predicate.)
 */
@IDPrefix("RLT")
public class RelationType extends DomainDocument {

  /** The name that uniquely identifies this type. */
  private String typeName;
  /** The type token of the 'active' participant of the relation. */
  private Class<? extends DomainDocument> sourceDocType;
  /** The type token of the 'passive' participant of the relation. */
  private Class<? extends DomainDocument> targetDocType;
  /** If source and target doc types are the same, is relation(A,A) allowed? */
  private boolean reflexive;
  /** If source and target doc types are the same, does relation(A,B) imply relation(B,A)? */
  private boolean symmetric;

  // For deserialization...
  public RelationType() {}

  public RelationType(String typeName, Class<? extends DomainDocument> sourceDocType, Class<? extends DomainDocument> targetDocType) {
    this.typeName = typeName;
    this.sourceDocType = sourceDocType;
    this.targetDocType = targetDocType;
    this.reflexive = sourceDocType.equals(targetDocType);
    this.symmetric = false;
  }

  public RelationType(String typeName, Class<? extends DomainDocument> docType) {
    this(typeName, docType, docType);
  }

  @Override
  public String getDisplayName() {
    return getTypeName();
  }

  public String getTypeName() {
    return typeName;
  }

  public void setTypeName(String typeName) {
    this.typeName = typeName;
  }

  public Class<? extends DomainDocument> getSourceDocType() {
    return sourceDocType;
  }

  public void setSourceDocType(Class<? extends DomainDocument> sourceDocType) {
    this.sourceDocType = sourceDocType;
  }

  public Class<? extends DomainDocument> getTargetDocType() {
    return targetDocType;
  }

  public void setTargetDocType(Class<? extends DomainDocument> targetDocType) {
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
