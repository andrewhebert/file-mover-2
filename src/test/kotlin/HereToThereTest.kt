import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.FileTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class HereToThereTest {

    private lateinit var source: Path
    private lateinit var picsTarget: Path
    private lateinit var vidsTarget: Path
    private lateinit var bakTarget: Path
    private lateinit var pic: Path
    private lateinit var vid: Path
    private lateinit var unknown: Path

    @BeforeEach
    fun before() {
        source = Files.createTempDirectory("htt-source")
        picsTarget = Files.createTempDirectory("htt-pics")
        vidsTarget = Files.createTempDirectory("htt-vids")
        bakTarget = Files.createTempDirectory("htt-bak")
        pic = source.resolve("pic.jpg")
        vid = source.resolve("vid.mpg")
        unknown = source.resolve("file.unknown")
    }

    @Test
    fun `picture file is correctly moved`() {

        val sourceFile = Files.createFile(pic)

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
            pic,
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
    fun `pic is not moved when pic of same name is already there`() {

        //create a file as if it was moved there
        val preMovedFile = picsTarget.resolve("2022/01-January/pic.jpg")
        Files.createDirectories(preMovedFile.parent)
        Files.createFile(preMovedFile)

        Files.createFile(pic)
        Files.setLastModifiedTime(
            pic,
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
        assertTrue(Files.exists(pic))
    }

    @Test
    fun `vid file is correctly moved`() {

        val sourceFile = Files.createFile(vid)

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
            vid,
            sourceFileLastModified
        )

        walk(
            source.toAbsolutePath(),
            picsTarget.toAbsolutePath(),
            vidsTarget.toAbsolutePath(),
            bakTarget.toAbsolutePath()
        )

        val moved = vidsTarget.resolve("2022/01-January/vid.mpg")
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
    fun `vid is not moved when vid of same name is already there`() {

        //create a file as if it was moved there
        val preMovedFile = vidsTarget.resolve("2022/01-January/vid.mpg")
        Files.createDirectories(preMovedFile.parent)
        Files.createFile(preMovedFile)

        Files.createFile(vid)
        Files.setLastModifiedTime(
            vid,
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
        assertTrue(Files.exists(vid))
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

}