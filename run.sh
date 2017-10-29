#!/bin/bash

echo "Let's find some political donor data!"
INPUT_FILE="./input/itcont.txt"
ZIP_OUTPUT_FILE="./output/medianvals_by_zip.txt"
DATE_OUTPUT_FILE="./output/medianvals_by_date.txt"

sbt "run $INPUT_FILE $ZIP_OUTPUT_FILE $DATE_OUTPUT_FILE"

echo "Data processing is complete. Go check the output files!"