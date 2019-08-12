/*
 * Copyright (C) 2011 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.firstbrave.common.hash;

public interface HashFunction {
  /**
   * Begins a new hash code computation by returning an initialized, stateful {@code
   * Hasher} instance that is ready to receive data. Example: <pre>   {@code
   *
   *   HashFunction hf = Hashing.md5();
   *   HashCode hc = hf.newHasher()
   *       .putLong(id)
   *       .putBoolean(isActive)
   *       .hash();}</pre>
   */
  Hasher newHasher();

  /**
   * Begins a new hash code computation as {@link #newHasher()}, but provides a hint of the
   * expected size of the input (in bytes). This is only important for non-streaming hash
   * functions (hash functions that need to buffer their whole input before processing any
   * of it).
   */
  Hasher newHasher(int expectedInputSize);


  /**
   * Shortcut for {@code newHasher().putLong(input).hash()}; returns the hash code for the
   * given {@code long} value, interpreted in little-endian byte order. The implementation
   * <i>might</i> perform better than its longhand equivalent, but should not perform worse.
   */
  HashCode hashLong(long input);

  /**
   * Shortcut for {@code newHasher().putBytes(input).hash()}. The implementation
   * <i>might</i> perform better than its longhand equivalent, but should not perform
   * worse.
   */
  HashCode hashBytes(byte[] input);

  /**
   * Shortcut for {@code newHasher().putBytes(input, off, len).hash()}. The implementation
   * <i>might</i> perform better than its longhand equivalent, but should not perform
   * worse.
   *
   * @throws IndexOutOfBoundsException if {@code off < 0} or {@code off + len > bytes.length}
   *   or {@code len < 0}
   */
  HashCode hashBytes(byte[] input, int off, int len);

  /**
   * Shortcut for {@code newHasher().putUnencodedChars(input).hash()}. The implementation
   * <i>might</i> perform better than its longhand equivalent, but should not perform worse.
   * Note that no character encoding is performed; the low byte and high byte of each {@code char}
   * are hashed directly (in that order).
   *
   * @since 15.0 (since 11.0 as hashString(CharSequence)).
   */
  HashCode hashUnencodedChars(CharSequence input);

  /**
   * Returns the number of bits (a multiple of 32) that each hash code produced by this
   * hash function has.
   */
  int bits();
}
