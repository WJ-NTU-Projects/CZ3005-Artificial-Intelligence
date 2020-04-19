import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.TextAlignment;

public class UIButton {
    private final StackPane parent;
    private final Rectangle rectangle;
    private ImageView imageView;

    /**
     * CREATES A NEW IMAGE BUTTON SPECIFIC TO EACH STAGE DISPLAYING A SOLUTION FROM PROLOG
     * @param w BUTTON WIDTH
     * @param h BUTTON HEIGHT
     * @param stage THE CURRENT STAGE THAT DISPLAYS THIS BUTTON
     * @param internalString UNPROCESSED SOLUTION STRING FOR INTERNAL USE
     * @param displayString PROCESSED SOLUTION STRING TO BE DISPLAYED
     */
    public UIButton(double w, double h, String stage, String internalString, String displayString) {
        boolean withImage;

        // LOOKS FOR AN IMAGE CORRESPONDING TO THE STAGE AND INTERNAL STRING PROVIDED.
        // NOT ALL STAGES AND SOLUTIONS HAVE IMAGES, SO EXCEPTIONS WILL BE THROWN.
        // WHEN EXCEPTIONS ARE CAUGHT, WITHIMAGE FLAG IS SET TO FALSE SO THE IMAGEVIEW IS NOT ADDED TO THE PARENT VIEW.
        try {
            imageView = new ImageView();
            imageView.setImage(new Image(stage + "_" + internalString + ".jpg"));
            imageView.setFitWidth(h - 1);
            imageView.setFitHeight(h - 1);
            imageView.setMouseTransparent(true);
            withImage = true;
        } catch (Exception e) {
            withImage = false;
        }

        // BUTTON BACKGROUND
        rectangle = new Rectangle();
        rectangle.setWidth(w);
        rectangle.setHeight(h);
        rectangle.setFill(Color.rgb(200, 200, 200));
        rectangle.setStroke(Color.DARKGRAY);
        rectangle.setId("unselected");

        // BUTTON LABEL
        Label label = new Label();
        label.setText(displayString);
        label.setStyle("-fx-font-size: 14;");
        label.setPadding(new Insets(8.0, 8.0, 8.0, 8.0));
        label.setWrapText(true);
        label.setTextAlignment(TextAlignment.CENTER);
        label.setMouseTransparent(true);

        // AS IMAGE AND LABEL IS DISPLAYED SIDE BY SIDE
        // A CONTAINER IS CREATED FOR THE LABEL WITH THE WIDTH OF (BUTTON WIDTH - IMAGE WIDTH)
        // SUCH THAT THE LABEL CAN BE CENTERED IN THE REMAINING SPACE
        if (withImage) {
            VBox vbox = new VBox();
            vbox.setPrefWidth(w - h);
            vbox.getChildren().add(label);
            vbox.setAlignment(Pos.CENTER);

            HBox hbox = new HBox();
            hbox.setAlignment(Pos.CENTER);
            hbox.getChildren().add(imageView);
            hbox.getChildren().add(vbox);

            parent = new StackPane(rectangle, hbox);
        } else {
            parent = new StackPane(rectangle, label);
        }

        parent.prefWidth(w);
        parent.prefHeight(h);
        parent.setAlignment(Pos.CENTER);
    }

    /**
     * RETURNS THE PARENT STACKPANE VIEW SUCH THAT EVENT HANDLERS CAN BE SET.
     * @return PARENT STACKPANE VIEW
     */
    public StackPane getParent() {
        return this.parent;
    }

    /**
     * RETURNS THE ID OF THE BUTTON
     * @return EITHER "UNSELECTED" OR "SELECTED"
     */
    public String getId() {
        return this.rectangle.getId();
    }

    /**
     * SETS THE SELECTED STATE OF THE BUTTON.
     * THE ID OF THE BUTTON IS MODIFIED ACCORDINGLY AND THE BUTTON COLOR IS CHANGED ACCORDINGLY.
     * @param selected TRUE/FALSE FOR SELECTED/UNSELECTED
     */
    public void setSelected(boolean selected) {
        this.rectangle.setId(selected ? "selected" : "unselected");
        this.rectangle.setFill(selected ? Color.rgb(244, 232, 0) : Color.rgb(200, 200, 200));
    }
}
