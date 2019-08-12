package com.firstbrave.minhash.db;

import java.util.Arrays;

/**
 * 
 * @author dave created on 2018年3月9日
 */
public abstract class BaseSimhashDB {
	protected int hashSplitLength = 16;	// Simhash分割长度,按这个长度切成多个小块
	protected int hashLength = 128;		// Simhash二进制长度
	
	protected int matchHashArraySize = hashLength - hashSplitLength;
	protected int step = 2; //shingle划分步长
	
	public byte[] arraycopy(byte[] data1, byte[] data2) {
		byte[] bytes = new byte[data1.length + data2.length];
		System.arraycopy(data1, 0, bytes, 0, data1.length);
		System.arraycopy(data2, 0, bytes, data1.length, data2.length);
		
		return bytes;
	}
	
	public byte[][] arrySplit(byte[] bytes){
		byte[][] byteArray = new byte[2][bytes.length - 8];
		
		byteArray[0] = Arrays.copyOfRange(bytes, 0, 8);
		byteArray[1] = Arrays.copyOfRange(bytes, 8, bytes.length);
		return byteArray;
	}
	
}
