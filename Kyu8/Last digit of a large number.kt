
//The file of KOTLIN
import java.math.BigInteger;
fun lastDigit(base: BigInteger, exponent: BigInteger): Int = 
  base.modPow(exponent, BigInteger.TEN).toInt()
