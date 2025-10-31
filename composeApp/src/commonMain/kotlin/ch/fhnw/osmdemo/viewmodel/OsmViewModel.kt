package ch.fhnw.osmdemo.viewmodel


import kotlin.math.pow
import kotlinx.coroutines.launch
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.fhnw.osmdemo.view.Callout
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.utils.io.*
import org.jetbrains.compose.resources.painterResource
import osmdemo.composeapp.generated.resources.Res
import osmdemo.composeapp.generated.resources.map_marker
import ovh.plrapps.mapcompose.api.addCallout
import ovh.plrapps.mapcompose.api.addLayer
import ovh.plrapps.mapcompose.api.addMarker
import ovh.plrapps.mapcompose.api.centerOnMarker
import ovh.plrapps.mapcompose.api.centroidX
import ovh.plrapps.mapcompose.api.centroidY
import ovh.plrapps.mapcompose.api.enableMarkerDrag
import ovh.plrapps.mapcompose.api.onCalloutClick
import ovh.plrapps.mapcompose.api.onLongPress
import ovh.plrapps.mapcompose.api.onMarkerClick
import ovh.plrapps.mapcompose.api.onMarkerLongPress
import ovh.plrapps.mapcompose.api.onMarkerMove
import ovh.plrapps.mapcompose.api.onTap
import ovh.plrapps.mapcompose.api.removeAllMarkers
import ovh.plrapps.mapcompose.api.removeCallout
import ovh.plrapps.mapcompose.api.removeMarker
import ovh.plrapps.mapcompose.api.scale
import ovh.plrapps.mapcompose.api.scrollTo
import ovh.plrapps.mapcompose.api.setScrollOffsetRatio
import ovh.plrapps.mapcompose.api.visibleArea
import ovh.plrapps.mapcompose.core.TileStreamProvider
import ovh.plrapps.mapcompose.ui.layout.Forced
import ovh.plrapps.mapcompose.ui.state.MapState


expect fun createHttpClient() : HttpClient

/**
 * Shows how to use WMTS tile servers with MapCompose, such as Open Street Map.
 */
class OsmViewModel : ViewModel(){
    private val client = createHttpClient()

    private val TAP_TO_DISMISS_ID = "Tap me to dismiss"
    private val markerColor = Color(0xCC2196F3)

    //private fun createOSMUrl(row: Int, col: Int, zoomLvl: Int): String = "https://tile.openstreetmap.org/$zoomLvl/$col/$row.png"
    //private fun createOSMUrl(row: Int, col: Int, zoomLvl: Int): String = "https://tile.osm.ch/osm-swiss-style/$zoomLvl/$col/$row.png"
    private fun createOSMUrl(row: Int, col: Int, zoomLvl: Int): String = "https://tile.osm.ch/switzerland/$zoomLvl/$col/$row.png"
    //private fun createOSMUrl(row: Int, col: Int, zoomLvl: Int): String = "https://b.tile.opentopomap.org/$zoomLvl/$col/$row.png"


    private val tileStreamProvider = makeOsmTileStreamProvider()

    private val maxLevel = 19
    private val minLevel = 4
    private val tileSize = 256
    private val mapSize  = mapSizeAtLevel(wmtsLevel = maxLevel, tileSize = tileSize)

    private var markerCount = 0

