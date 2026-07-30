package com.shinytracker.feature.scan.impl

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.shinytracker.core.designsystem.ShinyTheme

private val PillShape: Shape = RoundedCornerShape(50)

/** Small draggable pill: screenshot-detect, automated-scan, and close actions. */
@Composable
fun ScanWidgetPill(
    isScanning: Boolean,
    onScreenshot: () -> Unit,
    onAutomatedScan: () -> Unit,
    onClose: () -> Unit,
    onDrag: (dx: Float, dy: Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier =
            modifier.pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onDrag(dragAmount.x, dragAmount.y)
                }
            },
        shape = PillShape,
        shadowElevation = 6.dp,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)) {
            IconButton(onClick = onScreenshot, enabled = !isScanning) {
                Icon(Icons.Filled.CameraAlt, contentDescription = "Screenshot scan")
            }
            IconButton(onClick = onAutomatedScan, enabled = !isScanning) {
                Icon(Icons.Filled.PlayArrow, contentDescription = "Automated box scan")
            }
            IconButton(onClick = onClose, enabled = !isScanning) {
                Icon(Icons.Filled.Close, contentDescription = "Close scan widget")
            }
        }
    }
}

@Preview
@Composable
private fun ScanWidgetPillPreview() {
    ShinyTheme {
        ScanWidgetPill(isScanning = false, onScreenshot = {}, onAutomatedScan = {}, onClose = {}, onDrag = { _, _ -> })
    }
}
