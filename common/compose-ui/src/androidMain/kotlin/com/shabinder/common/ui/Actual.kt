package com.shabinder.common.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.preferredHeight
import androidx.compose.foundation.layout.preferredWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.Dp
import androidx.core.net.toUri
import com.shabinder.common.Picture
import com.shabinder.common.database.appContext
import dev.chrisbanes.accompanist.coil.CoilImage

@Composable
actual fun ImageLoad(
    pic: Picture?,
    modifier: Modifier
){
    Image(pic?.image?.asImageBitmap(), vectorResource(R.drawable.music) ,"Image",modifier)
}

@Composable
fun Image(pic: ImageBitmap?, placeholder:ImageVector, desc: String,modifier:Modifier = Modifier) {
    if(pic == null) Image(placeholder,desc,modifier) else Image(pic,desc,modifier)
}

/*
@Composable
actual fun ImageLoad(
    url:String,
    loadingResource:ImageBitmap?,
    errorResource:ImageBitmap?,
    modifier: Modifier
){
    val imgUri = url.toUri().buildUpon().scheme("https").build()
    CoilImage(
        data = imgUri,
        contentScale = ContentScale.Crop,
        loading = { loadingResource?.let { Image(it,"loading image") } },
        error = { errorResource?.let { it1 -> Image(it1,"Error Image") } },
        modifier = modifier
    )
}
*/

@Composable
actual fun Toast(
    text: String,
    visibility: MutableState<Boolean>,
    duration: ToastDuration
){
    //We Have Android's Implementation of Toast so its just Empty
}

actual fun showPopUpMessage(text: String){
    android.widget.Toast.makeText(appContext,text, android.widget.Toast.LENGTH_SHORT).show()
}