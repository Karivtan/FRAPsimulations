import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
@Deprecated
// No longer needed
public class FrapSimulation {

	Hyphae h;
	int bleachedCompartment;
	int nMols;
	int nComps;
	int nBleached;
	int nItterations;
	int difleft;
	int difright;
	String title;
	
	public static void main(String[] args) {
		// eventueel grootte/lengte hyphae
		// deeltjes formaat
		// andere deeltjes in hyphae
		// first create a hyphae
		FrapSimulation fs = new FrapSimulation();
		JTextField titleF = new JTextField("Frap");
		JTextField nMolsF = new JTextField("10000");
		JTextField nCompsF = new JTextField("5");
		JTextField BCF = new JTextField("2");
		JTextField nBleachedF = new JTextField("5000");
		JTextField nIttF = new JTextField("300");
		JTextField difLeftF = new JTextField("100");
		JTextField difRightF = new JTextField("500");
		JFileChooser ch = new JFileChooser();
		ch.setDialogTitle("Choose where to store the data");
		ch.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int doption = ch.showOpenDialog(null);
		File f;
		if (doption == JFileChooser.APPROVE_OPTION) {
			f=ch.getSelectedFile();
		} else {
			JOptionPane.showMessageDialog(null, "No place to store data, quitting program");
			return;
		}
		Object [] message = {
				"What should be the filename without extension", titleF,
				"Number of molecules per compartment", nMolsF,
				"Number of compartments", nCompsF,
				"Which compartment should be bleached (count starts at 0)", BCF,
				"How many molecules should be bleached", nBleachedF,
				"How many itterations for the recovery", nIttF,
				"How many molecules diffuse left per itteration", difLeftF,
				"How many molecules diffuse right per itteration", difRightF
		};
		int option =JOptionPane.showConfirmDialog(null, message, "Enter FRAP settings", JOptionPane.OK_CANCEL_OPTION);
		if (option== JOptionPane.OK_OPTION) {
			fs.title=titleF.getText();
			fs.nMols=Integer.parseInt(nMolsF.getText());
			fs.nComps=Integer.parseInt(nCompsF.getText());
			fs.bleachedCompartment=Integer.parseInt(BCF.getText());
			fs.nBleached=Integer.parseInt(nBleachedF.getText());
			fs.nItterations=Integer.parseInt(nIttF.getText());
			fs.difleft=Integer.parseInt(difLeftF.getText());
			fs.difright=Integer.parseInt(difRightF.getText());
		} else {
			JOptionPane.showMessageDialog(null, "Taking standard parameters");
			fs.title=titleF.getText();
			fs.nMols=10000;
			fs.nComps=5;
			fs.bleachedCompartment=2;
			fs.nBleached=5000;
			fs.nItterations=300;
			fs.difleft=100;
			fs.difright=500;
		}
		fs.createHyphae(fs.nComps,fs.nMols);
		fs.Bleach(fs.bleachedCompartment, fs.nBleached);
		fs.Recovery(fs.nItterations, fs.difleft, fs.difright, f);
	}
	
	public void createHyphae(int ncomp, int nmol) {
		h = new Hyphae(ncomp,nmol,100);
	}
	
	public void Bleach (int BleachedCompartment, int nBleaches) {
		Compartment bc = h.comps[BleachedCompartment];
		ArrayList<FluorescentMolecule> ListToBleach = bc.fms;
		for (int i=0;i<nBleaches;i++) {
			ListToBleach.get((int)(Math.random()*bc.fms.size())).bleached=1;
		}
		// now we have bleached time for time lapse
	}
	
	public void Recovery(int nTimepoints, int difleft, int difright, File f) {
		ArrayList<ArrayList<Integer>> data = new ArrayList<ArrayList<Integer>>();

		for (int i=0;i<nTimepoints;i++) { // loop through timepoints
			// this part moves the molecules between hyphae. we need something seperate to move them wihtin the compartments
			// start at bleached component and go left
			for (int j=this.bleachedCompartment-1 ;j>=0;j--) {
				Compartment cp = h.comps[j];
				if (j!=0) {
					//System.err.println("Adding from" +j+" to "+ (j-1));
					for (int k=0;k<difleft;k++) { // each loop diffuses a single molecule
						// every loop we need to run a simple one dimensional diffusion within the compartment
						Compartment difto = h.comps[j-1];
						ArrayList<FluorescentMolecule> ML = cp.fms;
						int CM = (int)(Math.random()*ML.size()); // this now takes a random molecule, however this should depend on position information
						difto.fms.add(ML.get(CM));
						ML.remove(CM);
					//	System.err.println(ML.size());
					}	
				}
				//System.err.println("Adding from" +j+" to "+ (j+1));
				//System.err.println("starting right loop "+cp.fms.size());
				for (int k=0;k<difright;k++) {
					Compartment difto = h.comps[j+1];
					ArrayList<FluorescentMolecule> ML = cp.fms;
					int CM = (int)(Math.random()*ML.size());
					//System.out.println(ML.size()+" "+CM+" "+ k+","+j);
					difto.fms.add(ML.get(CM));
					ML.remove(CM);
				}
			}
			//start at bleached component and go right
			for (int j=this.bleachedCompartment;j<h.comps.length;j++) {
				Compartment cp = h.comps[j];
				//System.err.println("Adding from" +j+" to "+ (j-1));
				for (int k=0;k<difleft;k++) {
					Compartment difto = h.comps[j-1];
					ArrayList<FluorescentMolecule> ML = cp.fms;
					int CM = (int)(Math.random()*ML.size());
					difto.fms.add(ML.get(CM));
					ML.remove(CM);
				}	
				
				if (j!=h.comps.length-1) {
					//System.err.println("Adding from" +j+" to "+ (j+1));
					for (int k=0;k<difright;k++) {
						Compartment difto = h.comps[j+1];
						ArrayList<FluorescentMolecule> ML = cp.fms;
						int CM = (int)(Math.random()*ML.size());
						difto.fms.add(ML.get(CM));
						ML.remove(CM);
					}
				}
			}
			
			//fix compartment 0
			ArrayList<FluorescentMolecule> ML= h.comps[0].fms;
			for (int l=0;l<difright-difleft;l++) {
				ML.add(new FluorescentMolecule(0,100));
			}
			// fix end compartment
			ML= h.comps[h.comps.length-1].fms;
			for (int l=0;l<difright-difleft;l++) {
				int CM = (int)(Math.random()*ML.size());
				ML.remove(CM);
			}
			
			//System.err.println("new size "+ML.size());
			ArrayList<Integer> cd = new ArrayList<Integer>();
			for (int l=0;l<h.comps.length;l++) {
				int sum=this.nMols;
				Compartment cp = h.comps[l];
				ArrayList<FluorescentMolecule> CL=cp.fms;
				for (int m=0;m<CL.size();m++) {
					sum-=CL.get(m).bleached;
				}
				cd.add(sum);
				//System.out.print(sum+", ");
				
			}
			data.add(cd);
			//System.out.println("");
		}
		// here we export the data
		File fs = new File(f+"/"+title+".csv");
		System.out.println(f.toString());
		int counter=1; 
		while (fs.exists()) {
			fs = new File(f+"/"+title+counter+".csv");
			System.out.println(fs.toString());
			counter++;
		}
		try {
			System.out.println("writing file");
			PrintWriter pw = new PrintWriter(fs);
			for (ArrayList<Integer> k: data) {
				for (Integer l: k) {
					pw.print(l);
					pw.print(",");
				}
				pw.println();
			}
			pw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
	}
}
