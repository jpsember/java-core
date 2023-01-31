#!/usr/bin/env bash
set -eu

echo Unloading...
launchctl unload -w ~/Library/LaunchAgents/screenshotter.plist
echo Loading...
launchctl load -w ~/Library/LaunchAgents/screenshotter.plist
echo Viewing logs... press ^c to quit...
tail -f ~/Desktop/screenshotter_logfile.txt

