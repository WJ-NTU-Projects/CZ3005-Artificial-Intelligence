package wjayteo.cz3005.lab3.ui

import javafx.animation.FadeTransition
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Parent
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.scene.text.TextAlignment
import javafx.util.Duration
import org.jpl7.PrologException
import org.jpl7.Variable
import tornadofx.*
import wjayteo.cz3005.lab3.constants.Constants
import wjayteo.cz3005.lab3.tools.File
import wjayteo.cz3005.lab3.tools.Prolog
import java.io.IOException
import java.time.LocalDateTime
import kotlin.math.ceil


class ContentView: View() {
    companion object {
        private val timeNow: LocalDateTime = LocalDateTime.now()
        private val greetings: String = when {
            timeNow.hour < 12 -> "Good morning!"
            timeNow.hour < 19 -> "Good afternoon!"
            else              -> "Good evening!"
        }

        var nextButton: Rectangle? = null
    }

    private var selected: ArrayList<String> = arrayListOf()
    private var noneSelected: Boolean = false
    private var final: Boolean = false
    private val buttonList: ArrayList<Rectangle> = arrayListOf()
    private var noneList: ArrayList<Rectangle> = arrayListOf()
    override val root: Parent = vbox {
    }

    private fun next() {
        val ft = FadeTransition(Duration.millis(Constants.FADE_DURATION), MasterView.rootBox)
        ft.fromValue = 1.0
        ft.toValue = 0.0

        ft.setOnFinished {
            root.getChildList()?.clear()
            buttonList.clear()
            noneList.clear()
            selected.clear()
            nextButton = null
            noneSelected = false
            final = false
            runLater { loadUi() }
        }

        ft.play()
    }

    init {
        runLater { loadUi() }
    }

