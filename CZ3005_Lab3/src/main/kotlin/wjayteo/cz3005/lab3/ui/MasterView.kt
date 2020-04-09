package wjayteo.cz3005.lab3.ui

import javafx.animation.FadeTransition
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Parent
import javafx.scene.control.Label
import javafx.scene.layout.VBox
import javafx.util.Duration
import tornadofx.*
import wjayteo.cz3005.lab3.constants.Constants
import wjayteo.cz3005.lab3.tools.File
import wjayteo.cz3005.lab3.tools.TextToSpeech
import java.io.IOException


class MasterView : View("Subway Eat Trash - Version 1.0.0") {
    companion object {
        lateinit var tts: TextToSpeech
        var questionLabel: Label by singleAssign()
        var rootBox: VBox by singleAssign()

        fun printExceptionMessage(e: String, message: String) {
            System.err.println("========== EXCEPTION ==========")
            System.err.println(e)
            System.err.println("\n")
            System.err.println(message)
            System.err.println("===============================")
            error("An exception has occurred. Please check the console.")
        }

        fun restart() {
            runLater {
                try {
                    val lines: List<String> = File.readPrologFile("subway.pl")
                    var s = ""

                    for (line in lines) {
                        if (s.isNotBlank()) s += "\n"
                        s += line
                    }

                    File.replacePrologFileContent("subway_temp.pl", s)
                } catch (e: IOException) {
                    e.printStackTrace()
                    error("An error occurred while attempting to perform IO on Prolog file.")
                }
            }
        }
    }

    private val contentView: ContentView by inject()
    private var questionBox: VBox by singleAssign()
    private var progressIndicator: VBox by singleAssign()

    override val root: Parent = stackpane {
        prefWidth = Constants.WINDOW_WIDTH
        prefHeight = Constants.WINDOW_HEIGHT

        rootBox = vbox {
            style = "-fx-background-color: #FFFFFF;"

            vbox {
                prefWidth = Constants.WINDOW_WIDTH
                prefHeight = Constants.STAGE_TOP_HEIGHT
                alignment = Pos.CENTER

                questionLabel = label {
                    style = "-fx-font-size: 16;"
                    padding = Insets(8.0, 8.0, 8.0, 8.0)
                }
            }

            questionBox = vbox {
                prefWidth = Constants.WINDOW_WIDTH
                prefHeight = Constants.STAGE_CONTENT_HEIGHT
                alignment = Pos.CENTER
            }
        }

        progressIndicator = vbox {
            alignment = Pos.CENTER

            progressindicator {
                maxWidth = 100.0
                prefWidth = 100.0
            }

            region { prefHeight = 20.0 }
            label("Just a moment!") { style = "-fx-font-size: 24;" }
        }
    }

    init {
        currentStage?.isResizable = false
        currentStage?.sizeToScene()
        progressIndicator.hide()

        runLater {
            rootBox.requestFocus()
            rootBox.isDisable = true
            progressIndicator.show()

            runAsync {
                tts = TextToSpeech()
                restart()
            }.setOnSucceeded {
                progressIndicator.hide()
                questionBox.add(contentView)
                rootBox.isDisable = false
            }
        }
    }
}