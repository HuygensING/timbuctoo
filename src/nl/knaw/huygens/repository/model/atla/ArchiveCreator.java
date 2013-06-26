package nl.knaw.huygens.repository.model.atla;

import nl.knaw.huygens.repository.annotations.DocumentTypeName;
import nl.knaw.huygens.repository.annotations.IDPrefix;
import nl.knaw.huygens.repository.annotations.IndexAnnotation;
import nl.knaw.huygens.repository.model.DomainDocument;
import nl.knaw.huygens.repository.model.Keyword;
import nl.knaw.huygens.repository.model.util.Period;

import com.fasterxml.jackson.annotation.JsonIgnore;

@IDPrefix("ARC")
@DocumentTypeName("archivecreator")
public class ArchiveCreator extends DomainDocument {

  private Keyword[] keywords;
  private Period period;
  private String _id;
  private String aantekeningen;
  private String his_func;
  private String link_law;
  private String literatuur;
  private String made_by;
  private String name_english;
  private String name;
  private String notes;
  private String orig_filename;
  private String period_description;
  private String[] related_archives;
  private String[] related_creators;
  private String[] types;

  //  "related":[{"ids":[78],"type":"archive"}],

  @Override
  @JsonIgnore
  @IndexAnnotation(fieldName = "desc")
  public String getDescription() {
    return name;
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

  public String getHis_func() {
    return his_func;
  }

  public void setHis_func(String his_func) {
    this.his_func = his_func;
  }

  public String getLink_law() {
    return link_law;
  }

  public void setLink_law(String link_law) {
    this.link_law = link_law;
  }

  public String getLiteratuur() {
    return literatuur;
  }

  public void setLiteratuur(String literatuur) {
    this.literatuur = literatuur;
  }

  public String getMade_by() {
    return made_by;
  }

  public void setMade_by(String made_by) {
    this.made_by = made_by;
  }

  public String getName_english() {
    return name_english;
  }

  public void setName_english(String name_english) {
    this.name_english = name_english;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
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

  public String[] getRelated_archives() {
    return related_archives;
  }

  public void setRelated_archives(String[] related_archives) {
    this.related_archives = related_archives;
  }

  public String[] getRelated_creators() {
    return related_creators;
  }

  public void setRelated_creators(String[] related_creators) {
    this.related_creators = related_creators;
  }

  public String[] getTypes() {
    return types;
  }

  public void setTypes(String[] types) {
    this.types = types;
  }

}
