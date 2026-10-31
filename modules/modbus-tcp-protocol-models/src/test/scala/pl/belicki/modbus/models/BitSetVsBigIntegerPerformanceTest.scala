package pl.belicki.modbus.models

import org.scalatest.wordspec.AnyWordSpecLike
import pl.belicki.modbus.models.util.SpacedHex

import java.math.BigInteger
import scala.annotation.tailrec
import scala.language.postfixOps
import scala.util.Random

class BitSetVsBigIntegerPerformanceTest extends AnyWordSpecLike {

  "BigInteger" must {
    "be as fast as byte array" in {
      val bigInteger = BigInteger.ZERO

      @tailrec
      def bigIntHelper(bigInteger: BigInteger, iterations: Int): BigInteger = {
        if (iterations <= 0) bigInteger
        else bigIntHelper(bigInteger.flipBit(Random.nextInt(1024)), iterations - 1)
      }
      val bitSet = new java.util.BitSet

      val beforeBigInteger              = System.nanoTime()
      val newBigInteger                 = bigIntHelper(bigInteger, 1_000_000_000)
      val afterBigInteger, beforeBitSet = System.nanoTime()
      for {
        _ <- 1 to 1_000_000_000
      } bitSet.flip(Random.nextInt(1024))
      val afterBitSet = System.nanoTime()

      val bitSetDuration     = java.time.Duration.ofNanos(afterBitSet - beforeBitSet)
      val bigIntegerDuration = java.time.Duration.ofNanos(afterBigInteger - beforeBigInteger)

      println(s"BitSet     is: ${bitSet.toString.take(20)}..., took: $bitSetDuration")
      println(s"BigInteger is: ${newBigInteger.toString().take(20)}..., took: $bigIntegerDuration")

    }
  }

}
