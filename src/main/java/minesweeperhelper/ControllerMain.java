package minesweeperhelper;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ControllerMain {

    private Logger log = LogManager.getLogger(this.getClass());

    private Stage stage;

    private Button showHelpButton;

    private int stageSmallWidth = 200;
    private int stageSmallHeight = 100;

    private Stage helpScreenStage;

    protected void init(Stage stage, StackPane rootElement, Stage helpScreenStage,
            ControllerHelpScreen controllerHelpScreen, boolean debug) {

        this.stage = stage;

        this.helpScreenStage = helpScreenStage;

        Scene scene = new Scene(rootElement, stageSmallWidth, stageSmallHeight);
        scene.setFill(Color.TRANSPARENT);

        this.showHelpButton = new Button("Press and hold to get  help");

        EventHandler<MouseEvent> showButtonMouseClickHandler = event -> {
            if (MouseButton.PRIMARY.equals(event.getButton())) {
                new HelpScreen().showHelpScreen(controllerHelpScreen, debug);
            }
        };

        EventHandler<MouseEvent> showButtonMouseReleaseHandler = event -> {
            if (MouseButton.PRIMARY.equals(event.getButton())) {
                //todo uncomment
                //controllerHelpScreen.getStage().hide();
            }
        };

        this.showHelpButton.setOnMousePressed(showButtonMouseClickHandler);
        this.showHelpButton.setOnMouseReleased(showButtonMouseReleaseHandler);

        rootElement.getChildren().add(this.showHelpButton);

        stage.setScene(scene);
        stage.sizeToScene();
        stage.show();

        stage.setMinWidth(stage.getWidth());
        stage.setMinHeight(stage.getHeight());

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

}