package ch.fhnw.osmdemo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ch.fhnw.osmdemo.viewmodel.OsmViewModel
import ovh.plrapps.mapcompose.ui.MapUI
import ovh.plrapps.mapcompose.ui.state.MapState

@Composable
fun App() {
    val osm = remember { OsmViewModel() }

    MaterialTheme {
        Box(modifier = Modifier.windowInsetsPadding(WindowInsets.systemBars)
                               .fillMaxSize()){

            MapWithZoomControl(osm, Modifier.align(Alignment.Center )
                                            .fillMaxSize())

            Button(onClick  = { osm.addMarkerInCenter() },
                   modifier = Modifier.padding(8.dp).align(Alignment.BottomCenter)) {
                Text(text = "Add marker")
            }
        }
    }
}


@Composable
fun MapWithZoomControl(vm: OsmViewModel, modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        MapPanel(vm.state, Modifier.align(Alignment.TopStart))
        Column(modifier           = Modifier.align(Alignment.TopEnd)
                                            .padding(top = 10.dp, end = 10.dp)
                                            .background(color = Color(0xCC2196F3).copy(alpha = 0.3f),
                                                        shape = RoundedCornerShape(20.dp)),
               horizontalAlignment = Alignment.CenterHorizontally,
               verticalArrangement = Arrangement.spacedBy(5.dp)) {
            IconButton(onClick = { vm.zoomIn() }){
                Icon(imageVector        = Icons.Filled.ZoomIn,
                     contentDescription = "Zoom in",
                     modifier           = Modifier.padding(top = 5.dp).size(40.dp))
            }
            IconButton(onClick = { vm.zoomOut() }){
                Icon(imageVector        = Icons.Filled.ZoomOut,
                     contentDescription = "Zoom out",
                     modifier           = Modifier.padding(bottom = 5.dp).size(40.dp))
            }
        }
    }
}

@Composable
fun MapPanel(state: MapState, modifier: Modifier = Modifier) {
    MapUI(modifier = modifier,
          state    = state)
}