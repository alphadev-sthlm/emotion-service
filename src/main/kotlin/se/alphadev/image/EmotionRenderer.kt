package se.alphadev.image

import java.util.*

interface EmotionRenderer {
    fun render(image: ByteArray, faces: List<Face>, locale: Locale): Pair<ByteArray, ImageMimeType>
}