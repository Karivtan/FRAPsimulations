import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JProgressBar;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.time.LocalTime;


public class FrapSimulationWithInternalDiffusion {

	Hyphae h;
	int startB, endB, bleachedCompartment, nMols, nComps, nBleached,nItterations, nIttD, lostL, lostR, nSubComp;
	double diffD, difright, difleft;
	boolean diffBleach, refillL, refillR, moveL, moveR;
	String title;
	File f;
	int [] posHist= new int[100];
	int [] LR = new int[2];
	
	public static void main(String[] args) {
		// eventueel grootte/lengte hyphae
		// deeltjes formaat
		// andere deeltjes in hyphae
		// first create a hyphae
		FrapSimulationWithInternalDiffusion fs = new FrapSimulationWithInternalDiffusion();
		for (int i=0;i<100;i++) {
			fs.posHist[i]=0;
		}
		JTextField titleF = new JTextField("Frap");
		JTextField nMolsF = new JTextField("1000");
		JTextField nCompsF = new JTextField("5");
		JTextField startB = new JTextField("35");
		JTextField diffDF = new JTextField("50");
		JTextField endB = new JTextField("65");
		JTextField BCF = new JTextField("2");
		JTextField nBleachedF = new JTextField("100");
		JTextField nIttF = new JTextField("200");
		JTextField nIttD = new JTextField("100");
		JTextField difLeftF = new JTextField("25");
		JTextField difRightF = new JTextField("25");
		JTextField nSubCompF = new JTextField("3");
		JCheckBox diffB= new JCheckBox("Simmulate diffusion during bleaching (selected), or instantaneous bleach",true);
		JCheckBox refL= new JCheckBox("Refill molecules lost on the right from the left",true);
		JCheckBox refR= new JCheckBox("Refill molecules lost on the left from the right",true);
		JCheckBox moveL= new JCheckBox("Hyphae is closed on the left side",false);
		JCheckBox moveR= new JCheckBox("Hyphae is closed on the right side",false);
		JFileChooser ch = new JFileChooser();
		ch.setDialogTitle("Choose where to store the data");
		ch.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		File cDir=new File("C:\\Users\\willemsejj\\ownCloud - Joost Willemse@universiteitleiden.data.surfsara.nl (3)\\IBL_Streptomyces_cell_division (Projectfolder)\\Diffusion kinetics");
		ch.setCurrentDirectory(cDir);
		int doption = ch.showOpenDialog(null);
		if (doption == JFileChooser.APPROVE_OPTION) {
			fs.f=ch.getSelectedFile();
		} else {
			JOptionPane.showMessageDialog(null, "No place to store data, quitting program");
			return;
		}
		Object [] message = {
				"What should be the filename without extension", titleF,
				"Number of molecules per compartment", nMolsF,
				"Number of compartments", nCompsF,
				"How many subcompartments should be measured per compartment", nSubCompF,
				"Which compartment should be bleached (count starts at 0)", BCF,
				"Start position of bleach in the compartment (0-100)", startB,
				"End position of bleach in the compartment (0-100)", endB,
				"Chance of molecules diffusing to the left", diffDF,
				"Percentage of molecules in the compartment are bleached", nBleachedF,
				"Number of measurement itterations", nIttF,
				"Number of diffusion itterations per measurement itteration", nIttD,
				"Chance to diffuse to left compartment", difLeftF,
				"Chance to diffuse to right compartment", difRightF,
				diffB,
				refL,
				refR,
				moveL,
				moveR,
		};
		int option =JOptionPane.showConfirmDialog(null, message, "Enter FRAP settings", JOptionPane.OK_CANCEL_OPTION);
		if (option== JOptionPane.OK_OPTION) {
			fs.title=titleF.getText();
			fs.nMols=Integer.parseInt(nMolsF.getText());
			fs.nComps=Integer.parseInt(nCompsF.getText());
			fs.bleachedCompartment=Integer.parseInt(BCF.getText());
			fs.nBleached=Integer.parseInt(nBleachedF.getText());
			fs.nItterations=Integer.parseInt(nIttF.getText());
			fs.nIttD=Integer.parseInt(nIttD.getText());
			fs.difleft=Double.parseDouble(difLeftF.getText());
			fs.difright=Double.parseDouble(difRightF.getText());
			fs.startB=Integer.parseInt(startB.getText());
			fs.endB=Integer.parseInt(endB.getText());
			fs.diffBleach=diffB.isSelected();
			fs.refillL=refL.isSelected();
			fs.refillR=refR.isSelected();
			fs.moveL=moveL.isSelected();
			fs.moveR=moveR.isSelected();
			fs.nSubComp=Integer.parseInt(nSubCompF.getText());
			fs.diffD=Double.parseDouble(diffDF.getText());
		} else {
			JOptionPane.showMessageDialog(null, "No settings selected, quitting program");
			return;
		}
		fs.createHyphae(fs.nComps,fs.nMols);//this is still fine
		/*+1 on bleachedcompartments needed since we need to bleach one compartment further than actually indicated because of the 2 additional compartments*/
		System.out.println("Starting");
		fs.Bleach(fs.bleachedCompartment+1 , fs.nBleached,fs.startB,fs.endB, fs.nIttD, fs.diffBleach, fs.difleft, fs.difright);// we still need to bleach, however we need to consider the position
		System.out.println("Done");
	}
	
