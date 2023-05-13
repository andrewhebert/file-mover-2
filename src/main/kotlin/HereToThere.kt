#!/usr/local/bin/kotlin

import com.drew.imaging.ImageMetadataReader
import com.drew.metadata.Metadata
import com.drew.metadata.exif.ExifSubIFDDirectory
import com.drew.metadata.mov.metadata.QuickTimeMetadataDirectory
import com.drew.metadata.mp4.media.Mp4MetaDirectory
import kotlinx.datetime.*
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.nio.file.attribute.FileTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE
import java.time.format.FormatStyle


fun main(args: Array<String>) {
    val source = Path.of(args[0])
    val pictures = Path.of(args[1])
    val videos = Path.of(args[2])
    val backup = Path.of(args[3])
    walk(source, pictures, videos, backup)
}

fun walk(source: Path, pictures: Path, videos: Path, backup: Path) {
    Files.walk(source)
        .filter {
            !Files.isDirectory(it)
        }
        .forEach { path ->
            val (dest, creationDateTime) = path.getInputs(pictures, videos)

            dest?.let {

                val modifiedInst =
                    listOf(creationDateTime, Files.getLastModifiedTime(path).toKotlinInstant()).firstNotNullOf { it }
                val modifiedLdt = modifiedInst.toLocalDateTime(CENTRAL_TIME)
                val organized = modifiedLdt.toOrganizedDir()

                val target = Path.of("${dest}/${organized}/${path.fileName?.toString()}")

                val copied: Path? = path.copyMeTo(target)
                target.setLastModifiedTimeTo(modifiedInst)

                copied?.let {
                    println("Copied ${path.toAbsolutePath()} to ${copied.toAbsolutePath()}")
                    val nowLdt = Clock.System.now().toLocalDateTime(CENTRAL_TIME)
                    val bak = Path.of(
                        "${backup.toAbsolutePath()}/${
                            nowLdt.toJavaLocalDateTime().format(ISO_LOCAL_DATE)
                        }/${
                            nowLdt.toJavaLocalDateTime().format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
                        }/${path.fileName}"
                    )
                    try {
                        Files.createDirectories(bak.parent)
                        Files.move(path, bak)
                    } catch (e: Exception) {
                        println("There was a problem moving and backing up ${path.toAbsolutePath()} to ${bak.toAbsolutePath()}. Exception: ${e.message}")
                        e.printStackTrace()
                    }
                    println("bak'd up ${path.toAbsolutePath()} to ${bak.toAbsolutePath()}")
                }
            }
        }
}

val CENTRAL_TIME = TimeZone.of("America/Chicago")

fun Metadata.exifExtract() =
    try {
        getFirstDirectoryOfType(ExifSubIFDDirectory::class.java)
    } catch (e: Exception) {
        println("problem reading ExifSubIFDDirectory: ${e.message}")
        null
    }?.dateOriginal?.toInstant()?.toKotlinInstant()

fun Metadata.movExtract() =
    try {
        getFirstDirectoryOfType(QuickTimeMetadataDirectory::class.java)
    } catch (e: Exception) {
        println("problem reading QuickTimeMetadataDirectory: ${e.message}")
        null
    }?.getDate(QuickTimeMetadataDirectory.TAG_CREATION_TIME)
        ?.toInstant()?.toKotlinInstant()

fun Metadata.mp4Extract() =
    try {
        getFirstDirectoryOfType(Mp4MetaDirectory::class.java)
    } catch (e: Exception) {
        println("problem reading Mp4MetaDirectory: ${e.message}")
        null
    }?.getDate(Mp4MetaDirectory.TAG_CREATION_TIME)?.toInstant()
        ?.toKotlinInstant()

fun FileTime.toKotlinInstant(): Instant =
    Instant.fromEpochMilliseconds(this.toMillis())

fun LocalDateTime.toOrganizedDir(): String {
    return "${this.year}/${
        this.month.value.toString().padStart(2, '0')
    }-${this.month.name.lowercase().replaceFirstChar { it.uppercaseChar() }}"
}

fun Path.getInputs(pictures: Path, videos: Path): Pair<String?, Instant?> {
    var dest: String? = null
    var creationDateTime: Instant? = null
    when (this.toFile().extension) {
        "jpg", "jpeg" -> {
            dest = pictures.toString()
            creationDateTime =
                this.getMetadata()?.exifExtract()
        }

        "mp4", "mkv" -> {
            dest = videos.toString()
            creationDateTime =
                this.getMetadata()?.mp4Extract()
        }

        else -> {
            println(
                "${this.toFile().extension} is not supported.  Ignoring ${
                    this.toAbsolutePath()
                }"
            )
        }
    }
    return Pair(dest, creationDateTime)

}

fun Path.setLastModifiedTimeTo(instant: Instant): Path {
    return Files.setLastModifiedTime(this, FileTime.from(instant.toJavaInstant()))
}

fun Path.copyMeTo(target: Path): Path? {
    return try {
        Files.createDirectories(target.parent)
        Files.copy(this, target, StandardCopyOption.COPY_ATTRIBUTES)
    } catch (e: Exception) {
        println("There was a problem copying ${this.toAbsolutePath()} to ${target.toAbsolutePath()}. Exception: ${e.message}")
        e.printStackTrace()
        null
    }

}

fun Path.getMetadata(): Metadata? {
    return try {
        ImageMetadataReader.readMetadata(this.toFile())
    } catch (e: Exception) {
        println("problem with reading metadata on ${this}: ${e.message}")
        return null
    }
}