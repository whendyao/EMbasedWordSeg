package evaluation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import entity.Dictionary;
import seg.algorithm.EMAndDicDivAl;
import seg.algorithm.EmBasedMethod;

public class CrossValidation {
	public static Map<String, List<String>> expectedSeg = null;
	
	
	public static void EmBasedSegTest(String testPath){
		
		try {
			List<String[]> sampleList = new ArrayList<String[]>();
			List<String[]> testList = new ArrayList<String[]>();
			File file = new File(testPath);
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			String readLine = null;
			while((readLine=br.readLine())!=null){
				if (!readLine.equals("")) {
					String[] sentenceArray = readLine.split("[,|.|¡£|£¬]");
					sampleList.add(sentenceArray);
				}
			}
			Random rand = new Random();
			int testSize = sampleList.size()/10;
			for(int i=0;i<testSize;i++){
				int sampleIndex = rand.nextInt(sampleList.size());
				String[] sampleSelected = sampleList.remove(sampleIndex);
				testList.add(sampleSelected);
			}
			
			EMAndDicDivAl emSeger = new EMAndDicDivAl();
			emSeger.setTraincontenList(sampleList);
			emSeger.setMaxWordLength(3);
			emSeger.intializeDic();
			emSeger.EMIteration(false, 1000);
			Map<String, List<String>> SegResultMap = emSeger.paragraphProcess(sampleList, null);
			double FMeasure = FMeasureCal(SegResultMap);
			System.out.println("result Fmeasure is "+FMeasure);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public static void LoadSegInfor(String corpusPath) {
		expectedSeg = new HashMap<String, List<String>>();
		try {
			File file = new File(corpusPath);
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			String readLine;
			while((readLine=br.readLine()) != null){
				if (!readLine.equals("")) {
					String[] segs = readLine.split("/\\w+");
					List<String> segList = new ArrayList<String>();
					if(expectedSeg.containsKey(segs[0])){
						System.out.println("the row with key "+ segs[0] + "is repeated");
					}
					for (String seg : segs) {
						seg = seg.replaceAll(Dictionary.NO_CHINES_REGX, "");
						if(!"".equals(seg))
							segList.add(seg);
					}
					expectedSeg.put(segs[0], segList);
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static double FMeasureCal(Map<String, List<String>> segResult) {
		double recall;
		double precious;
		try {
			File resultFile = new File("E:\\testData\\DicDebuge\\result.txt");
			FileWriter resultfr = new FileWriter(resultFile);
			BufferedWriter resultBR = new BufferedWriter(resultfr);
			double expectedSegNum = 0;
			double actualSegNum = 0;
			double rightSegNum = 0;
			for(Map.Entry<String, List<String>> entry : segResult.entrySet()){
				String key = entry.getKey();
				List<String> actualSegList = entry.getValue();
				List<String> expectedSegList = expectedSeg.get(key);
				int index1 = 0;
				int index2 = 0;
				int ziCount1 = 0;
				int ziCount2 = 0;
				StringBuffer diffString1 = new StringBuffer();
				StringBuffer diffString2 = new StringBuffer();
				while(index1<actualSegList.size() && index2<expectedSegList.size()){

					String seg1 = actualSegList.get(index1);
					String seg2 = expectedSegList.get(index2);
					if(seg1.equals(seg2)){
						ziCount1 = ziCount1+seg1.length();
						ziCount2 = ziCount2 + seg2.length();
						index1 = index1+1;
						index2 = index2+1;
						expectedSegNum = expectedSegNum+1;
						actualSegNum = actualSegNum+1;
						rightSegNum = rightSegNum+1;
						if(diffString1.toString().length()>0 && diffString2.length()>0)
							resultBR.write(diffString1+"---"+diffString2+"\r\n");
						diffString1.setLength(0);
						diffString2.setLength(0);
					}else{
						if(ziCount1>ziCount2){
							diffString2.append(seg2+" ");
							ziCount2 = ziCount2 + seg2.length();
							index2 = index2+1;
							expectedSegNum = expectedSegNum+1;
						}else if(ziCount1<ziCount2){
							diffString1.append(seg1+" ");
							ziCount1 = ziCount1 + seg1.length();
							index1 = index1+1;
							actualSegNum = actualSegNum+1;
						}else{
							diffString2.append(seg2+" ");
							diffString1.append(seg1+" ");
							ziCount1 = ziCount1+seg1.length();
							ziCount2 = ziCount2 + seg2.length();
							index1 = index1+1;
							index2 = index2+1;
							expectedSegNum = expectedSegNum+1;
							actualSegNum = actualSegNum+1;
						}
					}
				}
				
				while(index1<actualSegList.size()){
					diffString1.append(actualSegList.get(index1));
					index1 = index1+1;
					actualSegNum = actualSegNum+1;
				}
				while(index2<expectedSegList.size()){
					diffString2.append(expectedSegList.get(index2));
					index2 = index2+1;
					expectedSegNum = expectedSegNum+1;
				}
				if(diffString1.toString().length()>0 && diffString2.length()>0)
					resultBR.write(diffString1+"---"+diffString2+"\r\n");
			}
			recall = rightSegNum/expectedSegNum;
			precious = rightSegNum/actualSegNum;
//			System.out.println("in final test recall is "+recall+" precious " + precious);
			resultBR.close();
			return (2*recall*precious)/(recall+precious);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return -1;
	}
	
	
	
	
	
	
	public static void EMandDicDivTest(String filepath) throws Exception{
		List<String[]> sampleList = new ArrayList<String[]>();
		List<String[]> testList = new ArrayList<String[]>();
		File file = new File(filepath);
		FileReader fr = new FileReader(file);
		BufferedReader br = new BufferedReader(fr);
		String readLine = null;
		while((readLine=br.readLine())!=null){
			if (!readLine.equals("")) {
				String[] sentenceArray = readLine.split("[,|.|¡£|£¬]");
				sampleList.add(sentenceArray);
			}
		}
		Random rand = new Random();
		int testSize = sampleList.size()/10;
		for(int i=0;i<testSize;i++){
			int sampleIndex = rand.nextInt(sampleList.size());
			String[] sampleSelected = sampleList.remove(sampleIndex);
			testList.add(sampleSelected);
		}
		EMAndDicDivAl seg = segConstruct(sampleList);
		Map<String, List<String>> segResult = seg.paragraphProcess(testList, null);
		double fmeasure = FMeasureCal(segResult);
		System.out.println("final f measure of the algorithm is " +fmeasure);
	}
	
	public static EMAndDicDivAl segConstruct(List<String[]> sampleList){
		int wordShiftNum = 0;
		List<String[]> testList = new ArrayList<String[]>(); 
		Random rand = new Random();
		int testSize = sampleList.size()/10;
		for(int i=0;i<testSize;i++){
			int sampleIndex = rand.nextInt(sampleList.size());
			String[] sampleSelected = sampleList.remove(sampleIndex);
			testList.add(sampleSelected);
		}
		EMAndDicDivAl seg = new EMAndDicDivAl();
		seg.setMaxWordLength(3);
		seg.setTraincontenList(sampleList);
		seg.setValidationList(testList);
		seg.intializeDic();
		seg.EMIteration(false,1000);
		Map<String, List<String>> segResult = seg.paragraphProcess(testList, null);
		System.out.println(FMeasureCal(segResult));
		boolean forwardFlage = true;
		double previousFmeasure = -1;
		double currentFmeasure = 0;
		while(wordShiftNum>0){
			previousFmeasure = -1;
			while(previousFmeasure<currentFmeasure){
				if(forwardFlage){
					seg.forwarProcess(wordShiftNum);
					seg.EMIteration(true,100);
				}else{
					seg.backWordProcess(wordShiftNum);
					seg.EMIteration(true,100);
				}
				segResult = seg.paragraphProcess(testList, null);
				previousFmeasure = currentFmeasure;
				currentFmeasure = FMeasureCal(segResult);
				System.out.println("current f measure is "+currentFmeasure);
			}
			wordShiftNum = wordShiftNum-5;
			forwardFlage = !forwardFlage;
			
		}
		previousFmeasure = -1;
		currentFmeasure = 0;
		double gama = 0;
		while(currentFmeasure>=previousFmeasure){
			Map<String, Double> puredDic = seg.DictionaryPuring(gama, gama);
			segResult = seg.paragraphProcess(testList, puredDic);
			previousFmeasure = currentFmeasure;
			currentFmeasure = FMeasureCal(segResult);
			System.out.println("when gama is "+gama +" f Measure is "+currentFmeasure);
			gama +=100;
		}
		gama = gama-100;
		double gama1 = gama;
		double gama2 = gama;
		double step = 100;
		double bestGama1 = gama1;
		double bestGama2 = gama2;
		while(step>1){
			previousFmeasure = -1;
			Map<String, Double> puredDic = seg.DictionaryPuring(gama1, gama2);
			segResult = seg.paragraphProcess(testList, puredDic);
			currentFmeasure = FMeasureCal(segResult);
			while(currentFmeasure>=previousFmeasure){
				puredDic = seg.DictionaryPuring(gama1, gama2);
				segResult = seg.paragraphProcess(testList, puredDic);
				previousFmeasure = currentFmeasure;
				currentFmeasure = FMeasureCal(segResult);
				System.out.println("when gama1 is "+gama1 +" f Measure is "+currentFmeasure);
				if(currentFmeasure>=previousFmeasure){
					bestGama1 = gama1;
				}
				gama1 = gama1 + step;
			}
			gama1 = gama1 -step;
			step = step/10;
		}
		step = 100;
		while(step>1){
			Map<String, Double> puredDic = seg.DictionaryPuring(gama1, gama2);
			segResult = seg.paragraphProcess(testList, puredDic);
			currentFmeasure = FMeasureCal(segResult);
			previousFmeasure = -1;
			while(currentFmeasure>=previousFmeasure){
				puredDic = seg.DictionaryPuring(gama1, gama2);
				segResult = seg.paragraphProcess(testList, puredDic);
				previousFmeasure = currentFmeasure;
				currentFmeasure = FMeasureCal(segResult);
				System.out.println("when gama2 is "+gama2 +" f Measure is "+currentFmeasure);
				gama2 = gama2-step;
				if(currentFmeasure>=previousFmeasure){
					bestGama2 = gama2;
				}
			}
			gama2 = gama2 + step;
			step = step/10;
		}
		return seg;
	}
}
