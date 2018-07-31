package test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import evaluation.CrossValidation;
import seg.algorithm.EmBasedMethod;

public class EmSegTest {
	@Test
	public void dicinitialTest(){
		EmBasedMethod em = new EmBasedMethod();
		em.loadTrainData("E:\\testData\\DicTest");
		em.setMaxWordLength(3);
		em.intializeDic();
		
		em.EMIteration();
	}
	
	@Test
	public void EmBasedSegTest(){
		CrossValidation.LoadSegInfor("E:\\testData\\DicTest\\199801.txt");
		CrossValidation.EmBasedSegTest("E:\\testData\\DicTest\\199801_1.txt");
		
	}
	@Test
	public void EmandDicDivSegTest() throws Exception{
		CrossValidation.LoadSegInfor("E:\\testData\\DicTest\\199801.txt");
		CrossValidation.EMandDicDivTest("E:\\testData\\DicTest\\199801_1.txt");
		
	}
}
