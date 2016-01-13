package nl.knaw.huygens.timbuctoo.server;


import io.dropwizard.Configuration;
import org.hibernate.validator.constraints.NotEmpty;

/*
 *TODO: add good default environment for Timbuctoo
 *  - example logins
 *  - example users
 *  - example authorization
 *  - example database
 */
public class TimbuctooConfiguration extends Configuration {
  @NotEmpty
  private String loginsFilePath;
  @NotEmpty
  private String usersFilePath;

  public String getLoginsFilePath() {
    return loginsFilePath;
  }

  public String getUsersFilePath() {
    return usersFilePath;
  }
}
