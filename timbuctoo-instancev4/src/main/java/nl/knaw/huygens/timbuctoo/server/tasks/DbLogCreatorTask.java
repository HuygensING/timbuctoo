package nl.knaw.huygens.timbuctoo.server.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMultimap;
import io.dropwizard.servlets.tasks.Task;
import nl.knaw.huygens.timbuctoo.model.Change;
import nl.knaw.huygens.timbuctoo.server.TinkerpopGraphManager;
import org.apache.tinkerpop.gremlin.neo4j.process.traversal.LabelP;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Comparator;

public class DbLogCreatorTask extends Task {
  private final TinkerpopGraphManager graphManager;
  private final ObjectMapper objectMapper;

  public DbLogCreatorTask(TinkerpopGraphManager graphManager) {
    super("createlog");
    this.graphManager = graphManager;
    objectMapper = new ObjectMapper();
  }

  @Override
  public void execute(ImmutableMultimap<String, String> parameters, PrintWriter output) throws Exception {
    output.write("LOG " + System.lineSeparator());
    Graph graph = graphManager.getGraph();

    graph.traversal().V().has("tim_id").as("a").outE().where(__.not(__.has(T.label, LabelP.of("VERSION_OF"))))
      .<Vertex>select("a")
      .dedup()
      .limit(100000)
      .order()
      .by("modified", (Comparator<String>) (o1, o2) -> {
        try {
          Change change1 = objectMapper.readValue(o1, Change.class);
          Change change2 = objectMapper.readValue(o2, Change.class);

          return Long.compare(change1.getTimeStamp(), change2.getTimeStamp());
        } catch (IOException e) {
          return 0;
        }
      })
      .forEachRemaining(vertex -> {
        VertexProperty<String> modified = vertex.property("modified");
        String modOutput = "<>";
        if (modified.isPresent()) {
          try {
            Change change = objectMapper.readValue(modified.value(), Change.class);
            modOutput = String.format("%d:%s", change.getTimeStamp(), change.getUserId());
          } catch (IOException e) {
            e.printStackTrace();
          }
        }

        output.write(String.format("%s - %s (rev %d) %s",
          modOutput,
          vertex.value("tim_id"),
          vertex.<Integer>value("rev"),
          System.lineSeparator()));
      });

  }
}
