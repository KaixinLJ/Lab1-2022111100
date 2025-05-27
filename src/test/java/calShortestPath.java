import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class calcShortestPathTest {

  TextGraphAnalyzer.DirectedGraph graph;

  @BeforeEach
  void setUp() {
    graph = new TextGraphAnalyzer.DirectedGraph();
    String text = "A B C D A C D B";
    String[] words = text.split(" ");
    for (int i = 0; i < words.length - 1; i++) {
      graph.addEdge(words[i], words[i + 1]);
    }
    TextGraphAnalyzer.saveGraphToFile(graph, "output.png", "png");

  }


  @Test
  void testNormalPath() {
    String result = TextGraphAnalyzer.calcShortestPath(graph, "A", "D");
    assertTrue(result.contains("Path from a to d"));
    assertTrue(result.contains("a → c → d"));
    assertTrue(result.contains("Length: 3"));
  }

  @Test
  void testSameStartAndEnd() {
    String result = TextGraphAnalyzer.calcShortestPath(graph, "A", "A");
    assertTrue(result.contains("Path from a to a: a"));
    assertTrue(result.contains("Length: 0"));
  }

  @Test
  void testNonexistentStart() {
    String result = TextGraphAnalyzer.calcShortestPath(graph, "X", "B");
    assertTrue(result.contains("No x in the graph!"));
  }

  @Test
  void testNonexistentEnd() {
    String result = TextGraphAnalyzer.calcShortestPath(graph, "A", "Y");
    assertTrue(result.contains("No y in the graph!"));
  }

  @Test
  void testNonexistentStartAndEnd() {
    String result = TextGraphAnalyzer.calcShortestPath(graph, "X", "Y");
    assertTrue(result.contains("No x or y in the graph!"));
  }

  @Test
  void testNoPathExists() {
    // 特殊情况需要单独建图，不能用统一的图
    TextGraphAnalyzer.DirectedGraph isolatedGraph = new TextGraphAnalyzer.DirectedGraph();
    isolatedGraph.addEdge("A", "B");
    isolatedGraph.addEdge("C", "D");

    String result = TextGraphAnalyzer.calcShortestPath(isolatedGraph, "A", "D");
    assertTrue(result.contains("No path exists from a to d!"));
  }
}
