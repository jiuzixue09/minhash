package com.firstbrave.fbiminhash;

import java.math.BigInteger;

import com.firstbrave.common.hash.HashCode;
import com.firstbrave.common.hash.Murmur3_128HashFunction;
import com.firstbrave.minhash.MinHash;
import com.firstbrave.minhash.token.NGramSentenceTokenizer;
import com.firstbrave.minhash.util.MinHashUtil;



public class TestSimHash {

	public static void main(String[] args) {

		String s1 = "腾讯音乐估值飙升，预计将在今年公开上市 来源：36氪 发布时间：7分钟前 华尔街日报援引知情人士透露，腾讯音乐娱乐集团的估值于近几周飙升。知情人士称，近期一些交易对腾讯音乐的估值约为250亿美元。对腾讯音乐股票的需求上升是因为腾讯预计将在今年晚些时候将其公开上市。";
		String s2 = "腾讯音乐估值飙升，预计将在今年公开上市  华尔街日报援引知情人士透露，腾讯音乐娱乐集团的估值于近几周飙升。知情人士称，近期一些交易对腾讯音乐的估值约为250亿美元。对腾讯音乐股票的需求上升是因为腾讯预计将在今年晚些时候将其公开上市。";

		BigInteger sg1 = MinHashUtil.getSignature(s1);
		BigInteger sg2 = MinHashUtil.getSignature(s2);

		System.out.println("s1 vs s2 :-> " + MinHashUtil.getHammingDistance(sg1, sg2));

		System.out.println(sg1 + ":" + sg2);
		System.out.println("s1 vs s2 :-> " + (double)(76-MinHashUtil.getHammingDistance(sg1, sg2))/76);
	
	}
	

}
