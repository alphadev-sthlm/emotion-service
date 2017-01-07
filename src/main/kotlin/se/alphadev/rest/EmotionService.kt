package se.alphadev.rest

import com.squareup.okhttp.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import java.io.InputStream
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.apache.http.HttpVersion.HTTP_1_1
import com.squareup.okhttp.Protocol
import org.springframework.util.MimeType


@RestController
class EmotionService {
    val client = OkHttpClient()

    val MAX_IMAGE_SIZE = 4 * 1024 * 1024

    val LOG = LoggerFactory.getLogger("EmotionService")

    @Value("\${emo-api.url}")
    var emoUrl: String = ""

    @Value("\${emo-api.key}")
    var emoKey: String = ""

    @RequestMapping("/emotions", method = arrayOf(RequestMethod.POST))
    fun emotions(req: HttpServletRequest, resp: HttpServletResponse) {

        val contentType = req.getHeader("content-type")
        val size = Integer.parseInt(req.getHeader("content-length"))

        if (size <= 0 || size > MAX_IMAGE_SIZE) {
            LOG.error("Invalid request image size: " + size)
            resp.status = 500
            return;
        }

        val imgBytes = readImageData(contentType, req.inputStream, size)
        val img = RequestBody.create(MediaType.parse(contentType.replace(";base64", "")), imgBytes)

        val emoReq = Request.Builder()
            .addHeader("Ocp-Apim-Subscription-Key", emoKey)
            .url(emoUrl)
            .post(img)
            .build()

        val empRes = client.newCall(emoReq).execute()
        val empResBody = empRes.body().string()

        LOG.info("Got response {} from {}", empRes , emoUrl)
        LOG.info("... with body {}", empResBody)

        val responseBody = ResponseBody.create(
                MediaType.parse(empRes.header("content-type")), empResBody)

        LOG.info("... responseBody {}", responseBody.string())

        if(empRes.isSuccessful) {
            resp.addHeader("content-type", empRes.header("content-type"))
            resp.outputStream.println( empResBody )
        } else {
            resp.status = 500
        }
        resp.flushBuffer()
    }

    private fun readImageData(contentType: String, input: InputStream, size: Int): ByteArray {
        val bytes = input.readBytes(size)

        if (contentType.endsWith(";base64")) {
            return Base64.getDecoder().decode(bytes)
        }

        return bytes
    }
}
