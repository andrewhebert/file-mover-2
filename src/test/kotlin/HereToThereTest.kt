import com.drew.imaging.ImageMetadataReader
import kotlinx.datetime.Instant
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption.REPLACE_EXISTING
import java.nio.file.attribute.FileTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.io.path.toPath
import kotlin.io.path.writeText

class HereToThereTest {

    private lateinit var source: Path
    private lateinit var picsTarget: Path
    private lateinit var vidsTarget: Path
    private lateinit var bakTarget: Path
    private lateinit var jpeg: Path
    private lateinit var mp4: Path
    private lateinit var unknown: Path

    //no headers
    private val giannisNoHeaders = javaClass.getResource("/giannis.jpg")!!.toURI().toPath()

    //iphone
    private val iphoneMP4 = javaClass.getResource("/iphone.mp4")!!.toURI().toPath()

    //mov file
    private val realMov = javaClass.getResource("/tired.mov")!!.toURI().toPath()

    @BeforeEach
    fun before() {
        source = Files.createTempDirectory("htt-source")
        picsTarget = Files.createTempDirectory("htt-pics")
        vidsTarget = Files.createTempDirectory("htt-vids")
        bakTarget = Files.createTempDirectory("htt-bak")
        jpeg = source.resolve("pic.jpg")
        mp4 = source.resolve("vid.mp4")
        unknown = source.resolve("file.unknown")
    }

