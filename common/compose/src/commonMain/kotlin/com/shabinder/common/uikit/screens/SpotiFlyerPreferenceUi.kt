package com.shabinder.common.uikit.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.RadioButton
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.SnippetFolder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.jetbrains.subscribeAsState
import com.shabinder.common.models.AudioQuality
import com.shabinder.common.preference.SpotiFlyerPreference
import com.shabinder.common.translations.Strings
import com.shabinder.common.uikit.configurations.SpotiFlyerTypography
import com.shabinder.common.uikit.configurations.colorAccent
import com.shabinder.common.uikit.configurations.colorOffWhite

@Composable
fun SpotiFlyerPreferenceContent(component: SpotiFlyerPreference) {
    val model by component.model.subscribeAsState()

    val stateVertical = rememberScrollState(0)

    Column(Modifier.fillMaxSize().padding(8.dp).verticalScroll(stateVertical)) {
        Spacer(Modifier.padding(top = 16.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, Color.Gray)
        ) {
            Column(Modifier.padding(12.dp)) {
                Text(
                    text = Strings.preferences(),
                    style = SpotiFlyerTypography.body1,
                    color = colorAccent
                )
                Spacer(modifier = Modifier.padding(top = 12.dp))

                SettingsRow(
                    icon = rememberVectorPainter(Icons.Rounded.MusicNote),
                    title = "Preferred Audio Quality",
                    value = model.preferredQuality.kbps + "KBPS"
                ) { save ->
                    val audioQualities = AudioQuality.values()

                    audioQualities.forEach { quality ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = (quality == model.preferredQuality),
                                    onClick = {
                                        component.setPreferredQuality(quality)
                                        save()
                                    }
                                )
                                .padding(horizontal = 16.dp, vertical = 2.dp)
                        ) {
                            RadioButton(
                                selected = (quality == model.preferredQuality),
                                onClick = {
                                    component.setPreferredQuality(quality)
                                    save()
                                }
                            )
                            Text(
                                text = quality.kbps + " KBPS",
                                style = SpotiFlyerTypography.h6,
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.padding(top = 12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().clickable(
                        onClick = { component.selectNewDownloadDirectory() }
                    )
                ) {
                    Icon(Icons.Rounded.SnippetFolder, Strings.setDownloadDirectory(), Modifier.size(32.dp), tint = Color(0xFFCCCCCC))
                    Spacer(modifier = Modifier.padding(start = 16.dp))
                    Column {
                        Text(
                            text = Strings.setDownloadDirectory(),
                            style = SpotiFlyerTypography.h6
                        )
                        Text(
                            text = model.downloadPath,
                            style = SpotiFlyerTypography.subtitle2
                        )
                    }
                }

                Spacer(Modifier.padding(top = 12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth()
                        .clickable(
                            onClick = { component.toggleAnalytics(!model.isAnalyticsEnabled) }
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    @Suppress("DuplicatedCode")
                    Icon(Icons.Rounded.Insights, Strings.analytics() + Strings.status(), Modifier.size(32.dp))
                    Spacer(modifier = Modifier.padding(start = 16.dp))
                    Column(
                        Modifier.weight(1f)
                    ) {
                        Text(
                            text = Strings.analytics(),
                            style = SpotiFlyerTypography.h6
                        )
                        Text(
                            text = Strings.analyticsDescription(),
                            style = SpotiFlyerTypography.subtitle2
                        )
                    }
                    Switch(
                        checked = model.isAnalyticsEnabled,
                        onCheckedChange = null,
                        colors = SwitchDefaults.colors(uncheckedThumbColor = colorOffWhite)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.padding(top = 8.dp))
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SettingsRow(
    icon: Painter,
    title: String,
    value: String,
    editContent: @Composable ColumnScope.(() -> Unit) -> Unit
) {

    var isEditMode by remember { mutableStateOf(false) }
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().clickable(
                onClick = { isEditMode = !isEditMode }
            ).padding(vertical = 6.dp)
        ) {
            Icon(icon, title, Modifier.size(32.dp), tint = Color(0xFFCCCCCC))
            Spacer(modifier = Modifier.padding(start = 16.dp))
            Column {
                Text(
                    text = title,
                    style = SpotiFlyerTypography.h6
                )
                Text(
                    text = value,
                    style = SpotiFlyerTypography.subtitle2
                )
            }
        }
        AnimatedVisibility(isEditMode) {
            Column {
                editContent {
                    isEditMode = false
                }
            }
        }
    }
}
