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
	ArrayList<int []> positionlist = new ArrayList<int []>();
	boolean diffused = false;
	FluorescentMolecule(int compartmentnumber, int compartmentlength){
		this.bleached=0;
		//this.position = (int)(Math.random()*(compartmentlength-1)); // will give every molecule a random position
		this.position = (int)Math.floor((Math.random()*(compartmentlength))); // will give every molecule a random position, will never reach the compartmentlenght position 
		this.compartment=compartmentnumber;
		this.positionlist.add(new int[] {compartment, position});
	}
	
	FluorescentMolecule(int compartmentnumber, int compartmentlength, int pos){
		this.bleached=0;
		//this.position = (int)(Math.random()*(compartmentlength-1)); // will give every molecule a random position
		this.position = pos; // will give every molecule a random position, will never reach the compartmentlenght position 
		this.compartment=compartmentnumber;
		this.positionlist.add(new int[] {compartment, position});
	}
}
