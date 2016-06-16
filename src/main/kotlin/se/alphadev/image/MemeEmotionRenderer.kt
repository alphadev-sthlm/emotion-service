package se.alphadev.image

import org.springframework.stereotype.Component
import java.awt.Color
import java.awt.Font
import java.awt.Graphics2D
import java.awt.GraphicsEnvironment
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.*
import javax.imageio.ImageIO

@Component
open class MemeEmotionRenderer : EmotionRenderer {
    val textToImageRatio = 16

    override fun render(image: ByteArray, faces: List<Face>, locale: Locale): Pair<ByteArray, ImageMimeType> {
        val mimg = ImageIO.read(ByteArrayInputStream(image))
        val g = mimg.createGraphics()
        //getAllFonts(g)
        val titleHeight = mimg.height / textToImageRatio
        g.font = Font("Helvetica", 1, titleHeight)

        val emotionType = EmotionType.valueOf(getMostPrevalentEmotion(faces))
        val emo = emotionType.getLocalized(locale)
        val headerWidth = g.fontMetrics.stringWidth(emo)

        //TOP
        val topY = titleHeight
        val topX = mimg.width / 2 - headerWidth / 2
        drawText(g, emo, topX, topY)

        //BOTTOM
        val text = emotionType.getDescription(locale)
        val  mockStr = text.toUpperCase()
        val textHeight = titleHeight - titleHeight / 3
        g.font = Font("Helvetica", 1, textHeight)

        val acc = textAsRows(g, mimg, mockStr)

        val nextY = mimg.height - (acc.size * titleHeight) + titleHeight/2
        drawRows(acc, g, mimg, nextY, titleHeight)

        g.dispose()
        val newImage = ByteArrayOutputStream(image.size)
        ImageIO.write(mimg, "jpg", newImage)
        return Pair(newImage.toByteArray(), ImageMimeType("image/jpeg"))
    }

    private fun drawRows(acc: MutableList<String>, g: Graphics2D, mimg: BufferedImage, startY: Int, titleHeight: Int) {
        var nextY = startY
        for (row in acc) {
            val nextX = mimg.width / 2 - g.fontMetrics.stringWidth(row) / 2
//            println("x$nextX y$nextY  $row")
            drawText(g, row, nextX, nextY)
            nextY += titleHeight
        }
    }

    private fun textAsRows(g: Graphics2D, mimg: BufferedImage, mockStr: String): MutableList<String> {
        val acc = mutableListOf<String>()
        var nextRow = ""
        for (word in mockStr.split(" ")) {
            if (g.fontMetrics.stringWidth(nextRow + " " + word) > mimg.width) {
                acc.add(nextRow)
                nextRow = word
            } else {
                nextRow = nextRow + " " + word
            }
        }
        acc.add(nextRow)
        return acc
    }

    private fun drawText(g: Graphics2D, text: String, x: Int, y: Int): Pair<Int, Int> {
        val outline = Math.max(g.fontMetrics.stringWidth("i") / 8, 1)

        //outline
        g.color = Color.BLACK
        g.drawString(text, ShiftWest(x, outline), ShiftNorth(y, outline));
        g.drawString(text, ShiftWest(x, outline), ShiftSouth(y, outline));
        g.drawString(text, ShiftEast(x, outline), ShiftNorth(y, outline));
        g.drawString(text, ShiftEast(x, outline), ShiftSouth(y, outline));
        //white text
        g.color = Color.WHITE
        g.drawString(text, x, y)
        return Pair(x + g.fontMetrics.stringWidth(text), y + g.fontMetrics.height)
    }

    private fun getMostPrevalentEmotion(faces: List<Face>): String {
        val main_emos = faces.map { it.strongestEmotion() }

        var largest = 0
        var mainType = EmotionType.neutral
        for (e in EmotionType.values()) {
            val count = main_emos.count { e.name.equals(it) }
            if (count > largest) {
                largest = count
                mainType = e
            }
        }
        return mainType.name
    }

    private fun getAllFonts(g: Graphics2D) {
        val e = GraphicsEnvironment.getLocalGraphicsEnvironment()
        val fonts = e.getAllFonts(); // Get the fonts
        val font_size = 20;
        val x = 20;
        var y = 25;
        val line_spacing = 25;
        for (font in fonts) {
            g.font = Font(font.fontName, 1, font_size);
            g.drawString(font.fontName, x, y);
            y += line_spacing;
        }
    }

    fun ShiftNorth(p: Int, distance: Int): Int {
        return (p - distance);
    }

    fun ShiftSouth(p: Int, distance: Int): Int {
        return (p + distance);
    }

    fun ShiftEast(p: Int, distance: Int): Int {
        return (p + distance);
    }

    fun ShiftWest(p: Int, distance: Int): Int {
        return (p - distance);
    }
}