package nl.knaw.huygens.timbuctoo;

import org.junit.AfterClass;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;

//separate class to make sure that the after is run after the AppRule has finished
public class IntegrationTestCleaner {
  protected static final String tempPath = resourceFilePath("integrationtest");

  @AfterClass
  public static void afterClass() throws Exception {
    Path directory = Paths.get(tempPath, "datasets");
    Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        Files.delete(file);
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        Files.delete(dir);
        return FileVisitResult.CONTINUE;
      }
    });
  }

}
