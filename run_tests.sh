#!/usr/bin/env bash
set -e
rm -rf out
mkdir -p out
find src test -name '*.java' -print > sources.txt
javac -encoding UTF-8 -d out @sources.txt
rm sources.txt
java -cp out ai.SelfTest
