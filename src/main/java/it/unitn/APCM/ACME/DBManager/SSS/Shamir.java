package it.unitn.APCM.ACME.DBManager.SSS;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * An implementation of Shamir's Secret Sharing over {@code GF(256)} to securely split secrets into
 * {@code N} parts, of which any {@code K} can be joined to recover the original secret.
 *
 * <p>{@link Scheme} uses the same GF(256) field polynomial as the Advanced Encryption Standard
 * (AES): {@code 0x11b}, or {@code x}<sup>8</sup> + {@code x}<sup>4</sup> + {@code x}<sup>3</sup> +
 * {@code x} + 1.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Shamir%27s_Secret_Sharing">Shamir's Secret
 *     Sharing</a>
 * @see <a href="http://www.cs.utsa.edu/~wagner/laws/FFM.html">The Finite Field {@code GF(256)}</a>
 */
public class Shamir {

  /**
   * Creates a new {@link Scheme} instance.
   *
   * @param random a {@link SecureRandom} instance
   * @param n the number of parts to produce (must be {@code >1})
   * @param k the threshold of joinable parts (must be {@code <= n})
   */
  private SecureRandom random;
  private int n;
  private int k;

  
  public Shamir(SecureRandom random, int n, int k) {
    this.random = random;
    checkArgument(k > 1, "K must be > 1");
    checkArgument(n >= k, "N must be >= K");
    checkArgument(n <= 255, "N must be <= 255");
    this.n = n;
    this.k = k;
  }

  public Shamir(){
    this.n = 5;
    this.k = 3;
  }

  /**
   * Splits the given secret into {@code n} parts, of which any {@code k} or more can be combined to
   * recover the original secret.
   *
   * @param secret the secret to split
   * @return a map of {@code n} part IDs and their values
   */
  public Map<Integer, byte[]> split(byte[] secret) {
    // generate part values
    final byte[][] values = new byte[n][secret.length];
    for (int i = 0; i < secret.length; i++) {
      // for each byte, generate a random polynomial, p
      final byte[] p = GF256.generate(random, k - 1, secret[i]);
      for (int x = 1; x <= n; x++) {
        // each part's byte is p(partId)
        values[x - 1][i] = GF256.eval(p, (byte) x);
      }
    }

    // return as a set of objects
    final Map<Integer, byte[]> parts = new HashMap<>(n());
    for (int i = 0; i < values.length; i++) {
      parts.put(i + 1, values[i]);
    }
    return Collections.unmodifiableMap(parts);
  }

  /**
   * Joins the given parts to recover the original secret.
   *
   * <p><b>N.B.:</b> There is no way to determine whether or not the returned value is actually the
   * original secret. If the parts are incorrect, or are under the threshold value used to split the
   * secret, a random value will be returned.
   *
   * @param parts a map of part IDs to part values
   * @return the original secret
   * @throws IllegalArgumentException if {@code parts} is empty or contains values of varying
   *     lengths
   */
  public byte[] join(Map<Integer, byte[]> parts) {
    checkArgument(parts.size() > 0, "No parts provided");
    final int[] lengths = parts.values().stream().mapToInt(v -> v.length).distinct().toArray();
    checkArgument(lengths.length == 1, "Varying lengths of part values");
    final byte[] secret = new byte[lengths[0]];
    for (int i = 0; i < secret.length; i++) {
      final byte[][] points = new byte[parts.size()][2];
      int j = 0;
      for (Map.Entry<Integer, byte[]> part : parts.entrySet()) {
        points[j][0] = part.getKey().byteValue();
        points[j][1] = part.getValue()[i];
        j++;
      }
      secret[i] = GF256.interpolate(points);
    }
    return secret;
  }

  /**
   * The number of parts the scheme will generate when splitting a secret.
   *
   * @return {@code N}
   */
  public int n() {
    return n;
  }

  /**
   * The number of parts the scheme will require to re-create a secret.
   *
   * @return {@code K}
   */
  public int k() {
    return k;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Shamir)) {
      return false;
    }
    final Shamir scheme = (Shamir) o;
    return n == scheme.n && k == scheme.k && Objects.equals(random, scheme.random);
  }

  @Override
  public int hashCode() {
    return Objects.hash(random, n, k);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", Shamir.class.getSimpleName() + "[", "]")
        .add("random=" + random)
        .add("n=" + n)
        .add("k=" + k)
        .toString();
  }

  private static void checkArgument(boolean condition, String message) {
    if (!condition) {
      throw new IllegalArgumentException(message);
    }
  }

  private byte[] parse(String stringToParse){
		if(stringToParse == ""){
			return null;
		}
		String[] parsedString = (stringToParse.split(","));
		byte[] bytes = new byte[parsedString.length];
		int i = 0;
		for(String s: parsedString){
			bytes[i] = (byte)(Integer.parseInt(s));
			i++;
		}
		return bytes;
	}

	public SecretKey getMasterSecret(){
		byte[] seed = null;
		byte[] k1 = null;
		byte[] k2 = null;
		byte[] k3 = null;
		byte[] k4 = null;
		byte[] k5 = null;

		try {
			String content = new String(Files.readAllBytes(Paths.get("SSS.txt")));
			String[] seedString = (content.split(";"));
			seed = parse(seedString[0]);
			k1 = parse(seedString[1]);
			k2 = parse(seedString[2]);
			k3 = parse(seedString[3]);
			k4 = parse(seedString[4]);
			k5 = parse(seedString[5]);
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.random = new SecureRandom(seed);	
		
		Map<Integer, byte[]> keys = new HashMap<Integer, byte[]>();
		if(k1 != null){ keys.put(1, k1); }
		if(k2 != null){ keys.put(2, k2); }
		if(k3 != null){ keys.put(3, k3); }
		if(k4 != null){ keys.put(4, k4); }
		if(k5 != null){ keys.put(5, k5); }
	
		byte[] secretByte = this.join(keys);
		//CHECK ENCRYPTION ALGORITHM
		return new SecretKeySpec(secretByte, 0, secretByte.length, "chacha20"); 
	}
}
