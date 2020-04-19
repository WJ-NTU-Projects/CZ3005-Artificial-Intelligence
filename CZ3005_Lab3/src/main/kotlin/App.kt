import javafx.scene.image.Image
import tornadofx.App
import ui.MainView

/**
 * Entry-point of the application.
 * Syntax: App(icon image source (optional), primary view to load)
 */
class App : App(Image("/pineapple.png"), MainView::class)