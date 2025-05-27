import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@code TextGraphAnalyzer} 是一个文本图分析器. 提供将文本文件转换为有向图的功能，并基于该图实现桥接词查询、文本生成、最短路径计算、PageRank
 * 计算和随机游走等操作。
 *
 * <p>图的节点是文本中的单词，边的权重表示单词之间相邻出现的次数。
 *
 * <p>同时支持将图导出为 DOT 文件，并调用 Graphviz 工具渲染成图片文件。
 *
 * @author dlj
 * @version 1.0
 */
public class TextGraphAnalyzer {

  private static final SecureRandom secureRandom = new SecureRandom();

  // Create graph from file - now returns a DirectedGraph

  /**
   * 从文本文件创建有向图。将文本内容解析为单词，并基于单词的相邻关系构建图.
   *
   * @param filePath 文本文件的路径
   * @return 表示文本中单词关系的有向图
   * @throws IOException 如果无法读取或处理文件时抛出异常
   */
  public static DirectedGraph createGraphFromFile(String filePath) throws IOException {

    String content = Files.readString(Paths.get(filePath));

    // Replace line breaks and punctuation with spaces
    content = content.replaceAll("[\\r\\n]", " ");
    content = content.replaceAll("[\\p{Punct}]", " ");

    // Extract words (only letters A-Z and a-z)
    Pattern pattern = Pattern.compile("[a-zA-Z]+");
    Matcher matcher = pattern.matcher(content);

    List<String> words = new ArrayList<>();
    while (matcher.find()) {
      words.add(matcher.group());
    }

    DirectedGraph graph = new DirectedGraph();
    // Build the graph from adjacent words
    for (int i = 0; i < words.size() - 1; i++) {
      graph.addEdge(words.get(i), words.get(i + 1));
    }

    return graph;
  }

  // Query bridge words - now takes graph as parameter

  /**
   * 查询两个单词之间的桥接词. 桥接词是指在原文中既出现在第一个单词之后，又出现在第二个单词之前的单词。
   *
   * @param graph 文本的有向图表示
   * @param word1 第一个单词
   * @param word2 第二个单词
   * @return 描述两个单词之间桥接词的字符串
   */
  public static String queryBridgeWords(DirectedGraph graph, String word1, String word2) {
    word1 = word1.toLowerCase();
    word2 = word2.toLowerCase();

    // Check if both words exist in the graph
    if (!graph.containsWord(word1) || !graph.containsWord(word2)) {
      return "No " + (!graph.containsWord(word1) ? word1 : "")
          + ((!graph.containsWord(word1) && !graph.containsWord(word2)) ? " or " : "")
          + (!graph.containsWord(word2) ? word2 : "") + " in the graph!";
    }

    // Find bridge words
    List<String> bridgeWords = new ArrayList<>();

    // For each neighbor of word1
    for (String bridgeCandidate : graph.getNeighbors(word1).keySet()) {
      // Check if this neighbor connects to word2
      if (graph.getNeighbors(bridgeCandidate).containsKey(word2)) {
        bridgeWords.add(bridgeCandidate);
      }
    }

    // Format and return the result
    if (bridgeWords.isEmpty()) {
      return "No bridge words from " + word1 + " to " + word2 + "!";
    } else {
      StringBuilder result = new StringBuilder("The bridge words from "
          + word1 + " to " + word2 + " are: ");
      for (int i = 0; i < bridgeWords.size(); i++) {
        if (i > 0) {
          result.append(i == bridgeWords.size() - 1 ? " and " : ", ");
        }
        result.append(bridgeWords.get(i));
      }
      result.append(".");
      return result.toString();
    }
  }

  // Find bridge words for generation - returns list
  private static List<String> findBridgeWords(DirectedGraph graph, String word1, String word2) {
    word1 = word1.toLowerCase();
    word2 = word2.toLowerCase();

    List<String> bridgeWords = new ArrayList<>();

    // Check if both words exist in the graph
    if (!graph.containsWord(word1) || !graph.containsWord(word2)) {
      return bridgeWords; // Empty list if either word doesn't exist
    }

    // For each neighbor of word1
    for (String bridgeCandidate : graph.getNeighbors(word1).keySet()) {
      // Check if this neighbor connects to word2
      if (graph.getNeighbors(bridgeCandidate).containsKey(word2)) {
        bridgeWords.add(bridgeCandidate);
      }
    }

    return bridgeWords;
  }

