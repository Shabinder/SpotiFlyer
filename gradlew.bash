#!/bin/bash
#
# Copyright 2015 the original author or authors.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# ##########################################################################
#
#  Gradle startup script for linux
#
# ##########################################################################

# Set local scope for the variables with windows NT shell
cd "$(dirname $0)" || echo "couldn't get current file directory properly... using fallback method of current working directory" || error 1
export DIRNAME
DIRNAME=$(pwd)
cd - || echo "another error because I couldn't get gradlew's directory" || error 1
if [[ -z $DIRNAME ]]; then DIRNAME=$(pwd); fi
export APP_BASE_NAME=$0
export APP_HOME=$DIRNAME

# Add default JVM options here. You can also use JAVA_OPTS and GRADLE_OPTS to pass JVM options to this script.
export DEFAULT_JVM_OPTS="-Xmx64m -Xms64m"

# Find java.exe
if [[ -z $JAVA_HOME ]]; then

  export JAVA_EXE=java
  $JAVA_EXE -version > /dev/null
  if [ ! $? == "0" ]; then
    echo .
    echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
    echo .
    echo Please set the JAVA_HOME variable in your environment to match the
    echo location of your Java installation.
    exit 1
    fi
else
  export JAVA_HOME=$JAVA_HOME
  export JAVA_EXE=$JAVA_HOME/bin/java
fi

if [[ -z $JAVA_EXE ]]; then

  echo.
  echo ERROR: JAVA_HOME is set to an invalid directory: $JAVA_HOME
  echo.
  echo Please set the JAVA_HOME variable in your environment to match the
  echo location of your Java installation.

  exit 1
fi

# Setup the command line

export CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar


# Execute Gradle
echo running $JAVA_EXE $DEFAULT_JVM_OPTS $JAVA_OPTS $GRADLE_OPTS -Dorg.gradle.appname=$APP_BASE_NAME -classpath $CLASSPATH org.gradle.wrapper.GradleWrapperMain "$@"
$JAVA_EXE $DEFAULT_JVM_OPTS $JAVA_OPTS $GRADLE_OPTS -Dorg.gradle.appname=$APP_BASE_NAME -classpath $CLASSPATH org.gradle.wrapper.GradleWrapperMain "$@"

#crash if gradle crashes
if [ ! $? == "0"  ]; then
  echo gradle failed with exit code $?.
  exit 1
fi
