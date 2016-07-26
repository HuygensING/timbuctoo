package nl.knaw.huygens.timbuctoo.rml;


public class SubjectMap {
  final TermMapContent termMapContent;
  final String className;

  public SubjectMap(TermMapContent termMapContent, String className) {
    this.termMapContent = termMapContent;
    this.className = className;
  }

  public SubjectMap(TermMapContent termMapContent) {
    this.termMapContent = termMapContent;
    this.className = null;
  }

}
