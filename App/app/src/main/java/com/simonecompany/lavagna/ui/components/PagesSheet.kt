package com.simonecompany.lavagna.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simonecompany.lavagna.ui.LavagnaIcons
import com.simonecompany.lavagna.ui.LocalStrings
import com.simonecompany.lavagna.vm.BoardViewModel

/**
 * Drawer delle pagine: replica .drawer della web app (pannello bianco a destra,
 * 320px, overlay scuro, cards con bordo 2px).
 */
@Composable
fun PagesDrawer(
    vm: BoardViewModel,
    open: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = LocalStrings.current
    val progress by animateFloatAsState(
        targetValue = if (open) 1f else 0f,
        animationSpec = tween(300),
        label = "drawer",
    )

    Box(modifier.fillMaxSize()) {
        if (progress > 0f) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f * progress))
                    .clickable(onClick = onDismiss)
            )
        }

        // pannello: parte da fuori schermo e scorre dentro
        Column(
            Modifier
                .align(Alignment.CenterEnd)
                .padding(end = ((1f - progress) * 320).dp)
                .fillMaxHeight()
                .width(320.dp)
                .background(Web.Sheet)
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    strings["pagesDrawerTitle"],
                    fontSize = 16.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = Web.TextStrong,
                )
                Icon(
                    LavagnaIcons.Close,
                    strings["close"],
                    Modifier
                        .size(20.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onDismiss)
                        .padding(6.dp),
                    tint = Web.Text,
                )
            }
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Web.Border)
            )

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(vm.pages.indices.toList()) { index ->
                    val active = index == vm.pageIndex
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (active) Web.BlueSoft else Color.Transparent)
                            .border(2.dp, if (active) Web.Blue else Web.Border, RoundedCornerShape(10.dp))
                            .clickable { vm.switchPage(index) }
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            strings["pageNumber"].replace("{n}", (index + 1).toString()),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Web.TextStrong,
                            modifier = Modifier.weight(1f),
                        )
                        Icon(
                            LavagnaIcons.Delete,
                            strings["delete"],
                            Modifier
                                .size(18.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { vm.deletePage(index) }
                                .padding(5.dp),
                            tint = Web.Danger,
                        )
                    }
                }
            }

            Box(
                Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Web.Border)
            )

            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                DrawerButton(strings["newPage"], primary = true, onClick = { vm.addPage() })
                DrawerButton(strings["deleteAllPages"], danger = true, onClick = { vm.clearAllPages() })
            }
        }
    }
}

@Composable
private fun DrawerButton(
    label: String,
    primary: Boolean = false,
    danger: Boolean = false,
    onClick: () -> Unit,
) {
    val bg = when {
        primary -> Web.BlueSoft
        danger -> Web.DangerSoft
        else -> Color.Transparent
    }
    val fg = when {
        primary -> Web.Blue
        danger -> Web.Danger
        else -> Web.Text
    }
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .border(1.dp, when {
                primary -> Web.Blue
                danger -> Web.Danger
                else -> Web.Border
            }, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 9.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = fg)
    }
}