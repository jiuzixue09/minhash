package com.firstbrave.minhash.db;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.firstbrave.minhash.entity.SignatureData;
import com.firstbrave.minhash.util.MinHashUtil;
import com.firstbrave.minhash.util.PriorityQueue;
import com.firstbrave.minhash.util.SerializeUtil;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;


/**
 * 基于Redis的MinHash
 * 
 * @author dave
 * created on 2018年3月9日
 */
public class MinhashDBRedis extends BaseSimhashDB{
	private final String redisGroupName;
	
	public static Consumer<Jedis> closeConnection;
	public static Supplier<Jedis> openConnection;
	

	public MinhashDBRedis(String redisGroupName){
		this.redisGroupName = redisGroupName;
		this.step = 10;
		init();
	}
	
	public MinhashDBRedis(String redisGroupName, int hashLength, int hashSplitLength){
		this.redisGroupName = redisGroupName;
		this.hashLength = hashLength;
		this.hashSplitLength = hashSplitLength;
		this.matchHashArraySize = hashLength-hashSplitLength ;
		this.step = 10;
		init();
	}
	
	private void init() {
		if(null == closeConnection || null == openConnection) {
			throw new NullPointerException("please init the closeConnetion and openConnection first!!!");
		}
		matchHashArraySize = matchHashArraySize / step + 1;
	}

	
	public boolean saveBySignature(String signature, Long dataId) throws Exception {
		return saveBySignature(new BigInteger(signature), dataId);
	}
	
	public boolean saveBySignature(BigInteger signature, Long dataId) throws Exception {
		boolean result = false;
		
		int[] hash = MinHashUtil.Signature2Binary(signature, hashLength);
		int[] sCode = new int[matchHashArraySize]; 
		
		Jedis jedis = null;
		try{
			jedis = openConnection.get();
			for (int i = 0; i < matchHashArraySize; i++) {
				sCode[i] = getHashCode(hash, i);
				
				byte[] key = (redisGroupName + ":simhash_" + i + ":" + sCode[i]).getBytes();
				
				jedis.sadd(key, arraycopy(SerializeUtil.LongToBytes(dataId), signature.toByteArray()));
				
			}
		}finally {
			closeConnection.accept(jedis);
		}
		
		result = true;
		
		return result;
	}

	
	public boolean deleteBySignature(BigInteger signature, Long dataId) throws Exception {
		boolean result = false;
		int[] hash = MinHashUtil.Signature2Binary(signature, hashLength);
		int[] sCode = new int[matchHashArraySize];
		
		
		Jedis jedis = null;
		try{
			jedis = openConnection.get();
			for (int i = 0; i < matchHashArraySize; i++) {
				sCode[i] = getHashCode(hash, i);
				
				byte[] key = (redisGroupName + ":simhash_" + i + ":" + sCode[i]).getBytes();
				
				jedis.srem(key,  arraycopy(SerializeUtil.LongToBytes(dataId), signature.toByteArray()));
				
			}
		}finally {
			closeConnection.accept(jedis);
		}
		
		result = true;
		return result;
	}

	private int getHashCode(int[] hash, int i) {
		return MinHashUtil.Hash2Int(hash, i * step, i * step + hashSplitLength);
	}

	
	/**
	 * 查询相似数据并返回相似度最高的N条记录
	 * 
	 * @param signature
	 * @param maxDistance
	 * @return
	 * @throws Exception 
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	public List<SignatureData> query(String signature, int maxDistance, int topN) throws Exception {
		return queryWhere(new BigInteger(signature), maxDistance,topN);
	}
	
	/**
	 * 查询相似数据并返回相似度最高的N条记录
	 * 
	 * @param signature
	 * @param maxDistance
	 * @return
	 * @throws Exception 
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	public List<SignatureData> query(BigInteger signature, int maxDistance, int topN) throws Exception {
		return queryWhere(signature, maxDistance,topN);
	}
	
	
	/**
	 * 查询相似数据并返回相似度最高的一条记录
	 * 
	 * @param signature
	 * @param maxDistance
	 * @param topN
	 * @return
	 * @throws Exception 
	 */
	public SignatureData queryTopOne(String signature, int maxDistance) throws Exception {
		return queryTopOne(new BigInteger(signature), maxDistance);
	}
	

