@file:Suppress("MayBeConstant", "SpellCheckingInspection")

object Versions {
    const val versionName = "2.2.0"
    const val kotlinVersion = "1.4.30"

    const val coroutinesVersion = "1.4.2"
    //const val compose = "1.0.0-alpha12"

    const val coilVersion = "0.4.1"
    //DI
    const val koin = "3.0.0-alpha-4"

    //Logger
    const val kermit = "0.1.8"

    //Internet
    const val ktor = "1.5.1"

    const val kotlinxSerialization = "1.1.0-RC"
    //Database
    const val sqlDelight = "1.4.4"

    const val sqliteJdbcDriver = "3.30.1"
    const val slf4j = "1.7.30"

    //Android
    const val versionCode = 15
    const val minSdkVersion = 24
    const val compileSdkVersion = 30
    const val targetSdkVersion = 29
    const val androidLifecycle = "2.3.0"
}
object Koin {
    val core = "org.koin:koin-core:${Versions.koin}"
    val test = "org.koin:koin-test:${Versions.koin}"
    val android = "org.koin:koin-android:${Versions.koin}"
    val androidViewModel = "org.koin:koin-androidx-viewmodel:${Versions.koin}"
}
object Androidx{
    const val androidxActivity = "androidx.activity:activity-compose:1.3.0-alpha02"
    const val core = "androidx.core:core-ktx:1.3.2"
    const val palette = "androidx.palette:palette-ktx:1.0.0"
    const val coroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.coroutinesVersion}"

    const val junit = "androidx.test.ext:junit:1.1.2"
    const val expresso = "androidx.test.espresso:espresso-core:3.3.0"

    /*object Compose{
        const val materialIcon = "androidx.compose.material:material-icons-extended:${Versions.compose}"
        const val ui = "androidx.compose.ui:ui:${Versions.compose}"
        const val uiGraphics = "androidx.compose.ui:ui-graphics:${Versions.compose}"
        const val uiTooling = "androidx.compose.ui:ui-tooling:${Versions.compose}"
        const val foundationLayout = "androidx.compose.foundation:foundation-layout:${Versions.compose}"
        const val material = "androidx.compose.material:material:${Versions.compose}"
        const val runtimeLiveData = "androidx.compose.runtime:runtime-livedata:${Versions.compose}"
    }*/
}
object JetBrains {
    object Kotlin {
        const val gradlePlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlinVersion}"
        const val serialization = "org.jetbrains.kotlin:kotlin-serialization:${Versions.kotlinVersion}"
        const val testCommon = "org.jetbrains.kotlin:kotlin-test-common:${Versions.kotlinVersion}"
        const val testJunit = "org.jetbrains.kotlin:kotlin-test-junit:${Versions.kotlinVersion}"
        const val testAnnotationsCommon = "org.jetbrains.kotlin:kotlin-test-annotations-common:${Versions.kotlinVersion}"
    }

    object Compose {
        // __LATEST_COMPOSE_RELEASE_VERSION__
        const val VERSION = "0.3.0"
        const val gradlePlugin = "org.jetbrains.compose:compose-gradle-plugin:$VERSION"
    }
}
object Decompose {
    private const val VERSION = "0.1.8"
    const val decompose = "com.arkivanov.decompose:decompose:$VERSION"
    const val decomposeIosX64 = "com.arkivanov.decompose:decompose-iosx64:$VERSION"
    const val decomposeIosArm64 = "com.arkivanov.decompose:decompose-iosarm64:$VERSION"
    const val extensionsCompose = "com.arkivanov.decompose:extensions-compose-jetbrains:$VERSION"
}
object MVIKotlin {
    private const val VERSION = "2.0.0"
    const val rx = "com.arkivanov.mvikotlin:rx:$VERSION"
    const val mvikotlin = "com.arkivanov.mvikotlin:mvikotlin:$VERSION"
    const val mvikotlinMain = "com.arkivanov.mvikotlin:mvikotlin-main:$VERSION"
    const val coroutines = "com.arkivanov.mvikotlin:mvikotlin-extensions-coroutines:$VERSION"
    const val keepers = "com.arkivanov.mvikotlin:keepers:$VERSION"
    const val mvikotlinMainIosX64 = "com.arkivanov.mvikotlin:mvikotlin-main-iosx64:$VERSION"
    const val mvikotlinMainIosArm64 = "com.arkivanov.mvikotlin:mvikotlin-main-iosarm64:$VERSION"
    const val mvikotlinLogging = "com.arkivanov.mvikotlin:mvikotlin-logging:$VERSION"
    const val mvikotlinTimeTravel = "com.arkivanov.mvikotlin:mvikotlin-timetravel:$VERSION"
    const val mvikotlinExtensionsReaktive = "com.arkivanov.mvikotlin:mvikotlin-extensions-reaktive:$VERSION"
}
object Badoo {
    object Reaktive {
        private const val VERSION = "1.1.19"
        const val reaktive = "com.badoo.reaktive:reaktive:$VERSION"
        const val reaktiveTesting = "com.badoo.reaktive:reaktive-testing:$VERSION"
        const val utils = "com.badoo.reaktive:utils:$VERSION"
        const val coroutinesInterop = "com.badoo.reaktive:coroutines-interop:$VERSION"
    }
}
object Ktor {
    val clientCore = "io.ktor:ktor-client-core:${Versions.ktor}"
    val clientJson = "io.ktor:ktor-client-json:${Versions.ktor}"
    val clientLogging = "io.ktor:ktor-client-logging:${Versions.ktor}"
    val clientSerialization = "io.ktor:ktor-client-serialization:${Versions.ktor}"

