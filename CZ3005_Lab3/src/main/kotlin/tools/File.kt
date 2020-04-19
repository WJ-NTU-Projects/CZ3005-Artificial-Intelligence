package tools

import java.io.File
import java.io.IOException
import java.nio.channels.FileChannel
import java.nio.file.*

class File {
    companion object {
        /**
         * Read every line in a file, collated into a list using File NIO.
         * @param file File name with extension to read from.
         * @throws IOException
         */
        @Throws(IOException::class)
        fun readPrologFile(file: String): List<String> {
            val path: Path = Paths.get("prolog/$file")
            createDirectoryIfNotExist(Paths.get("prolog"))
            createFileIfNotExist(path)
            return Files.readAllLines(path)
        }

        /**
         * Append a line to the end of a file.
         * @param file File name with extension to append to.
         * @param text String content to append.
         * @throws IOException
         */
        @Throws(IOException::class)
        fun appendToPrologFile(file: String, text: String) {
            val path: Path = Paths.get("prolog/$file")
            createDirectoryIfNotExist(Paths.get("prolog"))
            createFileIfNotExist(path)
            Files.write(path, ("\n$text").toByteArray(), StandardOpenOption.APPEND);
        }

        /**
         * Replace the entire content in a file.
         * @param file File name with extension to replace the content of.
         * @param text String content to replace the existing content. For multi-line replacement, use '\n' within the string.
         * @throws IOException
         */
        @Throws(IOException::class)
        fun replacePrologFileContent(file: String, text: String) {
            val path: Path = Paths.get("prolog/$file")
            createDirectoryIfNotExist(Paths.get("prolog"))
            createFileIfNotExist(path)
            FileChannel.open(path, StandardOpenOption.WRITE).truncate(0)
            Files.write(path, text.toByteArray())
        }

        @Throws(IOException::class)
        private fun createDirectoryIfNotExist(path: Path) {
            if (!Files.exists(path, LinkOption.NOFOLLOW_LINKS)) Files.createDirectory(path)
        }

        @Throws(IOException::class)
        private fun createFileIfNotExist(path: Path) {
            if (!Files.exists(path, LinkOption.NOFOLLOW_LINKS)) Files.createFile(path)
        }
    }
}