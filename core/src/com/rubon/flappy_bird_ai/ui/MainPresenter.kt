package com.rubon.flappy_bird_ai.ui

import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.utils.TimeUtils
import com.rubon.flappy_bird_ai.Genetics
import com.rubon.flappy_bird_ai.Tactics
import com.rubon.flappy_bird_ai.generations
import com.rubon.flappy_bird_ai.neuronet.Neuron
import java.lang.Integer.max
import java.nio.channels.Pipe
import java.util.*
import kotlin.ConcurrentModificationException
import kotlin.collections.ArrayList
import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.random.Random

const val g = 0.025f //константы связанные с механикой
const val spawnCd = 1600
const val pipesSpeed = 1/4f
const val jumpCd = 50
const val speedAfterJump = 15f
const val dt = 20 //время между тиками
const val timerT = 4L
const val pipeWidth = 80f

const val bottomPipeMin = 200
const val bottomPipeDelta = 300
const val gapMin = 200
const val gapDelta = 20

const val windowWidth = 600
const val windowHeight = 900
const val birdX = 170f

class Bird(val tactics: Tactics){
    val body = Rectangle(birdX, 400f, 50f, 45f)
    var speed = 0f
    var lastPressed = -jumpCd.toLong()
}

class MainPresenter(val mainGameClass: MainGameClass) {
    private val random = Random(758940231)
    var timer = Timer()
    val genetics = Genetics()
    val birds = ArrayList<Bird>(genetics.nets.size)
    var pipes = ArrayList<Rectangle>()
    private val gap = Rectangle()

    var generation = 0
    var score = 0
    var highscore = 0

    var alive = birds.size
    private var tick = 0L
    private var lastPipe = 0L

    fun reset(){
        highscore = max(score, highscore)
        score = 0
        birds.clear()
        for (i in 0 until genetics.nets.size) {
            val bird = Bird(genetics.nets[i])
            genetics.score[i] = 0
            birds += bird
        }

        alive = birds.size
        pipes.clear()
        generation++
        tick = 0
        lastPipe = 0

        timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {
                try {
                    turn()
                } catch (e: ConcurrentModificationException){}
            }
        }, 0, timerT)
    }

    fun getNearestPipes(): Pair<Rectangle, Rectangle>{
        var nearest = Rectangle(10000f, 0f, pipeWidth, 300f) //поиск ближайших труб
        var companion = Rectangle(10000f, 400f, pipeWidth, 500f)
        for (pipe in pipes) {
            if (pipe.y == 0f) { // если труба нижняя
                if (pipe.x + pipe.width > birdX + 50 && pipe.x < nearest.x) //константа - х + ширана птицы
                    nearest = pipe
            } else
                if (pipe.x + pipe.width > birdX + 50 && pipe.x < companion.x) //константа - х + ширана птицы
                    companion = pipe
        }
        return Pair(nearest, companion)
    }

    private fun turn() {
        synchronized(timer) {
            tick++

            var time = tick * dt
            if (time - lastPipe > spawnCd || pipes.size == 0) {    //создаём трубы
                val down = bottomPipeMin + random.nextInt(bottomPipeDelta)
                val size = gapMin + random.nextInt(gapDelta)
                var pipe = Rectangle(600f, 0f, pipeWidth, down.toFloat())
                pipes.add(pipe)
                pipe = Rectangle(600f, (down + size).toFloat(), pipeWidth, (900 - down - size).toFloat())
                pipes.add(pipe)
                lastPipe = time
            }

            val pair = getNearestPipes()
            val nearest = pair.first //поиск ближайших труб
            val companion = pair.second
            gap.x = nearest.x
            gap.y = nearest.height
            gap.width = nearest.width
            gap.height = companion.y - nearest.height

            for ((i,bird) in birds.withIndex()) {
                if (genetics.score[i] > 0) continue
                bird.body.y += bird.speed - g * dt * dt / 2
                bird.speed -= g * dt
                if (bird.body.y < 0 || bird.body.y > windowHeight - bird.body.height) {
                    die(i, abs(bird.body.y - (nearest.height + gap.height / 2)))
                }
            }

            var inc = false
            val toDel = LinkedList<Rectangle>() //двигаем и удаляем трубы
            for (pipe in pipes) {
                pipe.x -= pipesSpeed * dt
                if (abs(birdX - (pipe.x + pipe.width)) < pipesSpeed) //надо ли увеличивать очки
                    inc = true
                if (pipe.x < -pipe.width) {
                    toDel.add(pipe)
                }
                for ((i,bird) in birds.withIndex()) {
                    if (pipe.overlaps(bird.body)) {
                        die(i, abs(bird.body.y - (nearest.height + gap.height / 2)))
                    }
                }
            }

            for ((i,bird) in birds.withIndex())
                if (genetics.score[i] > 0)
                    bird.body.x -= pipesSpeed * dt
            for (x in toDel)
                pipes.remove(x)
            if (inc)
                score++

            if (alive == 0) {
                if (generation < generations)
                    reset()
                return
            }

            for ((i,bird) in birds.withIndex()) { //ходим
                if (genetics.score[i] > 0) continue
                val input = normalize(
                    nearest.x - bird.body.x,
                    bird.body.y - nearest.height,
                    companion.y - (bird.body.height + bird.body.y),
                    -bird.speed)
                if (bird.tactics.shouldJump(input)) { //прыжки
                    if (time - bird.lastPressed >= jumpCd) {
                        bird.speed = speedAfterJump
                        bird.lastPressed = time
                    }
                }
            }
        }
    }

    fun normalize(distance: Float, down: Float, top: Float, speed: Float): List<Float> {
        val result = ArrayList<Float>()
        result += Neuron.range(distance, -birdX, windowWidth-birdX)
        result += Neuron.range(down, -1.0f*(bottomPipeMin+bottomPipeDelta), -1.0f*(windowHeight-bottomPipeMin))
        result += Neuron.range(top, 1.0f*(bottomPipeMin+gapMin-windowHeight+45f), 1.0f*(bottomPipeMin+bottomPipeDelta+gapMin+gapDelta-45f)) //45f - высота птички\
        result += Neuron.range(speed, -speedAfterJump, g* sqrt(2*windowHeight/g))
        return result
    }

    private fun die(i: Int, l: Float) {
        if (genetics.score[i] > 0) return

        genetics.score[i] = tick
        genetics.semiscore[i] = l
        alive--
        if (alive == 0) {
            timer.cancel()
            timer.purge()
            genetics.goNext()
            if (generation < generations)
                mainGameClass.reset()
        }
    }
}