    @Test
    fun `picture file with no exif headers is correctly moved, using modified time only`() {

        val sourceFile = Files.copy(giannisNoHeaders, jpeg, REPLACE_EXISTING)

        val sourceLastModified = FileTime.from(
            ZonedDateTime.of(
                2022,
                1,
                1,
                3,
                0,
                0,
                0,
                ZoneId.systemDefault()
            ).toInstant()
        )
        Files.setLastModifiedTime(
            jpeg,
            sourceLastModified
        )

        walk(
            source.toAbsolutePath(),
            picsTarget.toAbsolutePath(),
            vidsTarget.toAbsolutePath(),
            bakTarget.toAbsolutePath()
        )

        val moved = picsTarget.resolve("2022/01-January/pic.jpg")
        assertTrue(Files.exists(moved))
        assertFalse(Files.exists(sourceFile))
        assertEquals(
            sourceLastModified.toInstant().toEpochMilli(),
            Files.getLastModifiedTime(moved).toInstant().toEpochMilli()
        )
        val nowZdt = ZonedDateTime.now()
        //backup exists
        assertTrue(
            Files.exists(
                bakTarget.resolve(nowZdt.format(DateTimeFormatter.ISO_LOCAL_DATE))
                    .resolve(nowZdt.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)))
                    .resolve(sourceFile.fileName)
            )
        )

    }

    @Test
    fun `when file exists of same content is it overwritten`() {
        val fileTime = FileTime.from(
            ZonedDateTime.of(
                2022,
                1,
                1,
                3,
                0,
                0,
                0,
                ZoneId.systemDefault()
            ).toInstant()
        )
        val sourceFile = Files.copy(giannisNoHeaders, jpeg, REPLACE_EXISTING)
        Files.createDirectories(picsTarget.resolve("2022/01-January"))
        val dest = Files.copy(giannisNoHeaders, picsTarget.resolve("2022/01-January/pic.jpg"), REPLACE_EXISTING)

        Files.setLastModifiedTime(jpeg, fileTime)
        Files.setLastModifiedTime(dest, fileTime)

        walk(
            source.toAbsolutePath(),
            picsTarget.toAbsolutePath(),
            vidsTarget.toAbsolutePath(),
            bakTarget.toAbsolutePath()
        )

        val moved = picsTarget.resolve("2022/01-January/pic.jpg")
        assertTrue(Files.exists(moved))
        assertFalse(Files.exists(sourceFile))
        assertEquals(
            fileTime.toInstant().toEpochMilli(),
            Files.getLastModifiedTime(moved).toInstant().toEpochMilli()
        )
        val nowZdt = ZonedDateTime.now()
        //backup exists
        assertTrue(
            Files.exists(
                bakTarget.resolve(nowZdt.format(DateTimeFormatter.ISO_LOCAL_DATE))
                    .resolve(nowZdt.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)))
                    .resolve(sourceFile.fileName)
            )
        )
    }

    @Test
    fun `pic with no exif headers is not moved when pic of same name is already there`() {

        //create a file as if it was moved there
        val preMovedFile = picsTarget.resolve("2022/01-January/pic.jpg")
        Files.createDirectories(preMovedFile.parent)
        Files.createFile(preMovedFile)

        Files.copy(giannisNoHeaders, jpeg, REPLACE_EXISTING)
        Files.setLastModifiedTime(
            jpeg,
            FileTime.from(
                ZonedDateTime.of(
                    2022,
                    1,
                    1,
                    3,
                    0,
                    0,
                    0,
                    ZoneId.systemDefault()
                ).toInstant()
            )
        )

        walk(
            source.toAbsolutePath(),
            picsTarget.toAbsolutePath(),
            vidsTarget.toAbsolutePath(),
            bakTarget.toAbsolutePath()
        )

        //the original pic remains
        assertTrue(Files.exists(jpeg))
    }

    @Test
    fun `mp4 file with no exif headers is correctly moved based on modified time`() {

        val sourceFile = Files.createFile(mp4)

        val sourceFileLastModified = FileTime.from(
            ZonedDateTime.of(
                2022,
                1,
                1,
                3,
                0,
                0,
                0,
                ZoneId.systemDefault()
            ).toInstant()
        )
        Files.setLastModifiedTime(
            mp4,
            sourceFileLastModified
        )

        walk(
            source.toAbsolutePath(),
            picsTarget.toAbsolutePath(),
            vidsTarget.toAbsolutePath(),
            bakTarget.toAbsolutePath()
        )

        val moved = vidsTarget.resolve("2022/01-January/vid.mp4")
        assertTrue(Files.exists(moved))
        assertFalse(Files.exists(sourceFile))
        assertEquals(
            sourceFileLastModified.toInstant().toEpochMilli(),
            Files.getLastModifiedTime(moved).toInstant().toEpochMilli()
        )
        val nowZdt = ZonedDateTime.now()
        //backup exists
        assertTrue(
            Files.exists(
                bakTarget.resolve(nowZdt.format(DateTimeFormatter.ISO_LOCAL_DATE))
                    .resolve(nowZdt.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)))
                    .resolve(sourceFile.fileName)
            )
        )

    }

    @Test
    fun `vid with no exif headers is not moved when vid of same name is already there and content is different`() {

        //create a file as if it was moved there
        val preMovedFile = vidsTarget.resolve("2022/01-January/vid.mp4")
        Files.createDirectories(preMovedFile.parent)
        preMovedFile.writeText("hi")

        Files.createFile(mp4)
        Files.setLastModifiedTime(
            mp4,
            FileTime.from(
                ZonedDateTime.of(
                    2022,
                    1,
                    1,
                    3,
                    0,
                    0,
                    0,
                    ZoneId.systemDefault()
                ).toInstant()
            )
        )

        walk(
            source.toAbsolutePath(),
            picsTarget.toAbsolutePath(),
            vidsTarget.toAbsolutePath(),
            bakTarget.toAbsolutePath()
        )

        //the original pic remains
        assertTrue(Files.exists(mp4))
    }

    @Test
    fun `file remains when extension is not supported`() {
        val sourceFile = Files.createFile(unknown)

        Files.setLastModifiedTime(
            unknown,
            FileTime.from(
                ZonedDateTime.of(
                    2022,
                    1,
                    1,
                    3,
                    0,
                    0,
                    0,
                    ZoneId.systemDefault()
                ).toInstant()
            )
        )

        walk(
            source.toAbsolutePath(),
            picsTarget.toAbsolutePath(),
            vidsTarget.toAbsolutePath(),
            bakTarget.toAbsolutePath()
        )

        assertTrue(Files.exists(sourceFile))
        assertEquals(Files.newDirectoryStream(vidsTarget).count(), 0)
        assertEquals(Files.newDirectoryStream(picsTarget).count(), 0)
    }

    @Test
    fun `test creation date is extracted from mp4`() {
        val metadata = ImageMetadataReader.readMetadata(javaClass.getResourceAsStream("/iphone.mp4"))
        assertEquals(Instant.parse("2023-04-15T16:54:13+00:00"), metadata.mp4Extract())
    }

    @Test
    fun `test creation date is extracted from jpg`() {
        val metadata = ImageMetadataReader.readMetadata(javaClass.getResourceAsStream("/iphone.JPG"))
        assertEquals(Instant.parse("2023-04-15T17:48:03.391+00:00"), metadata.exifExtract())
    }

    @Test
    fun `test creation date is extracted from mov`() {
        val metadata = ImageMetadataReader.readMetadata(javaClass.getResourceAsStream("/tired.mov"))
        assertEquals(Instant.parse("2016-12-11T03:27:19+00:00"), metadata.movExtract())
    }

}