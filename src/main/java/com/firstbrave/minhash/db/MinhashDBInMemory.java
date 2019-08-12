package com.firstbrave.minhash.db;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.firstbrave.minhash.entity.SignatureData;
import com.firstbrave.minhash.util.MinHashUtil;
import com.firstbrave.minhash.util.PriorityQueue;


/**
 * 基于内存的MinHash
 * 
 * @author dave
 * created on 2018年3月9日
 */
public class MinhashDBInMemory extends BaseSimhashDB{
	private int requiredCapacity = 16;		//hash表所需容量
	
	private List<SignatureData> list = new ArrayList<>();
	private ConcurrentHashMap<Integer, StringBuffer>[] map = null;
	private ConcurrentHashMap<BigInteger, Integer> fullSignMap;
	
	public MinhashDBInMemory(){
		init();
	}
	
	public MinhashDBInMemory(int hashLength, int hashSplitLength){
		this.hashLength = hashLength;
		this.hashSplitLength = hashSplitLength;
		matchHashArraySize = hashLength - hashSplitLength;
		init();
	}
	
	public MinhashDBInMemory(int hashLength, int hashSplitLength, int requiredCapacity, int step){
		this.hashLength = hashLength;
		this.hashSplitLength = hashSplitLength;
		matchHashArraySize = hashLength - hashSplitLength;
		
		this.requiredCapacity = requiredCapacity;
		this.step = step;
		init();
	}
	
	@SuppressWarnings("unchecked")
	private void init() {
		assert(step < hashSplitLength);
		matchHashArraySize = matchHashArraySize / step + 1;
		this.map = new ConcurrentHashMap[matchHashArraySize];
		
		this.fullSignMap = new ConcurrentHashMap<>(requiredCapacity);
		
		for (int i = 0; i < map.length; i++) {
			map[i] = new ConcurrentHashMap<>((int)(Math.pow(2, hashSplitLength) / 0.75) + 1);
		}
	}

	
	public boolean saveBySignature(String signature, Long dataId) {
		return saveBySignature(new BigInteger(signature), dataId);
	}
	
	public boolean saveBySignature(BigInteger signature, Long dataId) {
		if(fullSignMap.containsKey(signature)) return false;
		
		int index = 0;
		synchronized (list) {
			list.add(new SignatureData(dataId, signature));
			index = list.size() -1;
		}
		
		fullSignMap.put(signature, index);
		
		int[] hash = MinHashUtil.Signature2Binary(signature, hashLength);
		for (int i = 0; i < matchHashArraySize; i++) {
			map[i].computeIfAbsent(getHashCode(hash, i), StringBuffer::new).append(index).append(",");
		}
			
		return true;
	}

	public boolean deleteBySignature(String signature, Long dataId) throws Exception {
		return deleteBySignature(new BigInteger(signature), dataId);
	}
	
	
	private String removeElement(String source, String target) {
		return String.join(",", Arrays.stream(source.split(",")).filter(it -> !it.equals(target)).toArray(String[]::new));
	}
	
	public boolean deleteBySignature(BigInteger signature, Long dataId) throws Exception {
		int[] hash = MinHashUtil.Signature2Binary(signature, hashLength);
		
		Integer indexId = fullSignMap.get(signature);
		for (int i = 0; i < matchHashArraySize; i++) {
			int code = getHashCode(hash, i);
			
			String value = removeElement(map[i].get(code).toString(), indexId + "");
			map[i].put(code, new StringBuffer(Optional.ofNullable(value).orElse("")));
		}
		
		fullSignMap.remove(signature);
		list.remove(new SignatureData(dataId, signature));
		
		return true;
	}

	/**
	 * 查询相似数据
	 * 
	 * @param signature
	 * @param maxDistance
	 * @return
	 */
	public List<SignatureData> query(String signature, int maxDistance) {
		return queryWhere(signature, maxDistance,0);
	}
	
	/**
	 * 查询相似数据
	 * 
	 * @param signature
	 * @param maxDistance
	 * @return
	 */
	public List<SignatureData> query(String signature, int maxDistance, int topN) {
		return queryWhere(signature, maxDistance,topN);
	}

	private Map<Integer, Long> findHavingSameBlockSignature(BigInteger signature) {
		int[] hash = MinHashUtil.Signature2Binary(signature, hashLength);
        Map<Integer,Long> result = IntStream.range(0, matchHashArraySize).mapToObj(i -> Optional.ofNullable(map[i].get(getHashCode(hash, i))).orElse(new StringBuffer()).toString().split(","))
		.flatMap(it -> Arrays.stream(it)).filter(it -> it.length() > 0).map(Integer::parseInt)
		.collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
		
		return result;
	}

	private int getHashCode(int[] hash, int i) {
		return MinHashUtil.Hash2Int(hash, i * step, i * step + hashSplitLength -1);
	}
	
	private List<SignatureData> queryWhere(String strSignature, int maxDistance, int topN) {
		BigInteger signature = new BigInteger(strSignature);
		
		List<SignatureData> result = new ArrayList<>();
		Map<Integer, Long> signatures = findHavingSameBlockSignature(signature);
		if(signatures.size() < 1) return null;
		
		
		if(topN > 1) {
			PriorityQueue<Entry<Integer,Long>> queue = new PriorityQueue<Entry<Integer,Long>>() {
				@Override
				protected boolean lessThan(Entry<Integer,Long> a, Entry<Integer,Long> b) {
					return a.getValue() - b.getValue() > 0;
				}
			};
			
			queue.initialize(signatures.size());
			signatures.entrySet().forEach(queue::insert);
			if(queue.size() < 1) return null;
			
			Entry<Integer, Long> index = null;
			int distance = 0;
			
			for (int i = 0; i < signatures.size() && result.size() < topN; i++) {
				index = queue.pop();
				if(null == index) continue;
				SignatureData data = this.list.get(index.getKey());
				distance = MinHashUtil.getHammingDistance(signature, data.getSignature());
				if(distance > maxDistance) break;
				data.setDistance(distance);
				result.add(data);
			}
			
		}else {
			Integer index = -1;
			
			for (Entry<Integer, Long> r : signatures.entrySet()) {
				if(index < 0) {
					index = r.getKey();
					continue;
				} 
				if(signatures.get(index) < r.getValue()) index = r.getKey();
			}
			
		
			SignatureData data = this.list.get(index);
			int distance = MinHashUtil.getHammingDistance(signature, data.getSignature());
			if(distance < maxDistance) {
				data.setDistance(distance);
				result.add(data);
			}
		}
		return result.size() >0 ? result: null;
	}
	
	/**
	 * 查询相似数据
	 * 
	 * @param signature
	 * @param maxDistance
	 * @param topN
	 * @return
	 */
	public SignatureData queryTopOne(String signature, int maxDistance) {
		Integer index = fullSignMap.get(new BigInteger(signature));
		
		SignatureData rs = Optional.ofNullable(index).flatMap(it -> Optional.of(list.get(it))).orElseGet(() -> Optional
				.ofNullable(queryWhere(signature, maxDistance, 1)).flatMap(it -> Optional.of(it.get(0))).orElse(null)
		);
		
		return rs;
	}

	
}
