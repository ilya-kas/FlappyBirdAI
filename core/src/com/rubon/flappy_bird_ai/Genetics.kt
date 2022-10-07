package com.rubon.flappy_bird_ai

import com.badlogic.gdx.Gdx
import com.rubon.flappy_bird_ai.neuronet.NeuroNet
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet
import kotlin.random.Random

const val genSize = 10000 //количество особей в поколении
const val generations = 100 //количество поколений
const val mutations = 50   //мутаций в поколении

class Genetics {
    private val gens = 5 //количество генов в особи
    private val gen = ArrayList<ArrayList<Float>>()
    private val bestgen = ArrayList<Float>()
    private var bestgenScore = 0L
    private var bestgenSemiscore = 0f

    val score = ArrayList<Long>() //время жизни
    val semiscore = ArrayList<Float>() //разница по у до центра проёма
    private val random = Random(1204873)

    val nets = ArrayList<NeuroNet>()

    private val logs = Gdx.files.local("Best.txt")
    private val bestOfAll = HashSet<String>()

    init {
        logs.writeString("", false)

        for (i in 0 until genSize) {
            gen += ArrayList<Float>()
            score += 0
            semiscore += 0f
        }
        for (i in 0 until genSize)                 //init
            for (j in 0 until gens)
                gen[i].add(Math.random().toFloat() * 2 - 1)

        for (i in 0 until genSize) {
            nets += NeuroNet()
            nets[i].setupW(gen[i])
        }
        for (x in gen[0])
            bestgen += x
    }

    fun goNext() {
        findBest()

        bestOfAll.add(
            "Score: ${score[0]}, Dist: ${gen[0][0]}, Down: ${gen[0][1]}, Up: ${gen[0][2]}, Speed: ${gen[0][3]}, w: ${gen[0][4]}\n"
        )

        changeWorst()
        evolve()
        mutate();

        Collections.copy(gen[genSize - 1], bestgen)
    }

    fun save() {
        var nom = -1
        for (i in 0 until score.size)
            if (score[i] == 0L){
                nom = i
                break
            }
        if (nom != -1)
            bestOfAll.add(
                "Score: ${score[nom]}, Dist: ${gen[nom][0]}, Down: ${gen[nom][1]}, Up: ${gen[nom][2]}, Speed: ${gen[nom][3]}, w: ${gen[nom][4]}\n"
            )

        for (x in bestOfAll)
            logs.writeString(
            "$x\n".trimIndent(), true
        )
        logs.writeString("\n", true)
    }

    private fun evolve() {
        for (i in 0 until genSize/2) {
            val a = i*2 + (bestOfAll.size % 2)
            val b = i*2+1 + (bestOfAll.size % 2)
            if (b >= genSize) continue

            val p = random.nextInt(gens - 2)
            for (j in 0..p){
                val z = gen[a][j]
                gen[a][j] = gen[b][j]
                gen[b][j] = z
            }
        }
    }

    private fun mutate(){
        for (i in 0 until mutations)
            gen[random.nextInt(genSize)][random.nextInt(gens)] = Math.random().toFloat() * 2 - 1
    }

    private fun sort(l: Int, r: Int) {
        var i = l
        var j = r
        val mid = (l + r) / 2
        do {
            while (score[i] > score[mid] || score[i] == score[mid] && semiscore[i] < semiscore[mid]) i++
            while (score[mid] > score[j] || score[mid] == score[j] && semiscore[mid] < semiscore[j]) j--
            if (i <= j) {
                val z = score[i]
                score[i] = score[j]
                score[j] = z
                val z1 = semiscore[i]
                semiscore[i] = semiscore[j]
                semiscore[j] = z1
                val z2 = gen[i]
                gen[i] = gen[j]
                gen[j] = z2
                i++
                j--
            }
        } while (i <= j)
        if (l < j) sort(l, j)
        if (i < r) sort(i, r)
    }

    private fun findBest() {
        sort(0, genSize - 1)
        if (bestgenScore < score[0] || (bestgenScore == score[0] && bestgenSemiscore > semiscore[0])) {
            bestgenScore = score[0]
            bestgenSemiscore = semiscore[0]
            Collections.copy(bestgen, gen[0])
            println("Score: ${score[0]}, Dist: ${gen[0][0]}, Down: ${gen[0][1]}, Up: ${gen[0][2]}, Speed: ${gen[0][3]}, w: ${gen[0][4]}")
        }
    }

    private fun changeWorst() {
        for (i in genSize / 2 until genSize)
            Collections.copy(gen[i], gen[i - genSize / 2])
    }
}