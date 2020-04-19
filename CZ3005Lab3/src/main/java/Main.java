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
    // APPLICATION WINDOW SIZE
    public static final int HEIGHT = 600;
    public static final int WIDTH = 800;

    // QUESTIONS
    private final String[] questions = new String[] {
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

    private TTS tts;
    private BorderPane borderPane;
    private ArrayList<UIButton> buttons;
    private ArrayList<UIButton> noneButtons;
    private ArrayList<String> selected;
    private boolean isNone = false;
    private boolean isFinalPage = false;


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

        // PRIMARY PARENT CONTAINER
        borderPane = new BorderPane();

        // "WELCOME TO SUBWAY" PARENT BOX
        VBox topPane = new VBox();
        topPane.setPrefHeight(80);
        topPane.setAlignment(Pos.CENTER);
        topPane.setPadding(new Insets(8.0, 8.0, 8.0, 8.0));
        topPane.setStyle("-fx-background-color: #4e9b47;");

        Label topLabel = new Label();
        topLabel.setText("Welcome to Subway");
        topLabel.setStyle("-fx-font-size: 32;");

        // "EAT FRESH" PARENT BOX
        VBox bottomPane = new VBox();
        bottomPane.setPrefHeight(80);
        bottomPane.setAlignment(Pos.CENTER);
        bottomPane.setPadding(new Insets(8.0, 8.0, 8.0, 8.0));
        bottomPane.setStyle("-fx-background-color: #4e9b47;");

        Label bottomLabel = new Label();
        bottomLabel.setText("Eat Fresh");
        bottomLabel.setStyle("-fx-font-size: 32;");

        // ADDING VIEWS INTO THE PARENT BOXES
        topPane.getChildren().add(topLabel);
        bottomPane.getChildren().add(bottomLabel);
        borderPane.setTop(topPane);
        borderPane.setBottom(bottomPane);
        root.getChildren().add(borderPane);

        primaryStage.setScene(new Scene(root, WIDTH, HEIGHT));
        primaryStage.setResizable(false);
        primaryStage.show();

        // CONSULT PROLOG AND QUERY FOR CONTENT
        Prolog.consult("subway_dynamic.pl");
        setContent();
    }

    private void resetContent() {
        // REMOVE ALL VIEWS FROM THE CENTER: QUESTIONS, BUTTONS, ETC.
        // CLEAR ALL LISTS AND FLAGS
        // QUERY FOR CONTENT
        borderPane.setCenter(null);
        buttons.clear();
        noneButtons.clear();
        selected.clear();
        isNone = false;
        isFinalPage = false;
        setContent();
    }

    /**
     * SETS THE CONTENT OF THE MIDDLE VIEW.
     * CONTENT IS QUERIED FROM PROLOG.
     * VIEW GENERATED IS ASSIGNED TO CENTER OF PRIMARY PARENT BORDERPANE
     */
    private void setContent() {
        // PARENT CONTAINER
        VBox main = new VBox();

        // QUESTION CONTAINER
        VBox top = new VBox();
        top.setPrefHeight(100);
        top.setPrefWidth(800);
        top.setAlignment(Pos.CENTER);

        // BUTTONS CONTAINER
        VBox bottom = new VBox();
        bottom.setPrefHeight(300);
        bottom.setPrefWidth(800);
        bottom.setAlignment(Pos.CENTER);
        String[][] response = Prolog.query("ask", new Variable("X"), new Variable("Y"), new Variable("Z"));

        // MAKE SURE THE RESPONSE IS CORRECT, IF NOT DISPLAY AN ERROR IN CONSOLE.
        if (response.length != 3 || response[0].length != 1 || response[2].length != 1) {
            System.err.println("Invalid response size.");
            return;
        }

        String stage = response[0][0];
        isFinalPage = stage.equals("final");

        // SET THE QUESTION BASED ON THE STAGE RECEIVED.
        Label question = new Label();
        question.setStyle("-fx-font-size: 24; -fx-font-weight: bold");

        switch (stage) {
            case "meal":
                question.setText(questions[0]);
                break;
            case "size":
                question.setText(questions[1]);
                break;
            case "breads":
                question.setText(questions[2]);
                break;
            case "meat":
                question.setText(questions[3]);
                break;
            case "veggies":
                question.setText(questions[4]);
                break;
            case "sauce":
                question.setText(questions[5]);
                break;
            case "topup":
                question.setText(questions[6]);
                break;
            case "sides":
                question.setText(questions[7]);
                break;
            case "drinks":
                question.setText(questions[8]);
                break;
            case "final":
                question.setText("Here is your order. Thank you!");
                break;
            default:
                question.setText("Unknown?");
        }

        // NEXT BUTTON
        Rectangle nextButton = new Rectangle();
        nextButton.setWidth(100);
        nextButton.setHeight(50);
        nextButton.setFill(Color.rgb(200, 200, 200));
        nextButton.setStroke(Color.DARKGRAY);

        // EVENT HANDLER WHEN NEXT BUTTON IS CLICKED
        nextButton.setOnMouseClicked(event -> {
            // STOP TEXT TO SPEECH AUDIO
            tts.cut();

            // IF END BUTTON IS PRESSED (NEXT WILL BE REPLACED BY END)
            if (isFinalPage) {
                // RETRACT ALL ASSERTED QUERIES TO RESET THE KNOWLEDGE BASE
                String queryString;
                queryString = "retractall(selected(_,_))";
                System.out.println("Query: " + queryString);
                Query q = new Query(queryString);
                System.out.println(q.hasSolution());

                // SET THE CONSULT FILE AGAIN AND RESET THE VIEWS AND VARIABLES (resetContent())
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

            // ASSERT SELECTIONS TO THE KNOWLEDGE BASE BY GENERATING A COMMAND STRING TO SEND TO PROLOG
            String queryString;
            queryString = "retract(selected(" + stage + ",nil))";
            System.out.println("Query: " + queryString);
            Query q = new Query(queryString);
            System.out.println(q.hasSolution());
            queryString = "assert(selected(" + stage + ",";

            // IF SELECTION SIZE IS 1, JUST APPEND TO THE QUERY STRING.
            // ELSE STRING NEEDS TO BE A LIST THAT STARTS AND ENDS WITH [], COMMA-SEPARATED (EX. [A, B, C, D])
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

            // DRINKS IS THE FINAL STAGE BEFORE ORDER SUMMARY
            // RETRACT ALL ENTRIES WITH NIL SELECTION SO IT WILL NOT BE DISPLAYED ON THE SUMMARY (IRRELEVANT CONTENT)
            if (stage.equals("drinks")) {
                queryString = "retractall(selected(_,nil))";
                System.out.println("Query: " + queryString);
                q = new Query(queryString);
                System.out.println(q.hasSolution());
            }

            // CLEAR THE VIEW SO THAT CONTENT DO NOT STACK WHEN setContent() IS CALLED AT THE NEXT STAGE
            resetContent();
        });

        // LABEL FOR THE NEXT BUTTON
        Label nextLabel = new Label();
        nextLabel.setText("Next");
        nextLabel.setStyle("-fx-text-fill: #4e9b47; -fx-font-size: 14; -fx-font-weight: bold");
        nextLabel.setMouseTransparent(true);

        // STACKING THE LABEL ON TOP OF THE BUTTON
        StackPane nextButtonStackPane = new StackPane();
        nextButtonStackPane.setMaxWidth(100);
        nextButtonStackPane.setAlignment(Pos.CENTER);
        nextButtonStackPane.getChildren().add(nextButton);
        nextButtonStackPane.getChildren().add(nextLabel);

        // TOP CONTAINER CONSISTS OF THE QUESTION LABEL IN THE CENTER AND NEXT BUTTON AT THE RIGHT-END
        StackPane topStackPane = new StackPane(question, nextButtonStackPane);
        StackPane.setAlignment(question, Pos.CENTER);
        StackPane.setAlignment(nextButtonStackPane, Pos.CENTER_RIGHT);
        StackPane.setMargin(nextButtonStackPane, new Insets(0.0, 50.0, 0.0, 0.0));
        top.getChildren().add(topStackPane);

        // GET THE SOLUTIONS (CHOICES) AND DETERMINE THE NUMBER OF ROWS NEEDED TO DISPLAY THEM
        // UP TO 4 ITEMS PER ROW
        String[] solutions = response[1];
        int rows = (int) Math.ceil(solutions.length / 4.0);

        if (rows <= 0) {
            System.err.println("Row count is 0.");
            return;
        }

        // DETERMINE THE NUMBER OF COLUMNS FOR EACH ROW AS NUMBER OF SOLUTIONS MAY NOT BE DIVISIBLE BY 4, SO SOME ROWS WILL HAVE LESS THAN 4 ITEMS TO DISPLAY
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

                // REMAINING SOLUTION SIZE IS HALVED IN AN ATTEMPT TO EVENLY DISTRIBUTE ITEMS AMONGST THE LAST FEW ROWS.
                // INSTEAD OF DISPLAYING 5 ITEMS SUCH THAT -> ROW 1: 4 ITEMS, ROW 2: 1 ITEM
                // IT SPLITS IT INTO -> ROW 1: 3 ITEMS, ROW 2: 2 ITEMS
                int columns2 = (int) Math.ceil(temp / 2.0);

                // MAX NUMBER OF ITEMS PER ROW IS 4
                if (columns2 > 4) {
                    columns2 = 4;
                }

                columnsArray[row] = columns2;
                // DEDUCT THE NUMBER OF DISPLAY ITEMS FOR THE CURRENT ROW FROM THE SOLUTION SIZE
                temp -= columns2;
            }
        }

        // IF AT ORDER SUMMARY PAGE, DISPLAY THE QUERIED CONTENT DIFFERENTLY AS THERE IS NO SELECTION TO BE MADE
        // ELSE DISPLAY EACH SOLUTION IN A BUTTON
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

                // CAPITALISING EACH WORD IN THREE STAGES AS WORDS MAY BE JOINED USING _, : OR ,
                // BREAKS THE STRING BY THE SEPARATORS, CAPITALISE, THEN JOIN THEM BACK TOGETHER
                // FIRST STAGE
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

                // SECOND STAGE
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

                // THIRD STAGE
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

                // FINALLY, DISPLAY THE SOLUTIONS USING LABELS (NOT BUTTONS AS IT IS AN ORDER SUMMARY)
                // STRINGS TO THE LEFT OF SEPARATOR : IS THE STAGE, TO THE RIGHT IS THE SELECTIONS MADE BY THE USER
                // TWO VBOXES AND LABELS ARE USED FOR EACH OF THE STRING SUCH THAT THE WIDTH IS CONSISTENT ACROSS EACH ROW OF LABELS
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

                    // CAPITALISING EACH WORD IN THE SOLUTION
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

                    // EVENT HANDLER ON BUTTON CLICK
                    // DECIDES THE BUTTON SHOULD PROCESS THE CLICK BASED ON CURRENT SELECTIONS MADE
                    // isNone IS A SPECIAL FLAG THAT GETS SET WHEN A 'NONE' SOLUTION IS SELECTED BY THE USER
                    // 'NONE' LOCKS OUT ALL OTHER SOLUTIONS FROM BEING SELECTED IF MULTIPLE SELECTIONS ARE ALLOWED
                    // SIMILARLY, SELECTING ANY OTHER SOLUTION LOCKS OUT 'NONE' FROM BEING SELECTED
                    stackPane.setOnMouseClicked(event -> {
                        if (button.getId().equals("unselected") && selected.size() < count && !isNone) {
                            if (solution.equals("none")) {
                                if (selected.size() > 0) {
                                    return;
                                }

                                isNone = true;
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
                            isNone = solution.equals("none");
                            selected.add(solution);
                            button.setSelected(true);
                        } else if (button.getId().equals("selected")) {
                            isNone = false;
                            selected.remove(solution);
                            button.setSelected(false);
                        }
                    });

                    // BUTTONS ARE ADDED TO A LIST FOR MANIPULATIONS IN THE EVENT HANDLER
                    // 'NONE' SOLUTION BUTTONS ARE ADDED TO A SEPARATE LIST AS THEY ARE HANDLED DIFFERENTLY
                    if (solution.equals("none")) {
                        noneButtons.add(button);
                    } else {
                        buttons.add(button);
                    }

                    hBox.getChildren().add(stackPane);

                    // ADDS SOME SPACE TO THE BOTTOM OF THE ROW SO THEY DON'T CLUMP TOGETHER, EXCEPT THE LAST ROW
                    if (column != columns - 1) {
                        Region region = new Region();
                        region.setPrefWidth(50);
                        hBox.getChildren().add(region);
                    }

                    index++;
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
            tts.dictate("Hello! Welcome to Subway! " + question.getText());
        } else {
            tts.dictate(question.getText());
        }
    }
}
