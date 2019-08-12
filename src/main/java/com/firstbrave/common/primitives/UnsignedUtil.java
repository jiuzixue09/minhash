package com.firstbrave.common.primitives;

public final class UnsignedUtil {
  private UnsignedUtil() {}

  private static final int UNSIGNED_MASK = 0xFF;
  
  static final long INT_MASK = 0xffffffffL;

  /**
   * Returns the value of the given byte as an integer, when treated as
   * unsigned. That is, returns {@code value + 256} if {@code value} is
   * negative; {@code value} itself otherwise.
   *
   * @since 6.0
   */
  public static int toInt(byte value) {
    return value & UNSIGNED_MASK;
  }

  /**
   * Returns the value of the given {@code int} as a {@code long}, when treated as unsigned.
   */
  public static long toLong(int value) {
    return value & INT_MASK;
  }

}
