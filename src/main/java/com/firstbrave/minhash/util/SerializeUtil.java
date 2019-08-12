package com.firstbrave.minhash.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class SerializeUtil {
	public static void serialize(String fileName, Object object) throws IOException {
		try(FileOutputStream fos = new FileOutputStream(fileName); ObjectOutputStream oos = new ObjectOutputStream(fos)){
			oos.writeObject(object);
			oos.flush();
		}
	}
	
	public static Object deserialize(String fileName) throws IOException, ClassNotFoundException{
		Object object = null;
		try(FileInputStream fis = new FileInputStream(fileName); ObjectInputStream ois = new ObjectInputStream(fis)){
			object = ois.readObject();
			ois.close();
		}
		return object;
	}
	
	 /**  
     * 对象转数组  
     * @param obj  
     * @return  
	 * @throws IOException 
     */  
    public static byte[] toByteArray (Object obj) throws IOException {      
        byte[] bytes = null;      
        try(ByteArrayOutputStream bos = new ByteArrayOutputStream(); ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(obj);        
            oos.flush();         
            bytes = bos.toByteArray ();              
        }    
        return bytes;    
    }   
       
    /**  
     * 数组转对象  
     * @param bytes  
     * @return  
     * @throws IOException 
     * @throws ClassNotFoundException 
     */  
    public static Object toObject (byte[] bytes) throws ClassNotFoundException, IOException {      
        Object obj = null;      
        try(ByteArrayInputStream bis = new ByteArrayInputStream (bytes);ObjectInputStream ois = new ObjectInputStream (bis)) {        
            obj = ois.readObject();      
        }     
        return obj;    
    }   
    
    

          
    @SuppressWarnings("unchecked")
    public static <M> List<M> toListObject(byte[] in) throws IOException, ClassNotFoundException {  
        List<M> list = new ArrayList<>(); 
        
        try(ByteArrayInputStream bis = new ByteArrayInputStream (in);ObjectInputStream ois = new ObjectInputStream (bis)) {  
            if (in != null) {  
                while (bis.available() > 0) {  
					M m = (M)ois.readObject();  
                    if (m == null) break;  
                    list.add(m);  
                      
                }  
            }  
        }
          
        return  list;  
    }  

    @SuppressWarnings("unchecked")  
    public static <M> byte[] toByteArrayList(Object value) throws IOException {  
        if (value == null)  
            throw new NullPointerException("Can't serialize null");  
          
        List<M> values = (List<M>) value;  
        byte[] results = null;  
          
        try(ByteArrayOutputStream bos = new ByteArrayOutputStream(); ObjectOutputStream oos = new ObjectOutputStream(bos)) {  
            for (M m : values) {  
                oos.writeObject(m);  
            }  
            results = bos.toByteArray();  
        } 
          
        return results;  
    }  
    
    public static byte[] LongToBytes(long values) {  
        byte[] buffer = new byte[8]; 
        for (int i = 0; i < 8; i++) {   
             int offset = 64 - (i + 1) * 8;    
             buffer[i] = (byte) ((values >> offset) & 0xff); 
         }
        return buffer;  
   }

    public static long BytesToLong(byte[] buffer) {   
       long  values = 0;   
       for (int i = 0; i < 8; i++) {    
           values <<= 8; values|= (buffer[i] & 0xff);   
       }   
       return values;  
    } 
}
