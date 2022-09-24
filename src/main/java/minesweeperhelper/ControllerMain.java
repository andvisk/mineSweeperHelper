package minesweeperhelper;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

public class ControllerMain {

    private Logger log = LogManager.getLogger(this.getClass());

    // GRID
    public static final int MIN_WIDTH = 9;
    public static final int MIN_HEIGHT = 9;
    public static final int TOLLERANCE_IN_PERCENT = 35; // x/2 to one direction

    private Stage stage;

    private VBox buttonBox;
    private VBox statusBox;
    private HBox layoutBox;
    private Button showHelpButton;
    private Button closeHelpScreenButton;
    private Button exitButton;
    private ProgressIndicator progressIndicator;

    private int stageSmallWidth = 250;
    private int stageSmallHeight = 100;

    private Stage helpScreenStage;
    private ControllerHelpScreen controllerHelpScreen;

    protected void init(Stage stage, StackPane rootElement) {

        this.stage = stage;

        Scene scene = new Scene(rootElement, stageSmallWidth, stageSmallHeight);
        scene.setFill(Color.TRANSPARENT);

        buttonBox = new VBox();
        statusBox = new VBox();
        layoutBox = new HBox();

        int buttonBoxWidth = 150;

        showHelpButton = new Button("Press to get help");
        showHelpButton.setMinWidth(buttonBoxWidth);
        buttonBox.getChildren().add(showHelpButton);

        closeHelpScreenButton = new Button("Close help screen");
        closeHelpScreenButton.setMinWidth(buttonBoxWidth);
        closeHelpScreenButton.setDisable(true);
        buttonBox.getChildren().add(closeHelpScreenButton);

        exitButton = new Button("Exit");
        exitButton.setMinWidth(buttonBoxWidth);
        buttonBox.getChildren().add(exitButton);

        this.closeHelpScreenButton.setOnAction((event) -> {
            controllerHelpScreen.getStage().hide();
            closeHelpScreenButton.setDisable(true);
        });

        this.exitButton.setOnAction((event) -> {
            controllerHelpScreen.getStage().close();
            stage.close();
        });

        progressIndicator = new ProgressIndicator();
        VBox.setMargin(progressIndicator, new Insets(10, 0, 0, 10));
        statusBox.getChildren().add(progressIndicator);
        progressIndicator.visibleProperty().set(false);

        layoutBox.getChildren().add(buttonBox);
        layoutBox.getChildren().add(statusBox);

        rootElement.getChildren().add(layoutBox);

        stage.setScene(scene);
        stage.sizeToScene();
        stage.show();

        stage.setMinWidth(stage.getWidth());
        stage.setMinHeight(stage.getHeight());

        helpScreenStage = initHelpScreenStage();

        EventHandler<MouseEvent> showButtonMouseClickHandler = event -> {
            if (MouseButton.PRIMARY.equals(event.getButton())) {

                Mat screenShot = HelpScreen.getScreenShot(controllerHelpScreen);

                ProcessingService service = new ProcessingService(screenShot, MIN_WIDTH, MIN_HEIGHT, TOLLERANCE_IN_PERCENT);
                service.setOnScheduled(e -> progressIndicator.visibleProperty().set(true));
                service.setOnSucceeded(e -> {
                    Mat helpScreenMat = service.getValue();
                    if (helpScreenMat != null) {
                        HelpScreen.showHelpScreen(helpScreenMat, controllerHelpScreen);
                        closeHelpScreenButton.setDisable(false);
                    }
                    progressIndicator.visibleProperty().set(false);
                });
                service.restart();
            }
        };

        this.showHelpButton.setOnMousePressed(showButtonMouseClickHandler);
    }

    private Stage initHelpScreenStage() {
        Stage helpScreenStage = new Stage();
        helpScreenStage.initStyle(StageStyle.TRANSPARENT);

        helpScreenStage.setMaximized(true);

        StackPane rootElementHelpScreen = new StackPane();
        rootElementHelpScreen.setAlignment(Pos.TOP_LEFT);
        rootElementHelpScreen.setBackground(Background.EMPTY);

        controllerHelpScreen = new ControllerHelpScreen();
        controllerHelpScreen.init(helpScreenStage, rootElementHelpScreen, closeHelpScreenButton);

        return helpScreenStage;
    }

    public void updateImageView(ImageView view, javafx.scene.image.Image image) {
        onFXThread(view.imageProperty(), image);
    }

    public static <T> void onFXThread(final ObjectProperty<T> property, final T value) {
        Platform.runLater(() -> {
            property.set(value);
        });
    }

    public Stage getStage() {
        return stage;
    }

    public Button getHelpButton() {
        return showHelpButton;
    }

    public void stop() {
        helpScreenStage.close();
    }

    public ProgressIndicator getProgressIndicator() {
        return progressIndicator;
    }

    public Button getCloseHelpScreenButton() {
        return closeHelpScreenButton;
    }

}