  /**
   * 使用桥接词生成新文本。在输入文本的相邻单词之间插入随机选择的桥接词.
   *
   * @param graph     文本的有向图表示
   * @param inputText 要处理的输入文本
   * @return 插入了桥接词的新文本
   */
  public static String generateNewText(DirectedGraph graph, String inputText) {
    // Extract words from input text
    Pattern pattern = Pattern.compile("[a-zA-Z]+");
    Matcher matcher = pattern.matcher(inputText);
    List<String> words = new ArrayList<>();

    while (matcher.find()) {
      words.add(matcher.group());
    }

    if (words.size() <= 1) {
      return inputText; // Not enough words to insert bridge words
    }

    StringBuilder result = new StringBuilder();
    result.append(words.get(0));

    // For each pair of adjacent words
    for (int i = 0; i < words.size() - 1; i++) {
      String word1 = words.get(i);
      String word2 = words.get(i + 1);

      // Find bridge words
      List<String> bridgeWords = findBridgeWords(graph, word1, word2);

      // If bridge words exist, add a random one
      if (!bridgeWords.isEmpty()) {
        String bridgeWord = bridgeWords.get(secureRandom.nextInt(bridgeWords.size()));
        result.append(" ").append(bridgeWord);
      }

      // Add the second word
      result.append(" ").append(word2);
    }

    return result.toString();
  }

  /**
   * 计算两个单词之间的最短路径。使用 Dijkstra 算法查找从一个单词到另一个单词的最短路径.
   *
   * @param graph 文本的有向图表示
   * @param word1 起始单词
   * @param word2 目标单词
   * @return 描述最短路径的字符串，包括路径和距离
   */
  public static String calcShortestPath(DirectedGraph graph, String word1, String word2) {
    word1 = word1.toLowerCase();
    word2 = word2.toLowerCase();

    // Check if both words exist in the graph
    if (!graph.containsWord(word1) || !graph.containsWord(word2)) {
      return "No " + (!graph.containsWord(word1) ? word1 : "")
          + ((!graph.containsWord(word1) && !graph.containsWord(word2)) ? " or " : "")
          + (!graph.containsWord(word2) ? word2 : "") + " in the graph!";
    }

    // If words are the same
    if (word1.equals(word2)) {
      return "Path from " + word1 + " to " + word2 + ": " + word1 + "\nLength: 0";
    }

    // Dijkstra's algorithm
    Map<String, Integer> distance = new HashMap<>();
    Map<String, String> previous = new HashMap<>();
    PriorityQueue<String> queue = new PriorityQueue<>(
        Comparator.comparingInt(distance::get));

    // Initialize
    for (String word : graph.getAllWords()) {
      distance.put(word, Integer.MAX_VALUE);
      previous.put(word, null);
    }
    distance.put(word1, 0);
    queue.add(word1);

    // Process queue
    while (!queue.isEmpty()) {
      String current = queue.poll();

      // If we've reached the target word
      if (current.equals(word2)) {
        break;
      }

      int currentDistance = distance.get(current);

      // For each neighbor of current word
      for (Map.Entry<String, Integer> neighborEntry : graph.getNeighbors(current).entrySet()) {
        String neighbor = neighborEntry.getKey();
        int weight = neighborEntry.getValue();
        int newDistance = currentDistance + weight;

        if (newDistance < distance.get(neighbor)) {
          // Remove and re-add to update priority
          queue.remove(neighbor);

          distance.put(neighbor, newDistance);
          previous.put(neighbor, current);

          queue.add(neighbor);
        }
      }
    }

    // Check if a path was found
    if (distance.get(word2) == Integer.MAX_VALUE) {
      return "No path exists from " + word1 + " to " + word2 + "!";
    }

    // Reconstruct the path
    List<String> path = new ArrayList<>();
    String current = word2;
    while (current != null) {
      path.add(current);
      current = previous.get(current);
    }
    Collections.reverse(path);

    // Format the result
    String pathStr = String.join(" → ", path);
    return "Path from " + word1 + " to " + word2 + ": "
        + pathStr + "\nLength: " + distance.get(word2);
  }

