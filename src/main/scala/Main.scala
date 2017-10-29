import java.nio.file.{Files, Paths}

import scala.collection.JavaConverters._

object Main extends App {

  override def main(args: Array[String]): Unit = {
    require(args.length == 3, "You must provide a path for the input file and 2 output file names")
    val inputFilePath = args(0)
    val medianByZipPath = args(1)
    val medianByDatePath = args(2)

    // reading in file as an List of lines containing a List of Strings, each string is a cell
    val inputData = Files.lines(Paths.get(inputFilePath))
      .iterator
      .asScala
      .toList
      .map(_.split("\\|").toList)
      .withFilter(_.length == 21)   // if each line does not contain 21 cell spaces, we ignore that line

    // deserializing data
    val donors = inputData.map(createDonor)
    // input consideration #1 and 5
    val cleanedData = donors.filter(d => d.noOtherId && d.id.isDefined && d.amount.isDefined)

    val zipData = cleanedData.filter(_.zip.isDefined)   //  input consideration #4
    val dateData = cleanedData.filter(_.date.isDefined) //  input consideration #2

    val medianValByZip = zipData.foldLeft(Vector.empty[MedianByZip])(processMedianValByZip)
    val medianValByDate = dateData.foldLeft(Vector.empty[MedianByDate])(processMedianValByDate)
      .sortBy(d => (d.id, d.date))

    // writing data to files as defined in the arguments
    writeData(medianValByZip, medianByZipPath)      //  writing medianvals_by_zip data to .txt file
    writeData(medianValByDate, medianByDatePath)    //  writing medianvals_by_date data to .txt file
  }

  private def createDonor(rawData: List[String]): Donor = {
    Donor(
      id = rawData.headOption,              //  CMTE_ID is in position 1 (.headOption is same as .lift(0))
      zip = rawData.lift(10),               //  ZIP_CODE is in position 11
      date = rawData.lift(13),              //  TRANSACTION_DT is in position 14
      amount = rawData.lift(14),            //  TRANSACTION_AMT is in position 15
      noOtherId = rawData.lift(15)          //  OTHER_ID is in position 16
        .exists(_.trim.isEmpty)
    ).clean
  }

  private def processMedianValByZip(records: Vector[MedianByZip], donor: Donor): Vector[MedianByZip] = {
    val existingDonor = records.find(r => r.id == donor.id.get && r.zip == donor.zip.get)
    val donorAmt = donor.amount.get.toInt

    val recordToAdd = existingDonor.map {old =>   // new record being created using info from existing donor if found
      val donationAmounts = old.amounts :+ donorAmt
      old.copy(
        median = calculateMedian(donationAmounts),
        contributions = old.contributions + 1,
        totalAmount = old.totalAmount + donorAmt,
        amounts = donationAmounts
      )
    }.getOrElse( // otherwise a completely new record is created
      MedianByZip(
        id = donor.id.get,
        zip = donor.zip.get,
        median = donorAmt,
        contributions = 1,
        amounts = List(donorAmt),
        totalAmount = donorAmt
      )
    )

    records :+ recordToAdd
  }

  // TODO this algo needs cleanup, look into collection conversions
  private def processMedianValByDate(records: Vector[MedianByDate], donor: Donor): Vector[MedianByDate] = {
    val existingDonor = records.find(r => r.id == donor.id.get && r.date == donor.date.get)
    val donorAmt = donor.amount.get.toInt

    val recordToAdd = if (existingDonor.isDefined) {          // updating existing record if found
      val old = existingDonor.get
      val donationAmounts = old.amounts :+ donorAmt
      val updatedRecord = old.copy(
        median = calculateMedian(donationAmounts),
        contributions = old.contributions + 1,
        totalAmount = old.totalAmount + donorAmt,
        amounts = donationAmounts
      )
      records.toBuffer += updatedRecord -= existingDonor.get  // adding updated record and removing the existing
    } else {                                                  // a completely new record is created
      records :+ MedianByDate(
        id = donor.id.get,
        date = donor.date.get,
        median = donorAmt,
        contributions = 1,
        amounts = List(donorAmt),
        totalAmount = donorAmt
      )
    }

    recordToAdd.toVector
  }

  private def calculateMedian(numbers: List[Int]): Int = {
    val (lower, upper) = numbers.sorted.splitAt(numbers.size / 2)
    val median = if (numbers.size % 2 == 0) (lower.last + upper.head) / 2.0 else upper.head

    median.round.toInt
  }

  private def writeData(processedDonors: Vector[Output], outputFileLocation: String): Unit = {
    val writer = Files.newBufferedWriter(Paths.get(outputFileLocation))

    processedDonors.foreach { donor =>
      val text = donor match {
        case z: MedianByZip => s"${z.id}|${z.zip}|${z.median}|${z.contributions}|${z.totalAmount}"
        case d: MedianByDate => s"${d.id}|${d.date}|${d.median}|${d.contributions}|${d.totalAmount}"
      }
      writer.write(text)
      writer.flush()
      writer.newLine()
    }
  }
}

