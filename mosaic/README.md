# Mosaic

An experimental tool for building console UI in Kotlin using the Jetpack Compose compiler/runtime.
Inspired by [Ink](https://github.com/vadimdemedes/ink).

<img src="samples/jest/demo.svg">

(Heads up: this SVG has a slight [rendering bug](https://github.com/JakeWharton/mosaic/issues/6))

Jump to:
[Introduction](#Introduction) |
[Usage](#Usage) |
[Samples](#Samples) |
[FAQ](#FAQ) |
[License](#License)


## Introduction

The entrypoint to Mosaic is the `runMosaic` function.
The lambda passed to this function is responsible for both output and performing work.

Output (for now) happens through the `setContent` function.
You can call `setContent` multiple times, but as you'll see you probably won't need to.

```kotlin
fun main() = runMosaic {
  setContent {
    Text("The count is: 0")
  }
}
```

To change the output dynamically we can use local properties to hold state.
Let's update our counter to actually count to 20.

```kotlin
fun main() = runMosaic {
  var count = 0

  setContent {
    Text("The count is: $count")
  }

  for (i in 1..20) {
    delay(250)
    count = i
  }
}
```

**This will not work!** Our count stays are 0 for 5 seconds instead of incrementing until 20.
Instead, we have to use Compose's `State` objects to hold state.

```diff
-var count = 0
+var count by mutableStateOf(0)
```

Now, when the `count` value is updated, Compose will know that it needs to re-render the string.

```kotlin
fun main() = runMosaic {
  var count by mutableStateOf(0)

  setContent {
    Text("The count is: $count")
  }

  for (i in 1..20) {
    delay(250)
    count = i
  }
}
```

(Note: You may need to add imports for `androidx.compose.runtime.getValue` and `import androidx.compose.runtime.setValue` manually.)

<img src="samples/counter/demo.svg">


## Usage

In order to use Mosaic you must write your code in Kotlin and must apply the Compose Kotlin
compiler plugin.

For Gradle users, the Mosaic Gradle plugin will take care of applying the compiler plugin.

```groovy
buildscript {
  repositories {
    mavenCentral()
  }
  dependencies {
    classpath 'org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.10'
    classpath 'com.jakewharton.mosaic:mosaic-gradle-plugin:0.1.0'
  }
}

apply plugin: 'org.jetbrains.kotlin.jvm'
apply plugin: 'org.jakewharton.mosaic'
```

The runtime APIs will be made available automatically by applying the plugin.
Documentation is available at [jakewharton.github.io/mosaic/docs/0.x/](https://jakewharton.github.io/mosaic/docs/0.x/).

**Note**: Any module which contains a `@Composable`-annotated function or lambda must apply the
Mosaic plugin. While the runtime dependency will be available to downstream modules as a
transitive dependency, the compiler plugin is not inherited and must be applied to every module.

Since Kotlin compiler plugins are an unstable API, certain versions of Mosaic only work with
certain versions of Kotlin.

| Kotlin | Mosaic             |
|--------|--------------------|
| 1.5.10 | 0.1.0              |
| 1.5.20 | Blocked on Compose |

Versions of Kotlin older than 1.5.10 are not supported.
Versions newer than those listed may be supported but are untested.

<details>
<summary>Snapshots of the development version are available in Sonatype's snapshots repository.</summary>
<p>

```groovy
buildscript {
  repository {
    mavenCental()
    maven {
      url 'https://oss.sonatype.org/content/repositories/snapshots/'
    }
  }
  dependencies {
    classpath 'org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.10'
    classpath 'com.jakewharton.mosaic:mosaic-gradle-plugin:0.2.0-SNAPSHOT'
  }
}

apply plugin: 'org.jetbrains.kotlin.jvm'
apply plugin: 'org.jakewharton.mosaic'
```

Snapshot documentation is available at [jakewharton.github.io/mosaic/docs/latest/](https://jakewharton.github.io/mosaic/docs/latest/).

</p>
</details>


## Samples

Run `./gradlew installDist` to build the sample binaries.

 * [Counter](samples/counter): A simple increasing number from 0 until 20.

   `./samples/counter/build/install/counter/bin/counter`

 * [Jest](samples/jest): Example output of a test framework (such as JS's 'Jest').

   `./samples/jest/build/install/jest/bin/jest`

 * [Robot](samples/robot): An interactive, game-like program with keyboard control.

   `./samples/robot/build/install/robot/bin/robot`


## FAQ

### I thought Jetpack Compose was a UI toolkit for Android?

Compose is, at its core, a general-purpose runtime and compiler for tree and property manipulation
which is trapped inside the AndroidX monorepo and under the Jetpack marketing department. This
core can be used for _any_ tree on _any_ platform supported by Kotlin. It's an amazing piece of
technology.

Compose UI is the new UI toolkit for Android (and maybe [Desktop](https://www.jetbrains.com/lp/compose/)?).
The lack of differentiation between these two technologies has unfortunately caused Compose UI to
overshadow the core under the single "Compose" moniker in an unforced marketing error.

If you want another example of a non-Compose UI-based Compose project checkout JetBrains' [Compose for Web](https://blog.jetbrains.com/kotlin/2021/05/technology-preview-jetpack-compose-for-web/) project.

### Why doesn't work take place in a `LaunchedEffect`?

This is the goal. It is currently blocked by [issuetracker.google.com/178904648](https://issuetracker.google.com/178904648).

When that change lands, and Mosaic is updated, the counter sample will look like this:
```kotlin
fun main() = runMosaic {
  var count by remember { mutableStateOf(0) }

  Text("The count is: $count")

  LaunchedEffect(Unit) {
    for (i in 1..20) {
      delay(250)
      count = i
    }
  }
}
```


# License

    Copyright 2020 Jake Wharton

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
