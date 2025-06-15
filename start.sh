#!/bin/bash
JAR=$(find build/libs/*-all.jar | head -n 1)
java -jar "$JAR"