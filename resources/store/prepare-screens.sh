# get screen
adb shell screencap -p | sed 's/\r$//' > screen.png

# set locale de
adb shell 'setprop persist.sys.language de;
setprop persist.sys.country DE;
stop;
sleep 5;
start'

# set locale en
adb shell 'setprop persist.sys.language en;
setprop persist.sys.country GB;
stop;
sleep 5;
start'

# prepare app
adb uninstall de.medienDresden.illumina && adb install app/build/apk/app-google-release.apk && adb shell am start -n de.medienDresden.illumina/.activity.ConnectionActivity
