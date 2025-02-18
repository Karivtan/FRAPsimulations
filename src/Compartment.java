import java.util.ArrayList;

public class Compartment {
	ArrayList<FluorescentMolecule> fms;
	Compartment (int nFM){
		fms = new ArrayList<FluorescentMolecule>();
		for (int i =0;i<nFM;i++) {
			fms.add(new FluorescentMolecule());
		}
	}
}
