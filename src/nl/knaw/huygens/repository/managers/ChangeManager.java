package nl.knaw.huygens.repository.managers;

import java.util.Date;

import nl.knaw.huygens.repository.model.Change;
import nl.knaw.huygens.repository.model.Document;

import org.apache.commons.lang.StringUtils;
import org.restlet.data.Form;
import org.restlet.resource.ServerResource;
import org.restlet.security.User;

public class ChangeManager {
  public enum InfoType {
    SHIB, BASIC
  }

  private static class Info {
    protected String name;
    protected String id;
  }
  private interface InfoGetter {
    Info getInfo(ServerResource resource);
  }
  private static class ShibInfoGetter implements InfoGetter {
    @Override
    public Info getInfo(ServerResource resource) {
      Info rv = new Info();
      Form headers = (Form) resource.getRequestAttributes().get("org.restlet.http.headers");
      rv.id = headers.getFirstValue("persistent-id");
      String firstName = headers.getFirstValue("shib-givenname");
      String lastName = headers.getFirstValue("shib-surname");
      if (firstName != null && !firstName.isEmpty() && lastName != null && !lastName.isEmpty()) {
        rv.name = firstName + " " + lastName;
      } else if (firstName != null && !firstName.isEmpty()) {
        rv.name = firstName;
      } else {
        String commonName = headers.getFirstValue("shib-commonname");
        if (StringUtils.isEmpty(commonName)) {
          commonName = headers.getFirstValue("shib-email");
        }
        rv.name = commonName;
      }
      return rv;
    }
  }

  public class BasicInfoGetter implements InfoGetter {
    @Override
    public Info getInfo(ServerResource resource) {
      Info rv = new Info();
      try {
        User x = resource.getClientInfo().getUser();
        rv.id = x.getIdentifier();
        rv.name = x.getFirstName() + " " + x.getLastName();
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

  public void setDocumentChange(Document doc, ServerResource res) {
    doc.setLastChange(getChange(res));
  }

  public Change getChange(ServerResource resource) {
    long stamp = new Date().getTime();
    Info info = infoGetter.getInfo(resource);
    return new Change(stamp, info.id, info.name);
  }
}
