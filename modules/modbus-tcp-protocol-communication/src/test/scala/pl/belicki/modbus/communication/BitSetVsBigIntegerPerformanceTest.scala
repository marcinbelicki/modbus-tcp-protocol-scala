package pl.belicki.modbus.communication

import at.favre.lib.bytes.{Bytes, MutableBytes}
import org.scalatest.wordspec.AnyWordSpecLike
import pl.belicki.modbus.models.util.SpacedHex

import java.math.BigInteger
import scala.annotation.tailrec
import scala.language.postfixOps
import scala.util.Random

class BitSetVsBigIntegerPerformanceTest extends AnyWordSpecLike {

  "BigInteger" must {
    "be as fast as byte array" in {
      val bits       = 1024
      val iterations = 1_000_000
      def randomBit  = Random.nextInt(bits)

      val bigInteger = BigInteger.ZERO
      val bitSet     = new java.util.BitSet
      val bytes      = MutableBytes.allocate(bits / 8)

      @tailrec
      def bigIntHelper(bigInteger: BigInteger, iterations: Int): BigInteger = {
        if (iterations <= 0) bigInteger
        else bigIntHelper(bigInteger.flipBit(randomBit), iterations - 1)
      }

      val beforeBigInteger = System.nanoTime()

      val newBigInteger = bigIntHelper(bigInteger, iterations)

      val afterBigInteger, beforeBitSet = System.nanoTime()

      for {
        _ <- 1 to iterations
      } bitSet.flip(randomBit)

      val afterBitSet, beforeBytes = System.nanoTime()

      for {
        _ <- 1 to iterations
      } bytes.switchBit(randomBit)

      val afterBytes = System.nanoTime()

      val bitSetDuration     = java.time.Duration.ofNanos(afterBitSet - beforeBitSet)
      val bigIntegerDuration = java.time.Duration.ofNanos(afterBigInteger - beforeBigInteger)
      val bytesDuration      = java.time.Duration.ofNanos(afterBytes - beforeBytes)

      println(s"BitSet     is: ${bitSet.toString.take(20)}..., took: $bitSetDuration")
      println(s"BigInteger is: ${newBigInteger.toString().take(20)}..., took: $bigIntegerDuration")
      println(s"Bytes      are: ${bytes.toString.take(20)}..., took: $bytesDuration")

    }
  }

}
