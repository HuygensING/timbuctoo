package nl.knaw.huygens.timbuctoo.tools.conversion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Maps;

public class RelationPropertyVerfierTest extends PropertyVerifierTest {
  private static final String NEW_ID = "newId";
  private static final String OLD_ID = "oldId";
  private Map<String, String> oldIdNewIdMap;

  @Before
  @Override
  public void setup() {
    oldIdNewIdMap = Maps.newHashMap();
    oldIdNewIdMap.put(OLD_ID, NEW_ID);

    instance = new RelationPropertyVerifier(oldIdNewIdMap);
  }

  @Test
  public void checkRetrievesTheMappedNewIdFormTheOldIdNewIdMapForTheFieldSourceId() {
    // action
    instance.check("sourceId", OLD_ID, NEW_ID);

    // verify
    assertThat(instance.hasInconsistentProperties(), is(false));
    assertThat(instance.getMismatches(), hasSize(0));
  }

  @Test
  public void checkRetrievesTheMappedNewIdFormTheOldIdNewIdMapForTheFieldTargetId() {
    // action
    instance.check("targetId", OLD_ID, NEW_ID);

    // verify
    assertThat(instance.hasInconsistentProperties(), is(false));
    assertThat(instance.getMismatches(), hasSize(0));
  }
}