  /**
   * 计算指定单词的 PageRank 值. PageRank 是一种算法，用于评估网页或节点在网络中的重要性.
   *
   * @param graph 文本的有向图表示
   * @param word  要计算 PageRank 的单词
   * @return 指定单词的 PageRank 值
   */
  public static Double calcPageRank(DirectedGraph graph, String word) {
    word = word.toLowerCase();
    if (!graph.containsWord(word)) {
      return -1.0;
    }

    final int iterations = 500;
    final double damping = 0.85;

    int n = graph.size();
    Map<String, Double> rank = new HashMap<>();
    Map<String, Double> newRank = new HashMap<>();

    // 缓存单词列表，避免多次 getAllWords()
    Set<String> allWords = graph.getAllWords();

    // 初始化 rank 值
    double initialRank = 1.0 / n;
    for (String w : allWords) {
      rank.put(w, initialRank);
    }

    for (int i = 0; i < iterations; i++) {
      // 重置 newRank
      for (String w : allWords) {
        newRank.put(w, (1 - damping) / n);
      }

      // 计算悬挂节点贡献值
      double danglingWeight = 0;
      for (String w : allWords) {
        Map<String, Integer> outLinks = graph.getNeighbors(w);
        int totalWeight = outLinks.values().stream().mapToInt(Integer::intValue).sum();
        if (outLinks.isEmpty() || totalWeight == 0) {
          danglingWeight += damping * rank.get(w) / n;
        }
      }

      // 给所有节点加上 danglingWeight
      for (String w : allWords) {
        newRank.put(w, newRank.get(w) + danglingWeight);
      }

      // 分配 PageRank 给邻居
      for (String w : allWords) {
        Map<String, Integer> outLinks = graph.getNeighbors(w);
        int totalWeight = outLinks.values().stream().mapToInt(Integer::intValue).sum();
        if (totalWeight > 0) {
          for (Map.Entry<String, Integer> outLink : outLinks.entrySet()) {
            String neighbor = outLink.getKey();
            int weight = outLink.getValue();
            double addition = damping * rank.get(w) * (weight / (double) totalWeight);
            newRank.put(neighbor, newRank.get(neighbor) + addition);
          }
        }
      }

      // 更新 rank
      Map<String, Double> temp = rank;
      rank = newRank;
      newRank = temp;
    }

    // 归一化 rank 值
    double sum = rank.values().stream().mapToDouble(Double::doubleValue).sum();
    if (Math.abs(sum - 1.0) > 0.001) {
      for (Map.Entry<String, Double> entry : rank.entrySet()) {
        entry.setValue(entry.getValue() / sum);
      }
    }

    return rank.get(word);
  }


  /**
   * 在有向图上执行随机游走。从随机节点开始，随机选择下一个节点，直到遇到已访问的边或无法继续.
   *
   * @param graph 文本的有向图表示
   * @return 描述随机游走路径的字符串
   */
  public static String randomWalk(DirectedGraph graph) {
    if (graph.size() == 0) {
      return "Graph is empty.";
    }

    // Select a random starting node
    List<String> allWords = new ArrayList<>(graph.getAllWords());
    String currentWord = allWords.get(secureRandom.nextInt(allWords.size()));

    List<String> path = new ArrayList<>();
    Set<String> visitedEdges = new HashSet<>();
    path.add(currentWord);

    while (true) {
      // Get neighbors
      Map<String, Integer> neighbors = graph.getNeighbors(currentWord);

      // Exit if no outgoing edges
      if (neighbors.isEmpty()) {
        break;
      }

      // Choose a random neighbor
      List<String> neighborList = new ArrayList<>(neighbors.keySet());
      String nextWord = neighborList.get(secureRandom.nextInt(neighborList.size()));

      // Create edge key
      String edge = currentWord + "->" + nextWord;

      // Exit if edge already visited
      if (visitedEdges.contains(edge)) {
        path.add(nextWord + " (repeated edge)");
        break;
      }

      visitedEdges.add(edge);
      path.add(nextWord);
      currentWord = nextWord;
    }

    // Format the path
    return String.join(" -> ", path);
  }

  /**
   * Save the directed graph to disk as a DOT file for Graphviz.
   *
   * @param graph          The directed graph to save
   * @param outputFilePath The path to save the output file
   * @param format         The output format (dot, png, svg, pdf)
   * @return Whether the operation was successful
   */
  public static boolean saveGraphToFile(DirectedGraph graph, String outputFilePath, String format) {
    try {
      // Ensure the output directory exists
      File outputFile = new File(outputFilePath);
      if (outputFile.getParentFile() != null) {
        boolean dirsCreated = outputFile.getParentFile().mkdirs();
        if (!dirsCreated && !outputFile.getParentFile().exists()) {
          System.err.println("Failed to create directories for: " + outputFile.getParent());
          return false;
        }
      }

      // Handle different formats
      format = format.toLowerCase();
      String dotFilePath;

      if (format.equals("dot")) {
        dotFilePath = outputFilePath;
      } else {
        // Generate temporary DOT file
        dotFilePath = outputFilePath.replaceAll("\\.[^.]+$", ".dot");
      }

      // Generate DOT file content
      generateDotFile(graph, dotFilePath);

      // If requested format is not DOT, convert using Graphviz
      if (!format.equals("dot")) {
        renderWithGraphviz(dotFilePath, outputFilePath, format);
      }

      return true;
    } catch (Exception e) {
      System.err.println("Error saving graph: " + e.getMessage());
      e.printStackTrace();
      return false;
    }
  }

