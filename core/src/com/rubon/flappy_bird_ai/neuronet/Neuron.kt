package com.rubon.flappy_bird_ai.neuronet

import kotlin.math.exp

class Neuron(var prev: List<Neuron>?, var w: List<Float>?) {
    var output = 0f
    var sum = 0f

    fun calculate() {
        if (prev == null || w == null)
            return

        sum = w!![w!!.size - 1]
        for (i in prev!!.indices) {
            sum += prev!![i].output * w!![i]
        }
        output = func(sum, false)
    }

    fun update(w: ArrayList<Float>) {
        this.w = w
    }

    /**
     * активационная функция
     * @param mode есть ли отрицательные
     */
    private fun func(x: Float, mode: Boolean): Float {
        if (!mode)
            return (1.0 / (1.0 + exp(-x.toDouble()))).toFloat()
        else
            return ((exp((2 * x).toDouble()) - 1) / (exp((2 * x).toDouble()) + 1)).toFloat()
    }

    companion object{
        /**
         * функция приводит значение в диапазон [0..1]
         * @param _min нижняя граница исходного диапазона
         * @param _max верхняя граница исходного диапазона
         */
        fun range(_x: Float, _min: Float, _max: Float): Float {
            var x = _x
            var max = _max
            x -= _min
            max -= _min
            return x / max
        }
    }
}