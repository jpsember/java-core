#!/usr/bin/env bash
set -eu

dfa input json.rxp output src/main/resources/js/parsing/json.dfa ids src/main/java/js/json/JSUtils.java
