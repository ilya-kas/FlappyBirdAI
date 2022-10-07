package com.rubon.flappy_bird_ai.desktop

import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration
import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.rubon.flappy_bird_ai.ui.MainGameClass
import com.rubon.flappy_bird_ai.ui.windowHeight
import com.rubon.flappy_bird_ai.ui.windowWidth

fun main(arg: Array<String>) {
    val config = LwjglApplicationConfiguration()
    config.title = "Flappy bird"
    config.height = windowHeight
    config.width = windowWidth
    config.x = 600
    config.y = 40
    config.resizable = false
    LwjglApplication(MainGameClass(), config)
}