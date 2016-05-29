package se.alphadev.image

import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.SpringApplicationConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.web.WebAppConfiguration
import se.alphadev.HappyOMeterApplication
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import javax.imageio.ImageIO

/**
 * Runs all images in src/test/resources through all bean-annotated EmotionRenderer implementations
 */
@RunWith(SpringJUnit4ClassRunner::class)
@SpringApplicationConfiguration(classes = arrayOf(HappyOMeterApplication::class))
@WebAppConfiguration
class EmotionRendererTest {

    @Autowired
    lateinit var renderers: List<EmotionRenderer>

    @Test
    fun render() {

        val rect = Rect(0, 0, 0, 0)
        val elements = Pair(EmotionType.surprise.name, 0.9)
        val faces = listOf<Face>(Face(arrayListOf(elements), rect))

        var img: BufferedImage
        val folder = File("src/test/resources")
        //        val file = File("src/test/resources/test.jpg")
        renderers.forEach { renderer ->

            folder.listFiles()
                    .filter { it.isFile }
                    .filter { it.extension.equals("jpg") }
                    .forEachIndexed { i, file ->

                        try {
//                            println(file.canonicalPath)
                            img = ImageIO.read(file);
                            val baos = ByteArrayOutputStream();
                            ImageIO.write(img, "jpg", baos);
                            val imageInByte = baos.toByteArray();
                            val (newImage, mime) = renderer.render(imageInByte, faces, Locale.ENGLISH)
                            val rendererName = renderer.javaClass.simpleName
                            Files.write(Paths.get("target/test_$rendererName$i.jpg"), newImage);

                        } catch (e: Exception) {
                            throw e
                        }
                    }
        }
    }

}
