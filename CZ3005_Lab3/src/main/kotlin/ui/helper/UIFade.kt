package ui.helper

import javafx.animation.FadeTransition
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.Node
import javafx.util.Duration
import constants.Constants
import java.io.IOException

/**
 * Helper class that performs view fading in and out.
 */
class UIFade {
    companion object {
        /**
         * Fades in a node, with the option to of running code after the animation ends.
         * @param node Node to fade in.
         * @param event EventHandler to run after the animation ends.
         */
        fun fadeIn(node: Node, event: EventHandler<ActionEvent>? = null) {
            val ft = FadeTransition(Duration.millis(Constants.FADE_DURATION), node)
            ft.fromValue = 0.0
            ft.toValue = 1.0
            if (event != null) ft.onFinished = event
            ft.play()
        }

        /**
         * Fades out a node, with the option to of running code after the animation ends.
         * @param node Node to fade out.
         * @param event EventHandler to run after the animation ends.
         */
        fun fadeOut(node: Node, event: EventHandler<ActionEvent>? = null) {
            val ft = FadeTransition(Duration.millis(Constants.FADE_DURATION), node)
            ft.fromValue = 1.0
            ft.toValue = 0.0
            if (event != null) ft.onFinished = event
            ft.play()
        }
    }
}