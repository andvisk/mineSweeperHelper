package minesweeperhelper;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ControllerHelpScreen {

    private Logger log = LogManager.getLogger(this.getClass());

    private ImageView imageView;

    private StackPane rootElement;

    private Stage stage;

    protected void init(Stage stage, StackPane rootElement, Button closeHelpScreenButton) {

        this.rootElement = rootElement;
        this.stage = stage;

        Rectangle2D screenBounds = Screen.getPrimary().getBounds();

        Scene scene = new Scene(rootElement, screenBounds.getWidth(), screenBounds.getHeight());

        imageView = new ImageView();
        rootElement.getChildren().add(imageView);

        EventHandler<MouseEvent> showButtonMouseReleaseHandler = event -> {
            if (MouseButton.PRIMARY.equals(event.getButton())) {
                stage.hide();
                closeHelpScreenButton.setDisable(true);
            }
        };

        imageView.setOnMousePressed(showButtonMouseReleaseHandler);

        stage.setScene(scene);
        stage.sizeToScene();
        stage.show();

        stage.setMinWidth(stage.getWidth());
        stage.setMinHeight(stage.getHeight());

        stage.hide();

    }

    public StackPane getRootElement(){
        return rootElement;
    }

    public void updateImageView(ImageView view, javafx.scene.image.Image image) {
        Platform.runLater(() -> {
            view.imageProperty().set(image);
        });
    }

    public ImageView getImageView() {
        return imageView;
    }

    public Stage getStage(){
        return this.stage;
    }

}