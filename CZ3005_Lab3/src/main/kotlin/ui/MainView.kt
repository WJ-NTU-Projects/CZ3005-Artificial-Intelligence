package ui

import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.control.Label
import javafx.scene.image.Image
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.scene.text.TextAlignment
import org.jpl7.Variable
import tornadofx.*
import constants.Constants
import tools.File
import tools.Prolog
import tools.TTS
import ui.helper.UIFade
import ui.helper.UITimeGreetings
import kotlin.math.ceil

class MainView : View("Subway Sandwich Interactor - Teo Wei Jie, U1822263C, SSP2") {
    enum class RectangleState {
        DEFAULT,
        SELECTED,
        DISABLED
    }

    private var progressIndicatorView: VBox by singleAssign()
    private lateinit var contentNode: Node
    private lateinit var progressIndicatorLabel: Label
    private lateinit var promptLabel: Label
    private lateinit var optionsBox: VBox
    private lateinit var bottomRectangle: Rectangle
    private lateinit var bottomLabel: Label
    private lateinit var textToSpeech: TTS

    // Queried from prolog -> how many rows maximum to display the choices (means more columns for more choices).
    private lateinit var selectionRowsArray: IntArray

    // List of selected options for the CURRENT stage -> used to insert selections into prolog KB,
    private val selectedOptionList: ArrayList<String> = arrayListOf()

    // Set to true if the "None" option is selected (for choices that offer the "None" option)
    private var isNoneOptionSelected: Boolean = false

    // Total cost of selections to be displayed at the end of the program
    private var totalCost: Int = 0

    /**
     * Parent view variable, as required by the tornadofx library for every class extending View.
     */
    override val root: Parent = stackpane {
        // Views support css styles with prefix "-fx"
        style = "-fx-font-family: 'Verdana'; -fx-background-color: #FFFFFF; -fx-font-size: 14;"
        prefWidth = Constants.WINDOW_WIDTH
        prefHeight = Constants.WINDOW_HEIGHT

        contentNode = vbox {
            // Prompt or question area
            vbox {
                prefWidth = Constants.WINDOW_WIDTH
                prefHeight = Constants.MAIN_TOP_HEIGHT
                minHeight = Constants.MAIN_TOP_HEIGHT
                maxHeight = Constants.MAIN_TOP_HEIGHT
                alignment = Pos.CENTER

                promptLabel = label {
                    style = "-fx-font-size: 16;"
                    padding = Insets(8.0)
                }
            }

            // Choices area
            vbox {
                prefWidth = Constants.WINDOW_WIDTH
                prefHeight = Constants.MAIN_CENTER_HEIGHT
                minHeight = Constants.MAIN_CENTER_HEIGHT
                maxHeight = Constants.MAIN_CENTER_HEIGHT
                alignment = Pos.CENTER_LEFT
                // optionsBox contains views for the choices -> CLEAR WHEN CHANGING CONTENT
                optionsBox = vbox()
            }

            // Bottom button area
            vbox {
                prefWidth = Constants.WINDOW_WIDTH
                prefHeight = Constants.MAIN_BOTTOM_HEIGHT
                minHeight = Constants.MAIN_BOTTOM_HEIGHT
                maxHeight = Constants.MAIN_BOTTOM_HEIGHT
                alignment = Pos.CENTER

                stackpane {
                    bottomRectangle = rectangle(width = Constants.WINDOW_WIDTH, height = Constants.MAIN_BOTTOM_HEIGHT) {
                        fill = Constants.colors[1]
                        stroke = Color.DARKGRAY
                        strokeWidth = 1.0
                        setOnMouseEntered { opacity = 0.8 }
                        setOnMouseExited { opacity = 1.0 }
                    }

                    bottomLabel = label {
                        padding = Insets(8.0)
                        wrapTextProperty().value = true
                        textAlignment = TextAlignment.CENTER
                        isMouseTransparent = true
                    }
                }
            }
        }

        // Progress indicator used at launch while initialising stuffs.
        progressIndicatorView = vbox {
            style = "-fx-background-color: #FFFFFF;"
            alignment = Pos.CENTER
            progressindicator()

            progressIndicatorLabel = label("Just a moment!") {
                style = "-fx-font-size: 20;"
                padding = Insets(16.0)
            }
        }
    }

