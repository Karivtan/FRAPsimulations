
public class FluorescentMolecule {
	int bleached;
	int position;
	FluorescentMolecule(){
		this.bleached=0;
		this.position = (int)(Math.random()*100); // will give every molecule a random position
	}
}
