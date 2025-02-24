import java.util.ArrayList;

public class Compartment {
	ArrayList<FluorescentMolecule> fms;
	int compartmentnumber;
	Compartment (int nFM, int compartmentnumber){
		fms = new ArrayList<FluorescentMolecule>();
		for (int i =0;i<nFM;i++) {
			fms.add(new FluorescentMolecule(compartmentnumber));
			this.compartmentnumber=compartmentnumber;
		}
	}
}
