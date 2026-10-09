package com.simonecompany.lavagna.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FlipToBack
import androidx.compose.material.icons.outlined.FlipToFront
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simonecompany.lavagna.ui.components.Web
import com.simonecompany.lavagna.vm.BoardViewModel

/** .ctx-menu della web app: min-width 170px, raggio 10px, ombra 0 6px 20px. */
@Composable
fun SelectionContextMenu(vm: BoardViewModel, onDismiss: () -> Unit) {
    val state = vm.contextMenu ?: return
    val strings = LocalStrings.current

    Box(
        Modifier.offset {
            val p = vm.boardToScreen(Offset(state.at.x, state.at.y))
            IntOffset(p.x.toInt(), p.y.toInt())
        }
    ) {
        Column(
            Modifier
                .widthIn(min = 170.dp)
                .shadow(10.dp, RoundedCornerShape(10.dp))
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White)
                .padding(vertical = 4.dp)
        ) {
            ContextItem(Icons.Outlined.ContentCopy, strings["ctxDup"]) {
                vm.contextDuplicate(); onDismiss()
            }
            ContextItem(Icons.Outlined.FlipToFront, strings["ctxFront"]) {
                vm.contextBringToFront(); onDismiss()
            }
            ContextItem(Icons.Outlined.FlipToBack, strings["ctxBack"]) {
                vm.contextSendToBack(); onDismiss()
            }
            ContextItem(Icons.Outlined.Delete, strings["ctxDel"], danger = true) {
                vm.contextDelete(); onDismiss()
            }
        }
    }
}

@Composable
private fun ContextItem(
    icon: ImageVector,
    label: String,
    danger: Boolean = false,
    onClick: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp),
    ) {
        Icon(
            icon,
            null,
            Modifier.size(18.dp),
            tint = if (danger) Web.Danger else Web.TextStrong,
        )
        Text(
            label,
            fontSize = 13.sp,
            color = if (danger) Web.Danger else Web.TextStrong,
        )
    }
}