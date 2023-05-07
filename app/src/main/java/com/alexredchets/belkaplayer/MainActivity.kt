package com.alexredchets.belkaplayer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.ui.PlayerView
import com.alexredchets.belkaplayer.ui.theme.BelkaPlayerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onResume() {
        super.onResume()

        viewModel.loadVideos()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BelkaPlayerTheme {
                val currentVideo = viewModel.currentVideoData.collectAsState()
                viewModel.errorMessage.collectAsState().value?.let {
                    ErrorDialog(it, viewModel::resetErrorMessage)
                }
                var lifecycle by remember {
                    mutableStateOf(Lifecycle.Event.ON_CREATE)
                }
                val lifecycleOwner = LocalLifecycleOwner.current
                val loading = viewModel.loadingVisible.collectAsState()
                val isLoading by rememberUpdatedState(newValue = loading)
                if (isLoading.value) {
                    Loading()
                }
                DisposableEffect(lifecycleOwner) {
                    val observer = LifecycleEventObserver { _, event -> lifecycle = event }
                    lifecycleOwner.lifecycle.addObserver(observer)

                    onDispose {
                        lifecycleOwner.lifecycle.removeObserver(observer)
                    }
                }
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    TopAppBar(
                        title = {
                            Text(
                                text = stringResource(id = R.string.app_name),
                                color = colorResource(id = R.color.white)
                            )
                        },
                        colors = TopAppBarDefaults.smallTopAppBarColors(
                            containerColor = colorResource(
                                id = R.color.black
                            )
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    AndroidView(
                        factory = { context ->
                            PlayerView(context).apply {
                                player = viewModel.player
                                setShowFastForwardButton(false)
                                setShowRewindButton(false)
                            }
                        },
                        update = {
                            when (lifecycle) {
                                Lifecycle.Event.ON_PAUSE -> {
                                    it.onPause()
                                    it.player?.pause()
                                }
                                Lifecycle.Event.ON_RESUME -> {
                                    it.onResume()
                                }
                                else -> Unit
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16 / 9f)
                            .padding(start = 16.dp, end = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        item {
                            Text(
                                text = currentVideo.value?.title ?: "",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = currentVideo.value?.author ?: "",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)

                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = currentVideo.value?.description ?: "",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)

                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun ErrorDialog(message: String, onDismissDialog: (() -> Unit)? = null) {
        var dismissDialog by remember { mutableStateOf(false) }
        if (dismissDialog) {
            onDismissDialog?.invoke()
            return
        }
        AlertDialog(
            onDismissRequest = {
                // Dismiss the dialog when the user clicks outside the dialog or on the back
                // button. If you want to disable that functionality, simply use an empty
                // onCloseRequest.
                dismissDialog = true
            },
            title = {
                Text(text = getString(R.string.error_title))
            },
            text = {
                Text(message)
            },
            confirmButton = {
                Button(
                    onClick = {
                        dismissDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(
                        contentColor = colorResource(id = R.color.white)
                    )
                ) {
                    Text(getString(R.string.error_ok))
                }
            }
        )
    }

    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    fun Loading() {
        Dialog(
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = false
            ),
            onDismissRequest = { }) {
            Box(
                modifier = Modifier
                    .background(colorResource(id = R.color.loading_background))
                    .fillMaxSize()
            ) {
                CircularProgressIndicator(
                    color = colorResource(id = R.color.white),
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}