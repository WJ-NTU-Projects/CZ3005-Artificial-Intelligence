package ui.helper

import javafx.animation.FadeTransition
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.Node
import javafx.util.Duration
import constants.Constants

class UIFade {
    companion object {
        fun fadeIn(node: Node, event: EventHandler<ActionEvent>? = null) {
            val ft = FadeTransition(Duration.millis(Constants.FADE_DURATION), node)
            ft.fromValue = 0.0
            ft.toValue = 1.0
            if (event != null) ft.onFinished = event
            ft.play()
        }

        fun fadeOut(node: Node, event: EventHandler<ActionEvent>? = null) {
            val ft = FadeTransition(Duration.millis(Constants.FADE_DURATION), node)
            ft.fromValue = 1.0
            ft.toValue = 0.0
            if (event != null) ft.onFinished = event
            ft.play()
        }
    }
}