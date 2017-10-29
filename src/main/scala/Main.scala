import java.io.{BufferedWriter, File, FileWriter}

import net.tixxit.delimited._

object Main extends App {

  override def main(args: Array[String]): Unit = {
    require(args.length == 3, "You must provide a path for the input file and 2 output file names")
    val inputFilePath = args(0)
    val medianByZipPath = args(1)
    val medianByDatePath = args(2)

    val rawInputData = new File(inputFilePath)
    // setting up parser for .txt containing data
    val parser: DelimitedParser = DelimitedParser(DelimitedFormat.Guess)
    val rows: Vector[Either[DelimitedError, Row]] = parser.parseFile(rawInputData)

    println(s"Input data contains ${rows.size} lines") // TODO debug

    val data = rows.collect { case Right(row) => row.toList }
      .filter(list => list.size == 21) // after parsing if each line does not contain 21 cell spaces, we ignore that line

    println(s"Was only able to take in ${data.size} lines") // TODO debug

    // deserializing data
    val donors = data.map(createDonor)

    // input consideration #1 and 5
    val cleanedData = donors.filter(d => d.noOtherId && d.id.isDefined && d.amount.isDefined)

    val zipData = cleanedData.filter(_.zip.isDefined)   //  input consideration #4
    val dateData = cleanedData.filter(_.date.isDefined) //  input consideration #2

    val medianValByZip = zipData.foldLeft(Vector.empty[MedianByZip])(processMedianValByZip)
    medianValByZip.foreach(d => println("Here is zip data" + d))

    writeMedianVals(medianValByZip, medianByZipPath)

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
    val recordToAdd = if (existingDonor.isDefined) { // new record being created using info from existing donor if found
      val old = existingDonor.get
      val donationAmounts = old.amounts :+ donorAmt
      val (lower, upper) = donationAmounts.sorted.splitAt(donationAmounts.size / 2)
      val med = if (donationAmounts.size % 2 == 0) (lower.last + upper.head) / 2.0 else upper.head
      old.copy(
        median = med.round.toInt,
        contributions = old.contributions + 1,
        totalAmount = old.totalAmount + donorAmt,
        amounts = donationAmounts,
      )
    } else { // a completely new record is created
      MedianByZip(
        id = donor.id.get,
        zip = donor.zip.get,
        median = donorAmt,
        contributions = 1,
        amounts = List(donorAmt),
        totalAmount = donorAmt
      )
    }

    records :+ recordToAdd
  }

  private def writeMedianVals(processedDonors: Vector[MedianByZip], medianValFile: String): Unit = {
    val output = new FileWriter(medianValFile)
    val writer = new BufferedWriter(output)

    processedDonors.foreach { d =>
      val record = s"${d.id}|${d.zip}|${d.median}|${d.contributions}|${d.totalAmount}"
      writer.write(record)
      writer.flush()
      writer.newLine()
    }
  }
}

