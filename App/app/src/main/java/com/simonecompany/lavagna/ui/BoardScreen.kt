package com.simonecompany.lavagna.ui

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.simonecompany.lavagna.export.ImageFormat
import com.simonecompany.lavagna.ui.components.BottomBar
import com.simonecompany.lavagna.ui.components.BackgroundsDialog
import com.simonecompany.lavagna.ui.components.ColorPickerDialog
import com.simonecompany.lavagna.ui.components.ConfirmDialog
import com.simonecompany.lavagna.ui.components.MenuKind
import com.simonecompany.lavagna.ui.components.PagesDrawer
import com.simonecompany.lavagna.ui.components.SaveDialog
import com.simonecompany.lavagna.ui.components.TextEditorOverlay
import com.simonecompany.lavagna.ui.components.TopToolbar
import com.simonecompany.lavagna.ui.components.ZoomControls
import com.simonecompany.lavagna.vm.BoardSerializer
import com.simonecompany.lavagna.vm.BoardViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private sealed interface ConfirmRequest {
    data object ClearPage : ConfirmRequest
    data class DeletePage(val index: Int) : ConfirmRequest
    data object ClearAll : ConfirmRequest
}

private enum class ExportTarget { PNG, JPG, PDF }

/** Schermata principale della lavagna. */
@Composable
fun BoardScreen(vm: BoardViewModel) {
    val context = LocalContext.current
    val strings = LocalStrings.current

    var openMenu by remember { mutableStateOf(MenuKind.NONE) }
    var showPages by remember { mutableStateOf(false) }
    var confirm by remember { mutableStateOf<ConfirmRequest?>(null) }
    var showSave by remember { mutableStateOf(false) }
    var showColorPicker by remember { mutableStateOf(false) }

    fun toast(message: String) = Toast.makeText(context, message, Toast.LENGTH_SHORT).show()

    fun write(uri: Uri?, target: ExportTarget?) {
        if (uri == null || target == null) return
        val ok = when (target) {
            ExportTarget.PNG -> vm.writeImage(uri, ImageFormat.PNG)
            ExportTarget.JPG -> vm.writeImage(uri, ImageFormat.JPG)
            ExportTarget.PDF -> vm.writePdf(uri)
        }
        toast(if (ok) "${strings["save"]}: ${uri.lastPathSegment}" else "Errore")
    }

    val pngLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("image/png")) { write(it, ExportTarget.PNG) }
    val jpgLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("image/jpeg")) { write(it, ExportTarget.JPG) }
    val pdfLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { write(it, ExportTarget.PDF) }

    fun defaultFileName(): String =
        "lavagna " + SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US).format(Date())

    // carica la lavagna salvata e salva quando l'activity va in stop
    val filesDir = remember { context.filesDir }
    LaunchedEffect(Unit) { BoardSerializer.load(vm, filesDir) }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) BoardSerializer.save(vm, filesDir)
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val overlayOpen = openMenu != MenuKind.NONE || showPages || confirm != null ||
        showSave || showColorPicker || vm.textEdit != null || vm.contextMenu != null
    BackHandler(enabled = overlayOpen) {
        when {
            vm.contextMenu != null -> vm.closeContextMenu()
            vm.textEdit != null -> vm.cancelText()
            showColorPicker -> showColorPicker = false
            showSave -> showSave = false
            confirm != null -> confirm = null
            showPages -> showPages = false
            else -> openMenu = MenuKind.NONE
        }
    }

    CompositionLocalProvider(LocalStrings provides Strings.Table(Strings.of(Strings.defaultLang()))) {
        Box(
            Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars)
                .background(Color.White)
        ) {
            BoardSurface(vm, Modifier.fillMaxSize())

            TopToolbar(
                vm = vm,
                openMenu = openMenu,
                onMenuChange = { openMenu = it },
                onClearPage = { confirm = ConfirmRequest.ClearPage },
                onAddCustomColor = {
                    openMenu = MenuKind.NONE
                    showColorPicker = true
                },
            )

            vm.textEdit?.let { TextEditorOverlay(vm) }

            SelectionContextMenu(vm) { vm.closeContextMenu() }

            BottomBar(
                vm = vm,
                onOpenPages = { showPages = true },
                onExport = { showSave = true },
            )

            // come nella web app i controlli zoom compaiono solo in modalita' zoom
            AnimatedVisibility(
                visible = vm.zoomMode,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut(),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 10.dp, bottom = 60.dp),
            ) {
                ZoomControls(vm)
            }
        }

        if (showPages) {
            PagesDrawer(
                vm = vm,
                open = showPages,
                onDismiss = { showPages = false },
            )
        }

        when (val request = confirm) {
            ConfirmRequest.ClearPage -> ConfirmDialog(
                title = strings["clearPageTitle"],
                message = strings["clearPageMsg"],
                onConfirm = { vm.clearPage(); confirm = null },
                onDismiss = { confirm = null },
            )
            is ConfirmRequest.DeletePage -> ConfirmDialog(
                title = strings["deletePageTitle"],
                message = "${strings["deletePageMsg"]} ${strings["page"]} ${request.index + 1}?",
                onConfirm = { vm.deletePage(request.index); confirm = null },
                onDismiss = { confirm = null },
            )
            ConfirmRequest.ClearAll -> ConfirmDialog(
                title = strings["clearAllTitle"],
                message = strings["clearAllMsg"],
                onConfirm = { vm.clearAllPages(); confirm = null },
                onDismiss = { confirm = null },
            )
            null -> Unit
        }

        if (showColorPicker) {
            ColorPickerDialog(
                initial = vm.inkColor,
                onConfirm = {
                    vm.addCustomColor(it)
                    showColorPicker = false
                },
                onDismiss = { showColorPicker = false },
            )
        }

        if (openMenu == MenuKind.BACKGROUNDS) {
            BackgroundsDialog(
                vm = vm,
                onDismiss = { openMenu = MenuKind.NONE },
            )
        }

        if (showSave) {
            SaveDialog(
                initialName = defaultFileName(),
                onSave = { fileName ->
                    showSave = false
                    when {
                        fileName.endsWith(".png", true) -> pngLauncher.launch(fileName)
                        fileName.endsWith(".pdf", true) -> pdfLauncher.launch(fileName)
                        else -> jpgLauncher.launch(fileName)
                    }
                },
                onDismiss = { showSave = false },
            )
        }
    }
}