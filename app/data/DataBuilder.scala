package data

import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.util
import models.Account
import models.Address
import models.Customer
import models.Patient
import org.joda.time.DateTime


class DataBuilder {
  def createCustomers: List[Customer] = {
    try { // Simulate file access by creating temp file
      // create a temp file
      val temp = File.createTempFile("tempfile", ".tmp")
      // write it
      val bw = new BufferedWriter(new FileWriter(temp))
      bw.write("This is the temporary file content")
      bw.close
      System.out.println(" File Write Successful ")
    } catch {
      case e: IOException =>
        e.printStackTrace
    }
    val accounts1: Set[Account] =
      Set(new Account(1111, 321045, "CHECKING", 10000, 10),
          new Account(1112, 321045, "SAVING", 100000, 20))
    val customer1 = new Customer("ID-4242", 4242, "Joe", "Smith", DateTime.parse("1982-01-10").toDate, "123-45-3456", "000111222", "981-110-0101", "408-123-1233", new Address("High Street", "", "Santa Clara", "CA", "95054"), accounts1)
    val accounts2: Set[Account] =
      Set(new Account(2111, 421045, "CHECKING", 20000, 10),
          new Account(2112, 421045, "MMA", 200000, 20))
    val customer2 = new Customer("ID-4243", 4343, "Paul", "Jones", DateTime.parse("1973-01-03").toDate, "321-67-3456", "222665436", "981-110-0100", "302-767-8796", new Address("Main Street", "", "Sunnyvale", "CA", "94086"), accounts2)
    val accounts3: Set[Account] =
      Set(new Account(3111, 421045, "SAVING", 30000, 10),
          new Account(3112, 421045, "MMA", 300000, 20))
    val customer3 = new Customer("ID-4244", 4244, "Steve", "Toale", DateTime.parse("1979-03-08").toDate, "769-12-9987", "888225436", "981-110-0101", "650-897-2366", new Address("Main Street", "", "Redwood City", "CA", "95058"), accounts3)
    List(customer1, customer2, customer3)
  }

  def createPatients: List[Patient] = {
    val patient1 = new Patient(42, "Suchakra", "Sharma", DateTime.parse("1970-01-01").toDate, 42, 42, "linuxol", 42, 42, 42, 42)
    List(patient1)
  }
}
