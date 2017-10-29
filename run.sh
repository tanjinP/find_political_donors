#!/bin/bash

echo "Let's find some political donor data!"
inputFile="./input/itcont.txt"
byZipOutputFile="./output/medianvals_by_zip.txt"
byDateOutputFile="./output/medianvals_by_date.txt"

sbt "run $inputFile $byZipOutputFile $byDateOutputFile"

echo "Data processing is complete. Go check the output files!"