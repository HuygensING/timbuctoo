package nl.knaw.huygens.repository.model.atlg;

import java.util.List;

import nl.knaw.huygens.repository.annotations.DocumentTypeName;
import nl.knaw.huygens.repository.importer.database.AtlantischeGidsImporter.XRelated;
import nl.knaw.huygens.repository.model.Archiver;
import nl.knaw.huygens.repository.model.DocumentRef;

import com.google.common.collect.Lists;

@DocumentTypeName("atlgarchiver")
public class ATLGArchiver extends Archiver {

  /** Migration: Name of source file */
  private String origFilename;
  /** ING Forms: "Name" */
  private String nameNld;
  /** ING Forms: "English name" */
  public String nameEng;
  /** ING Forms: "Begin date" */
  private String beginDate;
  /** ING Forms: "End date" */
  private String endDate;
  /** ING Forms: "Period description" */
  private String periodDescription;
  /** ING Forms: "History/functions/occupations/activities" */
  private String history;
  /** ING Forms: "Title(s) related archive(s)" */
  private List<DocumentRef> relatedArchives;
  /** ING Forms: "Title(s) related creator(s)" */
  private List<DocumentRef> relatedArchivers;
  /** ING Forms: "Keyword(s) geography" */
  private List<DocumentRef> placeKeywords;
  /** ING Forms: "Keyword(s) subject" */
  private List<DocumentRef> subjectKeywords;
  /** ING Forms: "Keyword(s) person" */
  private List<DocumentRef> persons;
  /** ING Forms: "Remarks" */
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
    return getNameNld();
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

  public String getNameEng() {
    return nameEng;
  }

  public void setNameEng(String name) {
    nameEng = name;
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
