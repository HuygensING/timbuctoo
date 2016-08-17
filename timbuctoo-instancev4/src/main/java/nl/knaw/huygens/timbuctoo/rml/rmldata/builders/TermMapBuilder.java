package nl.knaw.huygens.timbuctoo.rml.rmldata.builders;

import nl.knaw.huygens.timbuctoo.rml.rdfshim.RdfLiteral;
import nl.knaw.huygens.timbuctoo.rml.rdfshim.RdfResource;
import nl.knaw.huygens.timbuctoo.rml.rmldata.termmaps.RrColumn;
import nl.knaw.huygens.timbuctoo.rml.rmldata.termmaps.RrConstant;
import nl.knaw.huygens.timbuctoo.rml.rmldata.termmaps.RrTemplate;
import nl.knaw.huygens.timbuctoo.rml.rmldata.termmaps.RrTermMap;
import nl.knaw.huygens.timbuctoo.rml.rmldata.termmaps.TermType;

import java.util.Optional;
import java.util.Set;

public class TermMapBuilder {
  private final boolean isObjectMap;
  private RrTermMap instance;

  TermMapBuilder(boolean isObjectMap) {
    this.isObjectMap = isObjectMap;
  }

  private TermType getDefaultTermType(boolean forColumn, boolean hasLanguage, boolean hasDatatype) {
    /*
    If the term map does not have a rr:termType property, then its term type is:

      rr:Literal, if it is an object map and at least one of the following conditions is true:
        It is a column-based term map.
        It has a rr:language property (and thus a specified language tag).
        It has a rr:datatype property (and thus a specified datatype).
      rr:IRI, otherwise.
     */
    if (isObjectMap && (forColumn || hasLanguage || hasDatatype)) {
      return TermType.Literal;
    } else {
      return TermType.IRI;
    }
  }

  public TermMapBuilder withConstantTerm(String value) {
    instance = new RrConstant(value);
    return this;
  }

  public TermMapBuilder withColumnTerm(String referenceString) {
    withColumnTerm(referenceString, Optional.empty(), Optional.empty(), Optional.empty());
    return this;
  }

  public TermMapBuilder withColumnTerm(String referenceString, TermType type) {
    withColumnTerm(referenceString, Optional.of(type), Optional.empty(), Optional.empty());
    return this;
  }

  public void withColumnTerm(RdfResource column, Set<RdfResource> termTypes, Set<RdfResource> languages,
                             Set<RdfResource> datatypes) {
    Optional<TermType> termType = termTypes.stream().findAny().flatMap(TermType::forNode);
    Optional<String> language = languages.stream().findAny().flatMap(RdfResource::asLiteral).map(RdfLiteral::getValue);
    Optional<String> datatype = datatypes.stream().findAny().flatMap(RdfResource::asIri);

    String value = column.asLiteral().orElseThrow(() -> new RuntimeException("")).getValue();
    withColumnTerm(value, termType, language, datatype);
  }

  public void withColumnTerm(String value, Optional<TermType> termType, Optional<String> language,
                             Optional<String> datatype) {
    if (termType.isPresent()) {
      instance = new RrColumn(value, termType.get());
    } else {
      instance = new RrColumn(value, getDefaultTermType(true, language.isPresent(), datatype.isPresent()));
    }
  }

  public TermMapBuilder withTemplateTerm(String referenceString) {
    withTemplateTerm(referenceString, Optional.empty(), Optional.empty(), Optional.empty());
    return this;
  }

  public TermMapBuilder withTemplateTerm(String referenceString, TermType type) {
    withTemplateTerm(referenceString, Optional.of(type), Optional.empty(), Optional.empty());
    return this;
  }

  public void withTemplateTerm(RdfResource column, Set<RdfResource> termTypes, Set<RdfResource> languages,
                             Set<RdfResource> datatypes) {
    Optional<TermType> termType = termTypes.stream().findAny().flatMap(TermType::forNode);
    Optional<String> language = languages.stream().findAny().flatMap(RdfResource::asLiteral).map(RdfLiteral::getValue);
    Optional<String> datatype = datatypes.stream().findAny().flatMap(RdfResource::asIri);

    String value = column.asLiteral().orElseThrow(() -> new RuntimeException("")).getValue();
    withTemplateTerm(value, termType, language, datatype);
  }

  public void withTemplateTerm(String value, Optional<TermType> termType, Optional<String> language,
                             Optional<String> datatype) {
    if (termType.isPresent()) {
      instance = new RrTemplate(value, termType.get());
    } else {
      instance = new RrTemplate(value, getDefaultTermType(false, language.isPresent(), datatype.isPresent()));
    }
  }

  RrTermMap build() {
    return instance;
  }

}
