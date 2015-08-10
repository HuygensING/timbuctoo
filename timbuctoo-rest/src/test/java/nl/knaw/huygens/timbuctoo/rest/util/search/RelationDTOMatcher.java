package nl.knaw.huygens.timbuctoo.rest.util.search;

import nl.knaw.huygens.hamcrest.CompositeMatcher;
import nl.knaw.huygens.hamcrest.PropertyEqualtityMatcher;
import nl.knaw.huygens.hamcrest.PropertyMatcher;
import nl.knaw.huygens.timbuctoo.model.RelationDTO;

import java.util.Map;

import static org.hamcrest.Matchers.endsWith;

public class RelationDTOMatcher extends CompositeMatcher<RelationDTO> {

  private RelationDTOMatcher() {
  }

  public static RelationDTOMatcher likeRelationDTO() {
    return new RelationDTOMatcher();
  }

  public RelationDTOMatcher withInternalType(String internalType) {
    this.addMatcher(new PropertyEqualtityMatcher<RelationDTO, String>("internalType", internalType) {
      @Override
      protected String getItemValue(RelationDTO item) {
        return item.getType();
      }
    });
    return this;
  }

  public RelationDTOMatcher withId(String id) {
    this.addMatcher(new PropertyEqualtityMatcher<RelationDTO, String>("id", id) {
      @Override
      protected String getItemValue(RelationDTO item) {
        return item.getId();
      }
    });
    return this;
  }

  public RelationDTOMatcher withPathThatEndsWith(String path) {
    this.addMatcher(new PropertyMatcher<RelationDTO, String>("path", endsWith(path)) {
      @Override
      protected String getItemValue(RelationDTO item) {
        return item.getPath();
      }
    });
    return this;
  }

  public RelationDTOMatcher withRelationName(String relationName) {
    this.addMatcher(new PropertyEqualtityMatcher<RelationDTO, String>("relationName", relationName) {
      @Override
      protected String getItemValue(RelationDTO item) {
        return item.getRelationName();
      }
    });
    return this;
  }

  public RelationDTOMatcher withSourceName(String sourceName) {
    this.addMatcher(new PropertyEqualtityMatcher<RelationDTO, String>("sourceName", sourceName) {
      @Override
      protected String getItemValue(RelationDTO item) {
        return item.getSourceName();
      }
    });
    return this;
  }


  public RelationDTOMatcher withSourceData(Map<String, Object> sourceData) {
    this.addMatcher(new PropertyEqualtityMatcher<RelationDTO, Map<String, ? extends Object>>("sourceData", sourceData) {
      @Override
      protected Map<String, ? extends Object> getItemValue(RelationDTO item) {
        return item.getSourceData();
      }
    });
    return this;
  }

  public RelationDTOMatcher withTargetName(String targetName) {
    this.addMatcher(new PropertyEqualtityMatcher<RelationDTO, String>("targetName", targetName) {
      @Override
      protected String getItemValue(RelationDTO item) {
        return item.getTargetName();
      }
    });
    return this;
  }


  public RelationDTOMatcher withTargetData(Map<String, Object> targetData) {
    this.addMatcher(new PropertyEqualtityMatcher<RelationDTO, Map<String, ? extends Object>>("targetData", targetData) {
      @Override
      protected Map<String, ? extends Object> getItemValue(RelationDTO item) {
        return item.getTargetData();
      }
    });
    return this;
  }
}
