import java.io.{BufferedWriter, File, FileWriter}
import java.nio.file.Paths

import net.tixxit.delimited._

object Main extends App {
  val path = new File(System.getProperty("user.dir")).getCanonicalPath
  val rawInputData = new File(path + "/input/itcont.txt")

  // setting up parser for .txt containing data
  val parser: DelimitedParser = DelimitedParser(DelimitedFormat.Guess)


  val rows: Vector[Either[DelimitedError, Row]] = parser.parseFile(rawInputData)

  val data = rows.map(_.right.get).map(r => r.toList)

  val donors = data.map {donorInfo =>
    Donor(
      id = donorInfo.headOption,
      zip = donorInfo.lift(10),
      date = donorInfo.lift(13),
      amount = donorInfo.lift(14),
      otherId = donorInfo.lift(15)
    ).clean
  }

  donors
    .filter(_.otherId.isEmpty)
    .foreach(d => println(d.medianValFormat()))

  val output = new FileWriter(path + "/output/medianvals_by_zip.txt")
  val writer = new BufferedWriter(output)

  donors
    .filter(_.otherId.isEmpty)
    .foreach { donor =>
      writer.write(donor.medianValFormat())
      writer.flush()
      writer.newLine()
    }



  case class Donor(
                    id: Option[String] = None,
                    zip: Option[String] = None,
                    date: Option[String] = None,
                    amount: Option[String] = None,
                    otherId: Option[String] = None
                  ) {
    def clean: Donor = {
      this.copy(
        id = this.id.filter(_.trim.nonEmpty),
        zip = this.zip.filter(_.trim.nonEmpty),
        date = this.date.filter(_.trim.nonEmpty),
        amount = this.amount.filter(_.trim.nonEmpty),
        otherId = this.otherId.filter(_.trim.nonEmpty)
      )
    }
    def medianValFormat(count: Int = 50, total: Int = 100): String = {
      val information = for {
        i <- this.id
        z <- this.zip
        a <- this.amount
      } yield s"$i|$z|$a|$count|$total"

      information.getOrElse(throw new Exception)
    }
  }

}

