package nl.knaw.huygens.repository.model.atlg;

import java.util.List;

import nl.knaw.huygens.repository.annotations.DocumentTypeName;
import nl.knaw.huygens.repository.model.DocumentRef;
import nl.knaw.huygens.repository.model.Legislation;

import com.google.common.collect.Lists;

@DocumentTypeName("atlglegislation")
public class ATLGLegislation extends Legislation {

  private String origFilename;
  private String reference;
  private String pages;
  private List<DocumentRef> placeKeywords;
  private List<DocumentRef> groupKeywords;
  private List<DocumentRef> otherKeywords;
  private List<DocumentRef> persons;
  private String originalArchivalSource;
  private String linkArchivalDBase;
  private String remarks;
  private String scan;
  private String partsToScan;
  private String madeBy;

  public ATLGLegislation() {
    placeKeywords = Lists.newArrayList();
    groupKeywords = Lists.newArrayList();
    otherKeywords = Lists.newArrayList();
    persons = Lists.newArrayList();
  }

  public String getOrigFilename() {
    return origFilename;
  }

  public void setOrigFilename(String origFilename) {
    this.origFilename = origFilename;
  }

  public String getReference() {
    return reference;
  }

  public void setReference(String reference) {
    this.reference = reference;
  }

  public String getPages() {
    return pages;
  }

  public void setPages(String pages) {
    this.pages = pages;
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

  public List<DocumentRef> getGroupKeywords() {
    return groupKeywords;
  }

  public void setGroupKeywords(List<DocumentRef> keywords) {
    groupKeywords = keywords;
  }

  public void addGroupKeyword(DocumentRef keyword) {
    if (keyword != null) {
      groupKeywords.add(keyword);
    }
  }

  public List<DocumentRef> getOtherKeywords() {
    return otherKeywords;
  }

  public void setOtherKeywords(List<DocumentRef> keywords) {
    otherKeywords = keywords;
  }

  public void addOtherKeyword(DocumentRef keyword) {
    if (keyword != null) {
      otherKeywords.add(keyword);
    }
  }

  public List<DocumentRef> getPersons() {
    return persons;
  }

  public void setPersons(List<DocumentRef> persons) {
    this.persons = persons;
  }

  public void addPerson(DocumentRef personRef) {
    if (personRef != null) {
      persons.add(personRef);
    }
  }

  public String getOriginalArchivalSource() {
    return originalArchivalSource;
  }

  public void setOriginalArchivalSource(String originalArchivalSource) {
    this.originalArchivalSource = originalArchivalSource;
  }

  public String getLinkArchivalDBase() {
    return linkArchivalDBase;
  }

  public void setLinkArchivalDBase(String linkArchivalDBase) {
    this.linkArchivalDBase = linkArchivalDBase;
  }

  public String getRemarks() {
    return remarks;
  }

  public void setRemarks(String remarks) {
    this.remarks = remarks;
  }

  public String getScan() {
    return scan;
  }

  public void setScan(String scan) {
    this.scan = scan;
  }

  public String getPartsToScan() {
    return partsToScan;
  }

  public void setPartsToScan(String partsToScan) {
    this.partsToScan = partsToScan;
  }

  public String getMadeBy() {
    return madeBy;
  }

  public void setMadeBy(String madeBy) {
    this.madeBy = madeBy;
  }

}
