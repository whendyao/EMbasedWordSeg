package seg.algorithm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import entity.CDictionary;
import entity.DicNode;
import entity.Dictionary;
import entity.SegPoint;

public class EMAndDicDivAl {
	CDictionary dictionary;
	private CDictionary CoreDictionary;
	List<String[]> traincontenList;
	int maxWordLength;
	private List<String[]> validationList;
	

	public List<String[]> getValidationList() {
		return validationList;
	}

	public void setValidationList(List<String[]> validationList) {
		this.validationList = validationList;
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
		this.CoreDictionary = new CDictionary();
		for(String[] setenceArray : traincontenList){
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
							
						}else{
							break;
						}
						j++;
					}
					i=i+1;
				}
			}
		}
		dictionary.normalize();
		dictionary.outputMaxAndMinFre();
//		System.out.println(dictionary.caltotalFre());
//		this.dictionary.outputDic("E:\\testData\\DicTest\\dic.txt");
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
	
	public void EMIteration(boolean coreInclue, double minChange){
		double previosTargetValue = -Double.MAX_VALUE;
		double currentTargetValue = -Double.MAX_VALUE/2;
		while((currentTargetValue-previosTargetValue)>=minChange){
			paramAdjust(this.traincontenList, coreInclue);
			previosTargetValue = currentTargetValue;
			currentTargetValue = calTargetValue(this.traincontenList);
			if(coreInclue){
				this.CoreDictionary.upDateDictionary();
			}
			this.dictionary.upDateDictionary();
		}
	}
	
	public void NgramDicConstruct(){
		int iterationNum = crossValidation();
		for(int i=0;i<iterationNum;i++){
			paramAdjust(this.traincontenList, false);
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
				paramAdjust(trainList, false);
				previosTargetValue = currentTargetValue;
				currentTargetValue = calTargetValue(validationList);
				interationNum = interationNum+1;
			}
			averageIterationNum = averageIterationNum + interationNum;
			
		}
		return averageIterationNum/10;
	}
	
	private void paramAdjust(List<String[]> trainData, boolean coreInclude) {
		for(String[] sentenceArray : trainData){
			for(String sentence : sentenceArray){
				List<String> atomList = this.atomseg(sentence);
				List<String[]> ambigiousPartList = this.generateAmbigiousPart(atomList);
				for(String[] ambisious : ambigiousPartList){
					updateFrequency(ambisious);
				}
			}
		}
		if(coreInclude){
			double totalFrequency = this.dictionary.calcachedtotalFre()+this.CoreDictionary.calcachedtotalFre();
			this.dictionary.normalizeCachedDci(totalFrequency);
			this.CoreDictionary.normalizeCachedDci(totalFrequency);
			System.out.println("core dictionary ");
//			this.CoreDictionary.outputCachedMaxAndMinFre();
//			this.CoreDictionary.outputCachedDic("E:\\testData\\DicDebuge\\coredic_chached.txt");
//			System.out.println("second dictionary ");
//			this.dictionary.outputCachedMaxAndMinFre();
//			this.dictionary.outputCachedDic("E:\\testData\\DicDebuge\\dic_chached.txt");
		}else{
			this.dictionary.normalizeCachedDci();
//			System.out.println("second dictionary ");
//			this.dictionary.outputCachedMaxAndMinFre();
//			this.dictionary.outputCachedDic("E:\\testData\\DicDebuge\\dic_chached.txt");
		}
		
		
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
					frequency = this.CoreDictionary.getWordFrequency(Ngram);
					if(frequency<0){
						frequency = this.dictionary.getWordFrequency(Ngram);
					}
				}else{
					frequency = this.CoreDictionary.getCachedFrequancy(Ngram);
					if(frequency<0){
						frequency = this.dictionary.getCachedFrequancy(Ngram);
					}
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
				double frequency = this.CoreDictionary.getWordFrequency(Ngram);
				if(frequency<0){
					frequency = this.dictionary.getWordFrequency(Ngram);
				}
				beta = beta + betas[i+forwardStep+1]*frequency;
				forwardStep = forwardStep+1;
			}
			betas[i] = beta;
		}
		return betas;
	}
	
	public void updateFrequency(String[]  sentence){
		double[] alphas = calAlphaI(sentence, true);
		double[] betas = calbetas(sentence);
		Map<String, Double> CoreUpdate = new HashMap<String, Double>();
		Map<String, Double> dicUpdate = new HashMap<String, Double>();
		double totalFrequency = 0;
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
				double originalFre = this.CoreDictionary.getWordFrequency(Ngram);
				double frequency;
				if(originalFre>=0){
					frequency = alphas[i]*betas[j+1]*originalFre/alphas[sentence.length];
//					CoreUpdate.put(Ngram, frequency);
					this.CoreDictionary.addWordTocache(Ngram, frequency);
				}else{
					originalFre = this.dictionary.getWordFrequency(Ngram);
					frequency = alphas[i]*betas[j+1]*originalFre/alphas[sentence.length];
					this.dictionary.addWordTocache(Ngram, frequency);
//					dicUpdate.put(Ngram, frequency);
				}
				totalFrequency = totalFrequency +frequency;
				j = j+1;
			}
		}
