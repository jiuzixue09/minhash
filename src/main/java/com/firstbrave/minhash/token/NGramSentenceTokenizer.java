package com.firstbrave.minhash.token;

import java.util.ArrayList;
import java.util.List;


public class NGramSentenceTokenizer implements Tokenizer  {
	private int minNGramSize;
    private int maxNGramSize;
    private int step = 1;
    private boolean keepOldTokens;

    public NGramSentenceTokenizer(int minNGramSize,int maxNGramSize,boolean keepOldTokens) {
        this.minNGramSize = minNGramSize;
        this.maxNGramSize = maxNGramSize;
        this.keepOldTokens = keepOldTokens;
    }
    

	public NGramSentenceTokenizer(int minNGramSize, int maxNGramSize, int step, boolean keepOldTokens) {
		super();
		this.minNGramSize = minNGramSize;
		this.maxNGramSize = maxNGramSize;
		this.keepOldTokens = keepOldTokens;
		this.step = step;
	}



	@Override
	public String[] tokenize(String text) {
		RegexSentenceTokenizer rt = new RegexSentenceTokenizer();
    	String[] initialTokens = rt.tokenize(text);
        List<String> tokens = new ArrayList<>();
        for (int i=0; i<initialTokens.length; i++) {
            String str = (String) initialTokens[i];
            if (keepOldTokens) tokens.add(str);
            if(str.length() > step){
            	for (int lo=0; lo<str.length(); lo+= step) {
            		for (int len=minNGramSize; len<=maxNGramSize; len++) {
            			if (lo+len<=str.length()) {
            				tokens.add(str.substring(lo,lo+len)); 
            			}
            		}
            	}
            }
        }
        return tokens.toArray(new String[0]);
	}
}
