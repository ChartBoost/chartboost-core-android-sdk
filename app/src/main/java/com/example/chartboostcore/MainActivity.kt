package com.example.chartboostcore

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import com.chartboost.chartboostcore.ChartboostCore
import com.chartboost.chartboostcore.initialization.ChartboostCoreInitializableModule
import com.chartboost.chartboostcore.initialization.ChartboostCoreInitializableModuleObserver
import com.chartboost.chartboostcore.initialization.ChartboostCoreModuleInitializationResult
import com.chartboost.chartboostcore.initialization.ChartboostCoreSdkConfiguration
import com.example.chartboostcore.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// This is a sample Activity to demonstrate how the Chartboost Core SDK can be initialized.
// TODO: Also have a Java version of this example.
class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        binding.btnInitializeChartboostCore.setOnClickListener {
            Toast.makeText(this, "Check console for logs", Toast.LENGTH_LONG).show()
            initializeChartboostCoreSdk()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    /**
     * This is a sample implementation of how the Chartboost Core SDK can be initialized.
     */
    private fun initializeChartboostCoreSdk() {
        // To initialize the Chartboost Core SDK, you need to provide a [Context], an [SdkConfiguration],
        // a list of [ChartboostCoreInitializableModule]s, and an [ChartboostCoreInitializableModuleObserver].

        // 1. Get a reference to the current context.
        val context = this

        // 2. Initialize the Chartboost Core SDK preferences and enable debug mode for verbose logging.
        ChartboostCore.initializePreferences(context)
        ChartboostCore.debug = true

        // 3. Start by creating a module.
        // This example instantiates a sample module called ModuleAlpha that does nothing useful.
        // Do the same for any other modules you want to initialize. Note that different modules will
        // expect different parameters in their constructors - what's shown here is just an example.
        val moduleAlpha = ModuleAlpha(appId = "123", someOtherId = "456")

        // 3a. (Optional) Instantiate other modules here.

        // 4. Create a List to hold all the modules you want to initialize.
        // In this example, we are going to include only the sample module created above - ModuleAlpha.
        val modules = listOf<ChartboostCoreInitializableModule>(moduleAlpha)

        // 5. Create a ChartboostCoreSdkConfiguration object.
        // This object currently contains only the Chartboost application identifier, which is given
        // an arbitrary value here. In production, make sure to replace this value with your own
        // Chartboost application identifier.
        val sdkConfiguration =
            ChartboostCoreSdkConfiguration(chartboostApplicationIdentifier = "123")

        // 6. Initialize the Chartboost Core SDK by calling ChartboostCore.initializeSdk().
        // Note that this function is asynchronous, so you can call it from a coroutine.
        CoroutineScope(Dispatchers.Main).launch {
            ChartboostCore.initializeSdk(
                context,
                sdkConfiguration,
                modules,
                // 7. (Optional) Create a ChartboostCoreInitializableModuleObserver object to receive callbacks
                // when the modules finish initializing. Here you can safely call the module's APIs to
                // interact with them.
                object : ChartboostCoreInitializableModuleObserver {
                    override fun onModuleInitializationCompleted(result: ChartboostCoreModuleInitializationResult) {
                        Log.d(
                            "[ChartboostCore]",
                            "Module ${result.module.moduleId} initialization completed with result: $result"
                        )
                    }
                }
            )
        }
    }
}
