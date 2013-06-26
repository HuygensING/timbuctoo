package nl.knaw.huygens.repository.model.atla;

import nl.knaw.huygens.repository.annotations.DocumentTypeName;
import nl.knaw.huygens.repository.annotations.IDPrefix;
import nl.knaw.huygens.repository.indexdata.IndexAnnotation;
import nl.knaw.huygens.repository.model.DomainDocument;
import nl.knaw.huygens.repository.model.util.Period;

import com.fasterxml.jackson.annotation.JsonIgnore;

@IDPrefix("ARM")
@DocumentTypeName("archivematerial")
public class ArchiveMaterial extends DomainDocument {

  private Keyword[] keywords;
  private Period period;
  private String _id;
  private String aantekeningen;
  private String code_subfonds;
  private String extent;
  private String finding_aid;
  private String itemno;
  private String link_law;
  private String made_by;
  private String notes;
  private String orig_filename;
  private String period_description;
  private String ref_code;
  private String rf_archive;
  private String scope;
  private String series;
  private String titel_eng;
  private String title;
  private String[] countries;
  private String[] overhead_titles;

  // "related":[{"ids":[3200],"type":"overhead_title"}],

  @Override
  @JsonIgnore
  @IndexAnnotation(fieldName = "desc")
  public String getDescription() {
    return title;
  }

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

  public String getAantekeningen() {
    return aantekeningen;
  }

  public void setAantekeningen(String aantekeningen) {
    this.aantekeningen = aantekeningen;
  }

  public String getCode_subfonds() {
    return code_subfonds;
  }

  public void setCode_subfonds(String code_subfonds) {
    this.code_subfonds = code_subfonds;
  }

  public String getExtent() {
    return extent;
  }

  public void setExtent(String extent) {
    this.extent = extent;
  }

  public String getFinding_aid() {
    return finding_aid;
  }

  public void setFinding_aid(String finding_aid) {
    this.finding_aid = finding_aid;
  }

  public String getItemno() {
    return itemno;
  }

  public void setItemno(String itemno) {
    this.itemno = itemno;
  }

  public String getLink_law() {
    return link_law;
  }

  public void setLink_law(String link_law) {
    this.link_law = link_law;
  }

  public String getMade_by() {
    return made_by;
  }

  public void setMade_by(String made_by) {
    this.made_by = made_by;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }

  public String getOrig_filename() {
    return orig_filename;
  }

  public void setOrig_filename(String orig_filename) {
    this.orig_filename = orig_filename;
  }

  public Period getPeriod() {
    return period;
  }

  public void setPeriod(Period period) {
    this.period = period;
  }

  public String getPeriod_description() {
    return period_description;
  }

  public void setPeriod_description(String period_description) {
    this.period_description = period_description;
  }

  public String getRef_code() {
    return ref_code;
  }

  public void setRef_code(String ref_code) {
    this.ref_code = ref_code;
  }

  public String getRf_archive() {
    return rf_archive;
  }

  public void setRf_archive(String rf_archive) {
    this.rf_archive = rf_archive;
  }

  public String getScope() {
    return scope;
  }

  public void setScope(String scope) {
    this.scope = scope;
  }

  public String getSeries() {
    return series;
  }

  public void setSeries(String series) {
    this.series = series;
  }

  public String getTitel_eng() {
    return titel_eng;
  }

  public void setTitel_eng(String titel_eng) {
    this.titel_eng = titel_eng;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String[] getCountries() {
    return countries;
  }

  public void setCountries(String[] countries) {
    this.countries = countries;
  }

  public String[] getOverhead_titles() {
    return overhead_titles;
  }

  public void setOverhead_titles(String[] overhead_titles) {
    this.overhead_titles = overhead_titles;
  }

}
