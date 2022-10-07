package com.rubon.flappy_bird_ai.neuronet

import com.rubon.flappy_bird_ai.Tactics
import kotlin.collections.ArrayList

class NeuroNet : Tactics {
    private val input = ArrayList<Neuron>()
    private val output = ArrayList<Neuron>()

    init {
        input += Neuron(null, null)
        input += Neuron(null, null)
        input += Neuron(null, null)
        input += Neuron(null, null)

        output += Neuron(input, listOf(0.2f, 0.2f, 0.2f, 0.2f, -0.6f))
    }

    private fun activate(): Float {
        output[0].calculate()
        return output[0].output
    }

    override fun shouldJump(_input: List<Float>): Boolean {
        for (i in 0 until input.size)
            input[i].output = _input[i]
        return activate() > 0.5
    }

    //устанавливает ссылки на массив со значениями w
    fun setupW(w: ArrayList<Float>) {
        output[0].update(w)
    }
}