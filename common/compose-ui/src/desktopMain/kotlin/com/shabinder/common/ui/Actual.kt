package com.shabinder.common.ui

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.vectorXmlResource
import com.shabinder.common.Picture
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

@Composable
actual fun ImageLoad(
    pic: Picture?,
    modifier: Modifier
){
    if(pic == null) {
        Image(
            vectorXmlResource("common/compose-ui/src/main/res/drawable/music.xml"),
            modifier
        )
    }
    else{
        Image(
            org.jetbrains.skija.Image.makeFromEncoded(
            toByteArray(pic.image)
        ).asImageBitmap(),
            modifier = modifier
        )
    }
}
fun toByteArray(bitmap: BufferedImage) : ByteArray {
    val baOs = ByteArrayOutputStream()
    ImageIO.write(bitmap, "png", baOs)
    return baOs.toByteArray()
}