package nl.knaw.huygens.timbuctoo.v5.datastores.rssource;

import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.ChangeType;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.CursorQuad;
import nl.knaw.huygens.timbuctoo.v5.util.RdfConstants;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ChangeListBuilder {
  private final ChangesQuadGenerator changesQuadGenerator;

  public ChangeListBuilder() {
    this.changesQuadGenerator = new ChangesQuadGenerator();
  }

  public List<String> retrieveChangeFileNames(Stream<Integer> versions) {
    return versions.filter(x -> x != Integer.MAX_VALUE)
                   .map(version -> "changes" + version + ".nqud")
                   .collect(Collectors.toList());
  }

  public Stream<String> retrieveChanges(Stream<CursorQuad> quads) {
    return quads.map(quad -> {
      Optional<String> dataType = quad.getValuetype();
      if (dataType == null || !dataType.isPresent()) {
        if (quad.getChangeType() == ChangeType.ASSERTED) {
          return changesQuadGenerator.onRelation(quad.getSubject(), quad.getPredicate(),
              quad.getObject(), quad.getGraph().orElse(null));
        } else if (quad.getChangeType() == ChangeType.RETRACTED) {
          return changesQuadGenerator.delRelation(quad.getSubject(), quad.getPredicate(),
              quad.getObject(), quad.getGraph().orElse(null));
        }
      } else {
        Optional<String> language = quad.getLanguage();
        if (language != null && language.isPresent() && dataType.get().equals(RdfConstants.LANGSTRING)) {
          if (quad.getChangeType() == ChangeType.ASSERTED) {
            return changesQuadGenerator.onLanguageTaggedString(
              quad.getSubject(),
              quad.getPredicate(),
              quad.getObject(),
              language.get(),
              quad.getGraph().orElse(null)
            );
          } else if (quad.getChangeType() == ChangeType.RETRACTED) {
            return changesQuadGenerator.delLanguageTaggedString(
              quad.getSubject(),
              quad.getPredicate(),
              quad.getObject(),
              language.get(),
              quad.getGraph().orElse(null)
            );
          }
        } else {
          if (quad.getChangeType() == ChangeType.ASSERTED) {
            return changesQuadGenerator.onValue(
              quad.getSubject(),
              quad.getPredicate(),
              quad.getObject(),
              dataType.get(),
              quad.getGraph().orElse(null)
            );
          } else if (quad.getChangeType() == ChangeType.RETRACTED) {
            return changesQuadGenerator.delValue(
              quad.getSubject(),
              quad.getPredicate(),
              quad.getObject(),
              dataType.get(),
              quad.getGraph().orElse(null)
            );
          }
        }
      }
      return ""; // return empty string for unchanged quads
    });
  }
}
