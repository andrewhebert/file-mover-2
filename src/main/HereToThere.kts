#!/usr/local/bin/kotlin

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.nio.file.attribute.FileTime
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE
import java.time.format.FormatStyle


main(args)

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
        .forEach { f ->
            var dest: String? = null
            when (f.toFile().extension) {
                "jpg", "jpeg", "png" -> {
                    dest = pictures.toString()
                }

                "mp4", "mpg", "mpeg", "mov" -> {
                    dest = videos.toString()
                }

                else -> {
                    println(
                        "${f.toFile().extension} is not supported.  Ignoring ${
                            f.toAbsolutePath()
                        }"
                    )
                }
            }
            dest?.let {
                val modifiedInst = Instant.ofEpochMilli(Files.getLastModifiedTime(f).toMillis())
                val modifiedZdt = ZonedDateTime.ofInstant(
                    modifiedInst,
                    ZoneId.systemDefault()
                )
                val organized =
                    "${modifiedZdt.year}/${
                        modifiedZdt.monthValue.toString().padStart(2, '0')
                    }-${modifiedZdt.month.name.lowercase().replaceFirstChar { it.uppercaseChar() }}"

                val target = Path.of("${dest}/${organized}/${f.fileName?.toString()}")

                val copied: Path? = try {
                    Files.createDirectories(target.parent)
                    Files.copy(f, target, StandardCopyOption.COPY_ATTRIBUTES)
                    Files.setLastModifiedTime(target, FileTime.from(modifiedInst))
                } catch (e: Exception) {
                    println("There was a problem copying ${f.toAbsolutePath()} to ${target.toAbsolutePath()}. Exception: ${e.message}")
                    e.printStackTrace()
                    null
                }
                copied?.let {
                    println("Copied ${f.toAbsolutePath()} to ${copied.toAbsolutePath()}")
                    val nowZdt = ZonedDateTime.now()
                    val bak = Path.of(
                        "${backup.toAbsolutePath()}/${
                            nowZdt.format(ISO_LOCAL_DATE)
                        }/${nowZdt.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))}/${f.fileName}"
                    )
                    try {
                        Files.createDirectories(bak.parent)
                        Files.move(f, bak)
                    } catch (e: Exception) {
                        println("There was a problem moving and backing up ${f.toAbsolutePath()} to ${bak.toAbsolutePath()}. Exception: ${e.message}")
                        e.printStackTrace()
                    }
                    println("bak'd up ${f.toAbsolutePath()} to ${bak.toAbsolutePath()}")
                }
            }
        }
}

