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
    private StackPane parent;
    private Rectangle rectangle;
    private ImageView imageView;
    private Label label;

    public UIButton(double w, double h, String stage, String internalString, String displayString) {
        boolean withImage;

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

        rectangle = new Rectangle();
        rectangle.setWidth(w);
        rectangle.setHeight(h);
        rectangle.setFill(Color.rgb(200, 200, 200));
        rectangle.setStroke(Color.DARKGRAY);
        rectangle.setId("unselected");

        label = new Label();
        label.setText(displayString);
        label.setStyle("-fx-font-size: 14;");
        label.setPadding(new Insets(8.0, 8.0, 8.0, 8.0));
        label.setWrapText(true);
        label.setTextAlignment(TextAlignment.CENTER);
        label.setMouseTransparent(true);

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

    public StackPane getParent() {
        return this.parent;
    }

    public String getId() {
        return this.rectangle.getId();
    }

    public void setSelected(boolean selected) {
        this.rectangle.setId(selected ? "selected" : "unselected");
        this.rectangle.setFill(selected ? Color.rgb(244, 232, 0) : Color.rgb(200, 200, 200));
    }
}
