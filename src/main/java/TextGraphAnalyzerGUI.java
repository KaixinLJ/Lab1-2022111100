import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.batik.swing.JSVGCanvas;
import java.awt.geom.AffineTransform;

public class TextGraphAnalyzerGUI extends JFrame {
    private TextGraphAnalyzer.DirectedGraph graph;
    private JTextArea outputArea;
    private JTextField word1Field, word2Field, inputTextField;
    private JTextField pathSourceField, pathTargetField;
    private JPanel graphPanel;
    private JFileChooser fileChooser;
    private String currentFilePath;
    private JButton saveGraphButton;
    private File currentSvgFile;
    private JSVGCanvas svgCanvas; // We'll use Batik's JSVGCanvas to display SVG

    public TextGraphAnalyzerGUI() {
        super("Text Graph Analyzer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);

        // Initialize components
        graph = null;
        fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Text files", "txt"));

        // Initialize the SVG Canvas
        try {
            // Use reflection to check if Batik is available
            Class.forName("org.apache.batik.swing.JSVGCanvas");
            svgCanvas = new org.apache.batik.swing.JSVGCanvas();
        } catch (ClassNotFoundException e) {
            // If Batik is not available, we'll handle this in the createOutputPanel method
            svgCanvas = null;
        }

        // Create main panels
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Left panel for controls
        JPanel controlPanel = createControlPanel();

        // Right panel for output
        JPanel outputPanel = createOutputPanel();

        // Add panels to main window
        mainPanel.add(controlPanel, BorderLayout.WEST);
        mainPanel.add(outputPanel, BorderLayout.CENTER);

        // Add menu bar
        setJMenuBar(createMenuBar());

        // Add main panel to frame
        add(mainPanel);

        // Display the window
        setVisible(true);
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // File menu
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);

        JMenuItem openItem = new JMenuItem("Open Text File", KeyEvent.VK_O);
        openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
        openItem.addActionListener(e -> openTextFile());

        JMenuItem saveGraphItem = new JMenuItem("Save Graph", KeyEvent.VK_S);
        saveGraphItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
        saveGraphItem.addActionListener(e -> saveGraph());

        JMenuItem exitItem = new JMenuItem("Exit", KeyEvent.VK_X);
        exitItem.addActionListener(e -> System.exit(0));

        fileMenu.add(openItem);
        fileMenu.add(saveGraphItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        // Help menu
        JMenu helpMenu = new JMenu("Help");
        helpMenu.setMnemonic(KeyEvent.VK_H);

        JMenuItem aboutItem = new JMenuItem("About", KeyEvent.VK_A);
        aboutItem.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Text Graph Analyzer\n" +
                        "A tool for analyzing word relationships in text documents.\n" +
                        "Version 1.0",
                "About Text Graph Analyzer",
                JOptionPane.INFORMATION_MESSAGE));

        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(helpMenu);

