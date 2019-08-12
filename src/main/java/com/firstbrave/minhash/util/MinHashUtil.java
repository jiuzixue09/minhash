package com.firstbrave.minhash.util;

import java.math.BigInteger;

import com.firstbrave.minhash.MinHash;


/**
 * MinHash工具类
 * 
 * @author dave
 * created on 2018年3月9日
 */
public class MinHashUtil {
	
	/**
	 * 将Hash值(二进制)转换为Int
	 * 
	 * @param hash    数组形式的 {0,1,0,1,...}的哈希值
	 * @param begin   要转换的哈希值段的起始位置
	 * @param end     要转换的哈希值段的结束位置
	 * @return  转化为整型的hash值
	 */
	public static int Hash2Int(int[] hash, int begin, int end) {
		int intCode = 0;
		for (int i = begin; i < end; i++) {
			intCode = (intCode << 1);
			intCode += hash[i];
		}
		
		return intCode;
	}
	
	/**
	 * 获得内容的指纹
	 * 
	 * @param source
	 * @return
	 */
	public static BigInteger getSignature(String content) {
		BigInteger result = null;

		if (content != null && content.length() > 0) {
			MinHash MinHash = new MinHash();
			result = MinHash.getSignature(content);
		}

		return result;
	}
	
	
	
	/**
	 * 签名转为二进制,如果达不到实际长度,需要在前面补0
	 * 
	 * @param signature
	 * @return
	 */
	public static String Signature2BinaryStr(BigInteger signature, int length) {
		String tmp = signature.toString(2);
		StringBuilder sb = new StringBuilder();
		if(tmp.length() < length) {
			for(int i=0; i<length-tmp.length(); i++) {
				sb.append("0");
			}
		}
		sb.append(tmp);
		
		return sb.toString();
	}
	
	
	/**
	 * 将签名转换为二进制
	 * 如:208560148841534216148409504476892512671 ->
	 * 10011100111001110011100100111100111011100011101011100101111010110000001100011001110001110000100010001001111011000100000110011111
	 * 
	 * @param signature
	 * @return
	 */
	public static int[] Signature2Binary(String signature, int length) {
		return Signature2Binary(new BigInteger(signature), length);
	}
	
	/**
	 * 将签名转换为二进制
	 * 如:208560148841534216148409504476892512671 ->
	 * 10011100111001110011100100111100111011100011101011100101111010110000001100011001110001110000100010001001111011000100000110011111
	 * 
	 * @param signature
	 * @return
	 */
	public static int[] Signature2Binary(BigInteger signature, int length) {
		String str = Signature2BinaryStr(signature, length);
	
		int[] result = new int[str.length()];
		for(int i=0; i<str.length(); i++) {
			result[i] = Integer.valueOf(str.charAt(i) + ""); 
		}
		
		return result;
	}
	
	/**
	 * 将签名二进制转换为BigInteger
	 * 如10011100111001110011100100111100111011100011101011100101111010110000001100011001110001110000100010001001111011000100000110011111 ->
	 * 208560148841534216148409504476892512671
	 * 
	 * @param bytes
	 * @return
	 */
	public static BigInteger SignatureBinary2BigInteger(String strSignature) {
		String[] str = strSignature.split("");
		
		BigInteger signature = BigInteger.ZERO;
		for (int i = 0; i < str.length; i++) {
			String s = str[i];
			if(s.compareTo("0") > 0) {
				signature = signature.add(BigInteger.ONE.shiftLeft(str.length - i - 1));
			}
		}
		return signature;
	}
	
	
	
	/**
	 * 获得两个签名的汉明距离
	 * 
	 * @param targetSignature 比较签名
	 * @return
	 */
	public static int getHammingDistance(BigInteger fromSignature, BigInteger toSignature) {
		BigInteger x = fromSignature.xor(toSignature);
		int tot = 0;

		// 统计x中二进制位数为1的个数
		// 我们想想，一个二进制数减去1，那么，从最后那个1（包括那个1）后面的数字全都反了，
		// 对吧，然后，n&(n-1)就相当于把后面的数字清0，
		// 我们看n能做多少次这样的操作就OK了。

		while (x.signum() != 0) {
			tot += 1;
			x = x.and(x.subtract(new BigInteger("1")));
		}

		return tot;
	}

	/**
	 * hash距离。二进制比较
	 * 
	 * @param targetHash 比较目标
	 * @return
	 */
	public static int getHashDistance(String fromHash, String toHash) {
		int distance;
		if (fromHash.length() != toHash.length()) {
			distance = -1;
		} else {
			distance = 0;
			for (int i = 0; i < fromHash.length(); i++) {
				if (fromHash.charAt(i) != toHash.charAt(i)) {
					distance++;
				}
			}
		}
		
		return distance;
	}
	
}