    /**
     * Constructor
     * Performs view and TTS initialisation, setting of Prolog fallback script and querying of initial content.
     */
    init {
        currentStage?.isResizable = false
        currentStage?.sizeToScene()

        // runLater makes sure we are back in UI thread.
        runLater {
            contentNode.requestFocus()
            progressIndicatorLabel.text = "Just a moment! Initialising some modules..."

            // Heavy load running asynchronously to prevent UI lock.
            // DO NOT RUN UI COMPONENTS STUFF ASYNCHRONOUSLY -> MUST RUN IN UI THREAD
            runAsync {
                textToSpeech = TTS()
                Prolog.fallback = "subway_temp.pl"
                resetContent()
            }.setOnSucceeded {
                // BACK IN UI THREAD
                val responseRows: Array<Array<String>> = Prolog.query("selectionRows", Variable("A"))
                selectionRowsArray = responseRows[0].map { it.toInt() }.toIntArray()

                // Fades out the progress indicator and load content when the animation ends (in curly braces).
                // EventHandler contains code (within the braces) to be executed at the end of the animation.
                UIFade.fadeOut(progressIndicatorView, EventHandler {
                    progressIndicatorView.hide()
                    loadContentFromProlog()
                })
            }
        }
    }

    /**
     * Copies content from "subway.pl" (unmodified file) into "subway_temp.pl" (to be modified as the program progresses).
     */
    private fun resetContent() {
        val lines: List<String> = File.readPrologFile("subway.pl")
        var s = ""

        for (line in lines) {
            if (s.isNotBlank()) s += "\n"
            s += line
        }

        File.replacePrologFileContent("subway_temp.pl", s)
    }

    /**
     * Sends a query to the prolog engine for content to be displayed.
     */
    private fun loadContentFromProlog() {
        // Clears views of previous choices (if any), list of previously selected options and resets the flag if "None" choice is selected previously.
        optionsBox.getChildList()?.clear()
        selectedOptionList.clear()
        isNoneOptionSelected = false

        // Sets the consulted file (although it'll fallback to whatever file is set in init if not called).
        Prolog.consult("subway_temp.pl")

        // Variables are named A, B, C... as JPL apparently arranges the returned solutions in alphabetically order.
        val prologResponse: Array<Array<String>> = Prolog.query("ask", Variable("A"), Variable("B"), Variable("C"), Variable("D"), Variable("E"), Variable("F"))

        // Hard decoding of response because why not.
        val responseType: String = prologResponse[0][0]
        val responseOptions: Array<String> = prologResponse[1]
        val responseMaxSelection: Int = prologResponse[2][0].toInt()
        val responsePrompt: String = prologResponse[3][0].drop(1).dropLast(1)
        val responseItemPrices: IntArray = prologResponse[4].map { it.toInt() }.toIntArray()
        val responseId: Int = prologResponse[5][0].toInt()

        // Bottom button text changes depending on the stage/type, and loads the views for choices to be displayed into the optionsBox.
        when (responseType) {
            "final" -> {
                bottomLabel.text = "MAKE PAYMENT"
                loadFinalContent(responseOptions)
            }

            "end" -> {
                bottomLabel.text = "PLACE NEW ORDER"
                loadSelectionContent(responseType, responseOptions, responseMaxSelection, responseItemPrices, responseId)
            }

            else -> {
                bottomLabel.text = "NEXT"
                loadSelectionContent(responseType, responseOptions, responseMaxSelection, responseItemPrices, responseId)
            }
        }

        bottomRectangle.onLeftClick { handleNextClick(responseType) }

        val responseRepeat: Array<Array<String>> = Prolog.query("repeat", Variable("A"))
        val repeat: Int = responseRepeat[0][0].toInt()

        val prompt: String =
            when (responseType) {
                "meal" -> if (repeat == 0) "${UITimeGreetings.getGreetings()} $responsePrompt" else responsePrompt
                "final" -> "$responsePrompt $totalCost dollars."
                else -> responsePrompt
            }

        // Displaying of prompt and reading it out using TTS
        promptLabel.text = prompt
        UIFade.fadeIn(contentNode, EventHandler { textToSpeech.nag(promptLabel.text) })
    }

