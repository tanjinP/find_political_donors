# Coding challenge for Insight: Find Political Donors
Problem is stated in the [original repo](https://github.com/InsightDataScience/find-political-donors)

## Approach
The program utilized a couple of things from Scala's standard `util` and `collection` libraries, as well as the Java `io/nio` and `time` libraries. The usages can be summarized as:
- Java: IO/NIO is used to read and write the files. Time is used for the parsing and checking of the transaction date data.
- Scala: Utils are used from control abstractions of `Try` and `break`. Collections are utilized for the conversion of a Java collection to Scala and also the less popular mutable collection data structures.

1. The input file is read as the argument to the program, we then process the file line by line. 
2. For each line we do the checks for the input file consideration and store relevant data into 2 mutable maps with the same structure:
```
val mapData: TreeMap[(String, String), DonorRecord] // key is a Tuple2, value is a case class containing relevant information to said key
```
3. These 2 maps correspond to the 2 output files. For the first map, `medianvals_by_zip.txt`, we are continuously writing as we are processing each line in addition to populating the map. 
4. For the second map, `medianvals_by_date.txt`, we write once the corresponding map is complete, that is done after processing every line of the input file. 
5. Each time we reference the map (in both cases), we check to see if an existing record exists. If so, we update it with the new information we have (new amount and newly calculated median), this is done for every line.

## Dependencies 
The following are required to run the program:
- Scala 2.12 (program was done on machine with [latest version, 2.12.4](https://www.scala-lang.org/download/), installed)
- Java 8: [JRE 8](http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html) was used to run the program

## Omissions
The program was originally written in SBT and included a dependency for the popular testing framework for Scala programs, [ScalaTest](http://www.scalatest.org/). I wrote a couple of simple unit tests for all the defs in the program, including the main def. I was having issues getting SBT to properly compile with the right Scala version for the `./insight_testsuite/` and was not able to get it to work. I eventually decided to forgo SBT entirely and just stick with a simple Scala script.

## Personal Thoughts
I thoroughly enjoyed this problem as it tested a few assumptions I had in the handling data in the Scala environment. My initial approach ([as evident by my earlier commits](https://github.com/tanjinP/find_political_donors/commit/0fb0d8863ebbcb99d7ec5b6ac925510a95e6fba0)) was to utilize immutable data structures and make multiple passes through the data as I mapped, filtered, collected, and eventually wrote out to the `.txt` files. Everything was going well - I completed the program and had my tests running… That is until I downloaded the publicly available data to run my program with, starting with the 100MB file. That's when the out of memory exceptions started to show and the sinking feeling set in. 

I tried utilizing lazy structures and avoiding multiple map operations but after 2 extremely late nights and soul searching I decided to go back to the drawing board. My final solution is a greedy approach through the input data that uses mutable data structures. While this wasn't my intention, at the end it got the job done (story of my life).

This is a wake up call on my end to investigate the lazy structures, particularly in Scala, and how to leverage them in the JVM with a preference towards performance. Naturally this will result in looking into Spark and how data structures are used to process large amounts of data in distributed environments, so I'm excited to research this further.
