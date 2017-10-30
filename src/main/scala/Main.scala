import java.io.BufferedWriter
import java.nio.file.{Files, Paths}
import java.time.LocalDate
import java.time.format.{DateTimeFormatter, ResolverStyle}

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.util.Try
import scala.util.control.Breaks._

object Main extends App {
  require(args.length == 3, "You must provide a path for the input file and 2 output file names")
  val inputFilePath = args(0)
  val medianByZipPath = args(1)
  val medianByDatePath = args(2)

  val zipWriter = Files.newBufferedWriter(Paths.get(medianByZipPath))
  val dateWriter = Files.newBufferedWriter(Paths.get(medianByDatePath))

  val zipData = mutable.TreeMap.empty[(String, String), DonorRecord]
  val dateData = mutable.TreeMap.empty[(String, String), DonorRecord]

  val t0 = System.nanoTime
  println(s"Program to sort through $inputFilePath is starting execution...")
  Files.lines(Paths.get(inputFilePath)).iterator.asScala.foreach { line =>
    val lineElements = line.split("\\|")
    // break and move to next record if input considerations are not met (21 possible elements or input considerations)
    breakable {
      if (lineElements.length != 21) break
      else if (!acceptableData(lineElements)) break
      else {
        val id = lineElements.head
        val amount = lineElements(14).toInt
        if (hasAcceptableZip(lineElements)) {
          val zip = lineElements(10).take(5)
          updateMap(key = (id, zip), amount, zipData)
          writeZipLine(id, zip, zipData((id, zip)), zipWriter) // printing zip relevant data as soon as possible
        }
        if (hasAcceptableDate(lineElements)) {
          val date = lineElements(13)
          updateMap(key = (id, date), amount, dateData)
        }
      }
    }
  }

  writeDates(dateData, dateWriter)  // processing all date relevant data before printing
  val t1 = System.nanoTime
  println(s"Program took ${BigDecimal((t1 - t0) / 1000000000.0).setScale(2, BigDecimal.RoundingMode.HALF_UP)}s to " +
    s"execute, look at results in the files: $medianByZipPath and $medianByDatePath")

  // everything below this line are helpers to execute main program above
  case class DonorRecord(
                          contributions: Int,
                          totalAmount: Int = 0,
                          amounts: List[Int] = List.empty,
                          currentMedian: Int = Int.MinValue
                        )

  private def acceptableData(line: Array[String]): Boolean = {
    val validId = line.headOption.exists(_.trim.nonEmpty)
    val validAmount = line.lift(14).exists(s => s.trim.nonEmpty || s.matches("^\\d+$"))
    val noOtherId = line.lift(15).exists(_.trim.isEmpty)

    validId && validAmount && noOtherId
  }

  private def hasAcceptableZip(line: Array[String]): Boolean = {
    line.lift(10)
      .map(_.take(5))
      .exists(s => s.trim.nonEmpty && s.matches("^\\d{5}$"))
  }

  private def hasAcceptableDate(line: Array[String]): Boolean = {
    val dateFormat = DateTimeFormatter.ofPattern("MMdduuuu")
      .withResolverStyle(ResolverStyle.STRICT)

    line.lift(13).filter(_.trim.nonEmpty)
      .map(s => Try(LocalDate.parse(s, dateFormat)))
      .exists(_.isSuccess)
  }

  private def updateMap(key: (String, String), amt: Int, data: mutable.TreeMap[(String, String), DonorRecord]): Unit = {
    val newValue = if(data.get(key).isDefined) {
      val existingRecord = data(key)
      existingRecord.copy(
        contributions = existingRecord.contributions + 1,
        amounts = existingRecord.amounts :+ amt,
        totalAmount = existingRecord.totalAmount + amt,
        currentMedian = calculateMedian(existingRecord.amounts :+ amt)
      )
    } else {
      DonorRecord(
        contributions = 1,
        amounts = List(amt),
        totalAmount = amt,
        currentMedian = amt
      )
    }

    data.update(key, newValue)
  }

  private def writeZipLine(id: String, zip: String, record: DonorRecord, writer: BufferedWriter): Unit = {
    val output = s"$id|$zip|${calculateMedian(record.amounts)}|${record.contributions}|${record.totalAmount}"
    writer.write(output)
    writer.flush()
    writer.newLine()
  }

  private def writeDates(data: mutable.TreeMap[(String, String), DonorRecord], writer: BufferedWriter): Unit = {
    data.toSeq
      .sortBy{case (k, _) => (k._1, k._2)}
      .foreach { case ((id, date), record) =>
        val output = s"$id|$date|${calculateMedian(record.amounts)}|${record.contributions}|${record.totalAmount}"
        writer.write(output)
        writer.flush()
        writer.newLine()
      }
  }

  private def calculateMedian(numbers: List[Int]): Int = {
    val (lower, upper) = numbers.sorted.splitAt(numbers.size / 2)
    val median = if (numbers.size % 2 == 0) (lower.last + upper.head) / 2.0 else upper.head

    median.round.toInt
  }
}