    /**
     * Initialises views for the choices to be displayed and adds them to the optionsBox defined in the parent node.
     */
    private fun loadSelectionContent(responseType: String, responseOptions: Array<String>, responseMaxSelection: Int, responseItemPrices: IntArray, responseId: Int) {
        var rows: Int = selectionRowsArray[responseId]
        var columnsLimit = 99
        val columnsList = ArrayList<Int>()

        // If rows are defined as 99 (no limit), dynamically assigns rows based on choices size.
        // Limits each row to 3 or 4 columns (if mod 3 = 0 then limit to 3, else 4).
        if (rows == 99) {
            val moduloThree: Int = responseOptions.size % 3
            columnsLimit = if (moduloThree == 0) 3 else 4
            rows = ceil(1.0 * responseOptions.size / columnsLimit).toInt()
        }

        // If choices size is 2, hard set it to 1 row max.
        if (responseOptions.size == 2) rows = 1

        // Determine how many columns per row based on calculated or obtained row above.
        if (rows > 1) {
            var temp = responseOptions.size

            for (row in 0 until rows) {
                if (row == rows - 1) {
                    columnsList.add(temp); break
                }

                var columns: Int = ceil(temp / 2.0).toInt()
                if (columns > columnsLimit) columns = columnsLimit
                columnsList.add(columns)
                temp -= columns
            }
        } else {
            columnsList.add(responseOptions.size)
        }

        // Each button is actually a rectangle...
        // Width and height of rectangles need to be explicitly set because default is 1.
        val rectangleWidth: Double = (1.0 * Constants.WINDOW_WIDTH / columnsList[0])
        val rectangleHeight: Double = (1.0 * Constants.MAIN_CENTER_HEIGHT / rows)

        // List of rectangles and its labels that are not of the "None" option, for opacity manipulation purpose..
        val rectangleList = ArrayList<Node>()
        val rectangleLabelList = ArrayList<Label>()

        // List of rectangles and its labels that are of the "None" option, for opacity manipulation purpose.
        val noneList = ArrayList<Node>()
        val noneLabelList = ArrayList<Label>()

        // Keeps track of the actual index of the choices in the list since we're dealing with rows and columns.
        var index = 0

        for (row in 0 until rows) {
            val rowRoot: Node = hbox()
            val columns: Int = columnsList[row]

            for (column in 0 until columns) {
                if (index >= responseOptions.size) break
                val option: String = responseOptions[index]

                // Capitalises each word in the choice.
                // Split because the words are joined using '_' so capitalize() won't work.
                var displayString: String = option.split("_").joinToString(" ") { it.trim().capitalize() }
                val displayCost: Int = if (responseItemPrices.size == 1) responseItemPrices[0] else responseItemPrices[index]
                displayString += if (displayCost > 0) "\n+\$${displayCost}.00" else "\n "

                // To stack the label on top of the rectangle to create a button.
                val stackPane: StackPane = stackpane {
                    alignment = Pos.CENTER
                    var rectangle: Node

                    // If an image is available (based on contents obtained from Prolog), the rectangle is a stackpane that stacks the rectangle and an imageview.
                    // Else, rectangle is just a rectangle.
                    try {
                        val image = Image("${responseType}_${option}.png")

                        rectangle = stackpane {
                            rectangle(width = rectangleWidth, height = rectangleHeight) {
                                id = "innerRectangle"
                                fill = if (responseType == "end") Constants.colors[9] else Constants.RECTANGLE_DEFAULT_COLOR
                                stroke = Color.DARKGRAY
                                strokeWidth = 1.0
                            }

                            vbox {
                                alignment = Pos.TOP_CENTER

                                imageview(image) {
                                    fitWidth = rectangleWidth
                                    fitHeight = rectangleHeight - 40
                                    isPreserveRatio = true
                                }
                            }
                        }
                    } catch (e: Exception) {
                        rectangle = rectangle(width = rectangleWidth, height = rectangleHeight) {
                            fill = if (responseType == "end") Constants.colors[9] else Constants.RECTANGLE_DEFAULT_COLOR
                            stroke = Color.DARKGRAY
                            strokeWidth = 1.0
                        }
                    }

                    // ID is used to control which button is selected or not. (ID is actually used for css but it serves the purpose in this case.)
                    rectangle.id = "unselected"

                    // Separate function otherwise this function gets super long and hard to debug.
                    rectangle.onLeftClick { handleOptionClick(rectangle, responseType, option, displayCost, responseMaxSelection, rectangleList, noneList, rectangleLabelList, noneLabelList) }

                    // On hover kind of effect.
                    rectangle.setOnMouseEntered { if (checkHoverLegal(rectangle.id, option, responseMaxSelection)) opacity = 0.8 }
                    rectangle.setOnMouseExited { if (checkHoverLegal(rectangle.id, option, responseMaxSelection)) opacity = 1.0 }

                    // Label to be stacked on the rectangle.
                    val rectangleLabel: Label = label(displayString) {
                        if (responseType == "end") {
                            style = "-fx-font-weight: bold;"
                            text = "${displayString.trim()}\n(App exits!)"
                        }

                        padding = Insets(8.0)
                        textAlignment = TextAlignment.CENTER
                        wrapTextProperty().value = true
                        isMouseTransparent = true
                    }

                    // Adds the rectangle to the lists!
                    if (option == "none") noneList.add(rectangle) else rectangleList.add(rectangle)
                    if (option == "none") noneLabelList.add(rectangleLabel) else rectangleLabelList.add(rectangleLabel)
                    StackPane.setAlignment(rectangleLabel, Pos.BOTTOM_CENTER)
                }

                // Adds the button to the hbox (row container).
                rowRoot.add(stackPane)
                index++
            }

            // Adds the row container (hbox) into optionsBox.
            optionsBox.add(rowRoot)
        }
    }

