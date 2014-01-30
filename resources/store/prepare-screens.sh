#!/bin/bash

# resize image for HTC One S
mogrify -resize 480x818 screen*.png

# remove 18px at the bottom
mogrify -chop 0x18+0+0 -gravity South screen*.png
