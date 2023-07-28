package gui.simple.calculator;

import javafx.application.Application;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class CalculatorApp extends Application {
    private final List<String> ops = Arrays.asList("+", "-", "*", "/", ".");
    private final StringBuilder currentExpression = new StringBuilder();
    private final SwitchButton switchButton = new SwitchButton();
    private final Calculator calc = new Calculator();
    private final GridPane root = new GridPane();
    private final Text resultField = new Text();
    private GridPane buttonGrid;


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Simple Calculator");
        primaryStage.setResizable(false);
        primaryStage.setMinWidth(300);
        primaryStage.setMinHeight(400);


        resultField.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        resultField.setFill(Color.WHITE);

        buttonGrid = createButtonGrid();

        switchButton.switchOnProperty().addListener((obs, oldVal, newVal) -> updateTheme());

        root.setPadding(new Insets(1));
        root.setHgap(3);
        root.setVgap(6);
        updateTheme(); // Apply the initial theme
        root.setAlignment(Pos.CENTER);
        switchButton.setAlignment(Pos.BOTTOM_CENTER);

        root.add(resultField, 0, 0);
        root.add(buttonGrid, 0, 1);
        root.add(switchButton, 0, 2);

        Scene scene = new Scene(root);
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                try {
                    String result = evaluateExpression();
                    resultField.setText(result);
                } catch (Exception ex) {
                    resultField.setText("Error");
                }
                currentExpression.setLength(0);
            }
        });
        primaryStage.setScene(scene);

        primaryStage.setWidth(300); // Set window width
        primaryStage.setHeight(400); // Set window height
        primaryStage.show();
    }

    private GridPane createButtonGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(5);
        grid.setVgap(5);

        String[][] buttonLabels = {
                {"7", "8", "9", "/"},
                {"4", "5", "6", "*"},
                {"1", "2", "3", "-"},
                {"0", ".", "=", "+"}
        };

        for (int i = 0; i < buttonLabels.length; i++) {
            for (int j = 0; j < buttonLabels[i].length; j++) {
                String label = buttonLabels[i][j];
                Button button = new Button(label);
                button.setMinSize(50, 50);
                button.setOnAction(e -> handleButtonAction(label));
                button.setStyle("-fx-background-color: " + (switchButton.isSwitchedOn() ? "#444444" : "#CCCCCC") + "; -fx-text-fill: " + (switchButton.isSwitchedOn() ? "white" : "black") + ";"); // Set button style
                button.setFocusTraversable(false);
                grid.add(button, j, i);
            }
        }

        return grid;
    }

    private void handleButtonAction(String label) {
        if (label.equals("=")) {
            try {
                String result = evaluateExpression();
                resultField.setText(result);
            } catch (Exception ex) {
                resultField.setText(ex.getMessage());
            }
            currentExpression.setLength(0);
        } else if (isOperator(label)) {
            int length = currentExpression.length();
            if (length > 0) {
                char lastChar = currentExpression.charAt(length - 1);
                if (isOperator(String.valueOf(lastChar))) {
                    currentExpression.setLength(length - 1); // Remove the last operator
                }
            }
            currentExpression.append(label);
            resultField.setText(currentExpression.toString());
        } else {
            currentExpression.append(label);
            resultField.setText(currentExpression.toString());
        }
    }

    private boolean isOperator(String label) {
        return label.equals("+") || label.equals("-") || label.equals("*") || label.equals("/") || label.equals(".");
    }

    private String evaluateExpression() {
        if (currentExpression.toString().isEmpty()) {
            return "0";
        }
        return formatDouble(calc.evaluate(currentExpression.toString()));
    }

    private static String formatDouble(double value) {
        DecimalFormat df = new DecimalFormat("#.###");
        String formatted = df.format(value);
        return formatted.endsWith(".0") ? formatted.replace(".0", "") : formatted;
    }


    private void updateTheme() {
        boolean isDarkTheme = switchButton.isSwitchedOn();
        resultField.setFill(isDarkTheme ? Color.WHITE : Color.BLACK);
        resultField.setStyle("-fx-background-color: " + (isDarkTheme ? "#333333" : "#F5F5F5") + ";");
        buttonGrid.setStyle("-fx-background-color: " + (isDarkTheme ? "#333333" : "#F5F5F5") + ";");
        buttonGrid.getChildren().forEach(node -> {
            if (node instanceof Button) {
                Button button = (Button) node;
                button.setStyle("-fx-background-color: " + (isDarkTheme ? "#444444" : "#CCCCCC") + "; -fx-text-fill: " + (isDarkTheme ? "white" : "black") + ";");
            }
        });
        root.setStyle("-fx-background-color: " + (isDarkTheme ? "#333333" : "#F5F5F5") + ";");
    }
}

