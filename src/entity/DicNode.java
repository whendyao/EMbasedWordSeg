package entity;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class DicNode {
	String currentChar;
	CharType wordType;
	double frequency;
	Map<String, DicNode> nextCharMap;
	public DicNode(){
		nextCharMap = new HashMap<String, DicNode>();
	}
	
	public DicNode(String nodeChar){
		nextCharMap = new HashMap<String, DicNode>();
		if(nodeChar.equals(Dictionary.NUM_IND)){
			this.wordType = CharType.NUM;
		}else if(nodeChar.equals(Dictionary.EN_IND)){
			this.wordType = CharType.EN;
		}else{
			this.wordType = CharType.CH;
		}
	}
	
	
	public String getCurrentChar() {
		return currentChar;
	}
	public void setCurrentChar(String currentChar) {
		this.currentChar = currentChar;
	}
	public CharType getWordType() {
		return wordType;
	}
	public void setWordType(CharType wordType) {
		this.wordType = wordType;
	}
	
	public Map<String, DicNode> getNextCharList() {
		return nextCharMap;
	}
	public void setNextCharList(Map<String, DicNode> nextCharList) {
		this.nextCharMap = nextCharList;
	}
	
	public double getFrequency() {
		return frequency;
	}
	public void setFrequency(double frequency) {
		this.frequency = frequency;
	}
	
	public void resetFrequency(){
		this.frequency =0;
		for(Map.Entry<String, DicNode> nextEntry : this.nextCharMap.entrySet()){
			DicNode nextNode = nextEntry.getValue();
			nextNode.resetFrequency();
		}
	}
	public double getItermfrequency(String Iterm){
		if(Iterm==null || Iterm.equals("")){
			return frequency;
		}else{
			String nextChar = Iterm.substring(0, 1);
			DicNode nextNode= nextCharMap.get(nextChar);
			if(nextNode==null){
				return -1;
			}else{
				return nextNode.getItermfrequency(Iterm.substring(1));
			}
		}
	}
	
	public void addIterm(String iterm, double frequency){
		if(iterm!=null && !iterm.equals("")){
			String nextChar = iterm.substring(0, 1);
			DicNode nextNode = this.nextCharMap.get(nextChar);
			if(nextNode ==null){
				nextNode = new DicNode();
				nextNode.setCurrentChar(nextChar);
				if(nextChar.equals(Dictionary.NUM_IND)){
					nextNode.setWordType(CharType.NUM);
				}else if(nextChar.equals(Dictionary.EN_IND)){
					nextNode.setWordType(CharType.EN);
				}else{
					nextNode.setWordType(CharType.CH);
				}
				nextCharMap.put(nextChar, nextNode);
			}
			nextNode.addIterm(iterm.substring(1), frequency);
		}else{
			if(this.frequency<0){
				this.frequency = 0;
			}
			this.frequency = this.frequency+frequency;
		}
	}
	
	public double calTotalFrequency(){
		double result = this.frequency>=0? this.frequency : 0;
		for(Map.Entry<String, DicNode> childEntry : this.nextCharMap.entrySet()){
			result =result + childEntry.getValue().calTotalFrequency();
		}
		return result;
	}
	
	public void normalize(double totalFrequncy){
		this.frequency = this.frequency>=0?this.frequency/totalFrequncy:this.frequency;
		for(Map.Entry<String, DicNode> childEntry : this.nextCharMap.entrySet()){
			childEntry.getValue().normalize(totalFrequncy);
		}
	}
	
	
	public String getDicContent(String parentContent){
		StringBuffer contentBuffer = new StringBuffer();
		String currentAndParentContent = parentContent+this.currentChar;
		if(this.nextCharMap!=null && this.nextCharMap.size()>0){
			contentBuffer.append(currentAndParentContent+"    "+this.frequency+"\r\n");
			for(Map.Entry<String, DicNode> nextEntry : this.nextCharMap.entrySet()){
				DicNode nextNode = nextEntry.getValue();
				contentBuffer.append(nextNode.getDicContent(currentAndParentContent));
			}
			return contentBuffer.toString();
		}else{
			return currentAndParentContent+"    "+this.frequency+"\r\n";
		}
	}
	
	public double calFreuencyLogSum(){
		double result = Math.log(this.frequency);
		for(Map.Entry<String, DicNode> childEntry : this.nextCharMap.entrySet()){
			result =result + childEntry.getValue().calTotalFrequency();
		}
		return result;
	}
	
	public double getMaxFre(){
		double max = this.frequency;
		for(Map.Entry<String, DicNode> childEntry : this.nextCharMap.entrySet()){
			double maxforChild = childEntry.getValue().getMaxFre();
			if(maxforChild>max){
				max = maxforChild;
			}
		}
		return max;
	}
	
	
	public double getMinFre(){
		double min = this.frequency>=0?this.frequency:0;
		for(Map.Entry<String, DicNode> childEntry : this.nextCharMap.entrySet()){
			double minForChild = childEntry.getValue().getMinFre();
			if(minForChild<min && minForChild>=0){
				min = minForChild;
			}
		}
		return min;
	}
	
	public DicNode getMostFrequentNode(){
		DicNode result = new DicNode();
		result.setFrequency(this.frequency);
		result.setCurrentChar(this.currentChar);
		for(Map.Entry<String, DicNode> childEntry : this.nextCharMap.entrySet()){
			DicNode mostFreNode  = childEntry.getValue().getMostFrequentNode();
			if(result.getFrequency()<mostFreNode.getFrequency()){
				String word = this.currentChar + mostFreNode.getCurrentChar();
				result.setCurrentChar(word);
				result.setFrequency(mostFreNode.getFrequency());
			}
		}
		return result;
	}
	
	public DicNode getListFrequentNode(){
		DicNode result = new DicNode();
		result.setFrequency(this.frequency);
		result.setCurrentChar(this.currentChar);
		for(Map.Entry<String, DicNode> childEntry : this.nextCharMap.entrySet()){
			DicNode listFreNode  = childEntry.getValue().getListFrequentNode();
			double leastFre = listFreNode.getFrequency();
			if(result.getFrequency() <0){
				String word = this.currentChar + listFreNode.getCurrentChar();
				result.setCurrentChar(word);
				result.setFrequency(listFreNode.getFrequency());
			}else if(result.getFrequency()>leastFre && leastFre>=0){
				String word = this.currentChar + listFreNode.getCurrentChar();
				result.setCurrentChar(word);
				result.setFrequency(listFreNode.getFrequency());
			}
		}
		return result;
	}
	
	public void removeWord(String FollowingChar){
		if(FollowingChar==null || FollowingChar.length()==0){
			this.frequency = -1;
		}else{
			String nextChar = FollowingChar.substring(0, 1);
			DicNode nextNode= nextCharMap.get(nextChar);
			if(nextNode==null){
				System.out.println("the reomoved word did not exist" + this.currentChar+FollowingChar);
			}else{
				this.removeWord(FollowingChar.substring(1));
			}
		}
	}
	
}
