#!/bin/bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk1.8.0_201.jdk/Contents/Home
export PATH=$JAVA_HOME/bin:$PATH
./gradlew assembleDebug appDistributionUploadPlayProdDebug --stacktrace