	/**
	 * 查询相似数据,并返回相似度最高的一条记录
	 * 
	 * @param signature
	 * @param maxDistance
	 * @param topN
	 * @return
	 * @throws Exception 
	 */
	public SignatureData queryTopOne(BigInteger signature, int maxDistance) throws Exception {
		List<SignatureData> rs = queryWhere(signature, maxDistance, 1);
		if(null == rs || rs.size() < 1) return null;
		return rs.get(0);
	}
	
	
	private Map<Long, SignatureData> findHavingSameBlockSignature(BigInteger signature, int maxDistance) throws Exception {
		Map<Long,SignatureData> result = new HashMap<>();
		
		int[] hash = MinHashUtil.Signature2Binary(signature, hashLength);
		int[] sCode = new int[matchHashArraySize];
		
		Jedis jedis = null; 
		Pipeline pipeline = null;
		SignatureData d = null;
		List<Object> results = null;
		try{
			jedis = openConnection.get();
			pipeline = jedis.pipelined();

			for (int i = 0; i < matchHashArraySize; i++) {
				sCode[i] = getHashCode(hash, i);
				
				byte[] key = (redisGroupName + ":simhash_" + i + ":" + sCode[i]).getBytes();
				pipeline.smembers(key);
			}
			results = pipeline.syncAndReturnAll();
		}finally {
			closeConnection.accept(jedis);
		}
		
		for(Object obj : results) {
			@SuppressWarnings("unchecked")
			Set<byte[]> smembers = (Set<byte[]>)obj;
			if(null == smembers) continue;
		
			for (byte[] b : smembers) {
				byte[][] bytes = arrySplit(b);
				d = new SignatureData(SerializeUtil.BytesToLong(bytes[0]), new BigInteger(bytes[1]));
				SignatureData t = result.get(d.getDataId());
				if(null != t){
					t.setSameBlockCount(t.getSameBlockCount() + 1);
				} else{
					d.setSameBlockCount(1);
					result.put(d.getDataId(), d);
				} 
			}
		}
		
		return result;
	}

	private List<SignatureData> queryWhere(BigInteger signature, int maxDistance, int topN) throws Exception {
		Map<Long,SignatureData> result = findHavingSameBlockSignature(signature, maxDistance);
		
		if(result.size() < 1) return null;
		List<SignatureData> list = new ArrayList<>();
		if(topN > 1) {
			PriorityQueue<SignatureData> queue = new PriorityQueue<SignatureData>() {
				@Override
				protected boolean lessThan(SignatureData a, SignatureData b) {
					return a.getSameBlockCount() - b.getSameBlockCount() > 0;
				}
			};
			queue.initialize(result.size());
			
			result.values().forEach(queue::insert);
			if(queue.size() < 1) return null;
			SignatureData data = null;
			int distance = 0;
			
			for (int i = 0; i < result.size(); i++) {
				data = queue.pop();
				if(null == data) continue;
				if(distance > maxDistance) break;
				
				distance = MinHashUtil.getHammingDistance(signature, data.getSignature());
				data.setDistance(distance);
				list.add(data);
				
			}
		}else {
			SignatureData data = null;
			for (SignatureData s : result.values()) {
				if(null == data) data = s;
				if(data.getSameBlockCount() < s.getSameBlockCount()) data = s;
			}
			int distance = MinHashUtil.getHammingDistance(signature, data.getSignature());
			if(distance < maxDistance) {
				data.setDistance(distance);
				list.add(data);
			}
		}
		
		return list;
	}

	
}