    val state = MapState(levelCount  = maxLevel + 1,
                         fullWidth   = mapSize,
                         fullHeight  = mapSize,
                         workerCount = 16) {
        minimumScaleMode(Forced(1 / 2.0.pow(maxLevel - minLevel))) }
                             .apply {
          addLayer(tileStreamProvider)

          onMarkerMove { id, x, y, _, _ ->
              println("move $id $x $y")
          }
          /**
           * On marker click, add a callout. If the id is [TAP_TO_DISMISS_ID], set auto-dismiss
           * to false. For this particular id, we programmatically remove the callout on tap.
           */
          onMarkerClick { id, x, y ->
              var shouldAnimate by mutableStateOf(true)
              addCallout(id             = id,
                         x              = x,
                         y              = y,
                         absoluteOffset = DpOffset(0.dp, (-50).dp),
                         autoDismiss    = id != TAP_TO_DISMISS_ID,
                         clickable      = id == TAP_TO_DISMISS_ID) {

                  Callout(point         = NormalizedPoint(x, y),
                          title         = id,
                          shouldAnimate = shouldAnimate) {
                      shouldAnimate = false
                  }
              }
          }

          /**
           * Register a click listener on callouts. We don't need to remove the other callouts
           * because they automatically dismiss on touch down.
           */
          onCalloutClick { id, _, _ ->
              if (id == TAP_TO_DISMISS_ID) removeCallout(TAP_TO_DISMISS_ID)
          }

          onMarkerLongPress { id, x, y ->
              println("on marker long press $id $x $y")
              removeMarker(id)
          }

          onTap { x, y ->
              addMarker("marker$markerCount", NormalizedPoint(x, y))
              println("on tap $x $y")
          }

          onLongPress { x, y ->
              println("on long press $x $y")
          }

         // enableRotation()
          setScrollOffsetRatio(0.5f, 0.5f)
    }

    init {
        addMarker("Brugg", FHNW)
        viewModelScope.launch {
            state.centerOnMarker("Brugg", destScale = 0.1)
        }
    }

    fun addMarker(id: String, geoPos : GeoPosition) = addMarker(id, geoPos.asNormalizedWebMercator())

    fun addMarker(id: String, point : NormalizedPoint){
        viewModelScope.launch {
            state.addMarker(id, point.x, point.y) {
                Icon(painter            = painterResource(Res.drawable.map_marker),
                     contentDescription = id,
                     modifier           = Modifier.size(50.dp),
                     tint               = markerColor
                    )
            }
            state.enableMarkerDrag(id)
            markerCount++
        }
    }

    fun addMarkerInCenter() {
        viewModelScope.launch {
            val area = state.visibleArea()
            val centerX = area.p1x + ((area.p2x - area.p1x) * 0.5)
            val centerY = area.p1y + ((area.p4y - area.p1y) * 0.5)
            addMarker("marker$markerCount", NormalizedPoint(centerX, centerY))
        }
    }

    fun clearMarkers(){
        state.removeAllMarkers()
        markerCount = 0
    }

    fun removeMarker(id: String){
        state.removeMarker(id)
        markerCount--
    }

    fun zoomIn() =
        viewModelScope.launch {
            state.scrollTo(x             = state.centroidX,
                           y             = state.centroidY,
                           destScale     = state.scale * 1.5f,
                           animationSpec = TweenSpec(800, easing = FastOutSlowInEasing))
    }

    fun zoomOut() =
        viewModelScope.launch {
            state.scrollTo(x             = state.centroidX,
                           y             = state.centroidY,
                           destScale     = state.scale / 1.5f,
                           animationSpec = TweenSpec(800, easing = FastOutSlowInEasing))
    }

    private fun makeOsmTileStreamProvider(): TileStreamProvider =
        TileStreamProvider { row, col, zoomLvl ->
            try {
                getByteArrayFromURL(createOSMUrl(row, col, zoomLvl)).asRawSource()
            }
            catch (e: Exception) {
                    ByteArray(tileSize).asRawSource()
                }
        }

    /**
     * wmts level are 0 based. At level 0, the map corresponds to just one
     * tile.
     */
    private fun mapSizeAtLevel(wmtsLevel: Int, tileSize: Int): Int = tileSize * 2.0.pow(wmtsLevel).toInt()

    private fun getCacheKey(row: Int, col: Int,  zoom: Int): String = "$zoom/$row/$col"

    private fun ByteArray.asRawSource() = ByteReadChannel(this).asSource()

    private suspend fun getByteArrayFromURL(url: String) : ByteArray = client.get(url).bodyAsBytes()


}


