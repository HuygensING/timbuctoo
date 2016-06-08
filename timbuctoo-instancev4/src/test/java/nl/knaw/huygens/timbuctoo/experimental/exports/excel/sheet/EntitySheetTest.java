package nl.knaw.huygens.timbuctoo.experimental.exports.excel.sheet;

import nl.knaw.huygens.timbuctoo.model.vre.Collection;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import nl.knaw.huygens.timbuctoo.server.HuygensIng;
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
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class EntitySheetTest {


  public static final int PROPERTY_NAME_ROW = 0;
  public static final int PROPERTY_TYPE_ROW = 1;
  public static final int PROPERTY_VALUE_DESCRIPTION_ROW = 2;
  public static final int TIM_ID_COL = 0;
  public static final int RELATION_COL = 1;
  public static final int NAMES_1_KEY_COL = 2;
  public static final int NAMES_1_VALUE_COL = 3;
  public static final int NAMES_2_KEY_COL = 4;
  public static final int NAMES_2_VALUE_COL = 5;
  public static final int TEMP_NAME_COL = 6;
  private static final int NAMES_COL = 2;
  private final Vres mappings = HuygensIng.mappings;
  private final Collection collection = mappings.getCollection("wwpersons").get();
  private final String[] relationTypes = null;
  private final String acceptedProp = "wwrelation_accepted";

  @Test
  public void renderToWorkbookRendersAnEmptyExcelSheetToWorkBookWhenPresentedWithAnEmptySetOfVertices() {
    final GraphWrapper graphWrapper = mock(GraphWrapper.class);
    final SXSSFWorkbook workbook = new SXSSFWorkbook();
    final Graph graph = newGraph().build();

    given(graphWrapper.getGraph()).willReturn(graph);

    EntitySheet instance = new EntitySheet(collection, workbook, graphWrapper, mappings, relationTypes, acceptedProp);

    instance.renderToWorkbook(graph.traversal().V().toSet());

    SXSSFSheet sheet = workbook.getSheet("wwpersons");
    assertThat(sheet, instanceOf(SXSSFSheet.class));
    assertCellEqual(sheet, PROPERTY_NAME_ROW, 0, "tim_id");
    assertCellEqual(sheet, PROPERTY_TYPE_ROW, 0, "uuid");
    assertThat(sheet.getRow(2).getCell(0), equalTo(null));

    assertThat(sheet.getRow(PROPERTY_NAME_ROW).getCell(1), equalTo(null));
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
            jsnO("type", jsn("FORENAME"), "value", jsn("foreName 1")),
            jsnO("type", jsn("SURNAME"), "value", jsn("surName 1"))
          )),
          jsnO("components", jsnA(
            jsnO("type", jsn("FORENAME"), "value", jsn("foreName 2")),
            jsnO("type", jsn("SURNAME"), "value", jsn("surName 2"))
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

    // Property name headers
    assertCellEqual(sheet, PROPERTY_NAME_ROW, TIM_ID_COL, "tim_id");
    assertCellEqual(sheet, PROPERTY_NAME_ROW, RELATION_COL, "isRelatedTo");
    assertCellEqual(sheet, PROPERTY_NAME_ROW, TEMP_NAME_COL, "tempName");
    assertCellEqual(sheet, PROPERTY_NAME_ROW, NAMES_COL, "names");

    // Property type headers
    assertCellEqual(sheet, PROPERTY_TYPE_ROW, TIM_ID_COL, "uuid");
    assertCellEqual(sheet, PROPERTY_TYPE_ROW, RELATION_COL, "relation");
    assertCellEqual(sheet, PROPERTY_TYPE_ROW, TEMP_NAME_COL, "text");
    assertCellEqual(sheet, PROPERTY_TYPE_ROW, NAMES_COL, "names");

    // Property value description headers
    assertCellEqual(sheet, PROPERTY_VALUE_DESCRIPTION_ROW, RELATION_COL, "wwpersons");
    assertCellEqual(sheet, PROPERTY_VALUE_DESCRIPTION_ROW, NAMES_1_KEY_COL, "1");
    assertCellEqual(sheet, PROPERTY_VALUE_DESCRIPTION_ROW, NAMES_2_KEY_COL, "2");

    // tim_id values
    assertCellEqual(sheet, 3, TIM_ID_COL, "123");
    assertCellEqual(sheet, 5, TIM_ID_COL, "234");


    // relation values
    assertCellEqual(sheet, 5, RELATION_COL, "123");

    // names columns
    assertCellEqual(sheet, 3, NAMES_1_KEY_COL, "forename");
    assertCellEqual(sheet, 4, NAMES_1_KEY_COL, "surname");

    assertCellEqual(sheet, 3, NAMES_1_VALUE_COL, "foreName 1");
    assertCellEqual(sheet, 4, NAMES_1_VALUE_COL, "surName 1");

    assertCellEqual(sheet, 3, NAMES_2_KEY_COL, "forename");
    assertCellEqual(sheet, 4, NAMES_2_KEY_COL, "surname");

    assertCellEqual(sheet, 3, NAMES_2_VALUE_COL, "foreName 2");
    assertCellEqual(sheet, 4, NAMES_2_VALUE_COL, "surName 2");

    // tempName column
    assertCellEqual(sheet, 3, TEMP_NAME_COL, "temp name");
    assertCellEqual(sheet, 5, TEMP_NAME_COL, "tmp 2");

  }


  private void assertCellEqual(SXSSFSheet sheet, int row, int col, String value) {
    assertThat(sheet.getRow(row).getCell(col).getStringCellValue(), equalTo(value));
  }
}
