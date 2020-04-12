import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import org.jpl7.Query;
import org.jpl7.Variable;

import java.util.ArrayList;

public class Main extends Application {
    public static final int HEIGHT = 600;
    public static final int WIDTH = 800;

    private TTS tts;
    private BorderPane borderPane;
    private ArrayList<UIButton> buttons;
    private ArrayList<UIButton> noneButtons;
    private ArrayList<String> selected;
    private boolean noneSelected = false;
    private boolean isFinalPage = false;
    private String[] prompts = new String[] {
        "Please select a meal:",
        "Please select size of sub:",
        "Please select a type of bread:",
        "Please select a meat base:",
        "Please select veggies:",
        "Please select up to 2 sauces:",
        "Please select top-ups if any:",
        "Please select a side:",
        "Please select a drink:"
    };

    public static void main(String[] args) {
        try {
            launch(args);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println();
            System.err.println("Exception caught.");
        }
    }

    @Override
    public void start(Stage primaryStage) {
        tts = new TTS();
        selected = new ArrayList<>();
        buttons = new ArrayList<>();
        noneButtons = new ArrayList<>();
        primaryStage.setTitle("CZ3005");
        StackPane root = new StackPane();
        root.setStyle("-fx-font-family: 'Calibri';");
        borderPane = new BorderPane();

        VBox topPane = new VBox();
        topPane.setPrefHeight(80);
        topPane.setAlignment(Pos.CENTER);
        topPane.setPadding(new Insets(8.0, 8.0, 8.0, 8.0));
        topPane.setStyle("-fx-background-color: #4e9b47;");

        Label topLabel = new Label();
        topLabel.setText("Welcome to Subway");
        topLabel.setStyle("-fx-font-size: 32;");

        VBox bottomPane = new VBox();
        bottomPane.setPrefHeight(80);
        bottomPane.setAlignment(Pos.CENTER);
        bottomPane.setPadding(new Insets(8.0, 8.0, 8.0, 8.0));
        bottomPane.setStyle("-fx-background-color: #4e9b47;");

        Label bottomLabel = new Label();
        bottomLabel.setText("Eat Fresh");
        bottomLabel.setStyle("-fx-font-size: 32;");

        topPane.getChildren().add(topLabel);
        bottomPane.getChildren().add(bottomLabel);
        borderPane.setTop(topPane);
        borderPane.setBottom(bottomPane);
        root.getChildren().add(borderPane);

        primaryStage.setScene(new Scene(root, WIDTH, HEIGHT));
        primaryStage.setResizable(false);
        primaryStage.show();
        Prolog.consult("subway_dynamic.pl");
        setContent();
    }

    private void resetContent() {
        borderPane.setCenter(null);
        buttons.clear();
        noneButtons.clear();
        selected.clear();
        noneSelected = false;
        isFinalPage = false;
        setContent();
    }

    private void setContent() {
        VBox main = new VBox();

        VBox top = new VBox();
        top.setPrefHeight(100);
        top.setPrefWidth(800);
        top.setAlignment(Pos.CENTER);

        VBox bottom = new VBox();
        bottom.setPrefHeight(300);
        bottom.setPrefWidth(800);
        bottom.setAlignment(Pos.CENTER);

        Label title = new Label();
        title.setStyle("-fx-font-size: 24; -fx-font-weight: bold");
        String[][] response = Prolog.query("ask", new Variable("X"), new Variable("Y"), new Variable("Z"));

        if (response.length != 3 || response[0].length != 1 || response[2].length != 1) {
            System.err.println("Invalid response.");
            return;
        }

        String stage = response[0][0];
        isFinalPage = stage.equals("final");

        switch (stage) {
            case "meal":
                title.setText(prompts[0]);
                break;
            case "size":
                title.setText(prompts[1]);
                break;
            case "breads":
                title.setText(prompts[2]);
                break;
            case "meat":
                title.setText(prompts[3]);
                break;
            case "veggies":
                title.setText(prompts[4]);
                break;
            case "sauce":
                title.setText(prompts[5]);
                break;
            case "topup":
                title.setText(prompts[6]);
                break;
            case "sides":
                title.setText(prompts[7]);
                break;
            case "drinks":
                title.setText(prompts[8]);
                break;
            case "final":
                title.setText("Here is your order. Thank you!");
                break;
            default:
                title.setText("Unknown?");
        }

        Rectangle nextRectangle = new Rectangle();
        nextRectangle.setWidth(100);
        nextRectangle.setHeight(50);
        nextRectangle.setFill(Color.rgb(200, 200, 200));
        nextRectangle.setStroke(Color.DARKGRAY);
        nextRectangle.setOnMouseClicked(event -> {
            tts.cut();

            if (isFinalPage) {
                String queryString;
                queryString = "retractall(selected(_,_))";
                System.out.println("Query: " + queryString);
                Query q = new Query(queryString);
                System.out.println(q.hasSolution());
                Prolog.consult("subway_dynamic.pl");
                resetContent();
                return;
            }

            if (selected.size() == 0) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setHeaderText("Please make a selection.");
                alert.show();
                return;
            }

            String queryString;
            queryString = "retract(selected(" + stage + ",nil))";
            System.out.println("Query: " + queryString);
            Query q = new Query(queryString);
            System.out.println(q.hasSolution());
            queryString = "assert(selected(" + stage + ",";

            if (selected.size() == 1) {
                queryString += selected.get(0) + "))";
            } else {
                StringBuilder stringBuilder = new StringBuilder();
                String str = "[";

                for (String s : selected) {
                    if (stringBuilder.length() > 0) {
                        stringBuilder.append(",");
                    }

                    stringBuilder.append(s);
                }

                str += stringBuilder.toString() + "]";
                queryString += str + "))";
            }

            System.out.println("Query: " + queryString);
            q = new Query(queryString);
            System.out.println(q.hasSolution());

            if (stage.equals("drinks")) {
                queryString = "retractall(selected(_,nil))";
                System.out.println("Query: " + queryString);
                q = new Query(queryString);
                System.out.println(q.hasSolution());
            }

            resetContent();
        });

