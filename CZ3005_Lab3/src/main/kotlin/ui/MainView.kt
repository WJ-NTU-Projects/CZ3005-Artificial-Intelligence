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
import tools.TextToSpeech
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
    private lateinit var textToSpeech: TextToSpeech
    private lateinit var selectionRowsArray: IntArray
    private val selectedOptionList: ArrayList<String> = arrayListOf()
    private var isNoneOptionSelected: Boolean = false
    private var totalCost: Int = 0

    override val root: Parent = stackpane {
        style = "-fx-font-family: 'Verdana'; -fx-background-color: #FFFFFF; -fx-font-size: 14;"
        prefWidth = Constants.WINDOW_WIDTH
        prefHeight = Constants.WINDOW_HEIGHT

        contentNode = vbox {
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

            vbox {
                prefWidth = Constants.WINDOW_WIDTH
                prefHeight = Constants.MAIN_CENTER_HEIGHT
                minHeight = Constants.MAIN_CENTER_HEIGHT
                maxHeight = Constants.MAIN_CENTER_HEIGHT
                alignment = Pos.CENTER_LEFT
                optionsBox = vbox()
            }

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

    init {
        System.out.println(System.getProperty("java.library.path"))
        currentStage?.isResizable = false
        currentStage?.sizeToScene()

        runLater {
            contentNode.requestFocus()
            progressIndicatorLabel.text = "Just a moment! Initialising some modules..."

            runAsync {
                textToSpeech = TextToSpeech()
                Prolog.fallback = "subway_temp.pl"
                resetContent()
            }.setOnSucceeded {
                val responseRows: Array<Array<String>> = Prolog.query("selectionRows", Variable("A"))
                selectionRowsArray = responseRows[0].map { it.toInt() }.toIntArray()

                UIFade.fadeOut(progressIndicatorView, EventHandler {
                    progressIndicatorView.hide()
                    loadContentFromProlog()
                })
            }
        }
    }

    private fun resetContent() {
        val lines: List<String> = File.readPrologFile("subway.pl")
        var s = ""

        for (line in lines) {
            if (s.isNotBlank()) s += "\n"
            s += line
        }

        File.replacePrologFileContent("subway_temp.pl", s)
    }

    private fun loadContentFromProlog() {
        optionsBox.getChildList()?.clear()
        selectedOptionList.clear()
        isNoneOptionSelected = false

        Prolog.consult("subway_temp.pl")
        val prologResponse: Array<Array<String>> = Prolog.query("ask", Variable("A"), Variable("B"), Variable("C"), Variable("D"), Variable("E"), Variable("F"))
        val responseType: String = prologResponse[0][0]
        val responseOptions: Array<String> = prologResponse[1]
        val responseMaxSelection: Int = prologResponse[2][0].toInt()
        val responsePrompt: String = prologResponse[3][0].drop(1).dropLast(1)
        val responseItemPrices: IntArray = prologResponse[4].map { it.toInt() }.toIntArray()
        val responseId: Int = prologResponse[5][0].toInt()

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

        promptLabel.text = prompt
        UIFade.fadeIn(contentNode, EventHandler { textToSpeech.nag(promptLabel.text) })
    }

    private fun loadSelectionContent(responseType: String, responseOptions: Array<String>, responseMaxSelection: Int, responseItemPrices: IntArray, responseId: Int) {
        var rows: Int = selectionRowsArray[responseId]
        var columnsLimit = 99
        val columnsList = ArrayList<Int>()

        if (rows == 99) {
            val moduloThree: Int = responseOptions.size % 3
            columnsLimit = if (moduloThree == 0) 3 else 4
            rows = ceil(1.0 * responseOptions.size / columnsLimit).toInt()
        }

        if (responseOptions.size == 2) rows = 1

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

        val rectangleWidth: Double = (1.0 * Constants.WINDOW_WIDTH / columnsList[0])
        val rectangleHeight: Double = (1.0 * Constants.MAIN_CENTER_HEIGHT / rows)
        val rectangleList = ArrayList<Node>()
        val noneList = ArrayList<Node>()
        val rectangleLabelList = ArrayList<Label>()
        val noneLabelList = ArrayList<Label>()
        var index = 0

        for (row in 0 until rows) {
            val rowRoot: Node = hbox()
            val columns: Int = columnsList[row]

            for (column in 0 until columns) {
                if (index >= responseOptions.size) break
                val option: String = responseOptions[index]
                var displayString: String = option.split("_").joinToString(" ") { it.trim().capitalize() }
                val displayCost: Int = if (responseItemPrices.size == 1) responseItemPrices[0] else responseItemPrices[index]
                displayString += if (displayCost > 0) "\n+\$${displayCost}.00" else "\n "

                val stackPane: StackPane = stackpane {
                    alignment = Pos.CENTER
                    var rectangle: Node

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

                    rectangle.id = "unselected"
                    rectangle.onLeftClick { handleOptionClick(rectangle, responseType, option, displayCost, responseMaxSelection, rectangleList, noneList, rectangleLabelList, noneLabelList) }
                    rectangle.setOnMouseEntered { if (checkHoverLegal(rectangle.id, option, responseMaxSelection)) opacity = 0.8 }
                    rectangle.setOnMouseExited { if (checkHoverLegal(rectangle.id, option, responseMaxSelection)) opacity = 1.0 }

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

                    if (option == "none") noneList.add(rectangle) else rectangleList.add(rectangle)
                    if (option == "none") noneLabelList.add(rectangleLabel) else rectangleLabelList.add(rectangleLabel)
                    StackPane.setAlignment(rectangleLabel, Pos.BOTTOM_CENTER)
                }

                rowRoot.add(stackPane)
                index++
            }

            optionsBox.add(rowRoot)
        }
    }

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

    private fun handleOptionClick(rectangle: Node, responseType: String, option: String, displayCost: Int, responseMaxSelection: Int, rectangleList: ArrayList<Node>, noneList: ArrayList<Node>, rectangleLabelList: ArrayList<Label>, noneLabelList: ArrayList<Label>) {
        if (responseType == "end") {
            close()
            return
        }

        if (rectangle.id == "unselected") {
            if (option == "none" && selectedOptionList.isNotEmpty()) return

            if (selectedOptionList.size >= responseMaxSelection && responseMaxSelection == 1) {
                for ((index, r) in rectangleList.withIndex()) setRectangleState(r, rectangleLabelList[index], RectangleState.DEFAULT)
                for ((index, r) in noneList.withIndex()) setRectangleState(r, noneLabelList[index], RectangleState.DEFAULT)
                selectedOptionList.clear()
                isNoneOptionSelected = false
            }

            if (selectedOptionList.size >= responseMaxSelection || isNoneOptionSelected) return
            if (option == "none") isNoneOptionSelected = true
            selectedOptionList.add("$option,$displayCost")
            setRectangleState(rectangle, null, RectangleState.SELECTED)

            when {
                isNoneOptionSelected                            -> for ((index, r) in rectangleList.withIndex()) if (r.id == "unselected") setRectangleState(r, rectangleLabelList[index], RectangleState.DISABLED)
                selectedOptionList.size >= responseMaxSelection -> for ((index, r) in rectangleList.withIndex()) if (r.id == "unselected") setRectangleState(r, rectangleLabelList[index], RectangleState.DISABLED)
                else                                            -> for ((index, r) in noneList.withIndex()) setRectangleState(r, noneLabelList[index], RectangleState.DISABLED)
            }
        } else if (rectangle.id == "selected") {
            for ((index, r) in rectangleList.withIndex()) if (r.id == "unselected") setRectangleState(r, rectangleLabelList[index], RectangleState.DEFAULT)
            isNoneOptionSelected = false
            selectedOptionList.remove("$option,$displayCost")
            setRectangleState(rectangle, null, RectangleState.DEFAULT)

            if (option != "none" && selectedOptionList.isEmpty()) {
                for ((index, r) in noneList.withIndex()) setRectangleState(r, noneLabelList[index], RectangleState.DEFAULT)
            }
        }
    }

    private fun handleNextClick(responseType: String) {
        if (responseType != "final" && responseType != "end" && selectedOptionList.isEmpty()) {
            error("Please select an item!"); return
        }

        textToSpeech.stfu()

        if (responseType == "end") {
            totalCost = 0
            resetContent()
            Prolog.updateRepeated(1)
            loadContentFromProlog()
            return
        }

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