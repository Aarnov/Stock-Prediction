package org.example;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.labels.CategoryToolTipGenerator;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.category.CategoryDataset;

import javax.swing.*;
import java.awt.*;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;

public class StockPredictionGraph {
    private DefaultCategoryDataset dataset = new DefaultCategoryDataset();
    private JFrame frame;
    private JPanel introPanel, graphPanel;
    private String lastHistoricalDate = "";
    private double lastHistoricalPrice = 0.0;

    public StockPredictionGraph() {
        frame = new JFrame("Stock Prediction App");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLayout(new CardLayout());

        // Initialize the intro page
        initIntroPage();

        frame.setVisible(true);
    }

    private void initIntroPage() {
        // Set a background color or image for the intro page to fill the space visually
        introPanel = new JPanel(new BorderLayout());
        introPanel.setBackground(new Color(240, 240, 240)); // Light grey background for the panel

        // Welcome Label with larger font and enhanced styling
        JLabel welcomeLabel = new JLabel("Welcome to My Stock Prediction App", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 36));
        welcomeLabel.setForeground(new Color(34, 34, 34)); // Dark color for the text
        welcomeLabel.setBorder(BorderFactory.createEmptyBorder(50, 10, 50, 10)); // Ample padding
        welcomeLabel.setBackground(new Color(211, 211, 211)); // Soft blue background
        welcomeLabel.setOpaque(true); // Make the label's background visible
        introPanel.add(welcomeLabel, BorderLayout.NORTH);