        return menuBar;
    }

    private JPanel createControlPanel() {
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBorder(new EmptyBorder(0, 0, 0, 10));
        controlPanel.setPreferredSize(new Dimension(250, 600));

        // File selection panel
        JPanel filePanel = new JPanel(new BorderLayout(5, 5));
        filePanel.setBorder(BorderFactory.createTitledBorder("Text File"));

        JButton browseButton = new JButton("Browse...");
        browseButton.addActionListener(e -> openTextFile());

        filePanel.add(browseButton, BorderLayout.CENTER);

        // Analysis options panel
        JPanel optionsPanel = new JPanel();
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
        optionsPanel.setBorder(BorderFactory.createTitledBorder("Analysis Options"));

        JButton showGraphButton = new JButton("Show Graph");
        showGraphButton.addActionListener(e -> showGraphVisualization());

        // Bridge Words panel
        JPanel bridgeWordsPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        bridgeWordsPanel.setBorder(BorderFactory.createTitledBorder("Bridge Words"));

        JPanel word1Panel = new JPanel(new BorderLayout(5, 5));
        word1Panel.add(new JLabel("Word 1:"), BorderLayout.WEST);
        word1Field = new JTextField(10);
        word1Panel.add(word1Field, BorderLayout.CENTER);

        JPanel word2Panel = new JPanel(new BorderLayout(5, 5));
        word2Panel.add(new JLabel("Word 2:"), BorderLayout.WEST);
        word2Field = new JTextField(10);
        word2Panel.add(word2Field, BorderLayout.CENTER);

        JButton queryBridgeButton = new JButton("Query Bridge Words");
        queryBridgeButton.addActionListener(e -> queryBridgeWords());

        bridgeWordsPanel.add(word1Panel);
        bridgeWordsPanel.add(word2Panel);
        bridgeWordsPanel.add(queryBridgeButton);

        // Generate Text panel
        JPanel generateTextPanel = new JPanel(new BorderLayout(5, 5));
        generateTextPanel.setBorder(BorderFactory.createTitledBorder("Generate Text"));

        inputTextField = new JTextField();
        JButton generateButton = new JButton("Generate with Bridge Words");
        generateButton.addActionListener(e -> generateNewText());

        generateTextPanel.add(inputTextField, BorderLayout.CENTER);
        generateTextPanel.add(generateButton, BorderLayout.SOUTH);

        // Shortest Path panel
        JPanel pathPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        pathPanel.setBorder(BorderFactory.createTitledBorder("Shortest Path"));

        JPanel sourcePanel = new JPanel(new BorderLayout(5, 5));
        sourcePanel.add(new JLabel("Source Word:"), BorderLayout.WEST);
        pathSourceField = new JTextField(10);
        sourcePanel.add(pathSourceField, BorderLayout.CENTER);

        JPanel targetPanel = new JPanel(new BorderLayout(5, 5));
        targetPanel.add(new JLabel("Target Word:"), BorderLayout.WEST);
        pathTargetField = new JTextField(10);
        targetPanel.add(pathTargetField, BorderLayout.CENTER);

        JButton findPathButton = new JButton("Find Shortest Path");
        findPathButton.addActionListener(e -> findShortestPath());

        pathPanel.add(sourcePanel);
        pathPanel.add(targetPanel);
        pathPanel.add(findPathButton);

        // PageRank panel
        JPanel pageRankPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        pageRankPanel.setBorder(BorderFactory.createTitledBorder("PageRank"));

        JPanel pageRankInputPanel = new JPanel(new BorderLayout(5, 5));
        pageRankInputPanel.add(new JLabel("Word (optional):"), BorderLayout.WEST);
        JTextField pageRankField = new JTextField(10);
        pageRankInputPanel.add(pageRankField, BorderLayout.CENTER);

        JButton pageRankButton = new JButton("Calculate PageRank");
        pageRankButton.addActionListener(e -> {
            String word = pageRankField.getText().trim();
            calculatePageRank(word);
        });

        pageRankPanel.add(pageRankInputPanel);
        pageRankPanel.add(pageRankButton);

        // Random Walk panel
        JPanel randomWalkPanel = new JPanel(new BorderLayout(5, 5));
        randomWalkPanel.setBorder(BorderFactory.createTitledBorder("Random Walk"));

        JButton randomWalkButton = new JButton("Perform Random Walk");
        randomWalkButton.addActionListener(e -> performRandomWalk());

        randomWalkPanel.add(randomWalkButton, BorderLayout.CENTER);

        // Add components to options panel
        optionsPanel.add(showGraphButton);
        optionsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        optionsPanel.add(bridgeWordsPanel);
        optionsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        optionsPanel.add(generateTextPanel);
        optionsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        optionsPanel.add(pathPanel);
        optionsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        optionsPanel.add(pageRankPanel);
        optionsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        optionsPanel.add(randomWalkPanel);

        // Add panels to control panel
        controlPanel.add(filePanel);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        controlPanel.add(optionsPanel);

        return controlPanel;
    }

    private JPanel createOutputPanel() {
        JPanel outputPanel = new JPanel(new BorderLayout(5, 5));
        outputPanel.setBorder(BorderFactory.createTitledBorder("Output"));

        // Create tabbed pane
        JTabbedPane tabbedPane = new JTabbedPane();

        // Text output tab
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(outputArea);

        // Graph visualization tab
        graphPanel = new JPanel(new BorderLayout());

        if (svgCanvas != null) {
            // Add scroll support to SVG canvas for large graphs
            JScrollPane svgScrollPane = new JScrollPane(svgCanvas);
            svgScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            svgScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

            graphPanel.add(svgScrollPane, BorderLayout.CENTER);

            // Add buttons for zoom control and saving
            JPanel graphControlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

            JButton zoomInButton = new JButton("+");
            zoomInButton.setToolTipText("Zoom In");
            zoomInButton.addActionListener(e -> zoomIn());

            JButton zoomOutButton = new JButton("-");
            zoomOutButton.setToolTipText("Zoom Out");
            zoomOutButton.addActionListener(e -> zoomOut());

            JButton resetZoomButton = new JButton("1:1");
            resetZoomButton.setToolTipText("Reset Zoom");
            resetZoomButton.addActionListener(e -> resetZoom());

            saveGraphButton = new JButton("Save Graph");
            saveGraphButton.setToolTipText("Save the current graph visualization");
            saveGraphButton.setEnabled(false);
            saveGraphButton.addActionListener(e -> saveGraph());

            graphControlPanel.add(zoomInButton);
            graphControlPanel.add(zoomOutButton);
            graphControlPanel.add(resetZoomButton);
            graphControlPanel.add(saveGraphButton);

            graphPanel.add(graphControlPanel, BorderLayout.SOUTH);
        } else {
            // If Batik is not available, show a message
            graphPanel.add(new JLabel("SVG visualization requires the Apache Batik library.", JLabel.CENTER), BorderLayout.CENTER);
        }

        tabbedPane.addTab("Text Output", scrollPane);
        tabbedPane.addTab("Graph Visualization", graphPanel);

        outputPanel.add(tabbedPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton clearButton = new JButton("Clear Output");
        clearButton.addActionListener(e -> outputArea.setText(""));
        buttonPanel.add(clearButton);

        outputPanel.add(buttonPanel, BorderLayout.SOUTH);

        return outputPanel;
    }
    private void openTextFile() {
        int returnVal = fileChooser.showOpenDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            currentFilePath = file.getAbsolutePath();

            try {
                graph = TextGraphAnalyzer.createGraphFromFile(currentFilePath);
                outputArea.setText("Graph created successfully from file: " + file.getName() + "\n");
                outputArea.append("Total words (nodes): " + graph.size() + "\n");

                // Generate the graph visualization immediately
                showGraphVisualization();

                JOptionPane.showMessageDialog(this,
                        "Text file loaded successfully!\n" +
                                "Words in graph: " + graph.size(),
                        "File Loaded",
                        JOptionPane.INFORMATION_MESSAGE);

            } catch (IOException e) {
                outputArea.setText("Error reading file: " + e.getMessage() + "\n");
                JOptionPane.showMessageDialog(this,
                        "Error loading file: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showGraph() {
        if (graph == null) {
            JOptionPane.showMessageDialog(this,
                    "Please load a text file first.",
                    "No Graph Available",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        outputArea.setText("===== Directed Graph =====\n");
        outputArea.append("Total words (nodes): " + graph.size() + "\n\n");

        // Sort words alphabetically for better readability
        java.util.List<String> sortedWords = new ArrayList<>(graph.getAllWords());
        Collections.sort(sortedWords);

        for (String word : sortedWords) {
            Map<String, Integer> neighbors = graph.getNeighbors(word);
            if (!neighbors.isEmpty()) {
                outputArea.append(word + " -> " + formatNeighbors(neighbors) + "\n");
            }
        }
    }

    private void showGraphVisualization() {
        if (graph == null) {
            JOptionPane.showMessageDialog(this,
                    "Please load a text file first.",
                    "No Graph Available",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        // 在这里调用showGraph()方法来显示文本信息
        showGraph();

        try {
            // Create a temporary SVG file
            currentSvgFile = File.createTempFile("graph_", ".svg");

            // 添加调试信息
            outputArea.append("Attempting to generate graph visualization...\n");
            outputArea.append("Temporary SVG file: " + currentSvgFile.getAbsolutePath() + "\n");

            // 检查Graphviz是否安装并显示信息
            if (!TextGraphAnalyzer.isGraphvizInstalled()) {
                outputArea.append("WARNING: Graphviz not found in system path!\n");
                JOptionPane.showMessageDialog(this,
                        "Graphviz is not installed or not found in system path.\n" +
                                "Please install Graphviz and ensure 'dot' command is available.\n\n" +
                                "Download: https://graphviz.org/download/",
                        "Graphviz Not Found",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            // 尝试使用简化版本的图形生成方法
            boolean success = generateSimpleGraph(currentSvgFile.getAbsolutePath());

            if (success && svgCanvas != null) {
                // Load the SVG into the canvas
                svgCanvas.setURI(currentSvgFile.toURI().toString());
                saveGraphButton.setEnabled(true);

                // Reset view
                resetZoom();

                // Switch to the Graph Visualization tab
                for (int i = 0; i < ((JTabbedPane)graphPanel.getParent()).getTabCount(); i++) {
                    if (((JTabbedPane)graphPanel.getParent()).getTitleAt(i).equals("Graph Visualization")) {
                        ((JTabbedPane)graphPanel.getParent()).setSelectedIndex(i);
                        break;
                    }
                }
            } else {
                outputArea.append("Failed to generate graph visualization.\n");
                outputArea.append("Please check if Graphviz is properly installed.\n");
            }
        } catch (IOException e) {
            outputArea.append("Error: " + e.getMessage() + "\n");
            JOptionPane.showMessageDialog(this,
                    "Error creating graph visualization: " + e.getMessage(),
                    "Visualization Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // 添加一个简化版本的图形生成方法
    private boolean generateSimpleGraph(String outputFile) {
        try {
            // 创建一个基本的DOT格式图表定义
            StringBuilder dotBuilder = new StringBuilder();
            dotBuilder.append("digraph TextGraph {\n");
            dotBuilder.append("  node [shape=box, style=filled, fillcolor=lightblue];\n");
            dotBuilder.append("  edge [color=gray];\n");

            // 添加所有单词及其连接
            for (String word : graph.getAllWords()) {
                // 添加节点
                dotBuilder.append("  \"").append(word).append("\";\n");

                // 添加边
                Map<String, Integer> neighbors = graph.getNeighbors(word);
                for (Map.Entry<String, Integer> entry : neighbors.entrySet()) {
                    String neighbor = entry.getKey();
                    int weight = entry.getValue();
                    dotBuilder.append("  \"").append(word).append("\" -> \"")
                            .append(neighbor).append("\" [label=\"").append(weight).append("\"];\n");
                }
            }

            dotBuilder.append("}\n");

            // 将DOT内容写入临时文件
            File dotFile = File.createTempFile("graph_", ".dot");
            Files.writeString(dotFile.toPath(), dotBuilder.toString());

            // 使用ProcessBuilder来执行dot命令
            ProcessBuilder pb = new ProcessBuilder(
                    "dot", "-Tsvg", "-o", outputFile, dotFile.getAbsolutePath());
            Process process = pb.start();

            // 等待进程完成
            int exitCode = process.waitFor();

            // 删除临时DOT文件
            dotFile.delete();

            // 检查输出文件是否存在且非空
            File outFile = new File(outputFile);
            return exitCode == 0 && outFile.exists() && outFile.length() > 0;

        } catch (Exception e) {
            outputArea.append("Error generating graph: " + e.getMessage() + "\n");
            return false;
        }
    }

    private void zoomIn() {
        if (svgCanvas != null) {
            AffineTransform at = svgCanvas.getRenderingTransform();
            at.scale(1.2, 1.2);
            svgCanvas.setRenderingTransform(at);
        }
    }

    private void zoomOut() {
        if (svgCanvas != null) {
            AffineTransform at = svgCanvas.getRenderingTransform();
            at.scale(0.8, 0.8);
            svgCanvas.setRenderingTransform(at);
        }
    }

    private void resetZoom() {
        if (svgCanvas != null) {
            svgCanvas.setRenderingTransform(new AffineTransform());
        }
    }

    private String formatNeighbors(Map<String, Integer> neighbors) {
        return neighbors.entrySet().stream()
                .map(e -> e.getKey() + " (weight: " + e.getValue() + ")")
                .collect(Collectors.joining(", "));
    }
    private void queryBridgeWords() {
        if (graph == null) {
            JOptionPane.showMessageDialog(this,
                    "Please load a text file first.",
                    "No Graph Available",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String word1 = word1Field.getText().trim();
        String word2 = word2Field.getText().trim();

        if (word1.isEmpty() || word2.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter both words.",
                    "Input Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String result = TextGraphAnalyzer.queryBridgeWords(graph, word1, word2);
        outputArea.setText(result + "\n");
    }

    private void generateNewText() {
        if (graph == null) {
            JOptionPane.showMessageDialog(this,
                    "Please load a text file first.",
                    "No Graph Available",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String inputText = inputTextField.getText().trim();

        if (inputText.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter text to process.",
                    "Input Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String newText = TextGraphAnalyzer.generateNewText(graph, inputText);
        outputArea.setText("Original text:\n" + inputText + "\n\n");
        outputArea.append("Text with bridge words:\n" + newText + "\n");
    }

    private void findShortestPath() {
        if (graph == null) {
            JOptionPane.showMessageDialog(this,
                    "Please load a text file first.",
                    "No Graph Available",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sourceWord = pathSourceField.getText().trim();
        String targetWord = pathTargetField.getText().trim();

        if (sourceWord.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter at least the source word.",
                    "Input Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (targetWord.isEmpty()) {
            // Calculate shortest paths from sourceWord to all other words
            outputArea.setText("Shortest paths from " + sourceWord + " to all other words:\n\n");

            List<String> destinations = new ArrayList<>(graph.getAllWords());
            Collections.sort(destinations);

            for (String target : destinations) {
                if (!target.equals(sourceWord)) {
                    outputArea.append(TextGraphAnalyzer.calcShortestPath(graph, sourceWord, target) + "\n\n");
                }
            }
        } else {
            String result = TextGraphAnalyzer.calcShortestPath(graph, sourceWord, targetWord);
            outputArea.setText(result + "\n");
        }
    }

    private void calculatePageRank(String word) {
        if (graph == null) {
            JOptionPane.showMessageDialog(this,
                    "Please load a text file first.",
                    "No Graph Available",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (word.isEmpty()) {
            // Calculate PageRank for all words and sort by value
            Map<String, Double> allRanks = new HashMap<>();
            outputArea.setText("Calculating PageRank values for all words...\n\n");

            for (String w : graph.getAllWords()) {
                allRanks.put(w, TextGraphAnalyzer.calcPageRank(graph, w));
            }

            // Sort by PageRank value (descending)
            List<Map.Entry<String, Double>> sortedRanks = new ArrayList<>(allRanks.entrySet());
            sortedRanks.sort(Map.Entry.<String, Double>comparingByValue().reversed());

            outputArea.append("PageRank values for all words (sorted):\n");
            for (Map.Entry<String, Double> entry : sortedRanks) {
                outputArea.append(String.format("%s: %.4f\n", entry.getKey(), entry.getValue()));
            }
        } else {
            Double pageRank = TextGraphAnalyzer.calcPageRank(graph, word);
            if (pageRank >= 0) {
                outputArea.setText(String.format("PageRank value for '%s': %.4f\n", word, pageRank));
            } else {
                outputArea.setText("Word '" + word + "' not found in the graph.\n");
            }
        }
    }

    private void performRandomWalk() {
        if (graph == null) {
            JOptionPane.showMessageDialog(this,
                    "Please load a text file first.",
                    "No Graph Available",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String walkResult = TextGraphAnalyzer.randomWalk(graph);
        outputArea.setText("Random Walk:\n\n");
        outputArea.append(walkResult + "\n");

        // Offer to save
        int choice = JOptionPane.showConfirmDialog(this,
                "Do you want to save this random walk to a file?",
                "Save Random Walk",
                JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            JFileChooser saveChooser = new JFileChooser();
            saveChooser.setDialogTitle("Save Random Walk");
            saveChooser.setSelectedFile(new File("random_walk_" + System.currentTimeMillis() + ".txt"));

            int saveChoice = saveChooser.showSaveDialog(this);
            if (saveChoice == JFileChooser.APPROVE_OPTION) {
                try {
                    Files.writeString(saveChooser.getSelectedFile().toPath(), walkResult);
                    JOptionPane.showMessageDialog(this,
                            "Random walk saved successfully!",
                            "File Saved",
                            JOptionPane.INFORMATION_MESSAGE);
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(this,
                            "Error saving file: " + e.getMessage(),
                            "Save Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
    private void saveGraph() {
        if (graph == null || currentSvgFile == null || !currentSvgFile.exists()) {
            JOptionPane.showMessageDialog(this,
                    "No graph visualization is currently available.",
                    "No Graph Available",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Create format options
        String[] formats = {"SVG (Scalable Vector Graphics)", "PNG (Portable Network Graphics)", "PDF (Portable Document Format)", "DOT (Graphviz Source)"};
        String format = (String) JOptionPane.showInputDialog(
                this,
                "Select output format:",
                "Save Graph",
                JOptionPane.QUESTION_MESSAGE,
                null,
                formats,
                formats[0]);

        if (format == null) return; // User canceled

        // Determine file extension
        String extension;
        if (format.startsWith("SVG")) extension = "svg";
        else if (format.startsWith("PNG")) extension = "png";
        else if (format.startsWith("PDF")) extension = "pdf";
        else extension = "dot";

        // Configure file chooser
        JFileChooser saveChooser = new JFileChooser();
        saveChooser.setDialogTitle("Save Graph");
        saveChooser.setSelectedFile(new File("graph_" + System.currentTimeMillis() + "." + extension));

        // Show save dialog
        int saveChoice = saveChooser.showSaveDialog(this);
        if (saveChoice == JFileChooser.APPROVE_OPTION) {
            String outputPath = saveChooser.getSelectedFile().getAbsolutePath();

            // Ensure file has correct extension
            if (!outputPath.toLowerCase().endsWith("." + extension)) {
                outputPath += "." + extension;
            }

            boolean success;

            // If SVG is selected and we already have an SVG file, just copy it
            if (extension.equals("svg") && currentSvgFile != null) {
                try {
                    Files.copy(currentSvgFile.toPath(), new File(outputPath).toPath(), StandardCopyOption.REPLACE_EXISTING);
                    success = true;
                } catch (IOException e) {
                    success = false;
                    JOptionPane.showMessageDialog(this,
                            "Error saving SVG file: " + e.getMessage(),
                            "Save Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            } else {
                // For other formats, use the graph to generate the file
                if (!extension.equals("dot") && !TextGraphAnalyzer.isGraphvizInstalled()) {
                    JOptionPane.showMessageDialog(this,
                            "Graphviz is not installed. Saving as DOT format instead.",
                            "Graphviz Not Found",
                            JOptionPane.WARNING_MESSAGE);
                    outputPath = outputPath.replaceAll("\\.[^.]+$", ".dot");
                    success = TextGraphAnalyzer.saveGraphToFile(graph, outputPath, "dot");
                } else {
                    // Use enhanced version for better quality
                    success = TextGraphAnalyzer.saveEnhancedGraphToFile(graph, outputPath, extension);
                }
            }

            if (success) {
                JOptionPane.showMessageDialog(this,
                        "Graph saved successfully to:\n" + outputPath,
                        "File Saved",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Failed to save graph.",
                        "Save Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        try {
            // Set Look and Feel to system default
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Create and display the GUI
        SwingUtilities.invokeLater(() -> new TextGraphAnalyzerGUI());
    }
}