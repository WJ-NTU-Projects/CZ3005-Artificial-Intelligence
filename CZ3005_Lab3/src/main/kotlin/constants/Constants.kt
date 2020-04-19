package constants

import javafx.scene.paint.Color

/**
 * Static constants referenced by other classes.
 */
class Constants {
    companion object {
        val RECTANGLE_DEFAULT_COLOR: Color = Color.LIGHTGRAY
        val colors: Array<Color> = arrayOf(
            Color.rgb(213, 251, 152),
            Color.rgb(164, 251, 152),
            Color.rgb(152, 251, 189),
            Color.rgb(152, 251, 239),
            Color.rgb(152, 213, 251),
            Color.rgb(152, 164, 251),
            Color.rgb(190, 152, 251),
            Color.rgb(240, 152, 251),
            Color.rgb(251, 152, 212),

            Color.rgb(251, 152, 164),
            Color.rgb(251, 189, 152),
            Color.rgb(251, 239, 152)
        )

        const val FADE_DURATION: Double = 200.0
        const val WINDOW_WIDTH: Double = 1200.0
        const val MAIN_TOP_HEIGHT: Double = 220.0
        const val MAIN_CENTER_HEIGHT: Double = 300.0
        const val MAIN_BOTTOM_HEIGHT: Double = 80.0
        private const val MAIN_INTERACTIVE_HEIGHT: Double = MAIN_CENTER_HEIGHT + MAIN_BOTTOM_HEIGHT
        const val FINAL_BOX_MARGIN: Double = 50.0
        const val FINAL_LEFT_WIDTH: Double = 200.0
        const val FINAL_RIGHT_WIDTH: Double = 200.0
        const val FINAL_CENTER_WIDTH: Double = WINDOW_WIDTH - FINAL_LEFT_WIDTH - FINAL_RIGHT_WIDTH
        const val WINDOW_HEIGHT: Double = MAIN_TOP_HEIGHT + MAIN_INTERACTIVE_HEIGHT
    }
}