//		for(Map.Entry<String, Double> wordEntry : CoreUpdate.entrySet()){
//			String word = wordEntry.getKey();
//			double frequency = wordEntry.getValue()/totalFrequency;
//			this.CoreDictionary.setWordFrequency(word, frequency);
//		}
//		for(Map.Entry<String, Double> wordEntry : dicUpdate.entrySet()){
//			String word = wordEntry.getKey();
//			double frequency = wordEntry.getValue()/totalFrequency;
//			this.dictionary.setWordFrequency(word, frequency);
//		}
		
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
	
	public List<String> ViterbiBasedWordSeg(String[] ambigiousSeg,Map<String, Double> userDic){
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
				double wordFre;
				if(userDic==null){
					wordFre = this.CoreDictionary.getWordFrequency(nGram);
					if(wordFre<0){
						wordFre = this.dictionary.getWordFrequency(nGram);
					}
					if(wordFre<0){
						wordFre = 0;
					}
				}else{
					wordFre = userDic.get(nGram)==null?0:userDic.get(nGram);
				}
				double SegProb = dynamicInfor.get(i-j).getSegProb()*wordFre;
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
	
	public List<String>  sentenceProcess(String sentence,Map<String, Double> userDic){
		List<String> result = new ArrayList<String>();
		List<String> atomList = atomseg(sentence);
		List<String[]> ambigiousSegList = generateAmbigiousPart(atomList);
		for(String[] ambigious : ambigiousSegList){
			List<String> segList = ViterbiBasedWordSeg(ambigious, userDic);
			result.addAll(segList);
		}
		return result;
	}
	
	public void forwarProcess(int n){
		for(int i=0;i<n;i++){
			DicNode wordNode = this.dictionary.removeMostFreWord();
			this.CoreDictionary.addWord(wordNode.getCurrentChar(), wordNode.getFrequency());
		}
		this.CoreDictionary.Hnormalize();
		this.dictionary.Hnormalize();
	}
	
	public void backWordProcess(int n){
		for(int i=0;i<n;i++){
			DicNode wordNode = this.CoreDictionary.removeLeastFreWord();
			if(wordNode.getCurrentChar()!=null){
				this.dictionary.addWord(wordNode.getCurrentChar(), wordNode.getFrequency());
			}
		}
		this.CoreDictionary.Hnormalize();
		this.dictionary.Hnormalize();
	}
	
	public Map<String, Double> DictionaryPuring(double gama1, double gama2){
		Map<String, Double> puredDic = new HashMap<String, Double>();
		Map<String, Double> coreMap = this.CoreDictionary.getDicMap();
		Map<String, Double> dicMap = this.dictionary.getDicMap();
		double maxMi = Double.MIN_VALUE;
		double minMi = Double.MAX_VALUE;
		for(Map.Entry<String, Double> wordEntry : coreMap.entrySet()){
			String word = wordEntry.getKey();
			double frequency = wordEntry.getValue();
			if(word.length()<=2){
				puredDic.put(word, frequency);
			}
		}
		for(Map.Entry<String, Double> wordEntry : dicMap.entrySet()){
			String word = wordEntry.getKey();
			double frequency = wordEntry.getValue();
			if(word.length()<=2){
				 puredDic.put(word, frequency);
			}
		}
		for(int i=3;i<=this.maxWordLength;i++){
			for(Map.Entry<String, Double> wordEntry : coreMap.entrySet()){
				String word = wordEntry.getKey();
				double frequency = wordEntry.getValue();
				if(word.length()==i){
					double Mi = wordSplit(gama1, gama2, puredDic, word, frequency);
					if(Mi>maxMi){
						maxMi = Mi;
					}
					if(Mi<minMi){
						minMi = Mi;
					}
				}
			}
			for(Map.Entry<String, Double> wordEntry : dicMap.entrySet()){
				String word = wordEntry.getKey();
				double frequency = wordEntry.getValue();
				if(word.length()==i){
					double Mi = wordSplit(gama1, gama2, puredDic, word, frequency);
					if(Mi>maxMi){
						maxMi = Mi;
					}
					if(Mi<minMi){
						minMi = Mi;
					}
				}
			}
		}
//		System.out.println("minimum mutual information gian is "+minMi + " maximum information gian is +" +maxMi);
		return puredDic;
	}

	private double  wordSplit(double gama1, double gama2, Map<String, Double> puredDic, String word, double frequency) {
		String mostProbLeft = null;
		String mostProbRight = null;
		double mostProb = 0;
		double Mi = 0;
		for(int j=0;j<word.length();j++){
			String leftPart = word.substring(0, j);
			String rightPart = word.substring(j);
			double leftProb = puredDic.get(leftPart)==null?0:puredDic.get(leftPart);
			double rightProb = puredDic.get(rightPart)==null?0:puredDic.get(rightPart);
			double splitProb = leftProb*rightProb;
			if(splitProb>mostProb){
				mostProb = splitProb;
				mostProbLeft = leftPart;
				mostProbRight = rightPart;
			}
		}
		if(mostProb!=0 && frequency!=0){
			Mi = Math.log(frequency/mostProb);
			if(Mi>gama1){
				puredDic.put(word,frequency);
			}else{
				double leftProb = puredDic.get(mostProbLeft);
				double rightProb = puredDic.get(mostProbRight);
				if(Mi<gama2){
					leftProb = leftProb + frequency*leftProb/(leftProb+rightProb);
					rightProb= rightProb + frequency*rightProb/(leftProb+rightProb);
					puredDic.put(mostProbLeft,leftProb);
					puredDic.put(mostProbRight,rightProb);
				}else{
					leftProb = leftProb + frequency*leftProb/(leftProb+rightProb)*2/3;
					rightProb= rightProb + frequency*rightProb/(leftProb+rightProb)*2/3;
					frequency = frequency/3;
					puredDic.put(mostProbLeft,leftProb);
					puredDic.put(mostProbRight,rightProb);
					puredDic.put(word, frequency);
				}
			}
		}
		return Mi;
	}
	
	public Map<String, List<String>> paragraphProcess(List<String[]> sampleList, Map<String, Double> userDic){
		Map<String,List<String>> SegResultMap = new HashMap<String,List<String>>();
		for(String[] testSample : sampleList){
			List<String> segResultList = new ArrayList<String>();
			String key = testSample[0];
			for(int i=1;i<testSample.length;i++){
				List<String> segResult = this.sentenceProcess(testSample[i], userDic);
				segResultList.addAll(segResult);
			}
			SegResultMap.put(key, segResultList);
		}
		return SegResultMap;
	}
}


