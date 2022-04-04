package xyz.vitorvigano.playground.framework

import xyz.vitorvigano.playground.domain.AudioPlayer

class BitmovinPlayer : AudioPlayer {

    override fun play() {
        print("Audio played")
    }

    override fun pause() {
        print("Audio paused")
    }
}