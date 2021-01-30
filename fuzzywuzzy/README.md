# FuzzyWuzzy-Kotlin

![badge][badge-android]
![badge][badge-native]
![badge][badge-js]
![badge][badge-jvm]
![badge][badge-linux]
![badge][badge-windows]
![badge][badge-mac]
![badge][badge-wasm]

[![CircleCI](https://circleci.com/gh/willowtreeapps/fuzzywuzzy-kotlin.svg?style=svg)](https://circleci.com/gh/willowtreeapps/fuzzywuzzy-kotlin)

Fuzzy string matching for Kotlin (JVM, iOS) - fork of [the Java fork](https://github.com/xdrop/fuzzywuzzy) of of [Fuzzy Wuzzy Python lib](https://github.com/seatgeek/fuzzywuzzy). For use in on JVM, Android, or Kotlin Multiplatform projects (JVM/Android, iOS, mac, linux)

Useful for selecting the closest matching string from a collection of strings.  Various algorithms are available.

See Java repo or Python repo for usage.

To add to project in the common module add the dependency:

```
sourceSets {
  commonMain {
      dependencies {
          implementation "com.willowtreeapps:fuzzywuzzy-kotlin:0.1.1"
      }
   }
}
```
[badge-android]: http://img.shields.io/badge/platform-android-brightgreen.svg?style=flat
[badge-native]: http://img.shields.io/badge/platform-native-lightgrey.svg?style=flat	
[badge-native]: http://img.shields.io/badge/platform-native-lightgrey.svg?style=flat
[badge-js]: http://img.shields.io/badge/platform-js-yellow.svg?style=flat
[badge-js]: http://img.shields.io/badge/platform-js-yellow.svg?style=flat
[badge-jvm]: http://img.shields.io/badge/platform-jvm-orange.svg?style=flat
[badge-jvm]: http://img.shields.io/badge/platform-jvm-orange.svg?style=flat
[badge-linux]: http://img.shields.io/badge/platform-linux-important.svg?style=flat
[badge-linux]: http://img.shields.io/badge/platform-linux-important.svg?style=flat 
[badge-windows]: http://img.shields.io/badge/platform-windows-informational.svg?style=flat
[badge-windows]: http://img.shields.io/badge/platform-windows-informational.svg?style=flat
[badge-mac]: http://img.shields.io/badge/platform-macos-lightgrey.svg?style=flat
[badge-mac]: http://img.shields.io/badge/platform-macos-lightgrey.svg?style=flat
[badge-wasm]: https://img.shields.io/badge/platform-wasm-darkblue.svg?style=flat
