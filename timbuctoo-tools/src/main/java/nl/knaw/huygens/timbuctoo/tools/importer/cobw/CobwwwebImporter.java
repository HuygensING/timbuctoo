package nl.knaw.huygens.timbuctoo.tools.importer.cobw;

/*
 * #%L
 * Timbuctoo tools
 * =======
 * Copyright (C) 2012 - 2014 Huygens ING
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

import java.util.List;
import java.util.Set;

import nl.knaw.huygens.tei.DelegatingVisitor;
import nl.knaw.huygens.tei.Document;
import nl.knaw.huygens.tei.Element;
import nl.knaw.huygens.tei.ElementHandler;
import nl.knaw.huygens.tei.Traversal;
import nl.knaw.huygens.tei.XmlContext;
import nl.knaw.huygens.tei.handlers.DefaultElementHandler;
import nl.knaw.huygens.timbuctoo.model.Person;
import nl.knaw.huygens.timbuctoo.model.cobw.COBWPerson;
import nl.knaw.huygens.timbuctoo.model.util.Datable;
import nl.knaw.huygens.timbuctoo.model.util.Link;

import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Importer for Norwegian data.
 */
public class CobwwwebImporter /* extends DefaultImporter */ {

  // Base URL for import
  private static final String URL = "https://www2.hf.uio.no/tjenester/bibliografi/Robinsonades";

  public static void main(String[] args) throws Exception {
    CobwwwebImporter importer = new CobwwwebImporter();
    importer.importAll();
  }

  private ObjectMapper mapper;

  public CobwwwebImporter() {
    mapper = new ObjectMapper();
  }

  public void importAll() throws Exception {
    String xml = getPersonsResource();
    List<String> personIds = parsePersonsResource(xml);
    System.out.printf("Retrieved %d person id's:%n", personIds.size());
    for (String id : personIds) {
      xml = getPersonResource(id);
      COBWPerson person = parsePersonResource(xml, id);
      System.out.println(mapper.writeValueAsString(person));
    }
  }
 
  private String getPersonsResource() throws Exception {
    ClientResource resource = new ClientResource(URL + "/persons");
    Representation representation = resource.get(MediaType.APPLICATION_XML);
    return representation.getText();
  }

  private String getPersonResource(String id) throws Exception {
    ClientResource resource = new ClientResource(URL + "/person/" + id + "/");
    Representation representation = resource.get(MediaType.APPLICATION_XML);
    return representation.getText();
  }

  private List<String> parsePersonsResource(String xml) {
    Document document = Document.createFromXml(xml);
    PersonsContext context = new PersonsContext();
    PersonsVisitor visitor = new PersonsVisitor(context);
    document.accept(visitor);
    return context.ids;
  }

  private COBWPerson parsePersonResource(String xml, String id) {
    Document document = Document.createFromXml(xml);
    PersonContext context = new PersonContext(id);
    PersonVisitor visitor = new PersonVisitor(context);
    document.accept(visitor);
    return context.person;
  }

  // ---------------------------------------------------------------------------

  private static class PersonsContext extends XmlContext {
    public final List<String> ids = Lists.newArrayList();
    public void addId(String id) {
      // somewhat inefficent, but we want to preserve ordering
      if (ids.contains(id)) {
        System.err.printf("## Duplicate entry %s%n", id);
      } else {
        ids.add(id);
      }
    }
  }

  private static class PersonsVisitor extends DelegatingVisitor<PersonsContext> {
    public PersonsVisitor(PersonsContext context) {
      super(context);
      addElementHandler(new PersonIdHandler(), "personId");
    }
  }

  private static class PersonIdHandler extends DefaultElementHandler<PersonsContext> {
    @Override
    public Traversal enterElement(Element element, PersonsContext context) {
      context.openLayer();
      return Traversal.NEXT;
    }
    @Override
    public Traversal leaveElement(Element element, PersonsContext context) {
      String id = context.closeLayer().trim();
      context.addId(id);
      return Traversal.NEXT;
    }
  }

  // ---------------------------------------------------------------------------

  private static class PersonContext extends XmlContext {
    public PersonContext(String id) {
      this.id = id;
    }
    public String id;
    public COBWPerson person = new COBWPerson();
    public void error(String format, Object... args) {
      System.err.printf("## [%s] %s%n", id, String.format(format, args));
    }
  }