  /**
   * Generate a DOT file from the graph.
   */
  private static void generateDotFile(DirectedGraph graph, String filePath) throws IOException {
    try (Writer writer =
        new OutputStreamWriter(new FileOutputStream(filePath), StandardCharsets.UTF_8)) {
      writer.write("digraph TextGraph {\n");

      // Graph attributes
      writer.write("  graph [fontname=\"Arial\", rankdir=LR];\n");
      writer.write("  node [fontname=\"Arial\", shape=ellipse];\n");
      writer.write("  edge [fontname=\"Arial\"];\n\n");

      // Write nodes
      for (String word : graph.getAllWords()) {
        String sanitizedWord = word.replace("\"", "\\\"");
        writer.write("  \"" + sanitizedWord + "\";\n");
      }

      writer.write("\n");

      // Write edges
      for (String from : graph.getAllWords()) {
        String sanitizedFrom = from.replace("\"", "\\\"");

        Map<String, Integer> neighbors = graph.getNeighbors(from);
        for (Map.Entry<String, Integer> entry : neighbors.entrySet()) {
          String to = entry.getKey();
          int weight = entry.getValue();
          String sanitizedTo = to.replace("\"", "\\\"");

          // Calculate edge thickness based on weight
          double penwidth = 1.0;
          if (weight > 1) {
            penwidth = 1.0 + Math.log10(weight);
          }

          writer.write("  \"" + sanitizedFrom + "\" -> \"" + sanitizedTo
              + "\" [label=\"" + weight + "\", weight=" + weight
              + ", penwidth=" + String.format("%.2f", penwidth) + "];\n");
        }
      }

      writer.write("}\n");
    }
  }

  /**
   * Render a DOT file using Graphviz.
   */
  private static void renderWithGraphviz(String dotFilePath, String outputFilePath, String format)
      throws IOException, InterruptedException {
    // Check if Graphviz is installed
    if (!isGraphvizInstalled()) {
      throw new IOException("Graphviz is not installed."
          + " Please install Graphviz or use DOT format.");
    }

    // Build the Graphviz command
    ProcessBuilder processBuilder = new ProcessBuilder(
        "dot", "-T" + format, "-o", outputFilePath, dotFilePath
    );

    processBuilder.redirectErrorStream(true);
    Process process = processBuilder.start();

    // Wait for the process to complete
    int exitCode = process.waitFor();
    if (exitCode != 0) {
      throw new IOException("Graphviz rendering failed with exit code " + exitCode);
    }

    if (!dotFilePath.equals(outputFilePath)) {
      File dotFile = new File(dotFilePath);
      boolean deleted = dotFile.delete();
      if (!deleted && dotFile.exists()) {
        System.err.println("Warning: Failed to delete temporary file: " + dotFilePath);
      }
    }
  }

