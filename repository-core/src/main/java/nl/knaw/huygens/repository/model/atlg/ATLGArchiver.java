package nl.knaw.huygens.repository.model.atlg;

import java.util.List;

import nl.knaw.huygens.repository.facet.FacetType;
import nl.knaw.huygens.repository.facet.IndexAnnotation;
import nl.knaw.huygens.repository.facet.IndexAnnotations;
import nl.knaw.huygens.repository.model.Archiver;
import nl.knaw.huygens.repository.model.EntityRef;
import nl.knaw.huygens.repository.model.util.PeriodHelper;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Lists;

public class ATLGArchiver extends Archiver {

  /** Migration: Name of source file */
  private String origFilename;
  /** ING Forms: "Name" */
  private String nameNld;
  /** ING Forms: "English name"; text searchable */
  public String nameEng;
  /** ING Forms: "Begin date"; date facet */
  private String beginDate;
  /** ING Forms: "End date"; date facet */
  private String endDate;
  /** ING Forms: "Period description" */
  private String periodDescription;
  /** ING Forms: "History/functions/occupations/activities"; text searchable */
  private String history;
  /** ING Forms: "Title(s) related archive(s)" */
  private List<EntityRef> relatedArchives;
  /** ING Forms: "Title(s) related creator(s)" */
  private List<EntityRef> relatedArchivers;
  /** ING Forms: "Keyword(s) geography"; place facet */
  private List<EntityRef> placeKeywords;
  /** ING Forms: "Keyword(s) subject"; subject facet */
  private List<EntityRef> subjectKeywords;
  /** ING Forms: "Keyword(s) person"; person facet */
  private List<EntityRef> persons;
  /** ING Forms: "Remarks"; text searchable */
  private String notes;
  /** ING Forms: "Literature" */
  private String literature;
  /** ING Forms: "Record made by-" */
  private String madeBy;
  /** ING Forms: "Reminders" ??? */
  private String reminders;
  /** ING Forms: "Binnenkomende relaties" */
  private XRelated[] related;
  /** ING Forms: ??? ("person", "family") */
  private List<String> types;

  public ATLGArchiver() {
    relatedArchives = Lists.newArrayList();
    relatedArchivers = Lists.newArrayList();
    placeKeywords = Lists.newArrayList();
    subjectKeywords = Lists.newArrayList();
    persons = Lists.newArrayList();
    types = Lists.newArrayList();
  }

  @Override
  public String getDisplayName() {
    return getNameEng();
  }

  public String getOrigFilename() {
    return origFilename;
  }

  public void setOrigFilename(String origFilename) {
    this.origFilename = origFilename;
  }

  public String getNameNld() {
    return nameNld;
  }

  public void setNameNld(String name) {
    nameNld = name;
  }

  @IndexAnnotations({ @IndexAnnotation(fieldName = "dynamic_sort_name", canBeEmpty = true, isFaceted = false, isSortable = true),
      @IndexAnnotation(fieldName = "dynamic_t_text", canBeEmpty = true, isFaceted = false, isSortable = false) })
  public String getNameEng() {
    return nameEng;
  }

  public void setNameEng(String name) {
    nameEng = name;
  }

  @IndexAnnotation(fieldName = "dynamic_s_begin_date", canBeEmpty = true, isFaceted = true, facetType = FacetType.DATE)
  public String getBeginDate() {
    return beginDate;
  }

  public void setBeginDate(String date) {
    beginDate = date;
  }

  @IndexAnnotation(fieldName = "dynamic_s_end_date", canBeEmpty = false, isFaceted = true, facetType = FacetType.DATE)
  public String getEndDate() {
    return endDate;
  }

  public void setEndDate(String date) {
    endDate = date;
  }

  @JsonIgnore
  @IndexAnnotations({ @IndexAnnotation(fieldName = "dynamic_sort_period", canBeEmpty = true, isFaceted = false, facetType = FacetType.PERIOD, isSortable = true),
      @IndexAnnotation(fieldName = "dynamic_s_period", canBeEmpty = true, isFaceted = true, facetType = FacetType.PERIOD, isSortable = false) })
  public String getActivePeriod() {
    return PeriodHelper.createPeriod(beginDate, endDate);
  }

  public String getPeriodDescription() {
    return periodDescription;
  }

  public void setPeriodDescription(String description) {
    periodDescription = description;
  }

  @IndexAnnotation(fieldName = "dynamic_t_text", canBeEmpty = true, isFaceted = false)
  public String getHistory() {
    return history;
  }

  public void setHistory(String history) {
    this.history = history;
  }

  public List<EntityRef> getRelatedArchives() {
    return relatedArchives;
  }

  public void setRelatedArchives(List<EntityRef> refs) {
    relatedArchives = refs;
  }

  public void addRelatedArchive(EntityRef ref) {
    relatedArchives.add(ref);
  }

  public List<EntityRef> getRelatedArchivers() {
    return relatedArchivers;
  }

  public void setRelatedArchivers(List<EntityRef> refs) {
    relatedArchivers = refs;
  }

  public void addRelatedArchiver(EntityRef ref) {
    relatedArchivers.add(ref);
  }

  @IndexAnnotation(fieldName = "dynamic_s_place", accessors = { "getDisplayName" }, canBeEmpty = true, isFaceted = true)
  public List<EntityRef> getPlaceKeywords() {
    return placeKeywords;
  }

  public void setPlaceKeywords(List<EntityRef> keywords) {
    placeKeywords = keywords;
  }

  public void addPlaceKeyword(EntityRef keyword) {
    if (keyword != null) {
      placeKeywords.add(keyword);
    }
  }

  @IndexAnnotation(fieldName = "dynamic_s_subject", accessors = { "getDisplayName" }, canBeEmpty = true, isFaceted = true)
  public List<EntityRef> getSubjectKeywords() {
    return subjectKeywords;
  }

  public void setSubjectKeywords(List<EntityRef> keywords) {
    subjectKeywords = keywords;
  }

  public void addSubjectKeyword(EntityRef keyword) {
    if (keyword != null) {
      subjectKeywords.add(keyword);
    }
  }

  @IndexAnnotation(fieldName = "dynamic_s_person", accessors = { "getDisplayName" }, canBeEmpty = true, isFaceted = true)
  public List<EntityRef> getPersons() {
    return persons;
  }

  public void setPersons(List<EntityRef> persons) {
    this.persons = persons;
  }

  public void addPerson(EntityRef person) {
    if (person != null) {
      persons.add(person);
    }
  }

  @IndexAnnotation(fieldName = "dynamic_t_text", canBeEmpty = true, isFaceted = false)
  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }

  public String getLiterature() {
    return literature;
  }

  public void setLiterature(String literature) {
    this.literature = literature;
  }

  public String getMadeBy() {
    return madeBy;
  }

  public void setMadeBy(String madeBy) {
    this.madeBy = madeBy;
  }

  public String getReminders() {
    return reminders;
  }

  public void setReminders(String reminders) {
    this.reminders = reminders;
  }

  public XRelated[] getRelated() {
    return related;
  }

  public void setRelated(XRelated[] related) {
    this.related = related;
  }

  @IndexAnnotation(fieldName = "dynamic_s_type", accessors = { "toString" }, canBeEmpty = true, isFaceted = true)
  public List<String> getTypes() {
    return types;
  }

  public void setTypes(List<String> types) {
    this.types = types;
  }

  public void addType(String type) {
    types.add(type);
  }

}