    private fun loadUi() {
        Prolog.consult("subway_temp.pl")
        val solutions: Array<Array<String>>

        try {
            solutions = Prolog.query("ask", Variable("A"), Variable("B"), Variable("C"), Variable("D"), Variable("E"))
        } catch (e: PrologException) {
            MasterView.printExceptionMessage("${e.message}", "Failed to run prolog query: 'ask'."); return
        }

        if (solutions.size != 5 || solutions[0].size != 1 || solutions[2].size != 1 || solutions[3].size != 1) {
            MasterView.printExceptionMessage("$solutions", "Prolog response is invalid."); return
        }

        val data = solutions[1]

        if (data.isEmpty()) {
            MasterView.printExceptionMessage("$solutions", "Prolog solution is empty."); return
        }

        val type = solutions[0][0]
        if (type == "final") final = true

        var question = solutions[3][0].drop(1).dropLast(1)
        if (type == "meal") question = "$greetings $question"
        MasterView.questionLabel.text = question

        val costList = ArrayList<Int>()
        val count: Int

        try {
            for (cost in solutions[4]) costList.add(cost.toInt())
            count = solutions[2][0].toInt()
        } catch (e: NumberFormatException) {
            MasterView.printExceptionMessage("${e.message}", "Number Format Exception."); return
        }

        val rows: Int = ceil(data.size / 3.0).toInt()
        val columnsList = ArrayList<Int>()

        if (rows > 1) {
            var temp = data.size

            for (row in 0 until rows) {
                if (row == rows - 1) {
                    columnsList.add(temp); break
                }

                var columns: Int = ceil(temp / 2.0).toInt()
                if (columns > 3) columns = 3
                columnsList.add(columns)
                temp -= columns
            }
        } else {
            columnsList.add(data.size)
        }

        if (final) {
            val rootBox = hbox {
                prefWidth = Constants.WINDOW_WIDTH
                prefHeight = Constants.STAGE_CENTER_HEIGHT
                paddingLeft = Constants.FINAL_BOX_MARGIN
                paddingRight = Constants.FINAL_BOX_MARGIN
            }

            val box1 = vbox { prefWidth = Constants.FINAL_LEFT_WIDTH; alignment = Pos.CENTER_LEFT }
            val box2 = vbox { prefWidth = Constants.FINAL_CENTER_WIDTH; alignment = Pos.CENTER_LEFT }
            val box3 = vbox { prefWidth = Constants.FINAL_RIGHT_WIDTH; alignment = Pos.CENTER_RIGHT }

            val solutions2: Array<Array<String>> = Prolog.query("getCosts", Variable("A"))

            if (solutions2.size != 1) {
                MasterView.printExceptionMessage("$solutions", "Prolog response is invalid."); return
            }

            val priceList = ArrayList<Int>()
            val prices: Array<String> = solutions2[0]

            try {
                for (price in prices) priceList.add(price.toInt())
            } catch (e: NumberFormatException) {
                MasterView.printExceptionMessage("${e.message}", "Number Format Exception."); return
            }

            var totalPrice: Int = 0

            for ((index, solution) in data.withIndex()) {
                var text: String
                val header: String
                val price: Int

                try {
                    text = solution.split("_").joinToString(" ") { it.trim().capitalize() }
                    text = text.split(": ").joinToString(": ") { it.split(", ").joinToString(", ") { x -> x.trim().capitalize() } }
                    val split: List<String> = text.split(": ")
                    text = split[1]
                    header = split[0]
                    price = priceList[index]
                    totalPrice += price
                } catch (e: RuntimeException) {
                    MasterView.printExceptionMessage("${e.message}", "Runtime Exception."); return
                }

                box1.add(label(header) {
                    style = "-fx-font-size: 12; -fx-font-weight: bold;"
                    padding = Insets(4.0)
                    wrapTextProperty().value = true
                })

                box2.add(label(text) {
                    style = "-fx-font-size: 12;"
                    padding = Insets(4.0)
                    wrapTextProperty().value = true
                })

                if (price == 0) {
                    box3.add(label("-") {
                        style = "-fx-font-size: 12;"
                        padding = Insets(4.0)
                        wrapTextProperty().value = true
                    })
                } else {
                    box3.add(label("+\$$price.00") {
                        style = "-fx-font-size: 12;"
                        padding = Insets(4.0)
                        wrapTextProperty().value = true
                    })
                }

                box1.add(region {
                    style = "-fx-background-color: #AAAAAA;"
                    prefHeight = 1.0
                })

                box2.add(region {
                    style = "-fx-background-color: #AAAAAA;"
                    prefHeight = 1.0
                })

                box3.add(region {
                    style = "-fx-background-color: #AAAAAA;"
                    prefHeight = 1.0
                })
            }

            box1.add(label(" ") {
                style = "-fx-font-size: 12; -fx-font-weight: bold;"
                padding = Insets(8.0, 4.0, 8.0, 4.0)
                wrapTextProperty().value = true
            })

            box2.add(label("Total") {
                style = "-fx-font-size: 12; -fx-font-weight: bold;"
                padding = Insets(8.0, 4.0, 8.0, 4.0)
                wrapTextProperty().value = true
            })

            box3.add(label("\$${totalPrice}.00") {
                style = "-fx-font-size: 12;"
                padding = Insets(8.0, 4.0, 8.0, 4.0)
                wrapTextProperty().value = true
            })

            rootBox.add(box1)
            rootBox.add(box2)
            rootBox.add(box3)
            root.add(rootBox)
        }

        else {
            var index = 0
            val buttonWidth: Double = Constants.WINDOW_WIDTH / columnsList[0]
            val vbox = vbox {
                prefHeight = Constants.STAGE_CENTER_HEIGHT
                minHeight = Constants.STAGE_CENTER_HEIGHT
                alignment = Pos.CENTER_LEFT
            }

            for (row in 0 until rows) {
                vbox.add(hbox {
                    val columns: Int = columnsList[row]

                    for (column in 0 until columns) {
                        val solution: String
                        var buttonText: String
                        val cost: Int

                        try {
                            solution = data[index]
                            buttonText = solution.split("_").joinToString(" ") { it.trim().capitalize() }
                            buttonText = buttonText.split(": ").joinToString(":\n") { it.split(", ").joinToString(", ") { x -> x.trim().capitalize() } }
                            cost = if (costList.size == 1) costList[0] else costList[index]
                            if (cost > 0) buttonText += "\n+\$${cost}.00"
                        } catch (e: RuntimeException) {
                            MasterView.printExceptionMessage("${e.message}", "Runtime Exception."); return@hbox
                        }

                        stackpane {
                            alignment = Pos.CENTER

                            //height = (Constants.STAGE_CENTER_HEIGHT / rows)
                            val r = rectangle(width = buttonWidth, height = 75) {
                                val fillColor = Constants.colors[4]
                                fill = Color.rgb(200, 200, 200)
                                stroke = Color.DARKGRAY
                                id = "unselected"

                                onLeftClick {
                                    if (id == "unselected" && selected.size < count && !noneSelected) {
                                        if (solution == "none") {
                                            if (selected.isNotEmpty()) return@onLeftClick
                                            noneSelected = true
                                        }

                                        id = "selected"
                                        selected.add("$solution,$cost")
                                        opacity = 1.0
                                        fill = fillColor

                                        if (noneSelected) {
                                            for (button in buttonList) {
                                                if (button.id == "unselected") button.opacity = 0.4
                                            }

                                            return@onLeftClick
                                        }

                                        if (selected.size >= count) {
                                            for (button in buttonList) {
                                                if (button.id == "unselected") button.opacity = 0.4
                                            }
                                        } else {
                                            for (button in noneList) {
                                                button.opacity = 0.4
                                            }
                                        }

                                        return@onLeftClick
                                    }

                                    if (id == "unselected" && selected.size >= count && count == 1) {
                                        for (button in buttonList) {
                                            button.id = "unselected"
                                            button.opacity = 1.0
                                            button.fill = Color.rgb(200, 200, 200)
                                        }

                                        for (button in noneList) {
                                            button.id = "unselected"
                                            button.opacity = 1.0
                                            button.fill = Color.rgb(200, 200, 200)
                                        }

                                        selected.clear()
                                        noneSelected = false

                                        if (solution == "none") noneSelected = true
                                        id = "selected"
                                        selected.add("$solution,$cost")
                                        opacity = 1.0
                                        fill = fillColor

                                        if (noneSelected) {
                                            for (button in buttonList) {
                                                if (button.id == "unselected") button.opacity = 0.4
                                            }

                                            return@onLeftClick
                                        }

                                        if (selected.size >= count) {
                                            for (button in buttonList) {
                                                if (button.id == "unselected") button.opacity = 0.4
                                            }
                                        } else {
                                            for (button in noneList) {
                                                button.opacity = 0.4
                                            }
                                        }

                                        return@onLeftClick
                                    }

                                    if (id == "selected") {
                                        for (button in buttonList) {
                                            if (button.id == "unselected") button.opacity = 1.0
                                        }

                                        id = "unselected"

                                        if (solution == "none") {
                                            noneSelected = false

                                            for (button in noneList) {
                                                if (button.id == "unselected") button.opacity = 1.0
                                            }
                                        } else if (selected.size == 0) {
                                            for (button in noneList) {
                                                if (button.id == "unselected") button.opacity = 1.0
                                            }
                                        }

                                        selected.remove("$solution,$cost")
                                        opacity = 1.0
                                        fill = Color.rgb(200, 200, 200)
                                        return@onLeftClick
                                    }
                                }

                                setOnMouseEntered { if ((id == "selected" && selected.size >= count && solution != "none" && !noneSelected) || (selected.size < count && solution != "none" && !noneSelected) || (solution == "none" && (selected.size == 0 || noneSelected))) opacity = 0.8 }
                                setOnMouseExited { if ((id == "selected" && selected.size >= count && solution != "none" && !noneSelected) || (selected.size < count && solution != "none" && !noneSelected) || (solution == "none" && (selected.size == 0 || noneSelected))) opacity = 1.0 }
                            }

                            if (solution == "none") noneList.add(r)
                            else buttonList.add(r)

                            label(buttonText) {
                                style = "-fx-font-size: 14;"
                                padding = Insets(8.0, 8.0, 8.0, 8.0)
                                wrapTextProperty().value = true
                                textAlignment = TextAlignment.CENTER
                                isMouseTransparent = true
                            }
                        }

                        index++
                    }
                })
            }
            root.add(vbox)
        }

        root.add(region { prefHeight = Constants.STAGE_BOTTOM_MARGIN })

        root.add(stackpane {
            nextButton = rectangle(width = Constants.WINDOW_WIDTH, height = Constants.STAGE_BOTTOM_HEIGHT) {
                fill = Constants.colors[1]

                onLeftClick {
                    if (final) {
                        MasterView.restart()
                        next()
                        return@onLeftClick
                    }

                    if (selected.isEmpty()) {
                        error("Please select an item!"); return@onLeftClick
                    }

                    val fileContent: Array<String>

                    try {
                        fileContent = File.readPrologFile("subway_temp.pl").toTypedArray()
                    } catch (e: IOException) {
                        MasterView.printExceptionMessage("${e.message}", "Failed to perform IO on prolog file."); return@onLeftClick
                    }

                    loop@
                    for ((lineIndex, line) in fileContent.withIndex()) {
                        if (!line.contains("selected($type, nil, 0).")) continue@loop

                        if (selected.size == 1) {
                            val split = selected[0].split(",")
                            fileContent[lineIndex] = "selected($type, ${split[0]}, ${split[1]})."
                            break@loop
                        }

                        var s = ""
                        var totalCost = 0

                        for (item in selected) {
                            val split = item.split(",")
                            if (s.isNotBlank()) s += ", "
                            s += split[0]
                            totalCost += split[1].toInt()
                        }

                        s = "[${s}]"
                        fileContent[lineIndex] = "selected($type, ${s}, ${totalCost})."
                        break@loop
                    }

                    var s = ""

                    for (line in fileContent) {
                        if (s.isNotBlank()) s += "\n"
                        s += line
                    }

                    try {
                        File.replacePrologFileContent("subway_temp.pl", s)
                    } catch (e: IOException) {
                        MasterView.printExceptionMessage("${e.message}", "Failed to perform IO on prolog file."); return@onLeftClick
                    }

                    MasterView.tts.stfu()
                    next()
                }

                setOnMouseEntered { opacity = 0.8 }
                setOnMouseExited { opacity = 1.0 }
            }

            label {
                style = "-fx-font-size: 14;"
                text = if (final) "PLACE NEW ORDER" else "NEXT"
                textFill = Color.BLACK
                wrapTextProperty().value = true
                textAlignment = TextAlignment.CENTER
            }
        })

        val ft = FadeTransition(Duration.millis(Constants.FADE_DURATION), MasterView.rootBox)
        ft.fromValue = 0.0
        ft.toValue = 1.0
        ft.setOnFinished { MasterView.tts.nag(MasterView.questionLabel.text) }
        ft.play()
    }
}