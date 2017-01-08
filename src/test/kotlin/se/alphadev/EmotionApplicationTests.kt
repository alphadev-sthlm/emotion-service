package se.alphadev

import com.squareup.okhttp.MediaType
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import com.squareup.okhttp.RequestBody
import org.bouncycastle.asn1.cms.CMSAttributes.contentType
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.SpringApplicationConfiguration
import org.springframework.boot.test.WebIntegrationTest
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.web.WebAppConfiguration
import se.alphadev.rest.EmotionService
import java.io.ByteArrayOutputStream
import java.io.File
import javax.imageio.ImageIO

@RunWith(SpringJUnit4ClassRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class EmotionApplicationTests {

    val client = OkHttpClient()

	@Test
	fun contextLoads() {

        val file = File("src/test/resources/test1.jpg")

        val baos = ByteArrayOutputStream();
        ImageIO.write( ImageIO.read(file), "jpg", baos);

        val emoReq = Request.Builder()
                .url("http://localhost:8090/emotions")
                .post(RequestBody.create( MediaType.parse("application/octet-stream"), baos.toByteArray() ))
                .build()

        val response = client.newCall(emoReq).execute()

        Assert.assertTrue(response.isSuccessful)
        Assert.assertTrue(response.body().string().contains( "faceRectangle" ) )

	}

}