        Label nextLabel = new Label();
        nextLabel.setText("Next");
        nextLabel.setStyle("-fx-text-fill: #4e9b47; -fx-font-size: 14; -fx-font-weight: bold");
        nextLabel.setMouseTransparent(true);

        StackPane nextButtonStackPane = new StackPane();
        nextButtonStackPane.setMaxWidth(100);
        nextButtonStackPane.setAlignment(Pos.CENTER);
        nextButtonStackPane.getChildren().add(nextRectangle);
        nextButtonStackPane.getChildren().add(nextLabel);

        StackPane topStackPane = new StackPane(title, nextButtonStackPane);
        StackPane.setAlignment(title, Pos.CENTER);
        StackPane.setAlignment(nextButtonStackPane, Pos.CENTER_RIGHT);
        StackPane.setMargin(nextButtonStackPane, new Insets(0.0, 50.0, 0.0, 0.0));
        top.getChildren().add(topStackPane);

        String[] solutions = response[1];
        int rows = (int) Math.ceil(solutions.length / 4.0);

        if (rows <= 0) {
            System.err.println("Row count is 0.");
            return;
        }

        int[] columnsArray = new int[rows];

        if (rows == 1) {
            columnsArray[0] = solutions.length;
        } else {
            int temp = solutions.length;

            for (int row = 0; row < rows; row++) {
                if (row == rows - 1) {
                    columnsArray[row] = temp;
                    break;
                }

                int columns2 = (int) Math.ceil(temp / 2.0);

                if (columns2 > 4) {
                    columns2 = 4;
                }

                columnsArray[row] = columns2;
                temp -= columns2;
            }
        }

        if (isFinalPage) {
            nextLabel.setText("End");

            HBox hBox = new HBox();
            hBox.setPrefWidth(WIDTH - 100);
            hBox.setMaxWidth(WIDTH - 100);
            hBox.setPrefHeight(300);
            hBox.setMaxHeight(300);
            hBox.setAlignment(Pos.CENTER);
            hBox.setStyle("-fx-background-color: #DDDDDD");

            VBox vBox1 = new VBox();
            vBox1.setPrefWidth(100);
            vBox1.setMaxWidth(100);
            vBox1.setPrefHeight(250);
            vBox1.setMaxHeight(250);
            VBox vBox2 = new VBox();
            vBox2.setPrefWidth(WIDTH - 250);
            vBox2.setMaxWidth(600);
            vBox2.setPrefHeight(250);
            vBox2.setMaxHeight(250);

            for (String solution : solutions) {
                String[] strings = solution.split("_");
                StringBuilder stringBuilder = new StringBuilder();

                for (String s : strings) {
                    if (stringBuilder.length() > 0) {
                        stringBuilder.append(" ");
                    }

                    String s2 = s.trim();
                    char[] chars = s2.toCharArray();
                    int c = chars[0];

                    if (c >= 65 && c <= 91) {
                        stringBuilder.append(s2);
                        continue;
                    }

                    chars[0] = (char) (((int) chars[0]) - 32);
                    stringBuilder.append(chars);
                }

                String solutionString = stringBuilder.toString();
                strings = solutionString.split(":");
                stringBuilder = new StringBuilder();

                for (String s : strings) {
                    if (stringBuilder.length() > 0) {
                        stringBuilder.append(": ");
                    }

                    String s2 = s.trim();
                    char[] chars = s2.toCharArray();
                    int c = chars[0];

                    if (c >= 65 && c <= 91) {
                        stringBuilder.append(s2);
                        continue;
                    }

                    chars[0] = (char) (((int) chars[0]) - 32);
                    stringBuilder.append(chars);
                }

                solutionString = stringBuilder.toString();
                strings = solutionString.split(", ");
                stringBuilder = new StringBuilder();

                for (String s : strings) {
                    if (stringBuilder.length() > 0) {
                        stringBuilder.append(", ");
                    }

                    String s2 = s.trim();
                    char[] chars = s2.toCharArray();
                    int c = chars[0];

                    if (c >= 65 && c <= 91) {
                        stringBuilder.append(s2);
                        continue;
                    }

                    chars[0] = (char) (((int) chars[0]) - 32);
                    stringBuilder.append(chars);
                }

                solutionString = stringBuilder.toString();
                strings = solutionString.split(": ");

                if (strings.length != 2) {
                    continue;
                }

                Label label = new Label();
                label.setText(strings[0].trim() + ": ");
                label.setStyle("-fx-font-size: 16;");
                label.setPadding(new Insets(4.0));
                vBox1.getChildren().add(label);

                label = new Label();
                label.setText(strings[1].trim());
                label.setStyle("-fx-font-size: 16;");
                label.setPadding(new Insets(4.0));
                vBox2.getChildren().add(label);
            }

            hBox.getChildren().add(vBox1);
            hBox.getChildren().add(vBox2);
            bottom.getChildren().add(hBox);
        }

