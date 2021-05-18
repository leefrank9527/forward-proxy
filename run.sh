#!/bin/sh

./gradlew clean build -x test

#java -Djava.net.useSystemProxies=true -jar build/libs/proxy-1.0-SNAPSHOT.jar

java -jar build/libs/proxy-1.0-SNAPSHOT.jar &
