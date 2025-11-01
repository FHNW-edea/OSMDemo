package ch.fhnw.osmdemo.viewmodel

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import okio.FileSystem
import okio.Path
import okio.SYSTEM


expect fun createHttpClient(): HttpClient
expect fun platformCacheDir(): Path


class CachingOsmTileLoader() {
    private val fs: FileSystem = FileSystem.SYSTEM

    private val cacheDir = platformCacheDir()

    private val client = createHttpClient()

    private fun createOSMUrl(row: Int, col: Int, zoomLvl: Int): String = "https://tile.openstreetmap.org/$zoomLvl/$col/$row.png"
    //private fun createOSMUrl(row: Int, col: Int, zoomLvl: Int): String = "https://tile.osm.ch/osm-swiss-style/$zoomLvl/$col/$row.png"
    //private fun createOSMUrl(row: Int, col: Int, zoomLvl: Int): String = "https://tile.osm.ch/switzerland/$zoomLvl/$col/$row.png"
    //private fun createOSMUrl(row: Int, col: Int, zoomLvl: Int): String = "https://b.tile.opentopomap.org/$zoomLvl/$col/$row.png"

    private val tileSize = 256

    suspend fun loadTile(row: Int, col: Int, zoomLvl: Int): ByteArray {
        val path = tilePath(zoomLvl, col, row)

        return if (fs.exists(path)) {
            readTile(path)
        } else {
            val url = createOSMUrl(row, col, zoomLvl)

            try {
                val response = client.get(url)
                if (response.status == HttpStatusCode.OK) {
                    val bytes = response.readRawBytes()
                    writeTile(path, bytes)
                    bytes
                } else {
                    ByteArray(tileSize)
                }
            } catch (_: Exception) {
                ByteArray(tileSize)
            }
        }
    }

    private fun readTile(path: Path) = fs.read(path) { readByteArray() }
    private fun writeTile(path: Path, bytes: ByteArray)  = fs.write(path) { write(bytes) }

    private fun tilePath(z: Int, x: Int, y: Int): Path {
        val dir = cacheDir / z.toString() / x.toString()
        if (!fs.exists(dir)) {
            fs.createDirectories(dir)
        }
        return dir / "$y.png"
    }

}