package nl.knaw.huygens.timbuctoo.model.cnw;

import java.util.List;

import nl.knaw.huygens.timbuctoo.facet.IndexAnnotation;
import nl.knaw.huygens.timbuctoo.model.Person;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.google.common.collect.Lists;

public class CNWPerson extends Person {

	private String name = "";
	private String koppelnaam = "";
	private List<String> networkDomains = Lists.newArrayList();
	private List<String> characteristics = Lists.newArrayList();
	private List<String> subdomains = Lists.newArrayList();
	private List<String> domains = Lists.newArrayList();
	private List<String> memberships = Lists.newArrayList(); // Lidmaatschappen : Om te zetten naar facet met sorteerbare lijst
	private String biodesurl = "";//Bioport url, Link, mogelijkheid tot doorklikken
	private String dbnlUrl = "";//Link, mogelijkheid tot doorklikken
	private List<CNWLink> verwijzingen = Lists.newArrayList();
	private String notities = ""; //Bronvermeldingen, tekstveld; Interface geen facet, wel zichtbaar in pop up (Onderscheid Korte of lange presentatie)
	private String opmerkingen = ""; //Tekstveld, met vast onderdeel (Afgesloten: XXXX-XX-XX);	Interface geen facet, wel zichtbaar in pop up
	private String aantekeningen = ""; // KLadblok: Niet zichtbaar voor gebruiker, wel bewaren
	private List<AltName> altNames = Lists.newArrayList();

	//	private String nametype = "";
	//	private String woonplaats = "";
	//	private String education = "";
	//	private String occupation = "";
	//	private String politics = "";
	//	private String opmPolitics = "";
	//	private String levensbeschouwing = "";
	//	private String literatuur = "";
	//	private String bntlUrl = "";//Link, mogelijkheid tot doorklikken
	//	private String dbngUrl = "";
	//	private String cenUrlAfz = "";
	//	private String cenUrlOntv = "";

	@IndexAnnotation(fieldName = "dynamic_s_membership", canBeEmpty = true, isFaceted = true)
	public List<String> getMemberships() {
		return memberships;
	}

	public void setActivities(List<String> activities) {
		this.memberships = activities;
	}

	public String getBiodesurl() {
		return biodesurl;
	}

	public void setBiodesurl(String biodesurl) {
		this.biodesurl = biodesurl;
	}

	@IndexAnnotation(fieldName = "dynamic_s_characteristic", canBeEmpty = true, isFaceted = true)
	public List<String> getCharacteristics() {
		return characteristics;
	}

	public void setCharacteristics(List<String> characteristicList) {
		this.characteristics = characteristicList;
	}

	public String getDbnlUrl() {
		return dbnlUrl;
	}

	public void setDbnlUrl(String dbnlUrl) {
		this.dbnlUrl = dbnlUrl;
	}

	public List<String> getDomains() {
		return domains;
	}

	public void setDomains(List<String> domains) {
		this.domains = domains;
	}

	public String getKoppelnaam() {
		return koppelnaam;
	}

	public void setKoppelnaam(String koppelnaam) {
		this.koppelnaam = koppelnaam;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAantekeningen() {
		return aantekeningen;
	}

	public void setAantekeningen(String aantekeningen) {
		this.aantekeningen = aantekeningen;
	}

	public String getNotities() {
		return notities;
	}

	public void setNotities(String notities) {
		this.notities = notities;
	}

	public String getOpmerkingen() {
		return opmerkingen;
	}

	public void setOpmerkingen(String opmerkingen) {
		this.opmerkingen = opmerkingen;
	}

	public List<CNWLink> getVerwijzingen() {
		return verwijzingen;
	}

	public void setVerwijzingen(List<CNWLink> verwijzingen) {
		this.verwijzingen = verwijzingen;
	}

	public List<String> getNetworkDomains() {
		return networkDomains;
	}

	public void setNetworkDomains(List<String> networkDomains) {
		this.networkDomains = networkDomains;
	}

	//	public String getDbngUrl() {
	//		return dbngUrl;
	//	}
	//
	//	public void setDbngUrl(String dbngUrl) {
	//		this.dbngUrl = dbngUrl;
	//	}
	//	public String getBntlUrl() {
	//		return bntlUrl;
	//	}
	//
	//	public void setBntlUrl(String bntlUrl) {
	//		this.bntlUrl = bntlUrl;
	//	}
	//
	//	public String getCenUrlAfz() {
	//		return cenUrlAfz;
	//	}
	//
	//	public void setCenUrlAfz(String cenUrlAfz) {
	//		this.cenUrlAfz = cenUrlAfz;
	//	}
	//
	//	public String getCenUrlOntv() {
	//		return cenUrlOntv;
	//	}
	//
	//	public void setCenUrlOntv(String cenUrlOntv) {
	//		this.cenUrlOntv = cenUrlOntv;
	//	}
	//	public String getNametype() {
	//		return nametype;
	//	}
	//
	//	public void setNametype(String nametype) {
	//		this.nametype = nametype;
	//	}
	//
	//	public String getOccupation() {
	//		return occupation;
	//	}
	//
	//	public void setOccupation(String occupation) {
	//		this.occupation = occupation;
	//	}
	//
	//	public String getOpmPolitics() {
	//		return opmPolitics;
	//	}
	//
	//	public void setOpmPolitics(String opmPolitics) {
	//		this.opmPolitics = opmPolitics;
	//	}
	//
	//	public String getPolitics() {
	//		return politics;
	//	}
	//
	//	public void setPolitics(String politics) {
	//		this.politics = politics;
	//	}
	//
	//	public String getWoonplaats() {
	//		return woonplaats;
	//	}
	//
	//	public void setWoonplaats(String woonplaats) {
	//		this.woonplaats = woonplaats;
	//	}
	//	public String getLevensbeschouwing() {
	//		return levensbeschouwing;
	//	}
	//
	//	public void setLevensbeschouwing(String levensbeschouwing) {
	//		this.levensbeschouwing = levensbeschouwing;
	//	}
	//
	//	public String getLiteratuur() {
	//		return literatuur;
	//	}
	//
	//	public void setLiteratuur(String literatuur) {
	//		this.literatuur = literatuur;
	//	}
	//	public String getEducation() {
	//		return education;
	//	}
	//
	//	public void setEducation(String education) {
	//		this.education = education;
	//	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}

	public List<AltName> getAltNames() {
		return altNames;
	}

	public void setAltNames(List<AltName> altNames) {
		this.altNames = altNames;
	}

	public List<String> getSubDomains() {
		return subdomains;
	}
}
