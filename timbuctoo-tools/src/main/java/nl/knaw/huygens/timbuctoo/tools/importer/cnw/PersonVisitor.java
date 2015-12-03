package nl.knaw.huygens.timbuctoo.tools.importer.cnw;

/*
 * #%L
 * Timbuctoo tools
 * =======
 * Copyright (C) 2012 - 2015 Huygens ING
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;

import nl.knaw.huygens.tei.DelegatingVisitor;
import nl.knaw.huygens.tei.Element;
import nl.knaw.huygens.tei.ElementHandler;
import nl.knaw.huygens.tei.Traversal;
import nl.knaw.huygens.tei.handlers.DefaultElementHandler;
import nl.knaw.huygens.timbuctoo.model.Person;
import nl.knaw.huygens.timbuctoo.model.Person.Gender;
import nl.knaw.huygens.timbuctoo.model.cnw.AltName;
import nl.knaw.huygens.timbuctoo.model.cnw.CNWLink;
import nl.knaw.huygens.timbuctoo.model.cnw.CNWPerson;
import nl.knaw.huygens.timbuctoo.model.util.Datable;
import nl.knaw.huygens.timbuctoo.model.util.PersonName;
import nl.knaw.huygens.timbuctoo.model.util.PersonNameComponent.Type;
import nl.knaw.huygens.timbuctoo.tools.importer.CaptureHandler;

public class PersonVisitor extends DelegatingVisitor<PersonContext> {
  private static final String NONE = "(leeg)";
	private static final Logger LOG = LoggerFactory.getLogger(PersonVisitor.class);

	private Map<String, Map<String, String>> listMaps;

	public PersonVisitor(PersonContext personContext, Map<String, Map<String, String>> listMaps) {
		super(personContext);
		this.listMaps = listMaps;
		addElementHandler(new PersonHandler(), "persoon");

		addElementHandler(new CNWPersNameHandler(), "persname");
		addElementHandler(new NameHandler(), "name");

		addElementHandler(new PersNamePartHandler(Type.ROLE_NAME), "preposition");
		addElementHandler(new PersNamePartHandler(Type.FORENAME), "firstname");
		addElementHandler(new PersNamePartHandler(Type.NAME_LINK), "intraposition");
		addElementHandler(new PersNamePartHandler(Type.SURNAME), "familyname");
		addElementHandler(new PersNamePartHandler(Type.GEN_NAME), "postposition");

		addElementHandler(new KoppelnaamHandler(), "koppelnaam");

		//		addElementHandler(new AltnameHandler(), "altname");
		addElementHandler(new NamesHandler(), "names");
		addElementHandler(new NametypeHandler(), "nametype");

		addElementHandler(new GenderHandler(), "sex");

		addElementHandler(new BirthHandler(), "birth");
		addElementHandler(new DeathHandler(), "death");

		addElementHandler(new DateHandler(), "date");///in birth, death
		addElementHandler(new YearHandler(), "year");
		addElementHandler(new MonthHandler(), "month");
		addElementHandler(new DayHandler(), "day");

		addElementHandler(new ValuesHandler(), "values");
		addElementHandler(new ValHandler(), "val");

		addElementHandler(new PlaceHandler(), "place");
		addElementHandler(new CountryHandler(), "country");

		//			addElementHandler(new WoonplaatsHandler(), "woonplaats");
		//			addElementHandler(new EducationHandler(), "education");
		//			addElementHandler(new OccupationHandler(), "occupation");

		addElementHandler(new NetworkHandler(), "network");
		addElementHandler(new DomainHandler(), "domain"); // in network

		addElementHandler(new DomeinHandler(), "domein"); // in  domains
		addElementHandler(new SubDomainHandler(), "subdomain"); // in network

		addElementHandler(new RelatieHandler(), "relatie");
		addElementHandler(new ReltypeHandler(), "reltype");
		addElementHandler(new KoppelnameHandler(), "koppelname");

		addElementHandler(new CharacteristicHandler(), "karakteristic");

		addElementHandler(new DomainsHandler(), "domains");

		//		addElementHandler(new ActivityHandler(), "act");
		//			addElementHandler(new PoliticsHandler(), "politics");
		//			addElementHandler(new OpmPoliticsHandler(), "opm_politics");
		//			addElementHandler(new LevensbeschouwingHandler(), "levensbeschouwing");
		//			addElementHandler(new LiteratuurHandler(), "literatuur");
		addElementHandler(new BiodesurlHandler(), "biodesurl");
		addElementHandler(new DbnlUrlHandler(), "dbnl_url");
		//			addElementHandler(new BntlUrlHandler(), "bntl_url");
		//			addElementHandler(new DbngUrlHandler(), "dbng_url");
		//			addElementHandler(new CenUrlAfzHandler(), "cen_url_afz");
		//			addElementHandler(new CenUrlOntvHandler(), "cen_url_ontv");

		addElementHandler(new LinkHandler(), "link");
		addElementHandler(new UrlHandler(), "url");
		addElementHandler(new IdentifierHandler(), "identifier");
		addElementHandler(new SoortHandler(), "soort");
		addElementHandler(new OpmerkingenHandler(), "opmerkingen"); // in link, persoon

		addElementHandler(new NotitiesHandler(), "notities");
		addElementHandler(new AantekeningenHandler(), "aantekeningen");
		addElementHandler(new MembershipHandler(), "lidmaatschap");
		addElementHandler(new PeriodicalHandler(), "periodiek");

	}

	private class PersonHandler implements ElementHandler<PersonContext> {

    @Override
		public Traversal enterElement(Element element, PersonContext context) {
			context.person = new CNWPerson();
			context.person.setId(context.pid);
			return Traversal.NEXT;
		}

		@Override
		public Traversal leaveElement(Element element, PersonContext context) {
      addLeegWhenEmpty(context.person.getNetworkDomains());
      addLeegWhenEmpty(context.person.getDomains());
      addLeegWhenEmpty(context.person.getSubDomains());
      addLeegWhenEmpty(context.person.getCombinedDomains());
      addLeegWhenEmpty(context.person.getCharacteristics());
      addLeegWhenEmpty(context.person.getMemberships());
      addLeegWhenEmpty(context.person.getPeriodicals());
			LOG.info("person={}", context.person);
			return Traversal.NEXT;
		}

    private void addLeegWhenEmpty(Set<String> list) {
      if (list.isEmpty()) {
        list.add(NONE);
      }
    }
	}

	private class CNWPersNameHandler implements ElementHandler<PersonContext> {
		@Override
		public Traversal enterElement(Element element, PersonContext context) {
			context.personName = new PersonName();
			return Traversal.NEXT;
		}

		@Override
		public Traversal leaveElement(Element element, PersonContext context) {
			context.person.addName(context.personName);
			return Traversal.NEXT;
		}
	}

	private class GenderHandler extends CaptureHandler<PersonContext> {
		@Override
		public void handleContent(Element element, PersonContext context, String text) {
			if (text.equals("male")) {
				context.person.setGender(Person.Gender.MALE);
			} else if (text.equals("female")) {
				context.person.setGender(Person.Gender.FEMALE);
				//			} else if (text.equals("not applicable")) {
				//				context.person.setGender(Person.Gender.NOT_APPLICABLE);
			} else {
				context.person.setGender(Person.Gender.UNKNOWN);
			}
		}
	}

	private class BirthHandler extends DefaultElementHandler<PersonContext> {
		@Override
		public Traversal enterElement(Element element, PersonContext context) {
			return Traversal.NEXT;
		}

		@Override
		public Traversal leaveElement(Element element, PersonContext context) {
			if (StringUtils.isNotEmpty(context.year)) {
				Datable birthDate = new Datable(context.year);
				context.person.setBirthDate(birthDate);
				context.person.setCnwBirthYear(birthDate.getFromYear());
			}
			context.year = "";

			return super.leaveElement(element, context);
		}

	}

	private class DeathHandler extends DefaultElementHandler<PersonContext> {
		@Override
		public Traversal enterElement(Element element, PersonContext context) {
			return Traversal.NEXT;
		}

		@Override
		public Traversal leaveElement(Element element, PersonContext context) {
			if (StringUtils.isNotEmpty(context.year)) {
				Datable cnwDeathYear = new Datable(context.year);
				context.person.setDeathDate(cnwDeathYear);
				context.person.setCnwDeathYear(cnwDeathYear.getToYear());
			}
			context.year = "";
			return super.leaveElement(element, context);
		}
	}

	private class OpmerkingenHandler extends CaptureHandler<PersonContext> {
		@Override
		public void handleContent(Element element, PersonContext context, String text) {
			if (element.getParent().hasName("persoon")) {
				context.person.setOpmerkingen(text);
			} else if (element.getParent().hasName("link")) {
				context.link.setUrl(text);
			}
		}
	}

	private class NotitiesHandler extends CaptureHandler<PersonContext> {
		@Override
		public void handleContent(Element element, PersonContext context, String text) {
			context.person.setNotities(text);
		}
	}

	private class AantekeningenHandler extends CaptureHandler<PersonContext> {
		@Override
		public void handleContent(Element element, PersonContext context, String text) {
			context.person.setAantekeningen(text);
		}
	}

	private class MembershipHandler extends CaptureHandler<PersonContext> {
		@Override
		public void handleContent(Element element, PersonContext context, String text) {
			String denormalized = denormalized(text, "memberships");
			if (denormalized != null) {
				context.person.getMemberships().add(denormalized);
			}
		}
	}

	private class PeriodicalHandler extends CaptureHandler<PersonContext> {
		@Override
		public void handleContent(Element element, PersonContext context, String text) {
			String denormalized = denormalized(text, "periodics");
			if (StringUtils.isNotEmpty(denormalized)) {
				context.person.getPeriodicals().add(denormalized);
			}
		}
	}

	private class BiodesurlHandler extends CaptureHandler<PersonContext> {
		@Override
		public void handleContent(Element element, PersonContext context, String text) {
			context.person.setBiodesurl(text);
		}
	}

	//		private class BntlUrlHandler extends CaptureHandler<ImportContext> {
	//			@Override
	//			public void handleContent(Element element, ImportContext context, String text) {
	//				context.person.setBntlUrl(text);
	//			}
	//		}
	//
	//		private class CenUrlAfzHandler extends CaptureHandler<ImportContext> {
	//			@Override
	//			public void handleContent(Element element, ImportContext context, String text) {
	//				context.person.setCenUrlAfz(text);
	//			}
	//		}
	//
	//		private class CenUrlOntvHandler extends CaptureHandler<ImportContext> {
	//			@Override
	//			public void handleContent(Element element, ImportContext context, String text) {
	//				context.person.setCenUrlOntv(text);
	//			}
	//		}

	private class CharacteristicHandler extends CaptureHandler<PersonContext> {
		@Override
		public void handleContent(Element element, PersonContext context, String text) {
			context.person.getCharacteristics().add(denormalized(text, "characteristic"));
		}
	}

	private class DateHandler extends CaptureHandler<PersonContext> {
		@Override
		public void handleContent(Element element, PersonContext context, String text) {
			//			context.person.setDate(text);
		}
	}

	private class DayHandler extends CaptureHandler<PersonContext> {
		@Override
		public void handleContent(Element element, PersonContext context, String text) {
			//			context.person.setDay(text);
		}
	}

	//		private class DbngUrlHandler extends CaptureHandler<ImportContext> {
	//			@Override
	//			public void handleContent(Element element, ImportContext context, String text) {
	//				context.person.setDbngUrl(text);
	//			}
	//		}

	private class DbnlUrlHandler extends CaptureHandler<PersonContext> {
		@Override
		public void handleContent(Element element, PersonContext context, String text) {
			context.person.setDbnlUrl(text);
		}
	}

	private static final String KEYPREFIX = "netwerkverwey_";

	private String denormalized(String text, String string) {
		return listMaps.get(KEYPREFIX + string).get(text);
	}

	private class DomainHandler extends CaptureHandler<PersonContext> {
		@Override
		public void handleContent(Element element, PersonContext context, String text) {
			if (element.getParent().hasName("network")) {
				context.person.getNetworkDomains().add(denormalized(text, "networks"));
			}
		}

	}

	private class DomeinHandler extends CaptureHandler<PersonContext> {
		@Override
		public void handleContent(Element element, PersonContext context, String text) {
			String denormalized = denormalized(text, "domains");
			context.person.getDomains().add(denormalized);
			context.person.getCombinedDomains().add(denormalized + "/*");
		}
	}

	Map<String, String> subdomainExtension = ImmutableMap.<String, String> builder()//
			.put("Visuele kunsten", "Beeldende kunsten/Visuele kunsten") //
			.put("Beeldhouwkunst", "Beeldende kunsten/Beeldhouwkunst") //
			.put("Toegepaste kunst", "Beeldende kunsten/Toegepaste kunst") //
			.put("Architectuur", "Beeldende kunsten/Architectuur") //
			.put("Letteren", "Letteren en Taal/Letteren") //
			.put("Taal", "Letteren en Taal/Taal") //
			.put("Familie", "Maatschappij/Familie") //
			.put("Biologie en Microbiologie", "Natuurwetenschappen/Biologie en Microbiologie") //
			.put("Botanie", "Natuurwetenschappen/Botanie") //
			.put("Astronomie", "Natuurwetenschappen/Astronomie") //
			.put("Scheikunde", "Natuurwetenschappen/Scheikunde") //
			.put("Wiskunde", "Natuurwetenschappen/Wiskunde") //
			.put("Natuurkunde", "Natuurwetenschappen/Natuurkunde") //
			.put("Industrie", "Economie en Financiën/Industrie") //
			.put("Midden- en kleinbedrijf", "Economie en Financiën/Midden- en kleinbedrijf") //
			.put("Financiën", "Economie en Financiën/Financiën") //
			.build();

	private class SubDomainHandler extends CaptureHandler<PersonContext> {
		@Override
		public void handleContent(Element element, PersonContext context, String text) {
			String denormalized = denormalized(text, "subdomains");
			if (denormalized != null) {
				context.person.getSubDomains().add(denormalized);
				if (subdomainExtension.containsKey(denormalized)) {
					context.person.getCombinedDomains().add(subdomainExtension.get(denormalized));
				}
			}
		}
	}

	private class DomainsHandler extends CaptureHandler<PersonContext> {
		@Override
		public void handleContent(Element element, PersonContext context, String text) {
			//			context.person.setDomains(text);
		}
	}

	//		private class EducationHandler extends CaptureHandler<ImportContext> {
	//			@Override
	//			public void handleContent(Element element, ImportContext context, String text) {
	//				context.person.setEducation(text);
	//			}
	//		}

	private class IdentifierHandler extends CaptureHandler<PersonContext> {
		@Override
		public void handleContent(Element element, PersonContext context, String text) {
			context.link.setIdentifier(text);
		}
	}

	private class KoppelnaamHandler extends CaptureHandler<PersonContext> {
		@Override
		public void handleContent(Element element, PersonContext context, String text) {
			context.person.setKoppelnaam(text);
		}
	}

	//		private class LevensbeschouwingHandler extends CaptureHandler<ImportContext> {
	//			@Override
	//			public void handleContent(Element element, ImportContext context, String text) {
	//				context.person.setLevensbeschouwing(text);
	//			}
	//		}

	private class LinkHandler extends DefaultElementHandler<PersonContext> {
		@Override
		public Traversal enterElement(Element element, PersonContext context) {
			context.link = new CNWLink();
			return super.enterElement(element, context);
		}

		@Override
		public Traversal leaveElement(Element element, PersonContext context) {
			context.person.getVerwijzingen().add(context.link);
			return super.leaveElement(element, context);
		}

	}

	//		private class LiteratuurHandler extends CaptureHandler<ImportContext> {
	//			@Override
	//			public void handleContent(Element element, ImportContext context, String text) {
	//				context.person.setLiteratuur(text);
	//			}
	//		}

	private class MonthHandler extends CaptureHandler<PersonContext> {
		@Override
		public void handleContent(Element element, PersonContext context, String text) {
			//			context.person.setMonth(text);
		}
	}

	private class NameHandler extends CaptureHandler<PersonContext> {
		@Override
		public void handleContent(Element element, PersonContext context, String text) {
			if (element.getParent().hasName("names")) {
				context.currentAltName.setDisplayName(text);
			} else {
				context.person.setName(text);
			}
		}
	}

	private class NamesHandler extends DefaultElementHandler<PersonContext> {
		@Override
		public Traversal enterElement(Element element, PersonContext context) {
			context.currentAltName = new AltName();
			return super.enterElement(element, context);
		}

		@Override
		public Traversal leaveElement(Element element, PersonContext context) {
			context.person.getAltNames().add(context.currentAltName);
			return super.leaveElement(element, context);
		}
	}

	private class NametypeHandler extends CaptureHandler<PersonContext> {

		@Override
		public void handleContent(Element element, PersonContext context, String text) {
			context.currentAltName.setNametype(denormalized(text, "nametype"));
		}
	}

	private class NetworkHandler extends CaptureHandler<PersonContext> {
		@Override
		public void handleContent(Element element, PersonContext context, String text) {
			//			context.person.setNetwork(text);
		}
	}

	//		private class OccupationHandler extends CaptureHandler<ImportContext> {
	//			@Override
	//			public void handleContent(Element element, ImportContext context, String text) {
	//				context.person.setOccupation(text);
	//			}
	//		}
	//
	//		private class OpmPoliticsHandler extends CaptureHandler<ImportContext> {
	//			@Override
	//			public void handleContent(Element element, ImportContext context, String text) {
	//				context.person.setOpmPolitics(text);
	//			}
	//		}

	//		private class PersnameHandler extends CaptureHandler<ImportContext> {
	//			@Override
	//			public void handleContent(Element element, ImportContext context, String text) {
	//				context.personName = new PersonName();
	//			}
	//		}

	private class PlaceHandler extends CaptureHandler<PersonContext> {
		@Override
		public void handleContent(Element element, PersonContext context, String text) {
			context.placeString = text;
		}
	}

	private class CountryHandler extends CaptureHandler<PersonContext> {
		@Override
		public void handleContent(Element element, PersonContext context, String text) {
			context.country = denormalized(text, "country");
		}
	}

	//		private class PoliticsHandler extends CaptureHandler<ImportContext> {
	//			@Override
	//			public void handleContent(Element element, ImportContext context, String text) {
	//				context.person.setPolitics(text);
	//			}
	//		}

	private class PersNamePartHandler extends CaptureHandler<PersonContext> {
		private Type type;

		public PersNamePartHandler(Type namePartType) {
			super();
			type = namePartType;
		}

		@Override
		public void handleContent(Element element, PersonContext context, String text) {
			context.personName.addNameComponent(type, text);
		}
	}

	// relations
	private class RelatieHandler extends DefaultElementHandler<PersonContext> {
		@Override
		public Traversal enterElement(Element element, PersonContext context) {
			//			context.currentRelation = new CNWRelation();
			//			context.currentRelation.setSourceType("person");
			//			context.currentRelation.setSourceId(context.pid);
			return super.enterElement(element, context);
		}

		@Override
		public Traversal leaveElement(Element element, PersonContext context) {
			context.person.addRelative(context.currentRelationType + " van: " + context.currentRelativeName);
			return super.leaveElement(element, context);
		}
	}

	//	Map<String, String> reltypeMap = ImmutableMap.<String, String> builder()//
	//			.put("broer", "isSiblingOf")//
	//			.put("child", "isParentOf")//
	//			.put("echtgenoot", "isSpouseOf")//
	//			.put("gezel", "isSpouseOf")//
	//			.put("grand", "isGrandparentOf")//
	//			.put("klein", "isGrandparentOf")//
	//			.put("parent", "isParentOf")//
	//			.put("verloofd", "isSpouseOf")//
	//			.build();
	Map<String, String> reltypeMapM = ImmutableMap.<String, String> builder()//
			.put("broer", "(schoon-)broer")//
			.put("child", "zoon")//
			.put("echtgenoot", "echtgenoot")//
			.put("gezel", "levensgezel")//
			.put("grand", "grootvader")//
			.put("klein", "kleinzoon")//
			.put("parent", "vader")//
			.put("verloofd", "verloofde")//
			.build();
	Map<String, String> reltypeMapF = ImmutableMap.<String, String> builder()//
			.put("broer", "zus")//
			.put("child", "dochter")//
			.put("echtgenoot", "echtgenote")//
			.put("gezel", "levelsgezellin")//
			.put("grand", "grootmoeder")//
			.put("klein", "kleindochter")//
			.put("parent", "moeder")//
			.put("verloofd", "verloofde")//
			.build();
	Map<String, String> reltypeMapU = ImmutableMap.<String, String> builder()//
			.put("broer", "broer/zus")//
			.put("child", "kind")//
			.put("echtgenoot", "echtgenoot/echtgenote")//
			.put("gezel", "levensgezel/levensgezellin")//
			.put("grand", "grootouder")//
			.put("klein", "kleinkind")//
			.put("parent", "ouder")//
			.put("verloofd", "verloofde")//
			.build();

	Map<Gender, Map<String, String>> relTypeMaps = ImmutableMap.of(Gender.MALE, reltypeMapM, Gender.FEMALE, reltypeMapF, Gender.UNKNOWN, reltypeMapU);

	private class ReltypeHandler extends CaptureHandler<PersonContext> {
		@Override
		public void handleContent(Element element, PersonContext context, String text) {
			context.currentRelationType = relTypeMaps.get(context.person.getGender()).get(text);
			//			context.currentRelation.setTypeName(text);
			//			context.currentRelation.setTypeType("relationtype");
			//			context.currentRelation.setTypeId(reltypeMap.get(text));
		}
	}

	private class KoppelnameHandler extends CaptureHandler<PersonContext> {
		@Override
		public void handleContent(Element element, PersonContext context, String text) {
			context.currentRelativeName = text;
			//			context.currentRelation.setTargetType("person");
			//			context.currentRelation.setTargetId(text);
		}
	}

	private class SoortHandler extends CaptureHandler<PersonContext> {
		@Override
		public void handleContent(Element element, PersonContext context, String text) {
			context.link.setSoort(text);
		}
	}

	private class UrlHandler extends CaptureHandler<PersonContext> {
		@Override
		public void handleContent(Element element, PersonContext context, String text) {
			context.link.setUrl(text);
		}
	}

	Map<String, String> translations = ImmutableMap.of("onzeker", "Onzeker", "unknown", "Onbekend");

	private class ValHandler extends CaptureHandler<PersonContext> {
		@Override
		public void handleContent(Element element, PersonContext context, String text) {
			String greatgrandparent = element.getParent().getParent().getParent().getName();
			if (!translations.containsKey(text)) {
				LOG.error("unknown value {} for <val>", text);
			}
			if ("birth".equals(greatgrandparent)) {
				context.person.setBirthDateQualifier(translations.get(text));
			} else if ("death".equals(greatgrandparent)) {
				context.person.setDeathDateQualifier(translations.get(text));
			} else {
				LOG.warn("unhandled <val> in {}/{}/{}", greatgrandparent, element.getParent().getParent().getName(), element.getParent().getName());
			}

		}
	}

	private class ValuesHandler extends CaptureHandler<PersonContext> {
		@Override
		public void handleContent(Element element, PersonContext context, String text) {
			//			context.person.setValues(text);
		}
	}

	//		private class WoonplaatsHandler extends CaptureHandler<ImportContext> {
	//			@Override
	//			public void handleContent(Element element, ImportContext context, String text) {
	//				context.person.setWoonplaats(text);
	//			}
	//		}

	private class YearHandler extends CaptureHandler<PersonContext> {
		@Override
		public void handleContent(Element element, PersonContext context, String text) {
			context.year = text;
		}
	}

}
