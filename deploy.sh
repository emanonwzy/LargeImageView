#!/usr/bin/env bash
./gradlew clean uploadArchives --stacktrace
if [ $? -eq 0 ]
then
    echo "deploy successful!"
    exit 0
else
    echo "deploy failed!"
    exit 1
fi