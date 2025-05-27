import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class queryBridgeWordsTest {

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
  void testWord1NotExist() {
    String result = TextGraphAnalyzer.queryBridgeWords(graph, "X", "C");
    assertEquals("No x in the graph!", result);
  }

  @Test
  void testWord2NotExist() {
    String result = TextGraphAnalyzer.queryBridgeWords(graph, "A", "Y");
    assertEquals("No y in the graph!", result);
  }

  @Test
  void testBothWordsNotExist() {
    String result = TextGraphAnalyzer.queryBridgeWords(graph, "X", "Y");
    assertEquals("No x or y in the graph!", result);
  }

  @Test
  void testNoBridgeWords() {
    String result = TextGraphAnalyzer.queryBridgeWords(graph, "B", "A");
    assertEquals("No bridge words from b to a!", result);
  }

  @Test
  void testOneBridgeWord() {
    // A -> B -> C 存在，检查 A -> C
    String result = TextGraphAnalyzer.queryBridgeWords(graph, "A", "C");
    // graph 结构中，A 的邻居是 B 和 C，B 的邻居是 C
    assertEquals("The bridge words from a to c are: b.", result);
  }

  @Test
  void testMultipleBridgeWords() {
    // 为了测试多个桥接词，先手动再加个 A -> D -> C
    graph.addEdge("A", "D");
    graph.addEdge("D", "C");

    String result = TextGraphAnalyzer.queryBridgeWords(graph, "A", "C");
    // A 的邻居有 B、C、D，B 和 D 都连到 C
    // 顺序是插入顺序，先 B 再 D
    assertEquals("The bridge words from a to c are: b and d.", result);
  }
  @Test
  public void testThreeOrMoreBridgeWords() {
    // 创建一个新的图来确保有3个或更多桥接词
    TextGraphAnalyzer.DirectedGraph testGraph = new TextGraphAnalyzer.DirectedGraph();

    // 构建路径: P -> R -> Q, P -> S -> Q, P -> T -> Q
    testGraph.addEdge("P", "R");
    testGraph.addEdge("R", "Q");
    testGraph.addEdge("P", "S");
    testGraph.addEdge("S", "Q");
    testGraph.addEdge("P", "T");
    testGraph.addEdge("T", "Q");

    String result = TextGraphAnalyzer.queryBridgeWords(testGraph, "P", "Q");
    // 期望输出类似: "The bridge words from p to q are: r, s and t."
    // 这会测试三元运算符的 false 分支 (", ")，因为有3个桥接词
    // 中间的桥接词会用 ", " 连接，最后一个用 " and " 连接
    assertTrue(result.contains(", ") && result.contains(" and "));
    assertTrue(result.startsWith("The bridge words from p to q are:"));
    assertTrue(result.endsWith("."));

    // 验证包含所有三个桥接词
    assertTrue(result.contains("r") && result.contains("s") && result.contains("t"));
  }
}