  /**
   * Check if Graphviz is installed.
   */
  public static boolean isGraphvizInstalled() {
    try {
      Process process = Runtime.getRuntime().exec("dot -V");
      int exitCode = process.waitFor();
      return exitCode == 0;
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Save the graph to a file with enhanced visual settings for better clarity.
   *
   * @param graph    The directed graph to save
   * @param filePath The path to save the file to
   * @param format   The format to save as (svg, png, pdf, or dot)
   * @return true if the operation was successful, false otherwise
   */
  public static boolean saveEnhancedGraphToFile(
      DirectedGraph graph, String filePath, String format) {
    try {
      // Create a temporary DOT file
      File dotFile = File.createTempFile("graph_", ".dot");

      try (PrintWriter writer = new PrintWriter(
          new OutputStreamWriter(new FileOutputStream(dotFile), StandardCharsets.UTF_8))) {
        // DOT file header with enhanced graph settings for better clarity
        writer.println("digraph G {");
        writer.println("  // Enhanced graph settings");
        writer.println("  graph [");
        writer.println("    bgcolor=\"white\",");
        writer.println("    rankdir=LR,");  // Left to right layout
        writer.println("    splines=true,");  // Use curved edges
        writer.println("    overlap=false,");  // Prevent node overlap
        writer.println("    fontname=\"Arial\",");
        writer.println("    fontsize=14,");
        writer.println("    dpi=300,");      // Higher resolution
        writer.println("    nodesep=0.5,");  // Increased spacing between nodes
        writer.println("    ranksep=0.75");  // Increased spacing between ranks
        writer.println("  ];");

        // Enhanced node settings
        writer.println("  node [");
        writer.println("    shape=ellipse,");
        writer.println("    style=\"filled,rounded\",");
        writer.println("    fillcolor=\"#E8F5E9\",");  // Light green background
        writer.println("    color=\"#2E7D32\",");      // Dark green border
        writer.println("    penwidth=1.5,");          // Thicker border
        writer.println("    fontname=\"Arial\",");
        writer.println("    fontsize=14,");           // Larger font
        writer.println("    fontcolor=\"#1B5E20\",");  // Dark green text
        writer.println("    margin=0.3,");            // Padding around the text
        writer.println("    height=0.6");             // Slightly larger nodes
        writer.println("  ];");

        // Enhanced edge settings
        writer.println("  edge [");
        writer.println("    penwidth=1.2,");         // Thicker edges
        writer.println("    arrowsize=0.8,");        // Larger arrows
        writer.println("    color=\"#4CAF50\",");     // Green edges
        writer.println("    fontname=\"Arial\",");
        writer.println("    fontsize=12,");          // Readable font size
        writer.println("    fontcolor=\"#455A64\"");  // Dark bluish gray for edge labels
        writer.println("  ];");

        // Add nodes
        for (String word : graph.getAllWords()) {
          writer.println("  \"" + word + "\" [label=\"" + word + "\"];");
        }

        // Add edges with weights
        for (String source : graph.getAllWords()) {
          Map<String, Integer> neighbors = graph.getNeighbors(source);

          for (Map.Entry<String, Integer> edge : neighbors.entrySet()) {
            String target = edge.getKey();
            int weight = edge.getValue();

            // Add edge with weight as label
            writer.println("  \"" + source + "\" -> \""
                + target + "\" [label=\" " + weight + " \"];");
          }
        }

        writer.println("}");
      }

      // Check if the file is to be saved as DOT format
      if (format.equalsIgnoreCase("dot")) {
        // Just copy the DOT file
        Files.copy(
            dotFile.toPath(),
            new File(filePath).toPath(),
            StandardCopyOption.REPLACE_EXISTING
        );
        return true;
      }

      // For other formats, use Graphviz to convert the DOT file
      if (!isGraphvizInstalled()) {
        return false;
      }

      // Build the Graphviz command
      String command = "dot -T" + format.toLowerCase() + " \"" + dotFile.getAbsolutePath()
          + "\" -o \"" + filePath + "\"";

      // Execute the command
      Process process = Runtime.getRuntime().exec(command);
      int exitCode = process.waitFor();

      // Check if the conversion was successful
      return exitCode == 0;
    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
      return false;
    }
  }

  /**
   * {@code DirectedGraph} 是一个有向图的实现. 提供添加单词、添加边、检查单词存在性、获取邻居等功能.
   */

  public static class DirectedGraph {

    Map<String, Map<String, Integer>> adjacencyList = new HashMap<>();
    Set<String> allWords = new HashSet<>();

    /**
     * 添加一个单词到图中. 如果单词已经存在，则不做任何操作.
     *
     * @param word 要添加的单词
     */
    public void addWord(String word) {
      word = word.toLowerCase();
      allWords.add(word);
      if (!adjacencyList.containsKey(word)) {
        adjacencyList.put(word, new HashMap<>());
      }
    }

    /**
     * Add an edge between two words. If the words do not exist, they will be added. The edge weight
     * is incremented by 1.
     *
     * @param from The starting word
     * @param to   The ending word
     */
    public void addEdge(String from, String to) {
      from = from.toLowerCase();
      to = to.toLowerCase();

      addWord(from);
      addWord(to);

      Map<String, Integer> edges = adjacencyList.get(from);
      edges.put(to, edges.getOrDefault(to, 0) + 1);
    }

    /**
     * Check if a word exists in the graph.
     *
     * @param word The word to check
     * @return true if the word exists, false otherwise
     */
    public boolean containsWord(String word) {
      return allWords.contains(word.toLowerCase());
    }

    /**
     * Get the neighbors of a word and their weights.
     *
     * @param word The word to get neighbors for
     * @return A map of neighboring words and their weights
     */
    public Map<String, Integer> getNeighbors(String word) {
      word = word.toLowerCase();
      return adjacencyList.getOrDefault(word, new HashMap<>());
    }

    // Get all words in the graph
    public Set<String> getAllWords() {
      return Collections.unmodifiableSet(allWords);
    }

    /**
     * Get the size of the graph (number of unique words).
     *
     * @return The number of unique words in the graph
     */
    public int size() {
      return allWords.size();
    }
  }
}