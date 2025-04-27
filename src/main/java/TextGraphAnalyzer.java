import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;
import java.util.stream.*;

public class TextGraphAnalyzer {
    // Define the graph structure
    public static class DirectedGraph {
        Map<String, Map<String, Integer>> adjacencyList = new HashMap<>();
        Set<String> allWords = new HashSet<>();

        // Add a word to the graph
        public void addWord(String word) {
            word = word.toLowerCase();
            allWords.add(word);
            if (!adjacencyList.containsKey(word)) {
                adjacencyList.put(word, new HashMap<>());
            }
        }

        // Add an edge between words
        public void addEdge(String from, String to) {
            from = from.toLowerCase();
            to = to.toLowerCase();

            addWord(from);
            addWord(to);

            Map<String, Integer> edges = adjacencyList.get(from);
            edges.put(to, edges.getOrDefault(to, 0) + 1);
        }

        // Check if a word exists in the graph
        public boolean containsWord(String word) {
            return allWords.contains(word.toLowerCase());
        }

        // Get neighbors of a word with edge weights
        public Map<String, Integer> getNeighbors(String word) {
            word = word.toLowerCase();
            return adjacencyList.getOrDefault(word, new HashMap<>());
        }

        // Get all words in the graph
        public Set<String> getAllWords() {
            return allWords;
        }

        // Get number of words in the graph
        public int size() {
            return allWords.size();
        }
    }

    private static final Random random = new Random();

    // Create graph from file - now returns a DirectedGraph
    public static DirectedGraph createGraphFromFile(String filePath) throws IOException {
        DirectedGraph graph = new DirectedGraph();
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

        // Build the graph from adjacent words
        for (int i = 0; i < words.size() - 1; i++) {
            graph.addEdge(words.get(i), words.get(i + 1));
        }

        return graph;
    }

    // Query bridge words - now takes graph as parameter
    public static String queryBridgeWords(DirectedGraph graph, String word1, String word2) {
        word1 = word1.toLowerCase();
        word2 = word2.toLowerCase();

        // Check if both words exist in the graph
        if (!graph.containsWord(word1) || !graph.containsWord(word2)) {
            return "No " + (!graph.containsWord(word1) ? word1 : "") +
                    ((!graph.containsWord(word1) && !graph.containsWord(word2)) ? " or " : "") +
                    (!graph.containsWord(word2) ? word2 : "") + " in the graph!";
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
            StringBuilder result = new StringBuilder("The bridge words from " + word1 + " to " + word2 + " are: ");
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

    // Generate new text using bridge words - now takes graph as parameter
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
                String bridgeWord = bridgeWords.get(random.nextInt(bridgeWords.size()));
                result.append(" ").append(bridgeWord);
            }

            // Add the second word
            result.append(" ").append(word2);
        }

