package com.firstbrave.minhash.entity;

import java.io.Serializable;
import java.math.BigInteger;

/**
 * 签名数据对象
 * 
 * @author dave
 * created on 2018年3月9日
 */
public class SignatureData implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 3577868159852691724L;
	
	private Long dataId;
	private BigInteger signature;
	private transient int distance = 0;
	private transient int sameBlockCount = 0;
	
	public int getSameBlockCount() {
		return sameBlockCount;
	}
	public void setSameBlockCount(int sameBlockCount) {
		this.sameBlockCount = sameBlockCount;
	}

	public Long getDataId() {
		return dataId;
	}
	public void setDataId(Long dataId) {
		this.dataId = dataId;
	}
	
	public BigInteger getSignature() {
		return signature;
	}
	public void setSignature(BigInteger signature) {
		this.signature = signature;
	}
	
	public int getDistance() {
		return distance;
	}
	public void setDistance(int distance) {
		this.distance = distance;
	}
	
	public SignatureData(Long dataId, BigInteger signature) {
		super();
		this.dataId = dataId;
		this.signature = signature;
	}
	
	
	@Override
	public boolean equals(Object obj) {
		boolean result = false;
		if(obj !=null && obj instanceof SignatureData) {
			SignatureData toObj = (SignatureData)obj;
			if(this.getDataId().equals(toObj.getDataId()) && this.getSignature().equals(toObj.getSignature())) {
				result = true;
			}
		}
		
		return result;
	}
	
	@Override
	public String toString() {
		return "[dataId:" + this.dataId + ",signature:" + this.signature + ",distance:" + this.distance + "]";
	}
	
	
}
