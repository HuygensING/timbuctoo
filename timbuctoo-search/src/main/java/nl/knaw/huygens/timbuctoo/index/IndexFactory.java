package nl.knaw.huygens.timbuctoo.index;

import java.util.Map;

import nl.knaw.huygens.timbuctoo.vre.VRE;

public interface IndexFactory {

  Map<String, Index> createIndexesFor(VRE vre);

}
