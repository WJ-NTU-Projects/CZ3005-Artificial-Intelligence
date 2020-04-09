package wjayteo.cz3005.lab3.constants

import javafx.scene.paint.Color

class Constants {
    companion object {
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

        const val FADE_DURATION: Double = 100.0
        const val WINDOW_WIDTH: Double = 900.0
        const val STAGE_TOP_HEIGHT: Double = 100.0
        const val STAGE_CENTER_HEIGHT: Double = 300.0
        const val STAGE_BOTTOM_MARGIN: Double = 30.0
        const val STAGE_BOTTOM_HEIGHT: Double = 50.0
        const val STAGE_CONTENT_HEIGHT: Double = STAGE_CENTER_HEIGHT + STAGE_BOTTOM_HEIGHT + STAGE_BOTTOM_MARGIN
        const val FINAL_BOX_MARGIN: Double = 50.0
        const val FINAL_LEFT_WIDTH: Double = 200.0
        const val FINAL_RIGHT_WIDTH: Double = 200.0
        const val FINAL_CENTER_WIDTH: Double = WINDOW_WIDTH - FINAL_LEFT_WIDTH - FINAL_RIGHT_WIDTH
        const val WINDOW_HEIGHT: Double = STAGE_TOP_HEIGHT + STAGE_CONTENT_HEIGHT
    }
}