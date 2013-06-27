package nl.knaw.huygens.repository.model.atla;

import nl.knaw.huygens.repository.annotations.DocumentTypeName;
import nl.knaw.huygens.repository.annotations.IndexAnnotation;
import nl.knaw.huygens.repository.model.Keyword;
import nl.knaw.huygens.repository.model.Legislation;
import nl.knaw.huygens.repository.model.util.Period;

@DocumentTypeName("atlalegislation")
public class AtlaLegislation extends Legislation {

  private Keyword[] keywords;
  private Period period;
  private String _id;
  private String aantekeningen;
  private String contents;
  private String link_archival_dbase;
  private String made_by;
  private String orig_filename;
  private String original_archival_source;
  private String other_publication;
  private String pages;
  private String reference;
  private String remarks;
  private String scan;
  private String see_also;
  private String titel_eng;
  private String[] related;

  public Keyword[] getKeywords() {
    return keywords;
  }

  public void setKeywords(Keyword[] keywords) {
    this.keywords = keywords;
  }

  public String get_id() {
    return _id;
  }

  public void set_id(String _id) {
    this._id = _id;
  }

  @IndexAnnotation(fieldName = "facet_t_aantekeningen")
  public String getAantekeningen() {
    return aantekeningen;
  }

  public void setAantekeningen(String aantekeningen) {
    this.aantekeningen = aantekeningen;
  }

  @IndexAnnotation(fieldName = "facet_t_contents")
  public String getContents() {
    return contents;
  }

  public void setContents(String contents) {
    this.contents = contents;
  }

  @IndexAnnotation(fieldName = "facet_t_link_archival_dbase")
  public String getLink_archival_dbase() {
    return link_archival_dbase;
  }

  public void setLink_archival_dbase(String link_archival_dbase) {
    this.link_archival_dbase = link_archival_dbase;
  }

  @IndexAnnotation(fieldName = "facet_t_made_by")
  public String getMade_by() {
    return made_by;
  }

  public void setMade_by(String made_by) {
    this.made_by = made_by;
  }

  @IndexAnnotation(fieldName = "facet_t_orig_filename")
  public String getOrig_filename() {
    return orig_filename;
  }

  public void setOrig_filename(String orig_filename) {
    this.orig_filename = orig_filename;
  }

  @IndexAnnotation(fieldName = "facet_t_original_archival_source")
  public String getOriginal_archival_source() {
    return original_archival_source;
  }

  public void setOriginal_archival_source(String original_archival_source) {
    this.original_archival_source = original_archival_source;
  }

  @IndexAnnotation(fieldName = "facet_t_other_publication")
  public String getOther_publication() {
    return other_publication;
  }

  public void setOther_publication(String other_publication) {
    this.other_publication = other_publication;
  }

  @IndexAnnotation(fieldName = "facet_t_pages")
  public String getPages() {
    return pages;
  }

  public void setPages(String pages) {
    this.pages = pages;
  }

  public Period getPeriod() {
    return period;
  }

  public void setPeriod(Period period) {
    this.period = period;
  }

  @IndexAnnotation(fieldName = "facet_t_reference")
  public String getReference() {
    return reference;
  }

  public void setReference(String reference) {
    this.reference = reference;
  }

  @IndexAnnotation(fieldName = "facet_t_remarks")
  public String getRemarks() {
    return remarks;
  }

  public void setRemarks(String remarks) {
    this.remarks = remarks;
  }

  @IndexAnnotation(fieldName = "facet_t_scan")
  public String getScan() {
    return scan;
  }

  public void setScan(String scan) {
    this.scan = scan;
  }

  @IndexAnnotation(fieldName = "facet_t_see_also")
  public String getSee_also() {
    return see_also;
  }

  public void setSee_also(String see_also) {
    this.see_also = see_also;
  }

  @IndexAnnotation(fieldName = "facet_t_titel_eng")
  public String getTitel_eng() {
    return titel_eng;
  }

  public void setTitel_eng(String titel_eng) {
    this.titel_eng = titel_eng;
  }

  public String[] getRelated() {
    return related;
  }

  public void setRelated(String[] related) {
    this.related = related;
  }

}