class Calculator {

    public double evaluate(String expression) {
        return new Object() {
            int pos = -1, ch;

            void nextChar() {
                ch = (++pos < expression.length()) ? expression.charAt(pos) : -1;
            }

            boolean isDigit(char c) {
                return c >= '0' && c <= '9';
            }

            double parseNumber() {
                StringBuilder sb = new StringBuilder();
                while (isDigit((char) ch) || ch == '.') {
                    sb.append((char) ch);
                    nextChar();
                }
                return Double.parseDouble(sb.toString());
            }

            void skipWhitespace() {
                while (Character.isWhitespace(ch)) {
                    nextChar();
                }
            }

            double eval() {
                nextChar();
                double value = parseExpression();
                if (pos < expression.length()) {
                    throw new RuntimeException("Unexpected: " + (char) ch);
                }
                return value;
            }

            double parseExpression() {
                double value = parseTerm();
                while (true) {
                    skipWhitespace();
                    if (ch == '+') {
                        nextChar();
                        value += parseTerm();
                    } else if (ch == '-') {
                        nextChar();
                        value -= parseTerm();
                    } else {
                        return value;
                    }
                }
            }

            double parseTerm() {
                double value = parseFactor();
                while (true) {
                    skipWhitespace();
                    if (ch == '*') {
                        nextChar();
                        value *= parseFactor();
                    } else if (ch == '/') {
                        nextChar();
                        value /= parseFactor();
                    } else {
                        return value;
                    }
                }
            }

            double parseFactor() {
                skipWhitespace();
                if (ch == '(') {
                    nextChar();
                    double value = parseExpression();
                    if (ch != ')') {
                        throw new RuntimeException("Missing closing parenthesis");
                    }
                    nextChar();
                    return value;
                } else if (ch == '-') {
                    nextChar();
                    return -parseFactor();
                } else if (ch == '+') {
                    nextChar();
                    return parseFactor();
                } else {
                    return parseNumber();
                }
            }
        }.eval();
    }
}

class SwitchButton extends Label {
    // credit to https://stackoverflow.com/a/17494262
    private final SimpleBooleanProperty switchedOn = new SimpleBooleanProperty(false);

    public SwitchButton() {
        Button switchBtn = new Button();
        switchBtn.setPrefWidth(40);
        switchBtn.setOnAction(t -> switchedOn.set(!switchedOn.get()));
        switchBtn.setFocusTraversable(false);

        setGraphic(switchBtn);

        switchedOn.addListener((ov, t, t1) -> {
            if (t1) {
                setText("DARK");
                setStyle("-fx-background-color: #323232;-fx-text-fill:white; -fx-background-radius: 5; -fx-border-color: white;");
                setContentDisplay(ContentDisplay.RIGHT);
            } else {
                setText("LITE");
                setStyle("-fx-background-color: #d4d4d4;-fx-text-fill:black; -fx-background-radius: 5; -fx-border-color: black;");
                setContentDisplay(ContentDisplay.LEFT);
            }
        });

        switchedOn.set(true);
    }

    public SimpleBooleanProperty switchOnProperty() {
        return switchedOn;
    }

    public boolean isSwitchedOn() {
        return switchedOn.get();
    }
}