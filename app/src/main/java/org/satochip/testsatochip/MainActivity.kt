package org.satochip.testsatochip

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.serialization.Serializable
import org.satochip.testsatochip.data.NfcResultCode
import org.satochip.testsatochip.data.TestItems
import org.satochip.testsatochip.ui.components.NfcDialog
import org.satochip.testsatochip.ui.theme.TestSatochipTheme
import org.satochip.testsatochip.ui.views.HomeView
import org.satochip.testsatochip.ui.views.ShowLogsView
import org.satochip.testsatochip.ui.views.TestsView
import org.satochip.testsatochip.viewmodels.TestSatochipViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: TestSatochipViewModel by viewModels()

    @Serializable
    object HomeView

    @Serializable
    object TestsView

    @Serializable
    object ShowLogsView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TestSatochipTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val context = LocalContext.current
                    viewModel.setContext(context)
                    val showNfcDialog = remember { mutableStateOf(false) } // for NfcDialog
                    // NfcDialog
                    if (showNfcDialog.value) {
                        NfcDialog(
                            openDialogCustom = showNfcDialog,
                            resultCodeLive = viewModel.resultCodeLive,
                            isConnected = viewModel.isCardConnected
                        )
                    }
                    Navigation(
                        context = context as Activity
                    )
                }
            }
        }
    }

    @Composable
    fun Navigation(
        context: Activity
    ) {
        val showNfcDialog = remember { mutableStateOf(false) } // for NfcDialog
        val navController = rememberNavController()
        if (showNfcDialog.value) {
            NfcDialog(
                openDialogCustom = showNfcDialog,
                resultCodeLive = viewModel.resultCodeLive,
                isConnected = viewModel.isCardConnected
            )
        }
        LaunchedEffect(viewModel.resultCodeLive) {
            if (viewModel.resultCodeLive == NfcResultCode.Ok) {
                navController.navigate(ShowLogsView)
            }
        }


        NavHost(
            navController = navController,
            startDestination = HomeView
        ) {
            composable<HomeView> {
                HomeView(
                    onClick = {
                        showNfcDialog.value = !showNfcDialog.value
                        viewModel.doTests(context, TestItems.ScanCard)
                    },
                    onClickTestsButton = {
                        navController.navigate(TestsView) {
                            popUpTo(0)
                        }
                    }
                )
            }
            composable<TestsView> {
                TestsView(
                    onClick = { item ->
                        when (item) {
                            TestItems.GoBack -> {
                                navController.navigate(HomeView) {
                                    popUpTo(0)
                                }
                            }

                            else -> {
                                showNfcDialog.value = !showNfcDialog.value
                                viewModel.doTests(context, item)
                            }
                        }
                    }
                )
            }
            composable<ShowLogsView> {
                ShowLogsView(
                    onClick = {
                        navController.navigate(HomeView) {
                            popUpTo(0)
                        }
                    }
                )
            }
        }
    }
}