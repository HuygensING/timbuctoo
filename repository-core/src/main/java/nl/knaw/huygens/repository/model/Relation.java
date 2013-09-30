package nl.knaw.huygens.repository.model;

import nl.knaw.huygens.repository.annotations.IDPrefix;

/**
 * A relation between domain documents.
 *
 * (This resembles an RDF statement.)
 * 
 * N.B. Relations are indexed by {@code RelationIndexer),
 * which does not require index annotations.
 * 
 * There is a conceptual problem to solve.
 * Suppose we have a relation between an ATLArchive and an ATLArchiver.
 * What types do we use? Those types or the primitive types?
 * When indexing, this problem comes back to use straightaway:
 * If we want to store the displayname (which we do), it depends on
 * the type specified which value we retrieve from the database
 * (it's a different variation we retrieve), so "just" indexing is
 * too simple a concept: we need an index for a VRE.
 */
@IDPrefix("REL")
public class Relation extends DomainDocument {

  /** A reference to the 'active' participant of the relation (resembles rdf:subject). */
  private Reference sourceRef;
  /** A reference to the property or characteristic of the subject (resembles rdf:predicate). */
  private Reference typeRef;
  /** A reference to the 'passive' participant of the relation (resembles rdf:object). */
  private Reference targetRef;

  // For deserialization...
  public Relation() {}

  public Relation(Reference sourceRef, Reference typeRef, Reference targetRef) {
    // TODO validate consistency
    this.sourceRef = sourceRef;
    this.typeRef = typeRef;
    this.targetRef = targetRef;
  }

  @Override
  public String getDisplayName() {
    return String.format("(%s, %s, %s)", sourceRef, typeRef, targetRef);
  }

  public Reference getSourceRef() {
    return sourceRef;
  }

  public void setSourceRef(Reference sourceRef) {
    this.sourceRef = sourceRef;
  }

  public Reference getTypeRef() {
    return typeRef;
  }

  public void setTypeRef(Reference typeRef) {
    this.typeRef = typeRef;
  }

  public Reference getTargetRef() {
    return targetRef;
  }

  public void setTargetRef(Reference targetRef) {
    this.targetRef = targetRef;
  }

  @Override
  public boolean equals(Object object) {
    if (object instanceof Relation) {
      Relation that = (Relation) object;
      return (this.typeRef == null ? that.typeRef == null : this.typeRef.equals(that.typeRef)) //
          && (this.sourceRef == null ? that.sourceRef == null : this.sourceRef.equals(that.sourceRef)) //
          && (this.targetRef == null ? that.targetRef == null : this.targetRef.equals(that.targetRef));
    }
    return false;
  }

  @Override
  public int hashCode() {
    int result = 17;
    result = 31 * result + (typeRef == null ? 0 : typeRef.hashCode());
    result = 31 * result + (sourceRef == null ? 0 : sourceRef.hashCode());
    result = 31 * result + (targetRef == null ? 0 : targetRef.hashCode());
    return result;
  }

}
