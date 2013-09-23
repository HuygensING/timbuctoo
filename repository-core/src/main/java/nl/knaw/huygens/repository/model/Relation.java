package nl.knaw.huygens.repository.model;

import nl.knaw.huygens.repository.annotations.IDPrefix;
import nl.knaw.huygens.repository.facet.IndexAnnotation;

/**
 * A relation between domain documents.
 *
 * (This resembles an RDF statement.)
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
  private Reference source;
  /** A reference to the property or characteristic of the subject (resembles rdf:predicate). */
  private Reference type;
  /** A reference to the 'passive' participant of the relation (resembles rdf:object). */
  private Reference target;

  // For deserialization...
  public Relation() {}

  public Relation(Reference source, Reference type, Reference target) {
    // TODO validate consistency
    this.source = source;
    this.type = type;
    this.target = target;
  }

  @Override
  public String getDisplayName() {
    return String.format("(%s, %s, %s)", source, type, target);
  }

  @IndexAnnotation(fieldName = "dynamic_s_source", accessors = { "getId" }, canBeEmpty = false, isFaceted = false)
  public Reference getSource() {
    return source;
  }

  public void setSource(Reference source) {
    this.source = source;
  }

  @IndexAnnotation(fieldName = "dynamic_s_type", canBeEmpty = false, isFaceted = false)
  public Reference getType() {
    return type;
  }

  public void setType(Reference type) {
    this.type = type;
  }

  @IndexAnnotation(fieldName = "dynamic_s_target", accessors = { "getId" }, canBeEmpty = false, isFaceted = false)
  public Reference getTarget() {
    return target;
  }

  public void setTarget(Reference target) {
    this.target = target;
  }

}
