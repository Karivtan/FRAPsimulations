import java.util.ArrayList;

public class FluorescentMolecule {
	int bleached;
	int position;
	int minPos,maxPos;
	int compartment;
	ArrayList<Integer> componentlocation=new ArrayList<Integer>();
	ArrayList<Integer> subcompartmentlocation=new ArrayList<Integer>();
	ArrayList<Character> DirectionList = new ArrayList<Character>();
	ArrayList<Integer> itterationlist = new ArrayList<Integer>();
	boolean diffused = false;
	FluorescentMolecule(int compartmentnumber){
		this.bleached=0;
		this.position = (int)(Math.random()*100); // will give every molecule a random position
		this.compartment=compartmentnumber;
	}
}
