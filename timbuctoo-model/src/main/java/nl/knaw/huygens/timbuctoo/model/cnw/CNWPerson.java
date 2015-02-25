package nl.knaw.huygens.timbuctoo.model.cnw;

import java.text.MessageFormat;
import java.util.List;

import nl.knaw.huygens.timbuctoo.facet.IndexAnnotation;
import nl.knaw.huygens.timbuctoo.model.Person;
import nl.knaw.huygens.timbuctoo.model.util.Datable;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

public class CNWPerson extends Person {

	private String name = "";
	private String koppelnaam = "";
	private List<String> networkDomains = Lists.newArrayList();

	private List<String> domains = Lists.newArrayList();
	private List<String> subdomains = Lists.newArrayList();
	private List<String> characteristics = Lists.newArrayList();

	private List<String> memberships = Lists.newArrayList(); // Lidmaatschappen : Om te zetten naar facet met sorteerbare lijst
	private String biodesurl = "";//Bioport url, Link, mogelijkheid tot doorklikken
	private String dbnlUrl = "";//Link, mogelijkheid tot doorklikken
	private List<CNWLink> verwijzingen = Lists.newArrayList();
	private String notities = ""; //Bronvermeldingen, tekstveld; Interface geen facet, wel zichtbaar in pop up (Onderscheid Korte of lange presentatie)
	private String opmerkingen = ""; //Tekstveld, met vast onderdeel (Afgesloten: XXXX-XX-XX);	Interface geen facet, wel zichtbaar in pop up
	private String aantekeningen = ""; // Kladblok: Niet zichtbaar voor gebruiker, wel bewaren
	private List<AltName> altNames = Lists.newArrayList();
	private List<String> relatives = Lists.newArrayList();
	private String birthdateQualifier = "";
	private String deathdateQualifier = "";

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

	@IndexAnnotation(fieldName = "dynamic_s_birthdatequalifier", canBeEmpty = true, isFaceted = false)
	public String getBirthdateQualifier() {
		return birthdateQualifier;
	}

	@IndexAnnotation(fieldName = "dynamic_s_deathdatequalifier", canBeEmpty = true, isFaceted = false)
	public String getDeathdateQualifier() {
		return deathdateQualifier;
	}

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

	public String getDbnlUrl() {
		return dbnlUrl;
	}

	public void setDbnlUrl(String dbnlUrl) {
		this.dbnlUrl = dbnlUrl;
	}

	public void setDomains(List<String> domains) {
		this.domains = domains;
	}

	@IndexAnnotation(fieldName = "dynamic_s_domain", canBeEmpty = false, isFaceted = true)
	public List<String> getDomains() {
		return domains;
	}

	public void setSubDomains(List<String> subdomains) {
		this.subdomains = subdomains;
	}

	@IndexAnnotation(fieldName = "dynamic_s_subdomain", canBeEmpty = true, isFaceted = true)
	public List<String> getSubDomains() {
		return subdomains;
	}

	public void setCharacteristics(List<String> characteristicList) {
		this.characteristics = characteristicList;
	}

	@IndexAnnotation(fieldName = "dynamic_s_characteristic", canBeEmpty = false, isFaceted = true)
	public List<String> getCharacteristics() {
		return characteristics;
	}

	public void setKoppelnaam(String koppelnaam) {
		this.koppelnaam = koppelnaam;
	}

	@IndexAnnotation(fieldName = "dynamic_s_koppelnaam", canBeEmpty = false, isFaceted = true)
	public String getKoppelnaam() {
		return koppelnaam;
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

	public void setNetworkDomains(List<String> networkDomains) {
		this.networkDomains = networkDomains;
	}

	@IndexAnnotation(fieldName = "dynamic_s_networkdomain", canBeEmpty = false, isFaceted = true)
	public List<String> getNetworkDomains() {
		return networkDomains;
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

	@IndexAnnotation(fieldName = "dynamic_s_altname", accessors = { "getName" }, canBeEmpty = true, isFaceted = true)
	public List<AltName> getAltNames() {
		return altNames;
	}

	public void setAltNames(List<AltName> altNames) {
		this.altNames = altNames;
	}

	@IndexAnnotation(fieldName = "dynamic_s_relatives", canBeEmpty = true, isFaceted = false)
	public List<String> getRelatives() {
		return relatives;
	}

	@IndexAnnotation(fieldName = "dynamic_s_shortdescription", canBeEmpty = false, isFaceted = false)
	public String getShortDescription() {
		String charString = characteristics.isEmpty() ? "" : ", " + Joiner.on(", ").join(characteristics);
		return MessageFormat.format("{0} ({1}-{2}){3}",//
				StringUtils.defaultIfBlank(getName(), getKoppelnaam()),//
				extractYear(getBirthDate()), //
				extractYear(getDeathDate()), //
				charString);
	}

	private String extractYear(Datable deathDate) {
		String deathYear = deathDate == null ? "?" : deathDate.toString();
		return deathYear;
	}

	public void addRelative(String relative) {
		relatives.add(relative);
	}

	public void setBirthDateQualifier(String qualifier) {
		birthdateQualifier = qualifier;
	}

	public void setDeathDateQualifier(String qualifier) {
		deathdateQualifier = qualifier;
	}

}