        // Center Panel for Stock Selection - Centering and structuring layout
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new GridBagLayout()); // GridBagLayout for better space distribution
        centerPanel.setBackground(Color.WHITE); // White background for center section
        centerPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40)); // Padding around the panel

        // Constraints for GridBagLayout to align components dynamically
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 0, 20, 0); // Add some space between components

        // Stock Selection Label (Centered and large)
        JLabel selectStockLabel = new JLabel("Select a Stock:");
        selectStockLabel.setFont(new Font("Arial", Font.PLAIN, 20));
        selectStockLabel.setForeground(new Color(70, 70, 70));
        centerPanel.add(selectStockLabel, gbc);

        // Enhanced JComboBox with modern styling (larger and eye-catching)
        gbc.gridy++;
        JComboBox<String> stockSelector = new JComboBox<>(new String[]{"Nifty50", "Nifty100", "Nifty200"});
        stockSelector.setFont(new Font("Arial", Font.PLAIN, 18));
        stockSelector.setPreferredSize(new Dimension(200, 40)); // Larger size for better interaction
        stockSelector.setMaximumSize(new Dimension(300, 40));
        stockSelector.setBackground(Color.WHITE);
        stockSelector.setForeground(Color.DARK_GRAY);
        stockSelector.setBorder(BorderFactory.createLineBorder(new Color(180, 180, 180), 2));
        centerPanel.add(stockSelector, gbc);

        // Spacer between combo box and button
        gbc.gridy++;
        centerPanel.add(Box.createVerticalStrut(30), gbc);

        // Select Button with grey color and darker grey on hover/click
        JButton selectButton = new JButton("Select Stock");
        selectButton.setFont(new Font("Arial", Font.BOLD, 18));
        selectButton.setPreferredSize(new Dimension(180, 50));
        selectButton.setBackground(new Color(169, 169, 169)); // Grey background
        selectButton.setForeground(Color.WHITE);
        selectButton.setFocusPainted(false);
        selectButton.setBorder(new javax.swing.border.LineBorder(Color.GRAY, 2, true)); // Rounded border

        // Hover and click effect for button (darker grey)
        selectButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                selectButton.setBackground(new Color(128, 128, 128)); // Darker grey on hover
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                selectButton.setBackground(new Color(169, 169, 169)); // Original grey color on exit
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                selectButton.setBackground(new Color(105, 105, 105)); // Even darker grey on click
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                selectButton.setBackground(new Color(128, 128, 128)); // Return to hover color after click
            }
        });

        // Button action to handle stock selection and transition to graph page
        selectButton.addActionListener(e -> {
            String selectedStock = (String) stockSelector.getSelectedItem();
            loadSelectedStockData(selectedStock);
            initGraphPage(selectedStock);
            switchToGraphPage();
        });

        gbc.gridy++;
        centerPanel.add(selectButton, gbc);

        // Add the center panel to the introPanel
        introPanel.add(centerPanel, BorderLayout.CENTER);

        // Add the introPanel to the frame
        frame.add(introPanel, "IntroPage");
    }



    private void initGraphPage(String stockName) {
        graphPanel = new JPanel(new BorderLayout());

        JFreeChart chart = ChartFactory.createLineChart(
                stockName + " Stock Prices", "Date", "Close Price",
                dataset, PlotOrientation.VERTICAL, true, true, false);

        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        adjustYAxisRange(plot);

        LineAndShapeRenderer renderer = new LineAndShapeRenderer(true, true);
        plot.setRenderer(renderer);
        renderer.setDefaultToolTipGenerator(new CustomToolTipGenerator());

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(800, 600));
        graphPanel.add(chartPanel, BorderLayout.CENTER);

        JButton backButton = new JButton("Back to Stock Selection");
        backButton.addActionListener(e -> switchToIntroPage());
        JButton showPredictionButton = new JButton("Show Prediction");
        showPredictionButton.addActionListener(e -> showPredictedData(stockName));


        JPanel bottomPanel = new JPanel();
        bottomPanel.add(backButton);
        bottomPanel.add(showPredictionButton);

        graphPanel.add(bottomPanel, BorderLayout.SOUTH);
        frame.add(graphPanel, "GraphPage");
    }

    private void switchToIntroPage() {
        CardLayout cl = (CardLayout) frame.getContentPane().getLayout();
        cl.show(frame.getContentPane(), "IntroPage");
    }

    private void switchToGraphPage() {
        CardLayout cl = (CardLayout) frame.getContentPane().getLayout();
        cl.show(frame.getContentPane(), "GraphPage");
    }

    private void loadSelectedStockData(String stockName) {
        String filePath = switch (stockName) {
            case "Nifty50" -> "src/main/resources/nifty50_stock_data.csv";
            case "Nifty100" -> "src/main/resources/nifty100_stock_data.csv";
            default -> "src/main/resources/nifty200_stock_data.csv";
        };

        dataset.clear();  // Clear the existing dataset
        loadCSVData(filePath);  // Load the selected stock data
    }

    private void loadCSVData(String filePath) {
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            reader.readNext(); // Skip header
            String[] line;
            while ((line = reader.readNext()) != null) {
                String date = line[0];
                double closePrice = Double.parseDouble(line[4]); // Assuming close price is in column 5
                dataset.addValue(closePrice, "Historical Price", date);
                lastHistoricalDate = date;
                lastHistoricalPrice = closePrice;
            }
        } catch (IOException | CsvValidationException e) {
            e.printStackTrace();
        }
    }

    private void showPredictedData(String stockName) {
        String predictionFilePath = switch (stockName) {
            case "Nifty50" -> "src/main/resources/stock_predictions_nifty50.csv";
            case "Nifty100" -> "src/main/resources/stock_predictions_nifty100.csv";
            default -> "src/main/resources/stock_predictions_nifty200.csv";
        };

        try (CSVReader reader = new CSVReader(new FileReader(predictionFilePath))) {
            reader.readNext(); // Skip header
            dataset.addValue(lastHistoricalPrice, "Predicted Price", lastHistoricalDate);

            String[] line;
            while ((line = reader.readNext()) != null) {
                String date = line[0];
                double predictedPrice = Double.parseDouble(line[1]);
                dataset.addValue(predictedPrice, "Predicted Price", date);
            }
        } catch (IOException | CsvValidationException e) {
            e.printStackTrace();
        }
    }

    private void adjustYAxisRange(CategoryPlot plot) {
        double minPrice = Double.MAX_VALUE;
        double maxPrice = Double.MIN_VALUE;

        for (int i = 0; i < dataset.getRowCount(); i++) {
            for (int j = 0; j < dataset.getColumnCount(); j++) {
                Number value = dataset.getValue(i, j);
                if (value != null) {
                    double price = value.doubleValue();
                    if (price < minPrice) minPrice = price;
                    if (price > maxPrice) maxPrice = price;
                }
            }
        }

        double buffer = (maxPrice - minPrice) * 0.5;
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setRange(minPrice - buffer, maxPrice + buffer);
    }

    private static class CustomToolTipGenerator implements CategoryToolTipGenerator {
        private final DecimalFormat df = new DecimalFormat("#.##");

        @Override
        public String generateToolTip(CategoryDataset dataset, int row, int column) {
            String seriesName = (String) dataset.getRowKey(row);
            String date = (String) dataset.getColumnKey(column);
            Number value = dataset.getValue(row, column);
            return String.format("%s: %s - %s", seriesName, date, df.format(value.doubleValue()));
        }
    }

    public static void main(String[] args) {
        new StockPredictionGraph();
    }
}