    val auth = "io.ktor:ktor-client-auth:${Versions.ktor}"
    val clientAndroid = "io.ktor:ktor-client-android:${Versions.ktor}"
    val clientCurl = "io.ktor:ktor-client-curl:${Versions.ktor}"
    val clientApache = "io.ktor:ktor-client-apache:${Versions.ktor}"
    val slf4j = "org.slf4j:slf4j-simple:${Versions.slf4j}"
    val clientIos = "io.ktor:ktor-client-ios:${Versions.ktor}"
    val clientCio = "io.ktor:ktor-client-cio:${Versions.ktor}"
    val clientJs = "io.ktor:ktor-client-js:${Versions.ktor}"
}

object Extras {
    const val youtubeDownloader = "com.github.sealedtx:java-youtube-downloader:2.5.1"
    const val fuzzyWuzzy = "me.xdrop:fuzzywuzzy:1.3.1"
    const val mp3agic = "com.mpatric:mp3agic:0.9.1"
    const val kermit = "co.touchlab:kermit:${Versions.kermit}"
    object Android {
        val razorpay = "com.razorpay:checkout:1.6.5"
        val fetch = "androidx.tonyodev.fetch2:xfetch2:3.1.6"
        val appUpdator = "com.github.amitbd1508:AppUpdater:4.1.0"
    }
}

object JetpackDataStore {
    val dep = "androidx.datastore:datastore-preferences-core:1.0.0-alpha05"
}

object Serialization {
    val core = "org.jetbrains.kotlinx:kotlinx-serialization-core:${Versions.kotlinxSerialization}"
}

object SqlDelight {
    val runtime = "com.squareup.sqldelight:runtime:${Versions.sqlDelight}"
    val coroutineExtensions = "com.squareup.sqldelight:coroutines-extensions:${Versions.sqlDelight}"

    const val gradlePlugin = "com.squareup.sqldelight:gradle-plugin:${Versions.sqlDelight}"
    const val androidDriver = "com.squareup.sqldelight:android-driver:${Versions.sqlDelight}"
    const val sqliteDriver = "com.squareup.sqldelight:sqlite-driver:${Versions.sqlDelight}"
    const val nativeDriver = "com.squareup.sqldelight:native-driver:${Versions.sqlDelight}"
    val nativeDriverMacos = "com.squareup.sqldelight:native-driver-macosx64:${Versions.sqlDelight}"
    val jdbcDriver = "org.xerial:sqlite-jdbc:${Versions.sqliteJdbcDriver}"
}