    /**
     * Loads the order summary into optionsBox.
     * Summary page is divided into three sections -> LEFT (item type), CENTER (items selected), RIGHT (cost).
     */
    private fun loadFinalContent(responseOptions: Array<String>) {
        val contentRoot: Node = hbox {
            paddingLeft = Constants.FINAL_BOX_MARGIN
            paddingRight = Constants.FINAL_BOX_MARGIN
        }

        val leftBox: VBox = vbox {
            prefWidth = Constants.FINAL_LEFT_WIDTH
            alignment = Pos.CENTER_LEFT
        }

        val centerBox: VBox = vbox {
            prefWidth = Constants.FINAL_CENTER_WIDTH
            alignment = Pos.CENTER_LEFT
        }

        val rightBox: VBox = vbox {
            prefWidth = Constants.FINAL_RIGHT_WIDTH
            alignment = Pos.CENTER_RIGHT
        }

        val responseCostList: Array<Array<String>> = Prolog.query("getCosts", Variable("A"))
        val costs: IntArray =  responseCostList[0].map { it.toInt() }.toIntArray()

        for ((index, option) in responseOptions.withIndex()) {
            var displayOption = option.split("_").joinToString(" ") { it.trim().capitalize() }
            displayOption = displayOption.split(": ").joinToString(": ") { it.split(", ").joinToString(", ") { x -> x.trim().capitalize() } }
            val split: List<String> = displayOption.split(": ")
            displayOption = split[1]
            val displayTitle = split[0]
            val cost = costs[index]
            totalCost += cost
            if (displayOption.toLowerCase() == "nil") continue

            leftBox.add(label(displayTitle) {
                style = "-fx-font-size: 12; -fx-font-weight: bold;"
                padding = Insets(4.0)
            })

            leftBox.add(region {
                style = "-fx-background-color: #AAAAAA;"
                prefHeight = 1.0
            })

            centerBox.add(label(displayOption) {
                style = "-fx-font-size: 12;"
                padding = Insets(4.0)
            })

            centerBox.add(region {
                style = "-fx-background-color: #AAAAAA;"
                prefHeight = 1.0
            })

            val displayCost: String = if (cost == 0) "-" else "+\$$cost.00"

            rightBox.add(label(displayCost) {
                style = "-fx-font-size: 12;"
                padding = Insets(4.0)
            })

            rightBox.add(region {
                style = "-fx-background-color: #AAAAAA;"
                prefHeight = 1.0
            })
        }

        // Adding the total cost display.
        leftBox.add(label(" ") {
            style = "-fx-font-size: 12; -fx-font-weight: bold;"
            padding = Insets(8.0, 4.0, 8.0, 4.0)
        })

        centerBox.add(label("Total") {
            style = "-fx-font-size: 12; -fx-font-weight: bold;"
            padding = Insets(8.0, 4.0, 8.0, 4.0)
        })

        rightBox.add(label("\$${totalCost}.00") {
            style = "-fx-font-size: 12;"
            padding = Insets(8.0, 4.0, 8.0, 4.0)
        })

        contentRoot.add(leftBox)
        contentRoot.add(centerBox)
        contentRoot.add(rightBox)
        optionsBox.add(contentRoot)
    }

