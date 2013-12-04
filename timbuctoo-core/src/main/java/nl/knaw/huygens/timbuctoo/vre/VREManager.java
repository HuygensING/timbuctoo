package nl.knaw.huygens.timbuctoo.vre;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.inject.Singleton;

@Singleton
public class VREManager {
  private static final String DEFAULT_VRE = "BaseVRE";
  private Map<String, VRE> vreMap;

  public VREManager() throws IOException {
    vreMap = Maps.newHashMap();
    List<VRE> vreList = ImmutableList.<VRE> of(new BaseVRE(), new DutchCaribbeanVRE(), new TestVRE());

    for (VRE vre : vreList) {
      vreMap.put(vre.getName(), vre);
    }

  }

  /**
   * Get's the VRE that belongs to {@code id}.
   * @param id the id of the VRE to get.
   * @return the VRE if one is found, null if the VRE cannot be found.
   */
  public VRE getVREById(String id) {
    return vreMap.get(id);
  }

  /**
   * Gets the VRE that is defined as the default.
   * @return
   */
  public VRE getDefaultVRE() {
    return this.getVREById(DEFAULT_VRE);
  }

  public Set<String> getAvailableVREs() {
    return vreMap.keySet();

  }

}
