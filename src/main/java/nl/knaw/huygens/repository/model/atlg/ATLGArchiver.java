package nl.knaw.huygens.repository.model.atlg;

import java.util.List;

import nl.knaw.huygens.repository.annotations.IndexAnnotation;
import nl.knaw.huygens.repository.annotations.IndexAnnotations;
import nl.knaw.huygens.repository.model.Archiver;
import nl.knaw.huygens.repository.model.DocumentRef;
import nl.knaw.huygens.repository.model.util.PeriodHelper;
import nl.knaw.huygens.solr.FacetType;

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
  private List<DocumentRef> relatedArchives;
  /** ING Forms: "Title(s) related creator(s)" */
  private List<DocumentRef> relatedArchivers;
  /** ING Forms: "Keyword(s) geography"; place facet */
  private List<DocumentRef> placeKeywords;
  /** ING Forms: "Keyword(s) subject"; subject facet */
  private List<DocumentRef> subjectKeywords;
  /** ING Forms: "Keyword(s) person"; person facet */
  private List<DocumentRef> persons;
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

  @IndexAnnotations({ @IndexAnnotation(fieldName = "facet_sort_name", canBeEmpty = true, isFaceted = false, isSortable = true),
      @IndexAnnotation(fieldName = "facet_t_text", canBeEmpty = true, isFaceted = false, isSortable = false) })
  public String getNameEng() {
    return nameEng;
  }

  public void setNameEng(String name) {
    nameEng = name;
  }

  @IndexAnnotation(fieldName = "facet_s_begin_date", canBeEmpty = true, isFaceted = true, facetType = FacetType.DATE)
  public String getBeginDate() {
    return beginDate;
  }

  public void setBeginDate(String date) {
    beginDate = date;
  }

  @IndexAnnotation(fieldName = "facet_s_end_date", canBeEmpty = false, isFaceted = true, facetType = FacetType.DATE)
  public String getEndDate() {
    return endDate;
  }

  public void setEndDate(String date) {
    endDate = date;
  }

  @JsonIgnore
  @IndexAnnotations({ @IndexAnnotation(fieldName = "facet_sort_period", canBeEmpty = true, isFaceted = false, facetType = FacetType.PERIOD, isSortable = true),
      @IndexAnnotation(fieldName = "facet_s_period", canBeEmpty = true, isFaceted = true, facetType = FacetType.PERIOD, isSortable = false) })
  public String getActivePeriod() {
    return PeriodHelper.createPeriod(beginDate, endDate);
  }

  public String getPeriodDescription() {
    return periodDescription;
  }

  public void setPeriodDescription(String description) {
    periodDescription = description;
  }

  @IndexAnnotation(fieldName = "facet_t_text", canBeEmpty = true, isFaceted = false)
  public String getHistory() {
    return history;
  }

  public void setHistory(String history) {
    this.history = history;
  }

  public List<DocumentRef> getRelatedArchives() {
    return relatedArchives;
  }

  public void setRelatedArchives(List<DocumentRef> refs) {
    relatedArchives = refs;
  }

  public void addRelatedArchive(DocumentRef ref) {
    relatedArchives.add(ref);
  }

  public List<DocumentRef> getRelatedArchivers() {
    return relatedArchivers;
  }

  public void setRelatedArchivers(List<DocumentRef> refs) {
    relatedArchivers = refs;
  }

  public void addRelatedArchiver(DocumentRef ref) {
    relatedArchivers.add(ref);
  }

  @IndexAnnotation(fieldName = "facet_s_place", accessors = { "getDisplayName" }, canBeEmpty = true, isFaceted = true)
  public List<DocumentRef> getPlaceKeywords() {
    return placeKeywords;
  }

  public void setPlaceKeywords(List<DocumentRef> keywords) {
    placeKeywords = keywords;
  }

  public void addPlaceKeyword(DocumentRef keyword) {
    if (keyword != null) {
      placeKeywords.add(keyword);
    }
  }

  @IndexAnnotation(fieldName = "facet_s_subject", accessors = { "getDisplayName" }, canBeEmpty = true, isFaceted = true)
  public List<DocumentRef> getSubjectKeywords() {
    return subjectKeywords;
  }

  public void setSubjectKeywords(List<DocumentRef> keywords) {
    subjectKeywords = keywords;
  }

  public void addSubjectKeyword(DocumentRef keyword) {
    if (keyword != null) {
      subjectKeywords.add(keyword);
    }
  }

  @IndexAnnotation(fieldName = "facet_s_person", accessors = { "getDisplayName" }, canBeEmpty = true, isFaceted = true)
  public List<DocumentRef> getPersons() {
    return persons;
  }

  public void setPersons(List<DocumentRef> persons) {
    this.persons = persons;
  }

  public void addPerson(DocumentRef person) {
    if (person != null) {
      persons.add(person);
    }
  }

  @IndexAnnotation(fieldName = "facet_t_text", canBeEmpty = true, isFaceted = false)
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

  @IndexAnnotation(fieldName = "facet_s_type", accessors = { "toString" }, canBeEmpty = true, isFaceted = true)
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
