package org.codelibs.minhash.analysis;

import java.io.IOException;
import java.util.Arrays;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.codelibs.minhash.util.FastBitSet;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.io.BaseEncoding;

/**
 * This class is a token filter to calculate MinHash value.
 * 
 * @author shinsuke
 *
 */
public class MinHashTokenFilter extends TokenFilter {

    private final CharTermAttribute termAttr = addAttribute(CharTermAttribute.class);

    private final PositionIncrementAttribute posIncrAttr = addAttribute(PositionIncrementAttribute.class);

    private final OffsetAttribute offsetAttr = addAttribute(OffsetAttribute.class);

    private HashFunction[] hashFunctions;

    private int hashBit;

    private long[] minHashValues;

    private String minHash;

    public MinHashTokenFilter(final TokenStream input,
            final HashFunction[] hashFunctions, final int hashBit) {
        super(input);
        this.hashFunctions = hashFunctions;
        this.hashBit = hashBit;
        minHashValues = new long[hashFunctions.length];
    }

    @Override
    public final boolean incrementToken() throws IOException {
        final int funcSize = hashFunctions.length;
        
        
        String[] terms = new String[funcSize];
        
        while (input.incrementToken()) {
            final String term = termAttr.toString();
            for (int i = 0; i < funcSize; i++) {
                final HashCode hashCode = hashFunctions[i]
                        .hashUnencodedChars(term);
                final long value = hashCode.asLong();
                if (value < minHashValues[i]) {
                    minHashValues[i] = value;
                    terms[i] = term;
                }
            }
        }
        
        if (minHash != null) {
            return false;
        }

        minHash = BaseEncoding.base64().encode(
                calcMinHash(minHashValues, hashBit));
        termAttr.setEmpty().append(minHash);
        posIncrAttr.setPositionIncrement(0);
        offsetAttr.setOffset(0, minHash.length());

        return true;
    }

    @Override
    public void reset() throws IOException {
        super.reset();
        Arrays.fill(minHashValues, Long.MAX_VALUE);
        minHash = null;
    }

    protected static byte[] calcMinHash(final long[] minHashValues,
            final int hashBit) {
        final int shift = 1;
        final int radix = 1 << shift;
        final long mask = radix - 1;
        int pos = 0;
        final int nbits = minHashValues.length * hashBit;
        final FastBitSet bitSet = new FastBitSet(nbits);
        for (long i : minHashValues) {
            for (int j = 0; j < hashBit; j++) {
                bitSet.set(pos, (int) (i & mask) == 1);
                pos++;
                i >>>= shift;
            }
        }
        return bitSet.toByteArray();
    }

}
