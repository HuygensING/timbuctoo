package nl.knaw.huygens.repository.jms;

import java.util.ArrayList;
import java.util.List;

import org.apache.activemq.broker.BrokerPlugin;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.security.AuthenticationUser;
import org.apache.activemq.security.SimpleAuthenticationPlugin;

public class Factory {

  public static void main(String[] args) throws Exception {
    BrokerService broker = new BrokerService();
    broker.setBrokerName("myBroker");
    broker.setDataDirectory("data/");
    SimpleAuthenticationPlugin authentication = new SimpleAuthenticationPlugin();
    List<AuthenticationUser> users = new ArrayList<AuthenticationUser>();
    users.add(new AuthenticationUser("admin", "password", "admins,publishers,consumers"));
    users.add(new AuthenticationUser("publisher", "password", "publishers,consumers"));
    users.add(new AuthenticationUser("consumer", "password", "consumers"));
    users.add(new AuthenticationUser("guest", "password", "guests"));
    authentication.setUsers(users);
    broker.setPlugins(new BrokerPlugin[] { authentication });
    broker.addConnector("tcp://localhost:61616");
    broker.start();
    System.out.println();
    System.out.println("Press any key to stop the broker");
    System.out.println();
    System.in.read();
  }

}