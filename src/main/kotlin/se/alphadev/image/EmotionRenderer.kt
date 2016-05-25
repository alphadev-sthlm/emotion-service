package se.alphadev.image

import se.alphadev.image.Face
import se.alphadev.image.ImageMimeType

interface EmotionRenderer {
    fun render(image: ByteArray, faces: List<Face>): Pair<ByteArray, ImageMimeType>
}