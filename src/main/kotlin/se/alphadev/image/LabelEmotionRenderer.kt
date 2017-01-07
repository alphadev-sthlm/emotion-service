package se.alphadev.image

import org.springframework.stereotype.Component
import java.awt.Color
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.*
import javax.imageio.ImageIO

@Component
class LabelEmotionRenderer : EmotionRenderer {
    override fun render(image: ByteArray, faces: List<Face>, locale: Locale): Pair<ByteArray, ImageMimeType> {
        val mimg = ImageIO.read(ByteArrayInputStream(image))
        val g = mimg.createGraphics()
        val fontMetrics = g.fontMetrics

        // TODO: Fix drawing of label, kind of awkward - must be a better way
        for (face in faces) {
            val emotion = EmotionType.valueOf(face.strongestEmotion()).getLocalized(locale)
            val textWidth = fontMetrics.stringWidth(emotion)

            g.color = Color.BLACK
            g.fillRect(face.rect.x, face.rect.y, textWidth + 2, fontMetrics.height + 2)

            g.color = Color.WHITE
            g.drawString(emotion, face.rect.x + 1, face.rect.y + fontMetrics.height/2 + 5)
        }

        g.dispose()

        val newImage = ByteArrayOutputStream(image.size)
        ImageIO.write(mimg, "jpg", newImage)

        return Pair(newImage.toByteArray(), ImageMimeType("image/jpeg"))
    }
}
