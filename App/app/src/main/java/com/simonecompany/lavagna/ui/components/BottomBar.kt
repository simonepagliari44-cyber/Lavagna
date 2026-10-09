package com.simonecompany.lavagna.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simonecompany.lavagna.ui.LavagnaIcons
import com.simonecompany.lavagna.ui.LocalStrings
import com.simonecompany.lavagna.vm.BoardViewModel

/**
 * Barra inferiore: replica .bottom-bar della web app (pallina bianca centrata in
 * basso, indicatori pagina e azioni).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BottomBar(
    vm: BoardViewModel,
    onOpenPages: () -> Unit,
    onExport: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = LocalStrings.current
    val m = Metrics.current()
    val iconOnlyLabels = !m.showLabels

    Box(modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        Surface(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            shape = RoundedCornerShape(if (m.showLabels) 22.dp else 16.dp),
            color = Web.Sheet,
            shadowElevation = 8.dp,
        ) {
            FitContentBar(
                gap = m.barGap,
                modifier = Modifier.padding(horizontal = m.barPadH, vertical = m.barPadV),
            ) {
                WebButton(LavagnaIcons.ChevronLeft, iconOnly = true, enabled = vm.pageIndex > 0) {
                    vm.prevPage()
                }

                Text(
                    "${strings["page"]} ${vm.pageIndex + 1} / ${vm.pageCount()}",
                    fontSize = m.indicatorFont.sp,
                    lineHeight = (m.indicatorFont + 2).sp,
                    fontWeight = FontWeight.Medium,
                    color = Web.TextStrong,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(if (m.showLabels) 70.dp else 45.dp),
                )

                WebButton(LavagnaIcons.ChevronRight, iconOnly = true, enabled = vm.pageIndex < vm.pageCount() - 1) {
                    vm.nextPage()
                }

                WebDivider()

                WebButton(LavagnaIcons.Add, label = strings["new"], onClick = { vm.addPage() })

                WebDivider()

                WebButton(LavagnaIcons.Pages, label = strings["pages"], onClick = onOpenPages)

                WebDivider()

                // pulsante Salva: blu pieno come nella web app
                Row(
                    Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Web.Blue)
                        .clickable(onClick = onExport)
                        .padding(horizontal = m.btnPadH, vertical = m.btnPadV),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Icon(LavagnaIcons.Save, null, Modifier.size(m.icon), tint = Color.White)
                    if (!iconOnlyLabels) {
                        Text(
                            strings["save"],
                            fontSize = m.btnFont.sp,
                            lineHeight = m.btnFont.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}
/** .zoom-controls della web app: pallina bianca in basso a destra, in fila. */
@Composable
fun ZoomControls(vm: BoardViewModel, modifier: Modifier = Modifier) {
    val m = Metrics.current()
    Surface(
        modifier = modifier.padding(end = 10.dp, bottom = 60.dp),
        shape = RoundedCornerShape(22.dp),
        color = Web.Sheet,
        shadowElevation = 8.dp,
    ) {
        Row(
            Modifier.padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            WebButton(
                LavagnaIcons.ZoomOut,
                iconOnly = true,
            ) { vm.zoomBy(1f / 1.25f) }
            WebButton(
                LavagnaIcons.ZoomIn,
                iconOnly = true,
            ) { vm.zoomBy(1.25f) }
            Box(
                Modifier
                    .padding(horizontal = 1.dp)
                    .width(1.dp)
                    .height(20.dp)
                    .background(Web.Border)
            )
            WebButton(LavagnaIcons.ZoomReset, iconOnly = true, onClick = { vm.resetZoom() })
        }
    }
}
