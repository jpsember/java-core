#!/usr/bin/env bash
set -eu


echo Removing sentinel file...
rm -f ~/Desktop/_err_sentinel.txt_

echo Unloading...
launchctl unload -w ~/Library/LaunchAgents/screenshotter.plist
echo Loading...
launchctl load -w ~/Library/LaunchAgents/screenshotter.plist
echo Viewing logs... press ^c to quit...
tail -f ~/Desktop/screenshotter_logfile.txt