        return result.toString();
    }

    // Calculate shortest path - now takes graph as parameter
    public static String calcShortestPath(DirectedGraph graph, String word1, String word2) {
        word1 = word1.toLowerCase();
        word2 = word2.toLowerCase();

        // Check if both words exist in the graph
        if (!graph.containsWord(word1) || !graph.containsWord(word2)) {
            return "No " + (!graph.containsWord(word1) ? word1 : "") +
                    ((!graph.containsWord(word1) && !graph.containsWord(word2)) ? " or " : "") +
                    (!graph.containsWord(word2) ? word2 : "") + " in the graph!";
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
        return "Path from " + word1 + " to " + word2 + ": " + pathStr + "\nLength: " + distance.get(word2);
    }

    // Calculate PageRank - now takes graph as parameter
    public static Double calcPageRank(DirectedGraph graph, String word) {
        word = word.toLowerCase();

        // Check if word exists in the graph
        if (!graph.containsWord(word)) {
            return -1.0; // Error value
        }

        // Number of iterations for convergence
        final int ITERATIONS = 500;
        // Damping factor
        final double DAMPING = 0.85;

        int n = graph.size();
        Map<String, Double> rank = new HashMap<>();
        Map<String, Double> newRank = new HashMap<>();

        // Initialize ranks
        double initialRank = 1.0 / n;
        for (String w : graph.getAllWords()) {
            rank.put(w, initialRank);
        }

        // Compute PageRank values iteratively
        for (int i = 0; i < ITERATIONS; i++) {
            // Reset new ranks
            for (String w : graph.getAllWords()) {
                newRank.put(w, (1 - DAMPING) / n);
            }

            // Handle dangling nodes (nodes with no outgoing links)
            double danglingWeight = 0;
            for (String w : graph.getAllWords()) {
                Map<String, Integer> outLinks = graph.getNeighbors(w);
                if (outLinks.isEmpty() || outLinks.values().stream().mapToInt(Integer::intValue).sum() == 0) {
                    danglingWeight += DAMPING * rank.get(w) / n;
                }
            }

            // Add dangling weight to all nodes
            for (String w : graph.getAllWords()) {
                newRank.put(w, newRank.get(w) + danglingWeight);
            }

            // For each word in the graph
            for (String w : graph.getAllWords()) {
                // Get outgoing links and their total weight
                Map<String, Integer> outLinks = graph.getNeighbors(w);
                int totalWeight = outLinks.values().stream().mapToInt(Integer::intValue).sum();

                // Distribute rank to neighbors
                if (totalWeight > 0) {
                    for (Map.Entry<String, Integer> outLink : outLinks.entrySet()) {
                        String neighbor = outLink.getKey();
                        int weight = outLink.getValue();

                        double addition = DAMPING * rank.get(w) * (weight / (double) totalWeight);
                        newRank.put(neighbor, newRank.get(neighbor) + addition);
                    }
                }
            }

            // Update ranks
            rank = new HashMap<>(newRank);
        }

        // Optional: Normalize ranks to ensure sum equals 1
        double sum = rank.values().stream().mapToDouble(Double::doubleValue).sum();
        if (Math.abs(sum - 1.0) > 0.001) {  // If sum is not close to 1
            for (String w : rank.keySet()) {
                rank.put(w, rank.get(w) / sum);
            }
        }

        return rank.get(word);
    }

    // Perform random walk - now takes graph as parameter
    public static String randomWalk(DirectedGraph graph) {
        if (graph.size() == 0) {
            return "Graph is empty.";
        }

        // Select a random starting node
        List<String> allWords = new ArrayList<>(graph.getAllWords());
        String currentWord = allWords.get(random.nextInt(allWords.size()));

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
            String nextWord = neighborList.get(random.nextInt(neighborList.size()));

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
     * Save the directed graph to disk as a DOT file for Graphviz
     * @param graph The directed graph to save
     * @param outputFilePath The path to save the output file
     * @param format The output format (dot, png, svg, pdf)
     * @return Whether the operation was successful
     */
    public static boolean saveGraphToFile(DirectedGraph graph, String outputFilePath, String format) {
        try {
            // Ensure the output directory exists
            File outputFile = new File(outputFilePath);
            if (outputFile.getParentFile() != null) {
                outputFile.getParentFile().mkdirs();
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
     * Generate a DOT file from the graph
     */
    private static void generateDotFile(DirectedGraph graph, String filePath) throws IOException {
        try (FileWriter writer = new FileWriter(filePath)) {
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

                    writer.write("  \"" + sanitizedFrom + "\" -> \"" + sanitizedTo +
                            "\" [label=\"" + weight + "\", weight=" + weight +
                            ", penwidth=" + String.format("%.2f", penwidth) + "];\n");
                }
            }

            writer.write("}\n");
        }
    }

    /**
     * Render a DOT file using Graphviz
     */
    private static void renderWithGraphviz(String dotFilePath, String outputFilePath, String format)
            throws IOException, InterruptedException {
        // Check if Graphviz is installed
        if (!isGraphvizInstalled()) {
            throw new IOException("Graphviz is not installed. Please install Graphviz or use DOT format.");
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

        // Delete the temporary DOT file if it's different from the output
        if (!dotFilePath.equals(outputFilePath)) {
            new File(dotFilePath).delete();
        }
    }

    /**
     * Check if Graphviz is installed
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
     * Save the graph to a file with enhanced visual settings for better clarity
     * @param graph The directed graph to save
     * @param filePath The path to save the file to
     * @param format The format to save as (svg, png, pdf, or dot)
     * @return true if the operation was successful, false otherwise
     */
    public static boolean saveEnhancedGraphToFile(DirectedGraph graph, String filePath, String format) {
        try {
            // Create a temporary DOT file
            File dotFile = File.createTempFile("graph_", ".dot");

            try (PrintWriter writer = new PrintWriter(dotFile)) {
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
                        writer.println("  \"" + source + "\" -> \"" + target + "\" [label=\" " + weight + " \"];");
                    }
                }

                writer.println("}");
            }

            // Check if the file is to be saved as DOT format
            if (format.equalsIgnoreCase("dot")) {
                // Just copy the DOT file
                Files.copy(dotFile.toPath(), new File(filePath).toPath(), StandardCopyOption.REPLACE_EXISTING);
                return true;
            }

            // For other formats, use Graphviz to convert the DOT file
            if (!isGraphvizInstalled()) {
                return false;
            }

            // Build the Graphviz command
            String command = "dot -T" + format.toLowerCase() + " \"" + dotFile.getAbsolutePath() + "\" -o \"" + filePath + "\"";

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
}