package entity;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Dictionary {
	public static String CHINESE_REGX = "[\u4E00-\u9FA5]";
	public static String NUM_REGX = "[£°-£¹]|[0-9]";
	public static String EN_REGX = "[[A-Z]|[a-z]]+";
	public static String NUM_IND = "N";
	public static String EN_IND = "E";
	public static String Num_regx = "[[£°-£¹]|[0-9]]+[.[[£°-£¹]|[0-9]]]*[%|£¥]*";
	public static String NO_CHINES_REGX = "[^\u4E00-\u9FA5]";
	
	Map<String, DicNode> rootMap;
	Map<String, DicNode> rooMapCache;
	public Dictionary(){
		rootMap = new HashMap<String, DicNode>();
		rooMapCache = new HashMap<String, DicNode>();
	}
	
	public void addWordTocache(String word, double frequency){
		String firstChar =  word.substring(0, 1);
		DicNode rootNode = this.rooMapCache.get(firstChar);
		if(rootNode==null){
			rootNode = new DicNode(firstChar);
			rootNode.setCurrentChar(firstChar);
			rooMapCache.put(firstChar, rootNode);
		}
		rootNode.addIterm(word.substring(1), frequency);
	}
	
	public void resetCacheFrequency(){
		for(Map.Entry<String, DicNode> rootEntry : this.rooMapCache.entrySet()){
			rootEntry.getValue().resetFrequency();
		}
	}
	
	public double getCachedFrequancy(String word){
		String firstChar =  word.substring(0, 1);
		DicNode rootNode = this.rooMapCache.get(firstChar);
		if (rootNode == null){
			return -1;
		}else{
			return rootNode.getItermfrequency(word.substring(1));
		}
	}
	
	public void upDateDictionary(){
		Map<String, DicNode> temp = this.rootMap;
		this.rootMap = this.rooMapCache;
		this.rooMapCache = temp;
		resetCacheFrequency();
	}
	
	public double getWordFrequency(String word){
		String firstChar =  word.substring(0, 1);
		DicNode rootNode = this.rootMap.get(firstChar);
		if (rootNode == null){
			return -1;
		}else{
			return rootNode.getItermfrequency(word.substring(1));
		}
	}
	
	public void addWord(String word, double frequency){
		String firstChar =  word.substring(0, 1);
		DicNode rootNode = this.rootMap.get(firstChar);
		DicNode cachedRoot = this.rooMapCache.get(firstChar);
		if(rootNode==null){
			rootNode = new DicNode(firstChar);
			rootNode.setCurrentChar(firstChar);
			rootMap.put(firstChar, rootNode);
		}
		if(cachedRoot==null){
			cachedRoot= new DicNode(firstChar);
			cachedRoot.setCurrentChar(firstChar);
			rooMapCache.put(firstChar, cachedRoot);
		}
		rootNode.addIterm(word.substring(1), frequency);
		cachedRoot.addIterm(word.substring(1), 0);
	}
	
	public double caltotalFre(){
		double result = 0;
		for(Map.Entry<String, DicNode> rootEntry : this.rootMap.entrySet()){
			result = result+rootEntry.getValue().calTotalFrequency();
		}
		return result;
	}
	
	public double calCachedTotalFre(){
		double result = 0;
		for(Map.Entry<String, DicNode> rootEntry : this.rooMapCache.entrySet()){
			result = result+rootEntry.getValue().calTotalFrequency();
		}
		return result;
	}
	
	public void normalize(){
		double totalFrequency = caltotalFre();
		for(Map.Entry<String, DicNode> rootEntry : this.rootMap.entrySet()){
			rootEntry.getValue().normalize(totalFrequency);
		}
	}
	
	public void Hnormalize(){
		double totalFrequency = caltotalFre()*2;
		for(Map.Entry<String, DicNode> rootEntry : this.rootMap.entrySet()){
			rootEntry.getValue().normalize(totalFrequency);
		}
	}
	
	public void normalizeCachedDci(){
		double totalFrequency = calCachedTotalFre();
		for(Map.Entry<String, DicNode> rootEntry : this.rooMapCache.entrySet()){
			rootEntry.getValue().normalize(totalFrequency);
		}
	}
	
	public void HnormalizeCachedDci(){
		double totalFrequency = calCachedTotalFre()*2;
		for(Map.Entry<String, DicNode> rootEntry : this.rooMapCache.entrySet()){
			rootEntry.getValue().normalize(totalFrequency);
		}
	}
	
	public void outputDic(String pathname){
		try {
			File file = new File(pathname);
			if(!file.exists()){
				file.createNewFile();
			}
			FileWriter fileWriter = new FileWriter(file);
			BufferedWriter bw = new BufferedWriter(fileWriter);
			String dicInfor;
			for(Map.Entry<String, DicNode> dicEntry : this.rootMap.entrySet()){
				dicInfor = dicEntry.getValue().getDicContent("");
				bw.write(dicInfor);
			}
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	public void outputCachedDic(String path){
		try {
			File file = new File(path);
			if(!file.exists()){
				file.createNewFile();
			}
			FileWriter fileWriter = new FileWriter(file);
			BufferedWriter bw = new BufferedWriter(fileWriter);
			String dicInfor;
			for(Map.Entry<String, DicNode> dicEntry : this.rooMapCache.entrySet()){
				dicInfor = dicEntry.getValue().getDicContent("");
				bw.write(dicInfor);
			}
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public double calFrequencyLogSum(){
		double result = 0;
		for(Map.Entry<String, DicNode> rootEntry : this.rooMapCache.entrySet()){
			result = result+rootEntry.getValue().calFreuencyLogSum();
		}
		return result;
	}
	
	public void outputCachedMaxAndMinFre(){
		double max = Double.MIN_VALUE;
		double min = Double.MAX_VALUE;
		for(Map.Entry<String, DicNode> rootEntry : this.rooMapCache.entrySet()){
			double maxTemp = rootEntry.getValue().getMaxFre();
			double minTemp = rootEntry.getValue().getMinFre();
			if(max<maxTemp){
				max = maxTemp;
			}
			if(minTemp<min){
				min = minTemp;
			}
		}
		System.out.println("maximum frequency for chached dictionary is " + max +" ninimum frequency for cachaed dictionary is "+min);
	}
	
	public void outputMaxAndMinFre(){
		double max = Double.MIN_VALUE;
		double min = Double.MAX_VALUE;
		for(Map.Entry<String, DicNode> rootEntry : this.rootMap.entrySet()){
			double maxTemp = rootEntry.getValue().getMaxFre();
			double minTemp = rootEntry.getValue().getMinFre();
			if(max<maxTemp){
				max = maxTemp;
			}
			if(minTemp<min){
				min = minTemp;
			}
		}
		System.out.println("maximum frequency for dictionary is " + max +" ninimum frequency fordictionary is "+min);
	}
	
	public DicNode getMostFrequentWord(){
		DicNode result = null;
		for(Map.Entry<String, DicNode> rootEntry : this.rootMap.entrySet()){
			DicNode childNode = rootEntry.getValue();
			DicNode childMostFreNode = childNode.getMostFrequentNode();
			if(result == null){
				result = childMostFreNode;
			}else{
				if(childMostFreNode.getFrequency()>result.getFrequency()){
					result = childMostFreNode;
				}
			}
		}
		return result;
	}
	
	public DicNode getListFrequentWord(){
		DicNode result = null;
		for(Map.Entry<String, DicNode> rootEntry : this.rootMap.entrySet()){
			DicNode childNode = rootEntry.getValue();
			DicNode childListFreNode = childNode.getMostFrequentNode();
			if(result == null){
				result = childListFreNode;
			}else{
				if(childListFreNode.getFrequency()<result.getFrequency()){
					result = childListFreNode;
				}
			}
		}
		return result;
	}
	
	public DicNode removeMostFreWord(){
		DicNode mostFreNode = this.getMostFrequentWord();
		String word = mostFreNode.getCurrentChar();
		String rootChar = word.substring(0,1);
		DicNode rootNode = rootMap.get(rootChar);
		rootNode.removeWord(word.substring(1));
		DicNode cachedRootNode = rooMapCache.get(rootChar);
		cachedRootNode.removeWord(word.substring(1));
		return mostFreNode;
	}
	
	public DicNode removeListFreWord(){
		DicNode listFreNode = this.getListFrequentWord();
		String word = listFreNode.getCurrentChar();
		String rootChar = word.substring(0,1);
		DicNode rootNode = rootMap.get(rootChar);
		rootNode.removeWord(word.substring(1));
		DicNode cachedRootNode = rooMapCache.get(rootChar);
		cachedRootNode.removeWord(word.substring(1));
		return listFreNode;
	}
	
}
