package entity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;

public class CDictionary {
	private Map<String, Double> dicMap;
	private Map<String, Double> cachedDicMap;
	
	public CDictionary()
	{
		this.dicMap = new HashMap<String, Double>();
		this.cachedDicMap = new HashMap<String, Double>();
	}
	
	public Map<String, Double> getDicMap() {
		return dicMap;
	}




	public void setDicMap(Map<String, Double> dicMap) {
		this.dicMap = dicMap;
	}




	public Map<String, Double> getCachedDicMap() {
		return cachedDicMap;
	}




	public void setCachedDicMap(Map<String, Double> cachedDicMap) {
		this.cachedDicMap = cachedDicMap;
	}




	public double getWordFrequency(String ngramString) {
		Double fre = this.dicMap.get(ngramString);
		if(fre==null){
			return -1;
		}else{
			return fre;
		}
	}
	public void addWord(String ngramString, double frequent) {
		Double fre = this.dicMap.get(ngramString);
		if(fre==null){
			fre = 0.0;
		}
		fre = fre+frequent;
		this.dicMap.put(ngramString, fre);
	}
	public void deletWord(String ngramWord){
		this.dicMap.remove(ngramWord);
	}
	public double caltotalFre(){
		double totalfre = 0;
		for(Map.Entry<String, Double> wordEntry : this.dicMap.entrySet()){
			totalfre = totalfre + wordEntry.getValue();
		}
		return totalfre;
	}
	public void normalize() {
		double totalFre = caltotalFre();
		for(Map.Entry<String, Double> wordEntry : this.dicMap.entrySet()){
			double wordFre = wordEntry.getValue()/totalFre;
			String word = wordEntry.getKey();
			this.dicMap.put(word, wordFre);
		}
		
	}
	public void Hnormalize() {
		double totalFre = caltotalFre()*2;
		for(Map.Entry<String, Double> wordEntry : this.dicMap.entrySet()){
			double wordFre = wordEntry.getValue()/totalFre;
			String word = wordEntry.getKey();
			this.dicMap.put(word, wordFre);
		}
		
	}
	public void outputMaxAndMinFre() {
		double maxFre = Double.MIN_VALUE;
		double MinFre = Double.MAX_VALUE;
		for(Map.Entry<String, Double> wordEntry : this.dicMap.entrySet()){
			double wordFre = wordEntry.getValue();
			if(wordFre>maxFre){
				maxFre = wordFre;
			}
			if(wordFre<MinFre){
				MinFre = wordFre;
			}
		}
		System.out.println("maximum frequency of the dictionary is "+maxFre+" minimum frequency is "+MinFre);
	}
	public void addWordTocache(String ngram, double frequency) {
		Double fre = this.cachedDicMap.get(ngram);
		if(fre==null){
			fre = 0.0;
		}
		fre = fre+frequency;
		this.cachedDicMap.put(ngram, fre);
		
	}
	public double getCachedFrequancy(String ngram) {
		Double fre = this.cachedDicMap.get(ngram);
		if(fre==null){
			return -1;
		}else{
			return fre;
		}
	}
	
	public double calcachedtotalFre(){
		double totalfre = 0;
		for(Map.Entry<String, Double> wordEntry : this.cachedDicMap.entrySet()){
			totalfre = totalfre + wordEntry.getValue();
		}
		return totalfre;
	}
	public void normalizeCachedDci() {
		double totalFre = calcachedtotalFre();
		for(Map.Entry<String, Double> wordEntry : this.cachedDicMap.entrySet()){
			double wordFre = wordEntry.getValue()/totalFre;
			String word = wordEntry.getKey();
			this.cachedDicMap.put(word, wordFre);
		}
	}
	public void normalizeCachedDci(double totalFre) {
		for(Map.Entry<String, Double> wordEntry : this.cachedDicMap.entrySet()){
			double wordFre = wordEntry.getValue()/totalFre;
			String word = wordEntry.getKey();
			this.cachedDicMap.put(word, wordFre);
		}
		
	}
	public void outputCachedMaxAndMinFre() {
		double maxFre = Double.MIN_VALUE;
		double MinFre = Double.MAX_VALUE;
		for(Map.Entry<String, Double> wordEntry : this.cachedDicMap.entrySet()){
			double wordFre = wordEntry.getValue();
			if(wordFre>maxFre){
				maxFre = wordFre;
			}
			if(wordFre<MinFre){
				MinFre = wordFre;
			}
		}
		System.out.println("maximum frequency of the dictionary is "+maxFre+" minimum frequency is "+MinFre);
		
	}
	public void outputCachedDic(String path) {
		
		try {
			File file = new File(path);
			if(!file.exists())
				file.createNewFile();
			FileWriter fw = new FileWriter(file);
			BufferedWriter bw = new BufferedWriter(fw);
			for(Map.Entry<String, Double> wordEntry : this.cachedDicMap.entrySet()){
				fw.write(wordEntry.getKey()+"   "+wordEntry.getValue()+"\r\n");
			}
			fw.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public void upDateDictionary() {
		Map<String, Double> temp = this.dicMap;
		this.dicMap = this.cachedDicMap;
		this.cachedDicMap = temp;
		this.cachedDicMap.clear();
	}
	public DicNode removeMostFreWord() {
		double maxFre = -1;
		String mostFreWord=null;
		for(Map.Entry<String, Double> wordEntry : this.dicMap.entrySet()){
			double wordFre = wordEntry.getValue();
			if(wordFre>maxFre){
				maxFre = wordFre;
				mostFreWord = wordEntry.getKey();
			}
		}
		this.dicMap.remove(mostFreWord);
		DicNode  node = new DicNode();
		node.setCurrentChar(mostFreWord);
		node.setFrequency(maxFre);
		return node;
	}
	public DicNode removeLeastFreWord() {
		double minFre = Double.MAX_VALUE;
		String leastFreWord=null;
		for(Map.Entry<String, Double> wordEntry : this.dicMap.entrySet()){
			double wordFre = wordEntry.getValue();
			if(wordFre<minFre){
				minFre = wordFre;
				leastFreWord = wordEntry.getKey();
			}
		}
		this.dicMap.remove(leastFreWord);
		DicNode  node = new DicNode();
		node.setCurrentChar(leastFreWord);
		node.setFrequency(minFre);
		return node;
	}
	
	public void setWordFrequency(String word, double frequency){
		this.dicMap.put(word, frequency);
	}

}
