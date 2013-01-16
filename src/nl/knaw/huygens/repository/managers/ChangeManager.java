package nl.knaw.huygens.repository.managers;

import java.util.Date;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.model.User;
import nl.knaw.huygens.repository.model.util.Change;

public class ChangeManager {
  public enum InfoType {
    SHIB, BASIC
  }

  private static class Info {
    protected String name;
    protected String id;
  }

  private interface InfoGetter {
    Info getInfo(HttpServletRequest req);
  }

  private static class ShibInfoGetter implements InfoGetter {
    @Override
    public Info getInfo(HttpServletRequest reqHeaders) {
      Info rv = new Info();
      Enumeration<String> persistentId = reqHeaders.getHeaders("persistent-id");
      Enumeration<String> firstName = reqHeaders.getHeaders("shib-givenname");
      Enumeration<String> lastName = reqHeaders.getHeaders("shib-surname");
      Enumeration<String> commonName = reqHeaders.getHeaders("shib-commonname");
      Enumeration<String> email = reqHeaders.getHeaders("shib-email");
      if (!persistentId.hasMoreElements()
          || (!firstName.hasMoreElements() && !lastName.hasMoreElements() && !commonName.hasMoreElements() && !email.hasMoreElements())) {
        throw new RuntimeException("Not enough identification information!");
      }
      rv.id = persistentId.nextElement();
      if (firstName.hasMoreElements()) {
        rv.name = firstName.nextElement();
        if (lastName.hasMoreElements()) {
          rv.name += " " + lastName.nextElement();
        }
      } else {
        rv.name = commonName.hasMoreElements() ? commonName.nextElement() : email.nextElement();
      }
      return rv;
    }
  }

  public static class BasicInfoGetter implements InfoGetter {
    @Override
    public Info getInfo(HttpServletRequest req) {
      Info rv = new Info();
      try {
        Object userObj = req.getAttribute("repo-user");
        if (!(userObj instanceof User)) {
          throw new Exception("Invalid user object for change.");
        }
        User x = (User) userObj; // resource.getClientInfo().getUser();
        rv.id = x.getId();
        rv.name = x.firstName + " " + x.lastName;
      } catch (Exception ex) {
        // Some exception, silently catch (yay evilness)
      }
      return rv;
    }
  }

  private InfoGetter infoGetter;

  public ChangeManager(InfoType t) {
    switch (t) {
      case BASIC:
        infoGetter = new BasicInfoGetter();
        break;
      case SHIB:
        infoGetter = new ShibInfoGetter();
        break;
    }
  }

  public void setDocumentChange(Document doc, HttpServletRequest req) {
    doc.setLastChange(getChange(req));
  }

  public Change getChange(HttpServletRequest req) {
    long stamp = new Date().getTime();
    Info info = infoGetter.getInfo(req);
    return new Change(stamp, info.id, info.name);
  }
}