    /**
     * Button click event handler.
     */
    private fun handleOptionClick(rectangle: Node, responseType: String, option: String, displayCost: Int, responseMaxSelection: Int, rectangleList: ArrayList<Node>, noneList: ArrayList<Node>, rectangleLabelList: ArrayList<Label>, noneLabelList: ArrayList<Label>) {
        // End of program -> close application.
        if (responseType == "end") {
            close()
            return
        }

        // If button clicked is unselected...
        if (rectangle.id == "unselected") {
            // If button is of "None" option and there are buttons 'selected' already (for multi-selection stages), DO NOTHING!
            if (option == "none" && selectedOptionList.isNotEmpty()) return

            // For single-selection stages, sets all buttons as unselected (current button clicked will be selected later).
            if (selectedOptionList.size >= responseMaxSelection && responseMaxSelection == 1) {
                for ((index, r) in rectangleList.withIndex()) setRectangleState(r, rectangleLabelList[index], RectangleState.DEFAULT)
                for ((index, r) in noneList.withIndex()) setRectangleState(r, noneLabelList[index], RectangleState.DEFAULT)
                selectedOptionList.clear()
                isNoneOptionSelected = false
            }

            // If "None" options is selected, or number of selected choices permitted is maxed for a stage, DO NOTHING!
            if (selectedOptionList.size >= responseMaxSelection || isNoneOptionSelected) return

            /* SETS THE BUTTON AS SELECTED AND ADD THE CHOICE INTO THE SELECTED OPTION LIST */
            // Set the flag to true if it is of "None" option.
            if (option == "none") isNoneOptionSelected = true
            selectedOptionList.add("$option,$displayCost")
            setRectangleState(rectangle, null, RectangleState.SELECTED)

            // Sets non-selected views to "disabled" state by reducing opacity when number of selected choices permitted is maxed.
            // Also, disable the "None" option if it is not selected for multi-selection stages.
            when {
                isNoneOptionSelected                            -> for ((index, r) in rectangleList.withIndex()) if (r.id == "unselected") setRectangleState(r, rectangleLabelList[index], RectangleState.DISABLED)
                selectedOptionList.size >= responseMaxSelection -> for ((index, r) in rectangleList.withIndex()) if (r.id == "unselected") setRectangleState(r, rectangleLabelList[index], RectangleState.DISABLED)
                else                                            -> for ((index, r) in noneList.withIndex()) setRectangleState(r, noneLabelList[index], RectangleState.DISABLED)
            }
        } else if (rectangle.id == "selected") {
            // Sets non-selected views to "enabled" state since quota is not maxed now.
            for ((index, r) in rectangleList.withIndex()) if (r.id == "unselected") setRectangleState(r, rectangleLabelList[index], RectangleState.DEFAULT)

            // Just unset the flag because "None" will not be selected no matter what now.
            isNoneOptionSelected = false

            // Remove the option associated to the clicked button from the selected option list.
            selectedOptionList.remove("$option,$displayCost")
            setRectangleState(rectangle, null, RectangleState.DEFAULT)

            // For multi-selection stages, if selected option list is empty, enable the "None" option.
            if (option != "none" && selectedOptionList.isEmpty()) {
                for ((index, r) in noneList.withIndex()) setRectangleState(r, noneLabelList[index], RectangleState.DEFAULT)
            }
        }
    }

