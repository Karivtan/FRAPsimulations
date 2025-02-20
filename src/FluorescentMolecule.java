
public class FluorescentMolecule {
	int bleached;
	int position;
	int minPos,maxPos;
	boolean lostL,lostR;
	FluorescentMolecule(){
		this.bleached=0;
		this.position = (int)(Math.random()*100); // will give every molecule a random position
		this.lostL=false;
		this.lostR=false;
	}
}
