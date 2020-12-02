/*
 * Copyright (C)  2020  Shabinder Singh
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.shabinder.spotiflyer.utils

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

const val NoInternetErrorCode = 222

class NetworkInterceptor: Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        Log.i("Network Requesting",chain.request().url.toString())
        return if (!isOnline()){
            //No Internet Connection
            showNoConnectionAlert()
            //Lets Stop the Incoming Request
            Response.Builder()
                .code(NoInternetErrorCode) // code(200.300) = successful else = unsuccessful
                .body("{}".toResponseBody(null)) // Empty Object
                .protocol(Protocol.HTTP_2)
                .message("No Internet Connection")
                .request(chain.request())
                .build()
        }else {
            val response = chain.proceed(chain.request())
            val responseBody = response.body
            val bodyString = responseBody?.string()
            //Log.i("Network Request",bodyString)
            //chain.proceed(chain.request())
            //Log.i("Network Request","{\"unchecked\":${bodyString}}")
            Response.Builder()
                .code(response.code) // code(200.300) = successful else = unsuccessful
                .body("{\"value\":${bodyString}}".toResponseBody(responseBody?.contentType())) // Whatever body
                .protocol(response.protocol)
                .message(response.message)
                .request(chain.request())
                .build()
//            chain.proceed(chain.request())
        }
    }
}