  private static class PersonVisitor extends DelegatingVisitor<PersonContext> {
    public PersonVisitor(PersonContext context) {
      super(context);
      setDefaultElementHandler(new IgnoreHandler());
      addElementHandler(new IdHandler(), "personId");
      addElementHandler(new TypeHandler(), "type");
      addElementHandler(new GenderHandler(), "gender");
      addElementHandler(new DateOfBirthHandler(), "dateOfBirth");
      addElementHandler(new DateOfDeathHandler(), "dateOfDeath");
      addElementHandler(new NameHandler(), "name");
      addElementHandler(new LanguageHandler(), "language");
      addElementHandler(new LinkHandler(), "Reference");
      addElementHandler(new NotesHandler(), "notes");
    }
  }

  private static class IgnoreHandler extends DefaultElementHandler<PersonContext> {
    private Set<String> ignoredNames = Sets.newHashSet("person", "names", "languages");
    @Override
    public Traversal enterElement(Element element, PersonContext context) {
      String name = element.getName();
      if (!ignoredNames.contains(name))  {
        context.error("Unexpected element: %s", name);
      }
      return Traversal.NEXT;
    }
  }

  private static abstract class CaptureHandler implements ElementHandler<PersonContext> {
    @Override
    public Traversal enterElement(Element element, PersonContext context) {
      context.openLayer();
      return Traversal.NEXT;
    }
    @Override
    public Traversal leaveElement(Element element, PersonContext context) {
      String text = context.closeLayer().trim();
      if (!text.isEmpty()) {
        handleContent(text, context);
      }
      return Traversal.NEXT;
    }
    public abstract void handleContent(String text, PersonContext context);
  }

  private static class IdHandler extends CaptureHandler {
    @Override
    public void handleContent(String text, PersonContext context) {
      if (!context.id.equals(text)) {
        context.error("ID mismatch: %s", text);
      }
    }
  }

  private static class TypeHandler extends CaptureHandler {
    @Override
    public void handleContent(String text, PersonContext context) {
      if (text.equalsIgnoreCase(Person.Type.ARCHETYPE)) {
        context.person.addType(Person.Type.ARCHETYPE);
      } else if (text.equalsIgnoreCase(Person.Type.AUTHOR)) {
        context.person.addType(Person.Type.AUTHOR);
      } else if (text.equalsIgnoreCase(Person.Type.PSEUDONYM)) {
        context.person.addType(Person.Type.PSEUDONYM);
      } else {
        context.error("Unknown type: %s", text);
      }
    }
  }

  private static class GenderHandler extends CaptureHandler {
    @Override
    public void handleContent(String text, PersonContext context) {
      if (text.equals("1")) {
        context.person.setGender(Person.Gender.MALE);
      } else if (text.equals("2")) {
        context.person.setGender(Person.Gender.FEMALE);
      } else if (text.equals("9")) {
        context.person.setGender(Person.Gender.NOT_APPLICABLE);
      } else {
        context.person.setGender(Person.Gender.UNKNOWN);
      }
    }
  }

  private static class DateOfBirthHandler extends CaptureHandler {
    @Override
    public void handleContent(String text, PersonContext context) {
      Datable datable = new Datable(text);
      context.person.setBirthDate(datable);
    }
  }

  private static class DateOfDeathHandler extends CaptureHandler {
    @Override
    public void handleContent(String text, PersonContext context) {
      Datable datable = new Datable(text);
      context.person.setDeathDate(datable);
    }
  }

  private static class NameHandler extends CaptureHandler {
    @Override
    public void handleContent(String text, PersonContext context) {
      context.person.addTempName(text);
    }
  }

  private static class LinkHandler extends CaptureHandler {
    @Override
    public void handleContent(String text, PersonContext context) {
      Link link = new Link(text, "NEWW");
      context.person.addLink(link);
    }
  }

  private static class NotesHandler extends CaptureHandler {
    @Override
    public void handleContent(String text, PersonContext context) {
      context.person.setNotes(text);
    }
  }

  private static class LanguageHandler implements ElementHandler<PersonContext> {
    @Override
    public Traversal enterElement(Element element, PersonContext context) {
      context.openLayer();
      return Traversal.NEXT;
    }
    @Override
    public Traversal leaveElement(Element element, PersonContext context) {
      String text = context.closeLayer().trim();
      if (!text.isEmpty()) {
        if (!element.hasParentWithName("languages")) {
          context.error("Unexpected value in element 'language': %s", text);
        } else if (context.person.getNationalities().contains(text)) {
        	  context.error("Duplicate value in element 'languages/language': %s", text);
        } else {
          context.person.addNationality(text);
        }
      }
      return Traversal.NEXT;
    }
  }

}
