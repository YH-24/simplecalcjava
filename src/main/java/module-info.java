module simplecalc {
    requires javafx.controls;
    requires javafx.fxml;

    opens gui.simple.calculator;
    exports gui.simple.calculator;

}