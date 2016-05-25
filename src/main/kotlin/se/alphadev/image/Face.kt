package se.alphadev.image

import java.util.*

class Face(val emotions: ArrayList<Pair<String, Double>>, val rect: Rect) {
    fun strongestEmotion(): String {
        return emotions.maxBy { it.second }!!.first
    }

    override fun toString(): String{
        return "Face(emotions=$emotions, rect=$rect)"
    }

}