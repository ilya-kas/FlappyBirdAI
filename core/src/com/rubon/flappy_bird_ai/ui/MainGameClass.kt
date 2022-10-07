package com.rubon.flappy_bird_ai.ui

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.utils.Align

internal const val pipeHeight = 500f

class MainGameClass : ApplicationAdapter() {
    private lateinit var presenter: MainPresenter

    private lateinit var batch: SpriteBatch
    private lateinit var stage: Stage
    private lateinit var tBird: Texture
    private lateinit var tPipe: Texture
    private lateinit var tBackground: Texture
    private lateinit var lGeneration: Label
    var lastSeenScore = 0
    private var spaceWasTapped = false

    override fun create() {
        presenter = MainPresenter(this)
        tBird = Texture(Gdx.files.internal("bird.png"))
        tPipe = Texture(Gdx.files.internal("pipe.png"))
        tBackground = Texture("back.png")

        batch = SpriteBatch()
        stage = Stage(ScreenViewport())

        val style = LabelStyle() //создание надписи номера поколения
        val generator = FreeTypeFontGenerator(Gdx.files.internal("font.ttf")) //загрузка шрифта
        val parameter = FreeTypeFontParameter()
        parameter.size = 20
        val font = generator.generateFont(parameter)
        generator.dispose()
        style.font = font

        lGeneration = Label("Generation: ${presenter.generation}", style)
        lGeneration.setSize(Gdx.graphics.width.toFloat(), 20f)
        lGeneration.setPosition(0f, 860f)
        lGeneration.setAlignment(Align.center)

        stage.addActor(lGeneration)

        reset()
    }

    fun reset() {
        presenter.reset()
        lastSeenScore = 0
        lGeneration.setText(
            """
            Generation: ${presenter.generation}
            Score: ${presenter.score}
            HighScore: ${presenter.highscore}
            """.trimIndent()
        )
    }

    override fun render() {
        synchronized(presenter.timer) {
            Gdx.gl.glClearColor(0f, 0f, 1f, 1f)
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

            if (Gdx.input.isKeyPressed(Input.Keys.SPACE) && !spaceWasTapped){
                presenter.genetics.save()
                spaceWasTapped = true
            } else if (!Gdx.input.isKeyPressed(Input.Keys.SPACE))
                spaceWasTapped = false

            batch.begin()
            batch.draw(tBackground, 0f, 0f, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat()) //фон

            for (bird in presenter.birds) {
                //if (genetics.count1[i]>0) continue;
                batch.draw(tBird, bird.body.x, bird.body.y, bird.body.width, bird.body.height)
            }
            for (pipe in presenter.pipes) {
                if (pipe.y == 0f)
                    batch.draw(tPipe, pipe.x, pipe.height - pipeHeight, pipe.width, pipeHeight)
                else batch.draw(
                    tPipe,
                    pipe.x, pipe.y,
                    0f, 0f, pipe.width, pipeHeight,
                    1f, 1f,
                    0f,
                    0, 0,
                    tPipe.width, tPipe.height,
                    false, true
                )
            }

            /*val pair = presenter.getNearestPipes()
            val sprite = Sprite(tPipe)
            sprite.setColor(0.5f, g, 0.5f, 0.5f)
            sprite.setBounds(pair.first.x, pair.first.y, pair.first.width, pair.first.height)
            sprite.draw(batch)
            sprite.setBounds(pair.second.x, pair.second.y, pair.second.width, pair.second.height)
            sprite.setFlip(false, true)
            sprite.draw(batch)*/

            batch.end()

            stage.act()
            stage.draw()

            if (lastSeenScore != presenter.score) {
                lGeneration.setText(
                    """
                    Generation: ${presenter.generation}
                    Score: ${presenter.score}
                    HighScore: ${presenter.highscore}
                    """.trimIndent()
                )
                lastSeenScore = presenter.score
            }
        }
    }

    override fun dispose() {
        presenter.genetics.save()
        batch.dispose()
        tBird.dispose()
        tPipe.dispose()
    }
}