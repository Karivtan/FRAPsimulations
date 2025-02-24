
public class Hyphae {
	Compartment [] comps;
	Hyphae (int nCompartments, int fMolecules){
		 comps = new Compartment[nCompartments+2];
		 comps[0]=new Compartment(0,0);
		for (int i=1;i<nCompartments+1;i++) {
			comps[i]=new Compartment(fMolecules,i);
		}
		comps[nCompartments+1]=new Compartment(0,0);
	}
}
