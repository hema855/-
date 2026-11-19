package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BrokenImage
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.ui.theme.PureWhite
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate900
import com.example.ui.theme.VolcanicOrange
import com.example.util.ReceiptImageStorage

@Composable
fun ReceiptViewerDialog(
    receiptFileName: String,
    title: String,
    subtitle: String? = null,
    amountText: String? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val receiptFile = remember(receiptFileName) {
        ReceiptImageStorage.resolveReceiptFile(context, receiptFileName)
    }
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xF20F172A))
        ) {
            if (receiptFile != null) {
                AsyncImage(
                    model = receiptFile,
                    contentDescription = "صورة الفاتورة: $title",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 84.dp, bottom = 28.dp, start = 8.dp, end = 8.dp)
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                val newScale = (scale * zoom).coerceIn(1f, 4.5f)
                                scale = newScale
                                offset = if (newScale > 1f) {
                                    Offset(offset.x + pan.x, offset.y + pan.y)
                                } else {
                                    Offset.Zero
                                }
                            }
                        }
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offset.x,
                            translationY = offset.y
                        )
                        .testTag("receipt_fullscreen_image")
                )
            } else {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.BrokenImage,
                        contentDescription = null,
                        tint = Slate300,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "تعذر العثور على ملف صورة الفاتورة على الجهاز",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Slate300
                    )
                }
            }

            // Top overlay header
            Surface(
                color = Slate900.copy(alpha = 0.88f),
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            color = PureWhite,
                            fontWeight = FontWeight.Bold
                        )
                        if (subtitle != null || amountText != null) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (subtitle != null) {
                                    Text(
                                        text = subtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Slate300
                                    )
                                }
                                if (amountText != null) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = VolcanicOrange
                                    ) {
                                        Text(
                                            text = amountText,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = PureWhite,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (scale > 1.05f) {
                            IconButton(
                                onClick = {
                                    scale = 1f
                                    offset = Offset.Zero
                                },
                                modifier = Modifier
                                    .background(Color.White.copy(alpha = 0.14f), CircleShape)
                                    .size(42.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.RestartAlt,
                                    contentDescription = "إعادة ضبط التكبير",
                                    tint = PureWhite
                                )
                            }
                        }
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.14f), CircleShape)
                                .size(42.dp)
                                .testTag("close_receipt_viewer_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "إغلاق",
                                tint = PureWhite
                            )
                        }
                    }
                }
            }
        }
    }
}
