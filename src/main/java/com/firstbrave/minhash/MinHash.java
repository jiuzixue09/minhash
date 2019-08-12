package com.firstbrave.minhash;

import java.math.BigInteger;
import java.util.Arrays;

import com.firstbrave.common.hash.HashCode;
import com.firstbrave.common.hash.HashFunction;
import com.firstbrave.common.hash.Murmur3_128HashFunction;
import com.firstbrave.minhash.token.NGramSentenceTokenizer;
import com.firstbrave.minhash.token.Tokenizer;

public class MinHash {
	private final HashFunction[] hashFunctions;
	
	// The number of bits for each hash value.
    private int hashBit = 1;
    // A base seed for hash functions.
    private int seed = 0;
    // The number of hash functions.
    private int num = 128;
    // Analyzer for 1-bit 128 hash.
	
    private Tokenizer tokenizer;
	public MinHash() {
		super();
		hashFunctions = createHashFunctions(seed,num);
		tokenizer = new NGramSentenceTokenizer(10, 10, 10, true);
	}

	public MinHash(int hashBit, int seed, int num) {
		super();
		this.hashBit = hashBit;
		this.seed = seed;
		this.num = num;
		
		hashFunctions = createHashFunctions(seed,num);
		tokenizer = new NGramSentenceTokenizer(10, 10, 10, true);
	}
	
	public BigInteger getSignature(String content) {
		if(null == content) return null;
		
		String[] terms = tokenizer.tokenize(content);
		if(null == terms || terms.length < 2) return null;
		return getSignature(terms);
	}

	public BigInteger getSignature(String[] content) {
        final int funcSize = hashFunctions.length;
        long[] minHashValues = new long[hashFunctions.length];
        Arrays.fill(minHashValues, Long.MAX_VALUE);
        
        String[] terms = new String[funcSize];
		        
		for (String term : content) {
			for (int i = 0; i < funcSize; i++) {
				final HashCode hashCode = hashFunctions[i].hashUnencodedChars(term);
				final long value = hashCode.asLong();
				if (value < minHashValues[i]) {
					minHashValues[i] = value;
					terms[i] = term;
				}
			}
		}
		
		return calcMinHash(minHashValues, hashBit);
	}
	
	
	private BigInteger calcMinHash(final long[] minHashValues, final int hashBit) {
		final int shift = 1;
		final int radix = 1 << shift;
		final long mask = radix - 1;
		int pos = 0;

		BigInteger signature = BigInteger.ZERO;
		for (long i : minHashValues) {
			for (int j = 0; j < hashBit; j++) {
				pos++;
				if((i & mask) > 0) {
					signature = signature.add(BigInteger.ONE.shiftLeft((num * hashBit) - pos));
				}
				i >>>= shift;
			}
		}
		return signature;
	}

	 /**
	  * Create hash functions.
	  * 
	  * @param seed a base seed
	  * @param num the number of hash functions.
	  * @return
	  */
	 public HashFunction[] createHashFunctions(final int seed,
	         final int num) {
	     final HashFunction[] hashFunctions = new HashFunction[num];
	     for (int i = 0; i < num; i++) {
	         hashFunctions[i] = new Murmur3_128HashFunction(seed + i);
	     }
	     return hashFunctions;
	 }

	public void setTokenizer(Tokenizer tokenizer) {
		this.tokenizer = tokenizer;
	}
	 

}
