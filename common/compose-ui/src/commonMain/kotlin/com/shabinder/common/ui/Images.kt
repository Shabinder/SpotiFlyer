package com.shabinder.common.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
expect fun DownloadImageTick(modifier: Modifier = Modifier)

@Composable
expect fun DownloadAllImage():ImageVector

@Composable
expect fun DownloadImageError(modifier: Modifier = Modifier)

@Composable
expect fun DownloadImageArrow(modifier: Modifier = Modifier)