    /**
     * Finishes the current stage, modifying the Prolog KB if necessary and loads content for the next stage (content determined by Prolog script).
     */
    private fun handleNextClick(responseType: String) {
        // If no item is selected, throw an error (for pages that requires selection).
        if (responseType != "final" && responseType != "end" && selectedOptionList.isEmpty()) {
            error("Please select an item!"); return
        }

        // Stops the TTS if it is still reading.
        textToSpeech.stfu()

        // If user chooses to restart the program at the end...
        if (responseType == "end") {
            totalCost = 0
            resetContent()
            Prolog.updateRepeated(1)
            loadContentFromProlog()
            return
        }

        // Order summary and payment stage have slightly different procedures.
        // Otherwise, it gathers every option selected in the selected option list and updates the Prolog knowledge base accordingly via file IO.
        // Options are gathered in a list to be inserted unless only one option is selected.
        when (responseType) {
            "final" -> Prolog.updateTotalCost(totalCost)

            "payment" -> {
                val split: List<String> = selectedOptionList[0].split(",")
                Prolog.updatePaymentMethod(split[0])
                progressIndicatorLabel.text = "Processing payment..."
                progressIndicatorView.opacity = 0.8
                progressIndicatorView.show()
            }

            else -> {
                val selection: String
                val cost: Int

                if (selectedOptionList.size == 1) {
                    val split: List<String> = selectedOptionList[0].split(",")
                    selection = split[0]
                    cost = split[1].toInt()
                } else {
                    var s = ""
                    var totalCost = 0

                    for (item in selectedOptionList) {
                        val split = item.split(",")
                        if (s.isNotBlank()) s += ", "
                        s += split[0]
                        totalCost += split[1].toInt()
                    }

                    s = "[${s}]"
                    selection = s
                    cost = totalCost
                }

                Prolog.updateSelection(responseType, selection, cost)
            }
        }

        if (responseType == "payment") {
            runAsync {
                Thread.sleep(1000)
            }.setOnSucceeded {
                progressIndicatorView.hide()
                progressIndicatorView.opacity = 1.0
                UIFade.fadeOut(contentNode, EventHandler { loadContentFromProlog() })
            }

            return
        }

        UIFade.fadeOut(contentNode, EventHandler { loadContentFromProlog() })
    }

    /**
     * Modifies the ID, opacity and fill colour of the rectangle based on the desired state as defined in the enum RectangleState.
     */
    private fun setRectangleState(rectangle: Node, rectangleLabel: Label?, state: RectangleState) {
        val id: String
        val opacity: Double
        val fillColor: Color

        when (state) {
            RectangleState.DEFAULT -> {
                id = "unselected"
                opacity = 1.0
                fillColor = Constants.RECTANGLE_DEFAULT_COLOR
            }

            RectangleState.SELECTED -> {
                id = "selected"
                opacity = 1.0
                fillColor = Constants.colors[4]
            }

            RectangleState.DISABLED -> {
                id = "unselected"
                opacity = 0.4
                fillColor = Constants.RECTANGLE_DEFAULT_COLOR
            }
        }

        rectangle.id = id
        rectangle.opacity = opacity
        rectangleLabel?.opacity = opacity

        if (rectangle is Rectangle) {
            rectangle.fill = fillColor
        } else if (rectangle is StackPane) {
            for (child in rectangle.children) {
                if (child is Rectangle && child.id == "innerRectangle") {
                    child.fill = fillColor
                    break
                }
            }
        }
    }

    /**
     * Checks if the rectangle of a button should perform the "hover" effect.
     */
    private fun checkHoverLegal(rectangleId: String, option: String, responseMaxSelection: Int): Boolean {
        if (option != "none" && !isNoneOptionSelected) {
            when {
                (rectangleId == "selected" && selectedOptionList.size >= responseMaxSelection) -> return true
                (selectedOptionList.size < responseMaxSelection)                               -> return true
            }
        }

        return (option == "none" && (selectedOptionList.size == 0 || isNoneOptionSelected))
    }
}