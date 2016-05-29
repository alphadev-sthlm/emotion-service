package se.alphadev.rest

import com.squareup.okhttp.MediaType
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import com.squareup.okhttp.RequestBody
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import se.alphadev.image.Face
import se.alphadev.image.MemeEmotionRenderer
import se.alphadev.image.Rect
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

// TODO: Add rate limiting
// TODO: Error handling
// TODO: Check image size - 4MB in emo service?
@RestController
class EmotionService {
    val client = OkHttpClient()

    @Autowired
    lateinit var renderer: MemeEmotionRenderer

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
        println(faces)

        val newImage = renderer.render(imgBytes, faces, req.locale)

        resp.addHeader("content-type", newImage.second.mimeType)
        resp.outputStream.write(newImage.first)
    }

    // TODO: Use ObjectMapper for serializeation instead - this is very dirty
    fun parseFaces(json: String): List<Face> {
        val jsonFaces = JSONTokener(json).nextValue()
        if (jsonFaces !is JSONArray) {
            return listOf()
        }

        val faces = arrayListOf<Face>()

        for (jsonFace in jsonFaces) {
            if (jsonFace !is JSONObject) {
                continue
            }

            val x = jsonFace.getJSONObject("faceRectangle").getInt("left")
            val y = jsonFace.getJSONObject("faceRectangle").getInt("top")
            val w = jsonFace.getJSONObject("faceRectangle").getInt("width")
            val h = jsonFace.getJSONObject("faceRectangle").getInt("height")
            val jsonScores = jsonFace.getJSONObject("scores")
            val scores = arrayListOf<Pair<String, Double>>()

            for (emo in jsonScores.keys()) {
                scores.add(Pair(emo, jsonScores.getDouble(emo)))
            }

            val strongestEmotion = scores.maxBy { it.second }
            faces.add(Face(scores, Rect(x, y, w, h))) // TODO: Dangerous
        }

        return faces
    }
}
