package se.alphadev.rest

import com.squareup.okhttp.MediaType
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import com.squareup.okhttp.RequestBody
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.awt.Color
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

data class Rect(val x: Int, val y: Int, val width: Int, val height: Int)

data class Face(val emotion: String, val rect: Rect)

data class ImageMimeType(val mimeType: String)

interface EmotionRenderer {
    fun render(image: ByteArray, faces: List<Face>): Pair<ByteArray, ImageMimeType>
}

class LabelEmotionRenderer : EmotionRenderer {
    override fun render(image: ByteArray, faces: List<Face>): Pair<ByteArray, ImageMimeType> {
        val mimg = ImageIO.read(ByteArrayInputStream(image))

        val g = mimg.createGraphics()
        val fontMetrics = g.fontMetrics

        // TODO: Fix drawing of label, kind of awkward - must be a better way
        for (face in faces) {
            val textWidth = fontMetrics.stringWidth(face.emotion)
            g.color = Color.BLACK
            g.fillRect(face.rect.x, face.rect.y, textWidth + 2, fontMetrics.height + 2)

            g.color = Color.WHITE
            g.drawString(face.emotion, face.rect.x + 1, face.rect.y + fontMetrics.height/2 + 5)
        }

        g.dispose()

        val newImage = ByteArrayOutputStream(image.size)
        ImageIO.write(mimg, "jpg", newImage)

        return Pair(newImage.toByteArray(), ImageMimeType("image/jpeg"))
    }
}

// TODO: Add rate limiting
// TODO: Error handling
// TODO: Check image size - 4MB in emo service?
@RestController
class EmotionService {
    val client = OkHttpClient()

    @Value("\${emo-api.url}")
    var emoUrl: String = ""

    @Value("\${emo-api.key}")
    var emoKey: String = ""

    @RequestMapping("/emotions")
    fun emotions(req: HttpServletRequest, resp: HttpServletResponse) {
        val contentType = req.getHeader("content-type")
        val size = Integer.parseInt(req.getHeader("content-length"))
        if (size <= 0) {
            throw RuntimeException("Invalid content length: " + size)
        }

        val imgBytes = req.inputStream.readBytes(size)
        val img = RequestBody.create(MediaType.parse(contentType), imgBytes)

        val emoReq = Request.Builder()
            .addHeader("Ocp-Apim-Subscription-Key", emoKey)
            .url(emoUrl)
            .post(img)
            .build();

        val emoResp = client.newCall(emoReq).execute();
        val faces = parseFaces(emoResp.body().string())
        val newImage = LabelEmotionRenderer().render(imgBytes, faces)

        resp.addHeader("content-type", newImage.second.mimeType)
        resp.outputStream.write(newImage.first)
    }

    // TODO: Use ObjectMapper for serializeation instead - this is very dirty
    fun parseFaces(json: String): List<Face> {
        val jsonFaces = JSONTokener(json).nextValue()
        if (jsonFaces !is JSONArray) {
            println("Not an JSONarray")
            return listOf()
        }

        val faces = arrayListOf<Face>()

        for (jsonFace in jsonFaces) {
            if (jsonFace !is JSONObject) {
                println("Not json object")
                continue
            }

            val x = jsonFace.getJSONObject("faceRectangle").getInt("left")
            val y = jsonFace.getJSONObject("faceRectangle").getInt("top")
            val w = jsonFace.getJSONObject("faceRectangle").getInt("width")
            val h = jsonFace.getJSONObject("faceRectangle").getInt("height")
            val jsonScores = jsonFace.getJSONObject("scores")
            val scores = arrayListOf<Pair<String, Double>>()

            for (emo in jsonScores.keys()) {
                // TODO: Localize emotion
                scores.add(Pair(emo, jsonScores.getDouble(emo)))
            }

            val strongestEmotion = scores.maxBy { it.second }
            faces.add(Face(strongestEmotion!!.first, Rect(x, y, w, h))) // TODO: Dangerous
        }

        return faces
    }
}
