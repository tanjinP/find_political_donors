#!/bin/bash

INPUT_FILE="./input/itcont.txt"
ZIP_OUTPUT_FILE="./output/medianvals_by_zip.txt"
DATE_OUTPUT_FILE="./output/medianvals_by_date.txt"

scala src/main/scala/Main.scala $INPUT_FILE $ZIP_OUTPUT_FILE $DATE_OUTPUT_FILE
