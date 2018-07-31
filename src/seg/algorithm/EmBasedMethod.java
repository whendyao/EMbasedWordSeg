package seg.algorithm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import entity.CDictionary;
import entity.Dictionary;
import entity.SegPoint;

public class EmBasedMethod {
	CDictionary dictionary;
	List<String[]> traincontenList;
	int maxWordLength;
	public Map<String, Double> dfMap;
	public double dfThresh = 20;
	public CDictionary getDictionary() {
		return dictionary;
	}

	public void setDictionary(CDictionary dictionary) {
		this.dictionary = dictionary;
	}

	public List<String[]> getTraincontenList() {
		return traincontenList;
	}

	public void setTraincontenList(List<String[]> traincontenList) {
		this.traincontenList = traincontenList;
	}

	public int getMaxWordLength() {
		return maxWordLength;
	}

	public void setMaxWordLength(int maxWordLength) {
		this.maxWordLength = maxWordLength;
	}

	public void intializeDic(){
		Random rand = new Random();
		dictionary = new CDictionary();
		this.dfMap = new HashMap<String, Double>();
		for(String[] setenceArray : traincontenList){
			Set<String> wordSet = new HashSet<String>();
			for(String setence : setenceArray){
				List<String> atomList = this.atomseg(setence);
				int i=0;
				while(i<atomList.size()){
					int j=0;
					String NgramString="";
					while(j<this.maxWordLength &&(i+j)<atomList.size()){
						
						String atomUnit = atomList.get(i+j);
						if(atomUnit.matches(Dictionary.Num_regx)){
//							atomUnit = Dictionary.NUM_IND;
//							NgramString = NgramString+atomUnit;
//							dictionary.addWord(NgramString, 1);
							break;
						}else if(atomUnit.matches(Dictionary.EN_REGX)){
//							atomUnit= Dictionary.EN_IND;
//							NgramString = NgramString+atomUnit;
//							dictionary.addWord(NgramString, 1);
							break;
						}else if(atomUnit.matches(Dictionary.CHINESE_REGX)){
							NgramString = NgramString+atomUnit;
							if(dictionary.getWordFrequency(NgramString)<=0){
								dictionary.addWord(NgramString, rand.nextDouble());
							}
							if(!wordSet.contains(NgramString)){
								wordSet.add(NgramString);
								Double df = this.dfMap.get(NgramString);
								if(df==null){
									df = 0.0;
								}
								df++;
								this.dfMap.put(NgramString, df);
							}
							
						}else{
							break;
						}
						j++;
					}
					i=i+1;
				}
			}
		}
		this.dicPure();
		dictionary.normalize();
		dictionary.outputMaxAndMinFre();
//		System.out.println(dictionary.caltotalFre());
//		this.dictionary.outputDic("E:\\testData\\DicTest\\dic.txt");
	}
	
	public void dicPure(){
		for(Map.Entry<String, Double> dfEntry : this.dfMap.entrySet()){
			double df = dfEntry.getValue();
			String word = dfEntry.getKey();
			if(df<this.dfThresh){
				this.dictionary.deletWord(word);
			}
		}
	}
	
	public List<String> atomseg(String input){
		List<String> resultList = new ArrayList<>();
		int i = 0;
		while(i<input.length()){
			int j=i;
			String atomString = input.substring(i, j+1);
			if(atomString.matches(Dictionary.CHINESE_REGX)){
				resultList.add(atomString);
				i=j+1;
			}else{
				while(atomString.matches(Dictionary.Num_regx)){
					j++;
					if(j>=input.length()){
						j=input.length();
						break;
					}
					atomString = input.substring(i,j+1);
				}
				while(atomString.matches(Dictionary.EN_REGX)){
					j++;
					if(j>=input.length()){
						j=input.length();
						break;
					}
					atomString = input.substring(i,j+1);
				}
				if(i<j){
					resultList.add(input.substring(i,j));
					i=j;
				}else{
					resultList.add(input.substring(i,i+1));
					i=i+1;
				}
			}
		}
		return resultList;
	}
	
