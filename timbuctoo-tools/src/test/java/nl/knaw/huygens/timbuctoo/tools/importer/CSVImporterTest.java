package nl.knaw.huygens.timbuctoo.tools.importer;

/*
 * #%L
 * Timbuctoo tools
 * =======
 * Copyright (C) 2012 - 2014 Huygens ING
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import com.google.common.base.Joiner;

public class CSVImporterTest {

  @Test
  public void testSimpleInput() throws Exception {
    testLineCount(3, "1", "2", "3");
  }

  @Test
  public void testInputWithEmptyLines() throws Exception {
    testLineCount(2, "1", "", "2", "");
  }

  @Test
  public void testInputWithCommentLines() throws Exception {
    testLineCount(2, "-- comment", "-- comment", "1", "-- comment", "2");
  }

  private void testLineCount(int expectedNumberOfLines, String... lines) throws Exception {
    String input = Joiner.on('\n').join(lines);
    InputStream stream = IOUtils.toInputStream(input, "UTF-8");
    TestImporter importer = new TestImporter();
    importer.handleFile(stream, 0, false);
    assertThat(importer.getCount(), equalTo(expectedNumberOfLines));
  }

  private static class TestImporter extends CSVImporter {
    private int count = 0;
    public int getCount() {
      return count;
    }

    @Override
    protected void handleLine(String[] items) throws Exception {
      count++;
    }
  }

}