        else {
            nextLabel.setText("Next");
            int count = Integer.parseInt(response[2][0]);
            int index = 0;

            for (int row = 0; row < rows; row++) {
                HBox hBox = new HBox();
                hBox.setAlignment(Pos.CENTER);
                int columns = columnsArray[row];

                for (int column = 0; column < columns; column++) {
                    String solution = solutions[index];
                    String[] strings = solution.split("_");
                    StringBuilder stringBuilder = new StringBuilder();

                    for (String s : strings) {
                        if (stringBuilder.length() > 0) {
                            stringBuilder.append(" ");
                        }

                        String s2 = s.trim();
                        char[] chars = s2.toCharArray();
                        int c = chars[0];

                        if (c >= 65 && c <= 91) {
                            continue;
                        }

                        chars[0] = (char) (((int) chars[0]) - 32);
                        stringBuilder.append(chars);
                    }

                    String labelString = stringBuilder.toString();
                    double width = (700.0 - (50.0 * (columnsArray[0] - 1))) / columnsArray[0];
                    double height = 60;
                    UIButton button = new UIButton(width, height, stage, solution, labelString);
                    StackPane stackPane = button.getParent();
                    stackPane.setOnMouseClicked(event -> {
                        if (button.getId().equals("unselected") && selected.size() < count && !noneSelected) {
                            if (solution.equals("none")) {
                                if (selected.size() > 0) {
                                    return;
                                }

                                noneSelected = true;
                            }

                            selected.add(solution);
                            button.setSelected(true);
                        } else if (button.getId().equals("unselected") && selected.size() >= count && count == 1) {
                            for (UIButton button1 : buttons) {
                                button1.setSelected(false);
                            }

                            for (UIButton button1 : noneButtons) {
                                button1.setSelected(false);
                            }

                            selected.clear();
                            noneSelected = solution.equals("none");
                            selected.add(solution);
                            button.setSelected(true);
                        } else if (button.getId().equals("selected")) {
                            noneSelected = false;
                            selected.remove(solution);
                            button.setSelected(false);
                        }
                    });

                    if (solution.equals("none")) {
                        noneButtons.add(button);
                    } else {
                        buttons.add(button);
                    }

                    hBox.getChildren().add(stackPane);

                    if (column != columns - 1) {
                        Region region = new Region();
                        region.setPrefWidth(50);
                        hBox.getChildren().add(region);
                    }

                    index++;

//                    rectangle.setOnMouseEntered(event -> {
//                        if (rectangle.getId().equals("selected") && selected.size() >= count && !solution.equals("none") && !noneSelected) {
//                            rectangle.setOpacity(0.8);
//                        } else if (selected.size() < count && !solution.equals("none") && !noneSelected) {
//                            rectangle.setOpacity(0.8);
//                        } else if (solution.equals("none") && (selected.size() == 0 || noneSelected)) {
//                            rectangle.setOpacity(0.8);
//                        }
//                    });
//
//                    rectangle.setOnMouseExited(event -> {
//                        if (rectangle.getId().equals("selected") && selected.size() >= count && !solution.equals("none") && !noneSelected) {
//                            rectangle.setOpacity(1.0);
//                        } else if (selected.size() < count && !solution.equals("none") && !noneSelected) {
//                            rectangle.setOpacity(1.0);
//                        } else if (solution.equals("none") && (selected.size() == 0 || noneSelected)) {
//                            rectangle.setOpacity(1.0);
//                        }
//                    });
                }

                bottom.getChildren().add(hBox);
                Region region = new Region();
                region.setPrefHeight(30);
                bottom.getChildren().add(region);
            }
        }

        main.getChildren().add(top);
        main.getChildren().add(bottom);
        borderPane.setCenter(main);

        if (stage.equals("meal")) {
            tts.dictate("Hello! Welcome to Subway! " + title.getText());
        } else {
            tts.dictate(title.getText());
        }
    }
}