	public void loadTrainData(String folderPath){
		try {
			File trainFolder = new File(folderPath);
			File[] trainFileArray = trainFolder.listFiles();
			traincontenList = new ArrayList<String[]>();
			StringBuffer sb = new StringBuffer();
			for(File trainFile : trainFileArray){
				FileReader fr = new FileReader(trainFile);
				BufferedReader br = new BufferedReader(fr);
				sb.setLength(0);
				String readLine;
				while((readLine=br.readLine())!=null){
					readLine = readLine.replaceAll("\\d{8}-\\d{2}-\\d{3}-\\d{3}  ", ",");
					sb.append(readLine);
				}
				traincontenList.add(sb.toString().split("[,|.|¡£|£¬]"));
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void EMIteration(){
		double previosTargetValue = -Double.MAX_VALUE;
		double currentTargetValue = -Double.MAX_VALUE/2;
		while((currentTargetValue-previosTargetValue)>=100){
			paramAdjust(this.traincontenList);
//			System.out.println(currentTargetValue);
			previosTargetValue = currentTargetValue;
			currentTargetValue = calTargetValue(this.traincontenList);
			this.dictionary.upDateDictionary();
		}
	}
	
	public void NgramDicConstruct(){
		int iterationNum = crossValidation();
		for(int i=0;i<iterationNum;i++){
			paramAdjust(this.traincontenList);
			this.dictionary.upDateDictionary();
		}
	}
	
	public int crossValidation(){
		int validationSize = this.traincontenList.size()/10;
		int averageIterationNum = 0;
		for(int i=0;i<10;i++){
			List<String[]> trainList = new ArrayList<String[]>();
			List<String[]> validationList = new ArrayList<String[]>();
			int startIndex = i*validationSize;
			int endIndex = (i+1)*validationSize;
			for(int j=0;j<traincontenList.size();j++){
				if(j>=startIndex && j<endIndex){
					validationList.add(traincontenList.get(j));
				}else{
					trainList.add(traincontenList.get(j));
				}
			}
			double previosTargetValue = -Double.MAX_VALUE;
			double currentTargetValue = 0;
			int interationNum = 0;
			while(currentTargetValue>previosTargetValue){
				paramAdjust(trainList);
				previosTargetValue = currentTargetValue;
				currentTargetValue = calTargetValue(validationList);
				interationNum = interationNum+1;
			}
			averageIterationNum = averageIterationNum + interationNum;
			
		}
		return averageIterationNum/10;
	}
	
	private void paramAdjust(List<String[]> trainData) {
		for(String[] sentenceArray : trainData){
			for(String sentence : sentenceArray){
				List<String> atomList = this.atomseg(sentence);
				List<String[]> ambigiousPartList = this.generateAmbigiousPart(atomList);
				for(String[] ambisious : ambigiousPartList){
					updateFrequency(ambisious);
				}
			}
		}
		this.dictionary.normalizeCachedDci();
		this.dictionary.outputCachedMaxAndMinFre();
//		dictionary.outputDic("E:\\testData\\DicDebuge\\dic.txt");
		this.dictionary.outputCachedDic("E:\\testData\\DicDebuge\\dic_chached.txt");
	}
	
	public double[] calAlphaI(String[] sentence, boolean dicFlage){
		int len = sentence.length;
		double[] alphas = new double[len+1];
		alphas[0]=1;
		for(int i=0;i<len;i++){
			double alpha = 0;
			int backStep = 0;
			String Ngram = "";
			while(backStep<this.maxWordLength && backStep<=i){
				String newChar = sentence[i-backStep];
				if(newChar.matches(Dictionary.Num_regx)){
					newChar = Dictionary.NUM_IND;
				}else if(newChar.matches(Dictionary.EN_REGX)){
					newChar = Dictionary.EN_IND;
				}
				Ngram = newChar+Ngram ;
				double frequency = 0;
				if(dicFlage){
					frequency = this.dictionary.getWordFrequency(Ngram);
				}else{
					frequency = this.dictionary.getCachedFrequancy(Ngram);
				}
				alpha = alpha + alphas[i-backStep]*frequency;
				backStep =backStep +1;
			}
			alphas[i+1]=alpha;
		}
		return alphas;
	}
	
	public double[] calbetas(String[] sentence){
		int len = sentence.length;
		double[] betas = new double[len+1];
		betas[len] = 1;
		for(int i=len-1;i>=0;i--){
			double beta = 0;
			int forwardStep = 0;
			String Ngram = "";
			while(forwardStep<this.maxWordLength && (forwardStep+i)<len){
				String newChar = sentence[i+forwardStep];
				if(newChar.matches(Dictionary.Num_regx)){
					newChar = Dictionary.NUM_IND;
				}else if(newChar.matches(Dictionary.EN_REGX)){
					newChar = Dictionary.EN_IND;
				}
				Ngram = Ngram+newChar;
				beta = beta + betas[i+forwardStep+1]*this.dictionary.getWordFrequency(Ngram);
				forwardStep = forwardStep+1;
			}
			betas[i] = beta;
		}
		return betas;
	}
	
	public void updateFrequency(String[]  sentence){
		double[] alphas = calAlphaI(sentence, true);
		double[] betas = calbetas(sentence);
		for(int i=0;i<sentence.length;i++){
			int j=i;
			String Ngram = "";
			while((j-i)<this.maxWordLength && j<sentence.length){
				String newChar = sentence[j];
				if(newChar.matches(Dictionary.Num_regx)){
					newChar = Dictionary.NUM_IND;
				}else if(newChar.matches(Dictionary.EN_REGX)){
					newChar = Dictionary.EN_IND;
				}
				Ngram = Ngram + newChar;
				double frequency = alphas[i]*betas[j+1]*this.dictionary.getWordFrequency(Ngram)/alphas[sentence.length];
				this.dictionary.addWordTocache(Ngram, frequency);
				j = j+1;
			}
		}
		
	}
	
	
	
	public List<String[]> generateAmbigiousPart(List<String> atomList){
		List<String[]> result = new ArrayList<>();
		int i=0;
		while(i<atomList.size()){
			int j=i;
			while(j<atomList.size()){
				String atom = atomList.get(j);
				if(atom.matches(Dictionary.CHINESE_REGX)){
					j=j+1;
				}else{
					break;
				}
			}
			if (j>i) {
				int ambigiousLength = j - i;
				String[] ambigiousArray = new String[ambigiousLength];
				for (int counter = 0; counter < ambigiousLength; counter++) {
					ambigiousArray[counter] = atomList.get(i + counter);
				}
				result.add(ambigiousArray);
				i=j;
			}else{
				i=i+1;
			}
			
		}
		return result;
	}
	
	public double calTargetValue(List<String[]> validation){
		double result = 0;
		double totalProb = 0;
		for(String[] sentenceArray : validation){
			for(String sentence : sentenceArray){
				List<String> atomList = this.atomseg(sentence);
				List<String[]> ambigiousPartList = this.generateAmbigiousPart(atomList);;
				for(String[] ambisious : ambigiousPartList){
					double currentQ = calAlphaI(ambisious, true)[ambisious.length];
					double nextQ = calAlphaI(ambisious, false)[ambisious.length];
					if(currentQ<=0 || nextQ<=0){
						System.out.println("unexpected calc current Q = "+currentQ +" next Q = "+nextQ);
						nextQ = 1;
					}
					result = result + Math.log(nextQ);
					totalProb = totalProb + Math.log(nextQ);
				}
			}
		}
		System.out.println("observed probability is "+totalProb);
		return result;
	}
	
	public List<String> ViterbiBasedWordSeg(String[] ambigiousSeg){
		int len = ambigiousSeg.length;
		List<String> resultList = new ArrayList<>();
		Map<Integer, SegPoint> dynamicInfor = new HashMap<Integer, SegPoint>();
		List<Integer> segIndexlist = new ArrayList<>();
		SegPoint startPoint = new SegPoint(-1,0,1);
		dynamicInfor.put(0, startPoint);
		for(int i=1;i<=len;i++){
			int j=1;
			String nGram = "";
			double maxSegProb = Double.MIN_VALUE;
			int bestSegStartIndex = i-1;
			while(j<=this.maxWordLength && j<=i){
				nGram =  ambigiousSeg[i-j]+nGram;
				double SegProb = dynamicInfor.get(i-j).getSegProb()*this.dictionary.getWordFrequency(nGram);
				if(SegProb>maxSegProb){
					maxSegProb = SegProb;
					bestSegStartIndex = i-j;
				}
				j+=1;
			}
			SegPoint segPoint = new SegPoint(bestSegStartIndex, i, maxSegProb);
			dynamicInfor.put(i, segPoint);
		}
		int index = len;
		while(index>=0){
			segIndexlist.add(index);
			index = dynamicInfor.get(index).getSegStartIndex();
		}
		int counter = 0;
		for(int i=segIndexlist.size()-2;i>=0;i--){
			String word ="";
			while(counter<segIndexlist.get(i)){
				word = word + ambigiousSeg[counter];
				counter ++;
			}
			resultList.add(word);
		}
		return resultList;
	}
	
	public List<String>  sentenceProcess(String sentence){
		List<String> result = new ArrayList<String>();
		List<String> atomList = atomseg(sentence);
		List<String[]> ambigiousSegList = generateAmbigiousPart(atomList);
		for(String[] ambigious : ambigiousSegList){
			List<String> segList = ViterbiBasedWordSeg(ambigious);
			result.addAll(segList);
		}
		return result;
	}
	
}


