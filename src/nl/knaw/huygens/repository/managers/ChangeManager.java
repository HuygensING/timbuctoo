package nl.knaw.huygens.repository.managers;

import java.util.Date;
import java.util.List;

import javax.ws.rs.core.HttpHeaders;

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
    Info getInfo(HttpHeaders req);
  }

  private static class ShibInfoGetter implements InfoGetter {
    @Override
    public Info getInfo(HttpHeaders reqHeaders) {
      Info rv = new Info();
      List<String> persistentId = reqHeaders.getRequestHeader("persistent-id");
      List<String> firstName = reqHeaders.getRequestHeader("shib-givenname");
      List<String> lastName = reqHeaders.getRequestHeader("shib-surname");
      List<String> commonName = reqHeaders.getRequestHeader("shib-commonname");
      List<String> email = reqHeaders.getRequestHeader("shib-email");
      if (persistentId.isEmpty()
          || (firstName.isEmpty() && lastName.isEmpty() && commonName.isEmpty() && email.isEmpty())) {
        throw new RuntimeException("Not enough identification information!");
      }
      rv.id = persistentId.get(0);
      if (!firstName.isEmpty()) {
        rv.name = firstName.get(0);
        if (!lastName.isEmpty()) {
          rv.name += " " + lastName.get(0);
        }
      } else {
        rv.name = commonName.isEmpty() ? email.get(0) : commonName.get(0);
      }
      return rv;
    }
  }

  public class BasicInfoGetter implements InfoGetter {
    @Override
    public Info getInfo(HttpHeaders reqHeaders) {
      Info rv = new Info();
      reqHeaders.getCookies();
      try {
        // FIXME this is really broken.
        User x = null; // resource.getClientInfo().getUser();
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

  public void setDocumentChange(Document doc, HttpHeaders reqHeaders) {
    doc.setLastChange(getChange(reqHeaders));
  }

  public Change getChange(HttpHeaders reqHeaders) {
    long stamp = new Date().getTime();
    Info info = infoGetter.getInfo(reqHeaders);
    return new Change(stamp, info.id, info.name);
  }
}
