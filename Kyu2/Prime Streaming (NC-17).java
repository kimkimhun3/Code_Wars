import java.util.stream.IntStream;
import java.util.BitSet;

public class Primes {
  private static final int N = 810000000;
  private static final BitSet SIEVE = new BitSet(N + 1);
  static {
    for(int i=2; i*i<=N; i++) if(!SIEVE.get(i)) for(int j=i; i*j<=N; j++) SIEVE.set(i * j);
  }

  public static IntStream stream() {
    return IntStream.range(2, N).filter(x -> !SIEVE.get(x));
  }
}
