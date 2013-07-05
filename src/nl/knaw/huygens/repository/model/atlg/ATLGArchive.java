package nl.knaw.huygens.repository.model.atlg;

import java.util.List;

import nl.knaw.huygens.repository.annotations.DocumentTypeName;
import nl.knaw.huygens.repository.importer.database.AtlantischeGidsImporter.XRelated;
import nl.knaw.huygens.repository.model.Archive;
import nl.knaw.huygens.repository.model.DocumentRef;

import com.google.common.collect.Lists;

@DocumentTypeName("atlgarchive")
public class ATLGArchive extends Archive {

  /** Migration: Name of source file */
  private String origFilename;
  /** ING Forms: "Ref. code country" */
  private List<String> countries;
  /** ING Forms: "Ref. code repository" */
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
  /** ING Forms: "English title" */
  private String titleEng;
  /** ING Forms: "Begin date" */
  private String beginDate;
  /** ING Forms: "End date" */
  private String endDate;
  /** ING Forms: "Period description" */
  private String periodDescription;
  /** ING Forms: "Extent" */
  private String extent;
  /** ING Forms: "Title related overhead level of description" */
  private List<String> overheadTitles;
  /** ING Forms: "Additional finding aid" */
  private String findingAid;
  /** ING Forms: "Name(s) of Creator(s)" */
  private List<String> creators;
  /** ING Forms: "Scope and content" */
  private String scope;
  /** ING Forms: "Title(s) related underlying level(s) of description" ??? */
  private String relation;
  /** ING Forms: "Other related units of description" ??? */
  private String em;
  /** ING Forms: "Keyword(s) geography" */
  private List<DocumentRef> placeKeywords;
  /** ING Forms: "Keyword(s) subject" */
  private List<DocumentRef> subjectKeywords;
  /** ING Forms: "Keyword(s) person" */
  private List<DocumentRef> persons;
  /** ING Forms: "Remarks" ??? */
  private String notes;
  /** ING Forms: "Record made by-" */
  private String madeBy;
  /** ING Forms: "Reminders" ??? */
  private String reminders;
  /** ING Forms: "Binnenkomende relaties" ??? */
  private List<XRelated> related;

  public ATLGArchive() {
    countries = Lists.newArrayList();
    overheadTitles = Lists.newArrayList();
    creators = Lists.newArrayList();
    placeKeywords = Lists.newArrayList();
    subjectKeywords = Lists.newArrayList();
    persons = Lists.newArrayList();
    related = Lists.newArrayList();
  }

  @Override
  public String getDisplayName() {
    return getTitleNld();
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

  public List<String> getOverheadTitles() {
    return overheadTitles;
  }

  public void setOverheadTitles(List<String> titles) {
    overheadTitles = titles;
  }

  public void addOverheadTitle(String title) {
    overheadTitles.add(title);
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

  public String getRelation() {
    return relation;
  }

  public void setRelation(String relation) {
    this.relation = relation;
  }

  public String getEm() {
    return em;
  }

  public void setEm(String em) {
    this.em = em;
  }

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

  public List<XRelated> getRelated() {
    return related;
  }

  public void setRelated(List<XRelated> related) {
    this.related = related;
  }

  public void addRelated(XRelated item) {
    related.add(item);
  }

}
