package com.shabinder.common.models

sealed class CorsProxy(open val url: String){
    data class SelfHostedCorsProxy(override val url:String = "https://kind-grasshopper-73.telebit.io/cors/"):CorsProxy(url)
    data class PublicProxyWithExtension(override val url:String = "https://cors.bridged.cc/"):CorsProxy(url)

    fun toggle(mode:CorsProxy? = null):CorsProxy{
        mode?.let {
            corsProxy = mode
            return corsProxy
        }
        corsProxy = when(corsProxy){
            is SelfHostedCorsProxy -> PublicProxyWithExtension()
            is PublicProxyWithExtension -> SelfHostedCorsProxy()
        }
        return corsProxy
    }

    fun extensionMode():Boolean{
        return when(corsProxy){
            is SelfHostedCorsProxy -> false
            is PublicProxyWithExtension -> true
        }
    }
}

/*
* This Var Keeps Track for Cors Config in JS Platform
* Default Self Hosted, However ask user to use extension if possible.
* */
var corsProxy:CorsProxy = CorsProxy.SelfHostedCorsProxy()
