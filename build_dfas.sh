#!/usr/bin/env bash
set -eu

dfa input json.rxp output src/main/resources/js/parsing/json.dfa ids src/main/java/js/json/JSUtils.java
dfa input clean.rxp output src/main/resources/js/parsing/clean.dfa ids src/main/java/js/data/DataUtil.java
