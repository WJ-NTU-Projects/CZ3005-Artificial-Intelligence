package wjayteo.cz3005.lab3.tools

import java.io.File
import java.io.IOException
import java.nio.channels.FileChannel
import java.nio.file.*

class File {
    companion object {
        @Throws(IOException::class)
        fun readPrologFile(file: String): List<String> {
            val path: Path = Paths.get("prolog/$file")
            createDirectoryIfNotExist(Paths.get("prolog"))
            createFileIfNotExist(path)
            return Files.readAllLines(path)
        }

        @Throws(IOException::class)
        fun appendToPrologFile(file: String, text: String) {
            val path: Path = Paths.get("prolog/$file")
            createDirectoryIfNotExist(Paths.get("prolog"))
            createFileIfNotExist(path)
            Files.write(path, ("\n$text").toByteArray(), StandardOpenOption.APPEND);
//            FileChannel.open(path, StandardOpenOption.WRITE).truncate(0)
//            Files.write(path, text.toByteArray())
        }

        @Throws(IOException::class)
        fun replacePrologFileContent(file: String, text: String) {
            val path: Path = Paths.get("prolog/$file")
            createDirectoryIfNotExist(Paths.get("prolog"))
            createFileIfNotExist(path)
            FileChannel.open(path, StandardOpenOption.WRITE).truncate(0)
            Files.write(path, text.toByteArray())
        }

        private fun getPrologPath(): String {
            val workingDirectory = File(System.getProperty("user.dir"))
            val path: Path = Paths.get("${workingDirectory.path}\\prolog")
            createDirectoryIfNotExist(path)
            return path.toString()
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