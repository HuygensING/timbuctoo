package nl.knaw.huygens.repository.model.atlg;

import java.util.List;

import nl.knaw.huygens.repository.annotations.DocumentTypeName;
import nl.knaw.huygens.repository.annotations.IndexAnnotation;
import nl.knaw.huygens.repository.model.Archive;
import nl.knaw.huygens.repository.model.DocumentRef;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Lists;

@DocumentTypeName("atlgarchive")
public class ATLGArchive extends Archive {

  /** Migration: Name of source file */
  private String origFilename;
  /** ING Forms: "Ref. code country"; refcode facet */
  private List<String> countries;
  /** ING Forms: "Ref. code repository"; refcode facet */
  private String refCodeArchive;
  /** ING Forms: "Reference code" */
  private String refCode;
  /** ING Forms: "Code or indication of sub-fonds" */
  private String subCode;
  /** ING Forms: "Indication of series, Nos." */
  private String series;
  /** ING Forms: "Item, No." */
  private String itemNo;
  /** ING Forms: "Title" */
  private String titleNld;
  /** ING Forms: "English title"; text searchable */
  private String titleEng;
  /** ING Forms: "Begin date"; date facet */
  private String beginDate;
  /** ING Forms: "End date"; date facet */
  private String endDate;
  /** ING Forms: "Period description" */
  private String periodDescription;
  /** ING Forms: "Extent" */
  private String extent;
  /** ING Forms: "Additional finding aid" */
  private String findingAid;
  /** ING Forms: "Name(s) of Creator(s)" */
  private List<String> creators;
  /** ING Forms: "Scope and content" */
  private String scope;
  /** ING Forms: "Keyword(s) geography"; place facet */
  private List<DocumentRef> placeKeywords;
  /** ING Forms: "Keyword(s) subject"; subject facet */
  private List<DocumentRef> subjectKeywords;
  /** ING Forms: "Keyword(s) person"; person facet */
  private List<DocumentRef> persons;
  /** ING Forms: "Remarks"; text searchable */
  private String notes;
  /** ING Forms: "Record made by-" */
  private String madeBy;
  /** ING Forms: "Reminders" */
  private String reminders;
  /** ING Forms: "Title related overhead level of description" */
  private List<DocumentRef> overheadArchives;
  /** ING Forms: "Title(s) related underlying level(s) of description" */
  private List<DocumentRef> underlyingArchives;
  /** ING Forms: "Other related units of description" */
  private List<DocumentRef> relatedUnitArchives;

  public ATLGArchive() {
    countries = Lists.newArrayList();
    creators = Lists.newArrayList();
    placeKeywords = Lists.newArrayList();
    subjectKeywords = Lists.newArrayList();
    persons = Lists.newArrayList();
    overheadArchives = Lists.newArrayList();
    underlyingArchives = Lists.newArrayList();
    relatedUnitArchives = Lists.newArrayList();
  }

  @Override
  public String getDisplayName() {
    return getTitleEng();
  }

  public String getOrigFilename() {
    return origFilename;
  }

  public void setOrigFilename(String origFilename) {
    this.origFilename = origFilename;
  }

  public List<String> getCountries() {
    return countries;
  }

  public void setCountries(List<String> countries) {
    this.countries = countries;
  }

  public void addCountry(String country) {
    countries.add(country);
  }

  public String getRefCodeArchive() {
    return refCodeArchive;
  }

  public void setRefCodeArchive(String refCodeArchive) {
    this.refCodeArchive = refCodeArchive;
  }

  public String getRefCode() {
    return refCode;
  }

  public void setRefCode(String refCode) {
    this.refCode = refCode;
  }

  public String getSubCode() {
    return subCode;
  }

  public void setSubCode(String subCode) {
    this.subCode = subCode;
  }

  @JsonIgnore
  @IndexAnnotation(fieldName = "facet_s_refcode", canBeEmpty = true, isFaceted = true)
  public String getIndexedRefCode() {
    StringBuilder builder = new StringBuilder();
    for (String country : getCountries()) {
      appendTo(builder, country);
    }
    appendTo(builder, getRefCodeArchive());
    return builder.toString();
  }

  private void appendTo(StringBuilder builder, String text) {
    if (text != null && text.length() != 0) {
      if (builder.length() != 0) {
        builder.append(' ');
      }
      builder.append(text);
    }
  }

  public String getSeries() {
    return series;
  }

  public void setSeries(String series) {
    this.series = series;
  }

  public String getItemNo() {
    return itemNo;
  }

  public void setItemNo(String itemNo) {
    this.itemNo = itemNo;
  }

  public String getTitleNld() {
    return titleNld;
  }

  public void setTitleNld(String title) {
    titleNld = title;
  }

  @IndexAnnotation(fieldName = "facet_t_text", canBeEmpty = true, isFaceted = true)
  public String getTitleEng() {
    return titleEng;
  }

  public void setTitleEng(String title) {
    titleEng = title;
  }

  public String getBeginDate() {
    return beginDate;
  }

  public void setBeginDate(String date) {
    beginDate = date;
  }

  public String getEndDate() {
    return endDate;
  }

  public void setEndDate(String date) {
    endDate = date;
  }

  public String getPeriodDescription() {
    return periodDescription;
  }

  public void setPeriodDescription(String description) {
    periodDescription = description;
  }

  public String getExtent() {
    return extent;
  }

  public void setExtent(String extent) {
    this.extent = extent;
  }

  public String getFindingAid() {
    return findingAid;
  }

  public void setFindingAid(String findingAid) {
    this.findingAid = findingAid;
  }

  public List<String> getCreators() {
    return creators;
  }

  public void setCreators(List<String> creators) {
    this.creators = creators;
  }

  public void addCreator(String creator) {
    creators.add(creator);
  }

  public String getScope() {
    return scope;
  }

  public void setScope(String scope) {
    this.scope = scope;
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

  @IndexAnnotation(fieldName = "facet_t_text", canBeEmpty = true, isFaceted = true)
  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
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

  public List<DocumentRef> getOverheadArchives() {
    return overheadArchives;
  }

  public void setOverheadArchives(List<DocumentRef> archives) {
    overheadArchives = archives;
  }

  public void addOverheadArchive(DocumentRef archive) {
    overheadArchives.add(archive);
  }

  public List<DocumentRef> getUnderlyingArchives() {
    return underlyingArchives;
  }

  public void setUnderlyingArchives(List<DocumentRef> archives) {
    underlyingArchives = archives;
  }

  public void addUnderlyingArchive(DocumentRef archive) {
    underlyingArchives.add(archive);
  }

  public List<DocumentRef> getRelatedUnitArchives() {
    return relatedUnitArchives;
  }

  public void setRelatedUnitArchives(List<DocumentRef> archives) {
    relatedUnitArchives = archives;
  }

  public void addRelatedUnitArchive(DocumentRef archive) {
    relatedUnitArchives.add(archive);
  }

}
