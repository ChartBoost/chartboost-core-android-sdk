/*
 * Copyright 2023 Chartboost, Inc.
 * 
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.core.canary

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.chartboost.core.ChartboostCoreLogger
import com.chartboost.core.canary.Constants.CHARTBOOST_GREEN_COLOR
import com.chartboost.core.canary.Constants.CHARTBOOST_RED_COLOR
import com.chartboost.core.canary.Constants.CHARTBOOST_YELLOW_COLOR
import com.chartboost.core.canary.Utils.getDeclaredPropsForModule
import com.chartboost.core.error.ChartboostCoreError
import com.chartboost.core.error.ChartboostCoreException
import com.chartboost.core.initialization.InitializableModule
import com.chartboost.core.initialization.SdkConfiguration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject

/**
 * UI for the Initialization tab.
 */
object InitializationTab {
    /**
     * A map of module names to their instances. Leave the values empty for now - they will be
     * set by the user at runtime (hit each module's "Settings" button to set its values).
     *
     * Append new modules to this map as they are added to the app.
     */
    private val modules = mutableMapOf<String, InitializableModule>()

    /**
     * The main tab content.
     */
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun TabContent() {
        val context = LocalContext.current
        val initializationPatterns = listOf(
            "_s0",
            "_s1",
            "_s2",
            "_f0_s0",
            "_f0_s1",
            "_f0_s2",
            "_f1_s0",
            "_f1_s1",
            "_f1_s2",
            "_f2_s0",
            "_f2_s1",
            "_f2_s2",
            "_f0_f0_s0",
            "_f0_f0_s1",
            "_f0_f0_s2",
            "_f0_f1_s0",
            "_f0_f1_s1",
            "_f0_f1_s2",
            "_f0_f2_s0",
            "_f0_f2_s1",
            "_f0_f2_s2",
            "_f1_f0_s0",
            "_f1_f0_s1",
            "_f1_f0_s2",
            "_f1_f1_s0",
            "_f1_f1_s1",
            "_f1_f1_s2",
            "_f1_f2_s0",
            "_f1_f2_s1",
            "_f1_f2_s2",
            "_f2_f0_s0",
            "_f2_f0_s1",
            "_f2_f0_s2",
            "_f2_f1_s0",
            "_f2_f1_s1",
            "_f2_f1_s2",
            "_f2_f2_s0",
            "_f2_f2_s1",
            "_f2_f2_s2",
            "_f0_f0_f0_s0",
            "_f0_f0_f0_s1",
            "_f0_f0_f1_s0",
            "_f0_f0_f1_s1",
            "_f0_f1_f0_s0",
            "_f0_f1_f0_s1",
            "_f0_f1_f1_s0",
            "_f0_f1_f1_s1",
            "_f1_f0_f0_s0",
            "_f1_f0_f0_s1",
            "_f1_f0_f1_s0",
            "_f1_f0_f1_s1",
            "_f1_f1_f0_s0",
            "_f1_f1_f0_s1",
            "_f1_f1_f1_s0",
            "_f1_f1_f1_s1",
            "_f0_f0_f0_f0",
            "_f0_f0_f0_f1",
            "_f0_f0_f1_f0",
            "_f0_f0_f1_f1",
            "_f0_f1_f0_f0",
            "_f0_f1_f0_f1",
            "_f0_f1_f1_f0",
            "_f0_f1_f1_f1",
            "_f1_f0_f0_f0",
            "_f1_f0_f0_f1",
            "_f1_f0_f1_f0",
            "_f1_f0_f1_f1",
            "_f1_f1_f0_f0",
            "_f1_f1_f0_f1",
            "_f1_f1_f1_f0",
            "_f1_f1_f1_f1"
        )

        val initParams = listOf("param1", "param2")

        initializationPatterns.forEachIndexed { _, pattern ->
            val moduleId = "module$pattern"
            val version = "1.0.0"
            val module = createModuleFromPattern(pattern, moduleId, version, initParams)

            modules[moduleId] = module
            ChartboostCoreLogger.d("Created module with ID $moduleId and pattern $pattern")
        }

        // A list of module names.
        val moduleIds = modules.keys.toList()

        // A map of module name to its properties and values. The values are wrapped in a
        // MutableState so that they can be updated.
        val moduleConfigValues = remember {
            mutableStateOf(
                moduleIds.associateWith { moduleId ->
                    getDeclaredPropsForModule(
                        modules,
                        moduleId
                    ).mapValues { values ->
                        mutableStateOf(values.value)
                    }
                }.toMutableMap()
            )
        }

        // An instance of SdkConfiguration to be passed to Core. Leave the values
        // empty for now - they will be set by the user at runtime (hit the "Settings" button
        // to set its values).
        val config = remember { SdkConfiguration("") }

        // So that Canary doesn't have to manually keep track of the properties of SdkConfiguration,
        // use reflection to get the properties and their values.
        val configMap = mutableMapOf<String, MutableState<String>>()
        SdkConfiguration::class.java.declaredFields.forEach { field ->
            field.isAccessible = true

            val fieldName = field.name
            val fieldValue = field.get(config) as? String ?: ""

            configMap[fieldName] = remember { mutableStateOf(fieldValue) }
        }

        val primaryColor = MaterialTheme.colorScheme.primary

        // A map of module name to its icon tint color. The colors are wrapped in a MutableState
        // so that they can be updated. Icons are tinted green if the module is initialized,
        // yellow if it is initializing, and red if it is not initialized.
        val iconTintMap =
            remember { moduleIds.associateWith { mutableStateOf(primaryColor) } }

        // A map of module names to their checked states so we can keep track of which modules
        // the user wants to initialize.
        val checkedStates = remember {
            mutableStateOf(
                moduleIds.associateWith { mutableStateOf(true) }.toMutableMap()
            )
        }

        // Track whether initialization is in progress so that we can show a progress indicator
        val initializationInProgress = remember { mutableStateOf(false) }

        // Track whether initialization has started
        val initializationStarted = remember { mutableStateOf(false) }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = BottomSheetDefaults.SheetPeekHeight)
        ) {
            item {
                Text(
                    text = "Modules to initialize",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(16.dp)
                )
            }

            item {
                Text(
                    text = "Select the modules you want to initialize, and configure their " +
                            "parameters as needed. Then, click the Initialize button to " +
                            "initialize Core and selected modules.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp)
                )
            }

            // TODO: Make list items re-orderable
            items(moduleIds.size) { index ->
                val moduleId = moduleIds[index]

                ModuleListItem(
                    moduleId = moduleId,
                    isChecked = checkedStates.value[moduleId] ?: return@items,
                    iconTint = iconTintMap[moduleId] ?: return@items,
                    itemText = moduleConfigValues.value[moduleId] ?: return@items,
                    initializationStarted
                )
                Divider()
            }

            item {
                if (initializationInProgress.value) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, end = 16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    val showCoreConfigDialog = remember { mutableStateOf(false) }

                    OutlinedButton(
                        onClick = {
                            showCoreConfigDialog.value = true
                        },
                    ) {
                        Text(text = "Configure Core")
                    }
                    Spacer(modifier = Modifier.padding(start = 16.dp))
                    Button(
                        onClick = {
                            CoroutineScope(Main).launch {
                                val checkedModules = checkedStates.value
                                    .filter { it.value.value }
                                    .keys
                                    .map { modules[it]!! }
                                    .toList()

                                // Track whether initialization has started and is in progress separately
                                // so that we can show the status icons for each module even after
                                // initialization is over. Otherwise, the status icons will disappear.
                                initializationInProgress.value = true
                                initializationStarted.value = true

                                // Change the icon tint to yellow to indicate that initialization is in progress
                                iconTintMap.forEach { (_, value) ->
                                    value.value = CHARTBOOST_YELLOW_COLOR
                                }

                                CoreController.initialize(
                                    context,
                                    config,
                                    checkedModules,
                                    iconTintMap
                                )
                                initializationInProgress.value = false
                            }
                        },
                        enabled = !initializationInProgress.value,
                    ) {
                        Text(text = "Initialize Core")
                    }

                    if (showCoreConfigDialog.value) {
                        AlertDialog(
                            showDialog = showCoreConfigDialog,
                            moduleId = "Core",
                            title = "Core Configuration",
                            body = "Enter the app identifier to configure Core’s initialization.",
                            currentText = configMap,
                            onSave = { values ->
                                values.forEach { (key, value) ->
                                    configMap[key]?.value = value.value

                                    config.javaClass.getDeclaredField(key).apply {
                                        isAccessible = true
                                        set(config, value.value)
                                    }
                                }
                            },
                        )
                    }
                }
            }
        }
    }

    /**
     * A list item that represents a module. It contains a checkbox to select the module, and
     * an icon to indicate the module's initialization status.
     *
     * @param moduleId The name of the module
     * @param isChecked The checked state of the checkbox
     * @param iconTint The tint color of the icon
     * @param itemText A map of the module's configuration properties to their values
     * @param initializationStarted Whether initialization has started
     */
    @Composable
    fun ModuleListItem(
        moduleId: String,
        isChecked: MutableState<Boolean>,
        iconTint: MutableState<Color>,
        itemText: Map<String, MutableState<String>>,
        initializationStarted: MutableState<Boolean>,
    ) {
        ListItem(
            headlineContent = { Text(moduleId) },
            leadingContent = {
                Checkbox(
                    checked = isChecked.value,
                    onCheckedChange = { isChecked.value = it })
            },
            trailingContent = {
                Row(
                    modifier = Modifier,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val showModuleConfigDialog = remember { mutableStateOf(false) }

                    if (initializationStarted.value) {
                        Icon(
                            imageVector = when (iconTint.value) {
                                CHARTBOOST_GREEN_COLOR -> Icons.Default.CheckCircle
                                CHARTBOOST_RED_COLOR -> Icons.Default.Clear
                                else -> Icons.Default.Warning // TODO: Change this to ▶︎
                            },
                            contentDescription = "Module Initialization Status",
                            tint = iconTint.value,
                        )
                    }

                    IconButton(onClick = { showModuleConfigDialog.value = true }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Module Configuration",
                        )
                    }

                    if (showModuleConfigDialog.value) {
                        AlertDialog(
                            showModuleConfigDialog,
                            moduleId = moduleId,
                            title = "Module Configuration",
                            body = "Enter the required identifier(s) to configure this module’s initialization.",
                            currentText = itemText,
                            onSave = { values ->
                                setAndConfirmModuleValues(moduleId, values)
                            },
                        )
                    }
                }
            }
        )
    }

    /**
     * Displays an alert dialog with text fields for the given module.
     *
     * @param showDialog Whether to show the dialog
     * @param moduleId The name of the module to configure
     * @param title The title of the dialog
     * @param body The body text of the dialog
     * @param currentText The current text values for the text fields
     * @param onSave The callback to call when the user clicks the Save button
     */
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun AlertDialog(
        showDialog: MutableState<Boolean>,
        moduleId: String,
        title: String,
        body: String,
        currentText: Map<String, MutableState<String>>,
        onSave: (Map<String, MutableState<String>>) -> Unit,
    ) {
        val textFields = remember { mutableStateMapOf<String, MutableState<String>>() }

        // Get the list of properties for the module so we can arrange the text fields in the dialog.
        val fields = getDeclaredPropsForModule(modules, moduleId)
        fields.entries.forEach { entry ->
            textFields[entry.key] = remember { mutableStateOf(entry.value) }
        }

        AlertDialog(
            onDismissRequest = {
                showDialog.value = false
            }
        ) {
            Surface(
                modifier = Modifier
                    .wrapContentWidth()
                    .wrapContentHeight(),
                shape = MaterialTheme.shapes.large,
                tonalElevation = AlertDialogDefaults.TonalElevation
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Text(
                        text = body,
                        style = MaterialTheme.typography.bodyMedium,
                    )

                    LazyColumn {
                        items(currentText.size) { index ->
                            val (fieldName, fieldText) = currentText.entries.elementAt(index)

                            OutlinedTextField(
                                value = fieldText.value,
                                onValueChange = { fieldText.value = it },
                                label = { Text(fieldName) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = {
                                // Clear the text fields
                                textFields.forEach { (_, value) ->
                                    value.value = ""
                                }
                                fields.forEach { (key, value) ->
                                    currentText[key]!!.value = value
                                }
                                showDialog.value = false
                            },
                        ) {
                            Text("Discard")
                        }

                        TextButton(
                            onClick = {
                                onSave(currentText)
                                showDialog.value = false
                            },
                        ) {
                            Text("Save")
                        }
                    }
                }
            }
        }
    }

    /**
     * Sets the values of the properties for the given module and confirms that they were set correctly.
     *
     * @param moduleId The name of the module to set values for
     * @param values The values to set for the module
     */
    private fun setAndConfirmModuleValues(
        moduleId: String,
        values: Map<String, MutableState<String>>,
    ) {
        val module = modules[moduleId] ?: return
        module.javaClass.declaredFields.forEach { field ->
            field.isAccessible = true

            // TODO: This will crash if the field is not a String. We need to handle other types as well.
            field.set(module, values[field.name]?.value ?: "")
        }

        // TODO: Also save to disk so they persist a little more permanently
        // Right now even switching tabs will cause the values to be lost. The values currently
        // are persistent enough within the tab so we can test.

        // Confirm that the properties were set correctly by reading them back out of the module
        // and comparing them to the values in the map.
        module.javaClass.declaredFields.forEach { field ->
            field.isAccessible = true

            val fieldValue = field.get(module)
            val valueFromMap = values[field.name]?.value ?: ""

            if (fieldValue != valueFromMap) {
                Log.d(
                    "[ChartboostCore]",
                    "Mismatch in value for ${field.name}: field value is $fieldValue, value from map is $valueFromMap"
                )
            } else {
                Log.d(
                    "[ChartboostCore]",
                    "Successfully set ${field.name} to $fieldValue for module $moduleId"
                )
            }
        }
    }

    /**
     * Create a module on the fly from a pattern string. The pattern string is a series of actions
     * to perform in order, separated by underscores. Each action is a letter followed by a number.
     *
     * For example, the pattern "_f1_f2_f3_s4" will create a module with the following actions:
     *
     * 0. Initialize module
     * 1. (_f1) Fail after 1 second
     * 2. 💤 Sleep for 1 second (exponential backoff) before retrying
     * 3. (_f2) Fail after 2 seconds
     * 4. 💤 Sleep for 2 seconds (exponential backoff) before retrying
     * 5. (_f3) Fail after 3 seconds
     * 6. 💤 Sleep for 4 seconds (exponential backoff) before retrying
     * 7. (_s4) Succeed after 4 seconds
     *
     * Time failing after called to initialize: 1 + 2 + 3 = 6 seconds
     * Time spent in exponential backoff: 1 + 2 + 4 = 7 seconds
     * Time succeeding: 4 seconds
     * Total time taken to initialize: 6 + 7 + 4 = 17 seconds
     *
     * @param pattern The initialization pattern string to use
     * @param moduleId The ID of the module
     * @param version The version of the module
     * @param initParams Any additional arguments to pass to the module
     *
     * @return The [InitializableModule] instance created from the pattern
     */
    private fun createModuleFromPattern(
        pattern: String,
        moduleId: String,
        version: String,
        initParams: List<String>,
    ): InitializableModule {
        return object : InitializableModule {
            private var currentActionIndex = 0

            override val moduleId = moduleId
            override val moduleVersion = version

            override fun updateProperties(configuration: JSONObject) {
                // NO-OP
            }

            override suspend fun initialize(context: Context): Result<Unit> {
                val actions = pattern.split("_").drop(1)
                if (currentActionIndex >= actions.size) {
                    ChartboostCoreLogger.e("No more actions for module $moduleId")
                    return Result.failure(Exception("No more actions for module $moduleId"))
                }
                val action = actions[currentActionIndex]
                val type = action[0]
                val time = action.substring(1).toInt() * 1000L

                delay(time)

                currentActionIndex++ // Move to the next action

                return if (type == 'f') {
                    ChartboostCoreLogger.d("Returning failure for $moduleId")
                    Result.failure(ChartboostCoreException(ChartboostCoreError.InitializationError.SelfInducedFailure))
                } else {
                    ChartboostCoreLogger.d("Returning success for $moduleId")
                    Result.success(Unit)
                }
            }
        }
    }
}
