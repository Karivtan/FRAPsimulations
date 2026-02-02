import java.util.ArrayList;

public class Compartment {
	ArrayList<FluorescentMolecule> fms;
	int compartmentnumber;
	Compartment (int nFM, int compartmentnumber, int compartmentlength){
		fms = new ArrayList<FluorescentMolecule>();
		for (int i =0;i<nFM;i++) {
			fms.add(new FluorescentMolecule(compartmentnumber, compartmentlength));
			this.compartmentnumber=compartmentnumber;
		}
	}
}
