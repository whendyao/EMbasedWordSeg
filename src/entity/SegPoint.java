package entity;

public class SegPoint {
	private int segStartIndex;
	private int segEndIndex;
	private double segProb;
	public SegPoint(int startIndex, int endIndex, double segProb){
		this.segStartIndex = startIndex;
		this.segEndIndex = endIndex;
		this.segProb = segProb;
	}
	public int getSegStartIndex() {
		return segStartIndex;
	}
	public void setSegStartIndex(int segStartIndex) {
		this.segStartIndex = segStartIndex;
	}
	public int getSegEndIndex() {
		return segEndIndex;
	}
	public void setSegEndIndex(int segEndIndex) {
		this.segEndIndex = segEndIndex;
	}
	public double getSegProb() {
		return segProb;
	}
	public void setSegProb(double segProb) {
		this.segProb = segProb;
	}
	
}
