package com.den31006.dzmmanager

import java.io.*
import java.nio.charset.StandardCharsets

data class DzmEntry(val name: String, val offset: Long, val size: Long)

object DzmFormat {
    private val MAGIC = byteArrayOf('D'.code.toByte(), 'Z'.code.toByte(), 'M'.code.toByte(), 1)

    fun create(output: File, files: List<File>) {
        val entries = mutableListOf<DzmEntry>()
        var offset = 0L
        for (file in files) {
            entries += DzmEntry(file.name, offset, file.length())
            offset += file.length()
        }

        val manifest = buildString {
            append("{\"version\":1,\"files\":[")
            entries.forEachIndexed { i, e ->
                if (i > 0) append(',')
                append("{\"name\":\"")
                append(e.name.replace("\\", "\\\\").replace("\"", "\\\""))
                append("\",\"offset\":").append(e.offset)
                append(",\"size\":").append(e.size).append('}')
            }
            append("]}")
        }.toByteArray(StandardCharsets.UTF_8)

        DataOutputStream(BufferedOutputStream(FileOutputStream(output))).use { out ->
            out.write(MAGIC)
            out.writeInt(manifest.size)
            out.write(manifest)
            files.forEach { it.inputStream().use { input -> input.copyTo(out) } }
        }
    }

    fun list(file: File): List<String> {
        DataInputStream(BufferedInputStream(FileInputStream(file))).use { input ->
            val magic = ByteArray(4)
            input.readFully(magic)
            require(magic.contentEquals(MAGIC)) { "Not a DZM file" }
            val length = input.readInt()
            val manifest = ByteArray(length)
            input.readFully(manifest)
            val text = String(manifest, StandardCharsets.UTF_8)
            return Regex("\"name\":\"((?:\\\\.|[^\"])*)\"")
                .findAll(text)
                .map { it.groupValues[1].replace("\\\"", "\"").replace("\\\\", "\\") }
                .toList()
        }
    }
}
