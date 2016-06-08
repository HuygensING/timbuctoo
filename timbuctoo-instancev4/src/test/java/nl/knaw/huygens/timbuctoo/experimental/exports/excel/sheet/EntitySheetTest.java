package nl.knaw.huygens.timbuctoo.experimental.exports.excel.sheet;

import com.google.common.collect.Lists;
import nl.knaw.huygens.hamcrest.CompositeMatcher;
import nl.knaw.huygens.hamcrest.PropertyEqualityMatcher;
import nl.knaw.huygens.timbuctoo.model.vre.Collection;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import nl.knaw.huygens.timbuctoo.server.HuygensIng;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.junit.Test;

import java.io.IOException;

import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsn;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnA;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnO;
import static nl.knaw.huygens.timbuctoo.util.TestGraphBuilder.newGraph;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class EntitySheetTest {

  private static final int TIM_ID_COL = 0;
  private static final int RELATION_COL = 1;
  private static final int NAMES_1_KEY_COL = 2;
  private static final int NAMES_1_VALUE_COL = 3;
  private static final int NAMES_2_KEY_COL = 4;
  private static final int NAMES_2_VALUE_COL = 5;
  private static final int TEMP_NAME_COL = 6;
  private final Vres mappings = HuygensIng.mappings;
  private final Collection collection = mappings.getCollection("wwpersons").get();
  private final String[] relationTypes = null;
  private final String acceptedProp = "wwrelation_accepted";

  private static EntityRowMatcher likeRow() {
    return new EntityRowMatcher();
  }

  @Test
  public void renderToWorkbookRendersAnExcelSheetWithOnlyHeaderRows() {
    final GraphWrapper graphWrapper = mock(GraphWrapper.class);
    final SXSSFWorkbook workbook = new SXSSFWorkbook();
    final Graph graph = newGraph().build();

    given(graphWrapper.getGraph()).willReturn(graph);

    EntitySheet instance = new EntitySheet(collection, workbook, graphWrapper, mappings, relationTypes, acceptedProp);

    instance.renderToWorkbook(graph.traversal().V().toSet());

    SXSSFSheet sheet = workbook.getSheet("wwpersons");
    assertThat(Lists.newArrayList(sheet.rowIterator()), contains(
      // header row 1
      likeRow().withId("tim_id"),
      // header row 2
      likeRow().withId("uuid"),
      // empty header row 3
      likeRow()));
  }

  @Test
  public void renderToWorkbookRendersTheVerticesAsExcelCellsToTheSheet() throws IOException {
    final GraphWrapper graphWrapper = mock(GraphWrapper.class);
    final SXSSFWorkbook workbook = new SXSSFWorkbook();
    final Graph graph = newGraph()
      .withVertex("v1", v -> {
        v.withProperty("tim_id", "123");
        v.withProperty("types", jsnA(jsn("person"), jsn("wwperson")).toString());
        v.withProperty("wwperson_tempName", "temp name");
        v.withProperty("wwperson_names", jsnO("list", jsnA(
          jsnO("components", jsnA(
            jsnO("type", jsn("FORENAME"), "value", jsn("foreName 1"))
          )),
          jsnO("components", jsnA(
            jsnO("type", jsn("FORENAME"), "value", jsn("foreName 2"))
          ))
        )).toString());
      })
      .withVertex("v2", v -> {
        v.withProperty("tim_id", "234");
        v.withProperty("types", jsnA(jsn("person"), jsn("wwperson")).toString());
        v.withProperty("wwperson_tempName", "tmp 2");
        v.withOutgoingRelation("isRelatedTo", "v1", r -> {
          r.withIsLatest(true);
          r.withAccepted("wwrelation", true);
          return r;
        });
      })
      .build();
    given(graphWrapper.getGraph()).willReturn(graph);
    EntitySheet instance = new EntitySheet(collection, workbook, graphWrapper, mappings, relationTypes, acceptedProp);

    instance.renderToWorkbook(graph.traversal().V().toSet());
    SXSSFSheet sheet = workbook.getSheet("wwpersons");

    assertThat(Lists.newArrayList(sheet.rowIterator()), containsInAnyOrder(
      // header row 1
      likeRow().withId("tim_id").withRelation("isRelatedTo").withTempName("tempName").withNames("names", "", "", ""),
      // header row 2
      likeRow().withId("uuid").withRelation("relation").withTempName("text").withNames("names", "", "", ""),
      // header row 3
      likeRow().withRelation("wwpersons").withNames("1", "", "2", ""),
      likeRow().withId("123").withTempName("temp name").withNames("forename", "foreName 1", "forename", "foreName 2"),
      likeRow().withId("234").withTempName("tmp 2").withRelation("123")));
  }

  private void assertCellEqual(SXSSFSheet sheet, int row, int col, String value) {
    assertThat(sheet.getRow(row).getCell(col).getStringCellValue(), equalTo(value));
  }

  public static class EntityRowMatcher extends CompositeMatcher<Row> {

    private EntityRowMatcher() {

    }

    public EntityRowMatcher withId(String id) {
      this.addStringMatcher(id, TIM_ID_COL, "tim_id");
      return this;
    }

    public EntityRowMatcher withTempName(String tempName) {
      addStringMatcher(tempName, TEMP_NAME_COL, "tempName");
      return this;
    }

    public EntityRowMatcher withRelation(String relationId) {
      this.addStringMatcher(relationId, RELATION_COL, "relation");
      return this;
    }

    public EntityRowMatcher withNames(String name1Key, String name1Value, String name2Key, String name2Value) {
      this.addStringMatcher(name1Key, NAMES_1_KEY_COL, "name 1 key");
      this.addStringMatcher(name1Value, NAMES_1_VALUE_COL, "name 1 value");

      this.addStringMatcher(name2Key, NAMES_2_KEY_COL, "name 2 key");
      this.addStringMatcher(name2Value, NAMES_2_VALUE_COL, "name 2 value");

      return this;
    }

    private void addStringMatcher(final String value, final int col, final String desc) {
      this.addMatcher(new PropertyEqualityMatcher<Row, String>(desc, value) {
        @Override
        protected String getItemValue(Row item) {
          Cell cell = item.getCell(col);
          return cell == null ? "" : cell.getStringCellValue();
        }
      });
    }


  }
}
