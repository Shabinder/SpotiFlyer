/*
 *  * Copyright (c)  2021  Shabinder Singh
 *  * This program is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as published by
 *  * the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version.
 *  *
 *  * This program is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  * GNU General Public License for more details.
 *  *
 *  *  You should have received a copy of the GNU General Public License
 *  *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.shabinder.common.di

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.content.ContextCompat
import com.razorpay.Checkout
import com.shabinder.common.database.activityContext
import com.shabinder.common.di.worker.ForegroundService
import com.shabinder.common.models.AllPlatforms
import com.shabinder.common.models.TrackDetails
import kotlinx.coroutines.Dispatchers
import org.json.JSONObject
import com.codekidlabs.storagechooser.StorageChooser
import com.codekidlabs.storagechooser.StorageChooser.OnSelectListener



actual fun openPlatform(packageID: String, platformLink: String) {
    val manager: PackageManager = activityContext.packageManager
    try {
        val intent = manager.getLaunchIntentForPackage(packageID)
            ?: throw PackageManager.NameNotFoundException()
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        activityContext.startActivity(intent)
    } catch (e: PackageManager.NameNotFoundException) {
        val uri: Uri =
            Uri.parse(platformLink)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        activityContext.startActivity(intent)
    }
}
actual val dispatcherIO = Dispatchers.IO
actual val currentPlatform: AllPlatforms = AllPlatforms.Jvm

actual val isInternetAvailable: Boolean
    get() = internetAvailability.value ?: true

actual fun shareApp() {
    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, "Hey, checkout this excellent Music Downloader http://github.com/Shabinder/SpotiFlyer")
        type = "text/plain"
    }

    val shareIntent = Intent.createChooser(sendIntent, null)
    activityContext.startActivity(shareIntent)
}

actual fun giveDonation() = startPayment()

private fun startPayment(mainActivity: Activity = activityContext as Activity) {
    /*
    *  You need to pass current activity in order to let Razorpay create CheckoutActivity
    * */
    val co = Checkout().apply {
        setKeyID("rzp_live_3ZQeoFYOxjmXye")
        setImage(R.drawable.ic_spotiflyer_logo)
    }

    try {
        val preFill = JSONObject()

        val options = JSONObject().apply {
            put("name", "SpotiFlyer")
            put("description", "Thanks For the Donation!")
            // You can omit the image option to fetch the image from dashboard
            // put("image","https://github.com/Shabinder/SpotiFlyer/raw/master/app/SpotifyDownload.png")
            put("currency", "INR")
            put("amount", "4900")
            put("prefill", preFill)
        }

        co.open(mainActivity, options)
    } catch (e: Exception) {
        // showPop("Error in payment: "+ e.message)
        e.printStackTrace()
    }
}
actual fun queryActiveTracks() {
    val serviceIntent = Intent(activityContext, ForegroundService::class.java).apply {
        action = "query"
    }
    ContextCompat.startForegroundService(activityContext, serviceIntent)
}

actual suspend fun downloadTracks(
    list: List<TrackDetails>,
    fetcher: FetchPlatformQueryResult,
    dir: Dir
) {
    if (!list.isNullOrEmpty()) {
        val serviceIntent = Intent(activityContext, ForegroundService::class.java)
        serviceIntent.putParcelableArrayListExtra("object", ArrayList<TrackDetails>(list))
        activityContext.let { ContextCompat.startForegroundService(it, serviceIntent) }
    }
}