package nl.knaw.huygens.timbuctoo.database.tinkerpop;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TinkerPopConfig {
  @JsonProperty
  private HaConfig haconfig;

  public HaConfig getHaconfig() {
    return haconfig;
  }

  public boolean hasHaconfig() {
    return haconfig != null;
  }

  public class HaConfig {
    @JsonProperty
    private String uniqueIp;
    @JsonProperty
    private String initialHosts;
    @JsonProperty
    private String pullInterval;
    @JsonProperty
    private String pushFactor;
    @JsonProperty
    private String allowInitCluster;

    public String getServerId() {
      int result = 0;
      String[] parts = uniqueIp.split("\\.");
      for (String part : parts) {
        result = result << 8;
        result |= Integer.parseInt(part);
      }

      return result + "";
    }

    public String getIp() {
      return uniqueIp;
    }

    public String allowInitCluster() {
      return allowInitCluster;
    }

    public String getInitialHosts() {
      return initialHosts;
    }

    public String getPullInterval() {
      return pullInterval;
    }

    public String getPushFactor() {
      return pushFactor;
    }
  }
}

