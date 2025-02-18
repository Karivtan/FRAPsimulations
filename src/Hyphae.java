
public class Hyphae {
	Compartment [] comps;
	Hyphae (int nCompartments, int fMolecules){
		 comps = new Compartment[nCompartments];
		for (int i=0;i<nCompartments;i++) {
			comps[i]=new Compartment(fMolecules);
		}
	}
}
