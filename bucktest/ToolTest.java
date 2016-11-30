public class ToolTest {

  @org.junit.Test
  public void itWorks() {
    Support tool = new Support();
    if (tool.foo() == null) {
      throw new RuntimeException("fail!");
    }
  }
}