	public void createHyphae(int ncomp, int nmol) {
		h = new Hyphae(ncomp,nmol); // creates new hyphae with 2 empty compartments on each side
	}
	
	public void Bleach (int BleachedCompartment, int nBleaches, int startB, int endB, int nIttD, boolean diffB, double difleft, double difright) {
		Compartment bc = h.comps[BleachedCompartment]; // select the bleached compartment
		ArrayList<FluorescentMolecule> CompartmentMolecules = bc.fms; // get the molecules in there
		int nToBleach=nMols*nBleaches/100; // needed to stop the bleach when we also diffuse
		
			
		if (!diffB) {
			ArrayList<FluorescentMolecule> ListToBleach = new ArrayList<FluorescentMolecule>();
			for (FluorescentMolecule i: CompartmentMolecules) { // go through all molecules in the compartment
				if (i.position>startB && i.position<endB ) { // checks where the molecule is 
					ListToBleach.add(i);// creates the list that can potentially be bleached
				}
			}
		// Now we need to bleach
			for (FluorescentMolecule i:ListToBleach) {
				if (Math.random()<nBleaches/100) {// this means the molecule should be bleached
					i.bleached=1;
				}
			}
			File fs = new File(f+"/"+title+".csv");
			int counter=1; 
			File fs2 = new File(f+"/"+title+"Split.csv");
			while (fs.exists()) {
				fs = new File(f+"/"+title+counter+".csv");
				fs2 = new File(f+"/"+title+counter+"Split.csv");
				counter++;
			}
			try {
				PrintWriter pw = new PrintWriter(fs);
				PrintWriter pw2 = new PrintWriter(fs2);
				pw.println("sep=,");
				pw2.println("sep=,");
				
				pw.println("Number of molecules per compartment,"+nMols);
				pw.println("Number of compartments,"+nComps);
				pw.println("Number of subcompartments for analysis,"+nSubComp);
				pw.println("Which compartment should be bleached (count starts at 0),"+(BleachedCompartment-1));
				pw.println("Start position of bleach in the compartment (0-100),"+startB);
				pw.println("End position of bleach in the compartment (0-100),"+endB);
				pw.println("Percentage of molecules in the compartment are bleached,"+nBleached);
				pw.println("Number of measurement itterations,"+nItterations);
				pw.println("Number of diffusion itterations per measurement itteration,"+nItterations);
				pw.println("Chance of molecules diffusing to the left,"+diffD);
				pw.println("Chance to diffuse to left compartment,"+difleft);
				pw.println("Chance to diffuse to right compartment,"+difright);
				pw.println("Refill molecules lost on the right from the left,"+refillL);
				pw.println("Refill molecules lost on the left from the right,"+refillR);
				pw.println("Hyphae is closed on the left side,"+moveL);
				pw.println("Hyphae is closed on the left side,"+moveR);
				String str = "";
				for (int i=1;i<h.comps.length-1;i++) {
					str+="Compartment "+i+", ";
				}
				pw.println("Itteration, "+str);
				JProgressBar pb = new JProgressBar();
				pb.setMaximum(nItterations);
				JFrame jf = new JFrame("Calculating");
				JPanel jp = new JPanel();
				jp.setLayout(new BoxLayout(jp,BoxLayout.Y_AXIS));
				jf.add(jp);
				jp.add(new JLabel("Starting calculations at: "+LocalTime.now().toString()));
				jp.add(pb);
				jf.setSize(500,500);
				jf.setVisible(true);
				
				for (int i=0;i<nItterations;i++) { 
					pb.setValue(i+1);
					diffuseCycle(); 
					pw.print((i+1)+",");
					pw2.print((i+1)+",");
					MoleculeCounter(pw);
					SplitCompartmentCounter(pw2,nSubComp);
				}
				pw.close();
				pw2.close();
				jf.setVisible(false);
				
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		} else {
			//Only molecules that have been in the bleach area can be bleached, so we need to register max and min positions for every molecule
			//So we let them diffuse first, and then bleach away nBleaches/100 percent of the molecules that are in the compartment
			ArrayList<FluorescentMolecule> toremove=diffuseMolecules(CompartmentMolecules, nIttD, BleachedCompartment, difleft, difright);
			for (FluorescentMolecule deleting:toremove) {
				CompartmentMolecules.remove(deleting);
			}
			//now we know where all molecules have moved around in the compartment
			//Now we have the cleaned list without the ones that left the compartment
			//Now we can bleach the molecules in the current compartment

			ArrayList<FluorescentMolecule> toBleach = getBleachables(CompartmentMolecules,startB, endB);
			//Make sure bleaching will not try to bleach more molecules then are currenty possible
			if (nToBleach>toBleach.size()) {
				nToBleach=toBleach.size();
			}
			for (int i=0;i<nToBleach;i++) {
				// since start positions are random we can just bleach the first n ones.
				CompartmentMolecules.get(i).bleached=1;
			}
			//then we need to check the molecules that left the compartment and see if they are bleached
			//For now not implemented since most molecules stay within the compartment

			//Now we need to diffuse the molecules for nIttF*nIttD
			//After every nIttF we need to count the molecules per compartment
			File fs = new File(f+"/"+title+".csv");
			File fs2 = new File(f+"/"+title+"Split.csv");
			int counter=1; 
			while (fs.exists()) {
				fs = new File(f+"/"+title+counter+".csv");
				fs2 = new File(f+"/"+title+counter+"Split.csv");
				counter++;
			}
			try {
				PrintWriter pw = new PrintWriter(fs);
				PrintWriter pw2 = new PrintWriter(fs2);
				pw.println("sep=,");
				pw2.println("sep=,");
				pw.println("Number of molecules per compartment,"+nMols);
				pw.println("Number of compartments,"+nComps);
				pw.println("Number of subcompartments for analysis,"+nSubComp);
				pw.println("Which compartment should be bleached (count starts at 0),"+bleachedCompartment);
				pw.println("Start position of bleach in the compartment (0-100),"+startB);
				pw.println("End position of bleach in the compartment (0-100),"+endB);
				pw.println("Percentage of molecules in the compartment are bleached,"+nBleached);
				pw.println("Chance of molecules diffusing to the left,"+diffD);
				pw.println("Number of measurement itterations,"+nItterations);
				pw.println("Number of diffusion itterations per measurement itteration,"+nItterations);
				
				pw.println("Chance to diffuse left,"+difleft);
				pw.println("Chance to diffuse right,"+difright);
				pw.println("Refill molecules lost on the right from the left,"+refillL);
				pw.println("Refill molecules lost on the left from the right,"+refillR);
				pw.println("Hyphae is closed on the left side,"+moveL);
				pw.println("Hyphae is closed on the left side,"+moveR);
				String str = "";
				String str2 = "Itteration,";
				for (int i=1;i<h.comps.length-1;i++) {
					str+="Compartment "+i+", ";
				}
				for (int i=1;i<h.comps.length-1;i++) {
					for (int j=1;j<nSubComp+1;j++) {
						str2+="Compartment "+i+" Sub "+j + ", ";
					}
				}

				pw.println("Itteration, "+str);
				pw2.println(str2);
				
				JProgressBar pb = new JProgressBar();
				pb.setMaximum(nItterations);
				JFrame jf = new JFrame("Calculating");
				JPanel jp = new JPanel();
				jp.setLayout(new BoxLayout(jp,BoxLayout.Y_AXIS));
				jf.add(jp);
				jp.add(new JLabel("Starting calculations at: "+LocalTime.now().toString()));
				jp.add(pb);
				jf.setSize(500,500);
				jf.setVisible(true);
				
				for (int i=0;i<nItterations;i++) {
					pb.setValue(i+1);
					for (int k=0;k<100;k++) {
						posHist[k]=0;
					}
					for (int k=1;k<h.comps.length-1;k++) {
						ArrayList<FluorescentMolecule> fms = h.comps[k].fms;
						for (FluorescentMolecule counting:fms) {
							posHist[counting.position]+=1;
						}
					}
					diffuseCycle(); 
					pw.print((i+1)+",");
					MoleculeCounter(pw);
					pw2.print((i+1)+",");
					SplitCompartmentCounter(pw2,nSubComp);
					
					
					
				}
				pw.close();
				pw2.close();
				jf.setVisible(false);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
	
	}
	
	public void SplitCompartmentCounter(PrintWriter pw, int nparts) {
		int[] nmols = new int[nparts*nComps+1];//+1 needed for overflow if division is not perfect integer
		
		for (int i=0;i<nmols.length;i++) { // initialize on 0
			nmols[i]=0;
		}
		for (int i=1;i<h.comps.length-1;i++) {
			ArrayList<FluorescentMolecule> cm=h.comps[i].fms;
			for (FluorescentMolecule counting:cm) {
				
				if (counting.bleached==0) {
					// say it is 100 big then every 20 if five
					// say is is 100 big then every 25 if 4
					nmols[counting.position/(100/nparts)+(i-1)*nparts]+=1;
					// counting.position is between 0 and 99, with 5C it gets divided by 20
					
				}
			}
			//end up with a cleaned compartment list of molecules
		}
		for (int i=0;i<nmols.length;i++) { // initialize on 0
			pw.print(nmols[i]+", ");
		}
		pw.println();
	}
	

	public void MoleculeCounter(PrintWriter pw) {
		for (int i=1;i<h.comps.length-1;i++) {
			ArrayList<FluorescentMolecule> cm=h.comps[i].fms;
			int count=0;
			for (FluorescentMolecule counting:cm) {
				if (counting.bleached==0) {
					count++;
				}
			}
			pw.print(count+", ");
			//end up with a cleaned compartment list of molecules
		}
		pw.println();
	}
	
	public void diffuseCycle(){ // here we need to do the diffusion, and add new molecules when running out on the left and right
		lostL=0;
		lostR=0;
		for (int i=1;i<h.comps.length-1;i++) { // calculates diffusion from compartment 1 to n-1
			ArrayList<FluorescentMolecule> cm=h.comps[i].fms;
			ArrayList<FluorescentMolecule> toChange;
			if (moveL&&i==1) {
				toChange = diffuseMolecules(cm,nIttD,i,0,difright);
			} else if (moveR &&i==h.comps.length-2) {
				toChange = diffuseMolecules(cm,nIttD,i,difleft,0);
			} else {
				toChange = diffuseMolecules(cm,nIttD,i,difleft,difright);
			}
			for (FluorescentMolecule deleting:toChange) {
				cm.remove(deleting);
			}
		}
		lostL=h.comps[0].fms.size();
		lostR=h.comps[h.comps.length-1].fms.size();
		h.comps[0].fms=new ArrayList<FluorescentMolecule>(); // resets the diffused out molecules
		h.comps[h.comps.length-1].fms=new ArrayList<FluorescentMolecule>(); // resets the diffused out molecules right
		
		for (int i=1;i<h.comps.length-1;i++) { // calculates diffusion from compartment 1 to n-1
			ArrayList<FluorescentMolecule> cm=h.comps[i].fms;
			for (FluorescentMolecule fm:cm) {
				fm.diffused=false;
			}
		}
		
		
		if (refillL) {
			for (int i=0;i<lostR;i++) {
				h.comps[1].fms.add(new FluorescentMolecule(1));
			}
		}
		if (refillR) {
			for (int i=0;i<lostL;i++) {
				h.comps[h.comps.length-2].fms.add(new FluorescentMolecule(h.comps.length-2));
			}
		}
	}
	
	public ArrayList<FluorescentMolecule> getBleachables(ArrayList<FluorescentMolecule> cm, int startB, int endB){
		ArrayList<FluorescentMolecule> toBleach =new ArrayList<FluorescentMolecule>(); // needed to see which molecules to bleach
		for (FluorescentMolecule fm : cm) {
			if (fm.minPos<endB) {
				if (fm.maxPos>startB) {
					toBleach.add(fm);
				}
			} else if (fm.maxPos>startB) {
				if (fm.minPos<endB) {
					toBleach.add(fm);
				}
			}
		}
		return toBleach;
	}
	
	public ArrayList<FluorescentMolecule> diffuseMolecules(ArrayList<FluorescentMolecule> cm, int nIttD, int bc, double difleft, double difright) {
		// We need to simulate diffusion and mark the min and max position
		// Also if the max position>100 or the min position<0 we need to move it to another compartment based on the difleft, and or difright chances
		 // needed to move between compartments
		ArrayList<FluorescentMolecule> toremove =new ArrayList<FluorescentMolecule>(); // needed to see which molecules to remove
		for (FluorescentMolecule fm:cm) {// This diffuses the molecule
			int cminpos=fm.position; // if at any time during diffusion the molecule is in this region, it can be bleached
			int cmaxpos=fm.position;
			if(!fm.diffused) { // needed to prevent double diffusion which creates a bias to the left
				for (int j=0;j<nIttD;j++) {
					if (Math.random()<(diffD/100)) { //move left
						fm.position--;
						if (fm.position<0) {
							//check if it gets moved right or whether is bounces of the wall
							if (Math.random()*100<difleft) {
								fm.position=99; 
								cmaxpos=99;
								cminpos=99;
								fm.compartment--;
							} else {
								fm.position=1;//bounced so it moves back to position 1
							} 
						}  
						if (fm.position<cminpos &&fm.compartment==bc) {
							cminpos=fm.position;
							fm.minPos=cminpos;
						}
						
					} else { //move right
						fm.position++;
						if (fm.position>99) {
							//check if it gets moved right or whether is bounces of the wall
							if (Math.random()*100<difright) { // it diffuses right over the cell wall
								fm.position=0;
								cmaxpos=0;
								cminpos=0;
								fm.compartment++;
							} else { // it bounced
								fm.position=98;//bounced so it moves back to position 98
							} 
						}
						if (fm.position>cmaxpos && fm.compartment==bc) { 
							cmaxpos=fm.position;
							fm.maxPos=cmaxpos;						
						}
					}
				}
				//done checking the molecule
				// the compartment position was added to the molecule, so if the current compartment position == bc
				//if (cComp==bc) { // this means it moved back or stayed in to the original compartment nothing needs to be done
				if (fm.compartment!=bc) {
					//fm.compartment always equals cComp
					// need to remove, so clean list
					toremove.add(fm); // add it to the remove list
					// This also means we need to add it to the new compartments
					h.comps[fm.compartment].fms.add(fm);
					if (fm.compartment>bc) { // this shows there is a lot more moving left than right.
						LR[1]+=1;
					} else {
						LR[0]+=1;
					}
				}
				fm.diffused=true;
			}
		}
		return toremove;
	}

}
