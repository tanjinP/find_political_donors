import java.time.LocalDate
import java.time.format.DateTimeFormatter

import scala.util.Try

case class Donor(
                  id: Option[String] = None,
                  zip: Option[String] = None,
                  date: Option[String] = None,
                  amount: Option[String] = None,
                  noOtherId: Boolean
                ) {
  /**
    * Returns cleaned up the Donor object as per 'Input file considerations'
    */
  def clean: Donor = {
    val dateFormat = DateTimeFormatter.ofPattern("MMddyyyy")
    this.copy(
      id = this.id.filter(_.trim.nonEmpty),
      zip = this.zip.filter(_.trim.nonEmpty)
        .map(_.take(5))                                       //  consideration #3
        .find(_.matches("^\\d{5}$")),                         //  consideration #4
      date = this.date.filter(_.trim.nonEmpty)
        .map(s => Try(LocalDate.parse(s, dateFormat)))
        .find(_.isSuccess)                                    //  consideration #2
        .flatMap(_ => this.date),
      amount = this.amount
        .filter(s => s.trim.nonEmpty && s.matches("^\\d+$"))  // ensuring a valid number so .toInt is safe
    )
  }
}

trait Output
case class MedianByZip(
                        id: String,
                        zip: String,
                        median: Int,
                        contributions: Int,
                        totalAmount: Int,
                        amounts: List[Int] = List.empty
                      ) extends Output

case class MedianByDate(
                         id: String,
                         date: String,
                         median: Int,
                         contributions: Int,
                         totalAmount: Int,
                         amounts: List[Int] = List.empty
                       ) extends Output