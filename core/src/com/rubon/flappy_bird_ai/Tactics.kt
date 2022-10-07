package com.rubon.flappy_bird_ai

interface Tactics {
    fun shouldJump(_input: List<Float>): Boolean
}