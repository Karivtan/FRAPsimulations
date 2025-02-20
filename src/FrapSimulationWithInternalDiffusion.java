import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
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
	int startB, endB, bleachedCompartment, nMols, nComps, nBleached,nItterations, difleft,difright,nIttD, lostL, lostR, diffD;
	boolean diffBleach, refillL, refillR, moveL, moveR;
	String title;
	File f;
	
	public static void main(String[] args) {
		// eventueel grootte/lengte hyphae
		// deeltjes formaat
		// andere deeltjes in hyphae
		// first create a hyphae
		FrapSimulationWithInternalDiffusion fs = new FrapSimulationWithInternalDiffusion();
		JTextField titleF = new JTextField("Frap");
		JTextField nMolsF = new JTextField("10000");
		JTextField nCompsF = new JTextField("5");
		JTextField startB = new JTextField("35");
		JTextField diffDF = new JTextField("50");
		JTextField endB = new JTextField("65");
		JTextField BCF = new JTextField("2");
		JTextField nBleachedF = new JTextField("50");
		JTextField nIttF = new JTextField("200");
		JTextField nIttD = new JTextField("100");
		JTextField difLeftF = new JTextField("25");
		JTextField difRightF = new JTextField("25");
		JCheckBox diffB= new JCheckBox("Simmulate diffusion during bleaching (selected), or instantaneous bleach",true);
		JCheckBox refL= new JCheckBox("Refill molecules lost on the right from the left",true);
		JCheckBox refR= new JCheckBox("Refill molecules lost on the left from the right",true);
		JCheckBox moveL= new JCheckBox("Hyphae is closed on the left side",false);
		JCheckBox moveR= new JCheckBox("Hyphae is closed on the right side",false);
		JFileChooser ch = new JFileChooser();
		ch.setDialogTitle("Choose where to store the data");
		ch.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
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
				"Which compartment should be bleached (count starts at 0)", BCF,
				"Start position of bleach in the compartment (0-100)", startB,
				"End position of bleach in the compartment (0-100)", endB,
				"Percentage of molecules diffusing to the right", diffDF,
				"Percentage of molecules in the compartment are bleached", nBleachedF,
				"Number of measurement itterations", nIttF,
				"Number of diffusion itterations per measurement itteration", nIttD,
				"Chance of molecules diffuse left per itteration in %", difLeftF,
				"Chance molecules diffuse right per itteration in %", difRightF,
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
			fs.difleft=Integer.parseInt(difLeftF.getText());
			fs.difright=Integer.parseInt(difRightF.getText());
			fs.startB=Integer.parseInt(startB.getText());
			fs.endB=Integer.parseInt(endB.getText());
			fs.diffBleach=diffB.isSelected();
			fs.refillL=refL.isSelected();
			fs.refillR=refR.isSelected();
			fs.moveL=moveL.isSelected();
			fs.moveR=moveR.isSelected();
			fs.diffD=Integer.parseInt(diffDF.getText());
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
	
	public void Bleach (int BleachedCompartment, int nBleaches, int startB, int endB, int nIttD, boolean diffB, int difledft, int difright) {
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
			while (fs.exists()) {
				fs = new File(f+"/"+title+counter+".csv");
				counter++;
			}
			try {
				PrintWriter pw = new PrintWriter(fs);
				// #TODO add settings to file
				pw.println("Number of molecules per compartment,"+nMols);
				pw.println("Number of compartments,"+nComps);
				pw.println("Which compartment should be bleached (count starts at 0),"+bleachedCompartment);
				pw.println("Start position of bleach in the compartment (0-100),"+startB);
				pw.println("End position of bleach in the compartment (0-100),"+endB);
				pw.println("Percentage of molecules in the compartment are bleached,"+nBleached);
				pw.println("Number of measurement itterations,"+nItterations);
				pw.println("Number of diffusion itterations per measurement itteration,"+nItterations);
				pw.println("Percentage of molecules diffusing to the right,"+diffD);
				pw.println("Chance to diffuse left,"+difleft);
				pw.println("Chance to diffuse right,"+difright);
				pw.println("Refill molecules lost on the right from the left,"+refillL);
				pw.println("Refill molecules lost on the left from the right,"+refillR);
				pw.println("Hyphae is closed on the left side,"+moveL);
				pw.println("Hyphae is closed on the left side,"+moveR);
				
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
					MoleculeCounter(pw);
					/*
					for (int j=0;j<h.comps.length;j++) {
						System.out.print(h.comps[j].fms.size()+",");
					}
					System.out.println();*/
				}
				pw.close();
				jf.setVisible(false);
				
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		} else {
			//Only molecules that have been in the bleach area can be bleached, so we need to register max and min positions for every molecule
			//So we let them diffuse first, and then bleach away nBleaches/100 percent of the molecules that are in the compartment
			ArrayList<ArrayList<FluorescentMolecule>> toCheck=diffuseMolecules(CompartmentMolecules, nIttD, BleachedCompartment, difleft, difright);
			ArrayList<FluorescentMolecule> toremove=toCheck.get(0);
			for (FluorescentMolecule deleting:toremove) {
				CompartmentMolecules.remove(deleting);
			}
			ArrayList<FluorescentMolecule> toclean=toCheck.get(1);
			//now we know where all molecules have moved around in the compartment
			//Now we have the cleaned list without the ones that left the compartment
			//Now we can bleach the molecules in the current compartment
			for (int i=0;i<h.comps.length;i++) {
				ArrayList<FluorescentMolecule> cm=h.comps[i].fms;
				for (FluorescentMolecule delfm:toclean) {
					if (cm.contains(delfm) && i!=bleachedCompartment+1) {
						cm.remove(delfm);
					}
				}
				//end up with a cleaned compartment list of molecules
			}
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
			int counter=1; 
			while (fs.exists()) {
				fs = new File(f+"/"+title+counter+".csv");
				counter++;
			}
			try {
				PrintWriter pw = new PrintWriter(fs);
				// #TODO add settings to file
				pw.println("Number of molecules per compartment,"+nMols);
				pw.println("Number of compartments,"+nComps);
				pw.println("Which compartment should be bleached (count starts at 0),"+bleachedCompartment);
				pw.println("Start position of bleach in the compartment (0-100),"+startB);
				pw.println("End position of bleach in the compartment (0-100),"+endB);
				pw.println("Percentage of molecules in the compartment are bleached,"+nBleached);
				pw.println("Percentage of molecules diffusing to the right,"+diffD);
				pw.println("Number of measurement itterations,"+nItterations);
				pw.println("Number of diffusion itterations per measurement itteration,"+nItterations);
				
				pw.println("Chance to diffuse left,"+difleft);
				pw.println("Chance to diffuse right,"+difright);
				pw.println("Refill molecules lost on the right from the left,"+refillL);
				pw.println("Refill molecules lost on the left from the right,"+refillR);
				pw.println("Hyphae is closed on the left side,"+moveL);
				pw.println("Hyphae is closed on the left side,"+moveR);
				
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
					MoleculeCounter(pw);
					/*
					for (int j=0;j<h.comps.length;j++) {
						System.out.print(h.comps[j].fms.size()+",");
					}
					System.out.println();*/
				}
				pw.close();
				jf.setVisible(false);
				
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
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
		for (int i=1;i<h.comps.length-1;i++) {
			ArrayList<FluorescentMolecule> cm=h.comps[i].fms;
			ArrayList<ArrayList<FluorescentMolecule>> toChange;
			if (moveL&&i==1) {
				toChange = diffuseMolecules(cm,nIttD,i,0,difright);
/*				System.out.println("After 1st compartment diffusion");
				for (int j=0;j<h.comps.length;j++) {
					System.out.print(h.comps[j].fms.size()+",");
				}	
				System.out.println();*/
			} else if (moveR &&i==h.comps.length-2) {
				toChange = diffuseMolecules(cm,nIttD,i,difleft,0);
			} else {
				toChange = diffuseMolecules(cm,nIttD,i,difleft,difright);
			}
			ArrayList<FluorescentMolecule> toremove=toChange.get(0);
			ArrayList<FluorescentMolecule> toclean=toChange.get(1);
			for (FluorescentMolecule deleting:toremove) {
				cm.remove(deleting);
			}
			for (int j=0;j<h.comps.length;j++) {
				for (FluorescentMolecule delfm:toclean) {
					if (cm.contains(delfm) && j!=i) {
						cm.remove(delfm);
					}
				}
			}
			//end up with a cleaned compartment list of molecules
			//System.out.print(h.comps[i].fms.size()+",");
		}
		lostL=h.comps[0].fms.size();
		lostR=h.comps[h.comps.length-1].fms.size();
		//System.out.println(lostL+","+lostR+",");
		h.comps[0].fms=new ArrayList<FluorescentMolecule>(); // resets the diffused out molecules
		h.comps[h.comps.length-1].fms=new ArrayList<FluorescentMolecule>(); // resets the diffused out molecules right
		if (refillL) {
			for (int i=0;i<lostR;i++) {
				h.comps[1].fms.add(new FluorescentMolecule());
			}
		}
		if (refillR) {
			for (int i=0;i<lostL;i++) {
				h.comps[h.comps.length-2].fms.add(new FluorescentMolecule());
			}
		}


		//System.out.println();
	}
	
	public ArrayList<FluorescentMolecule> getBleachables(ArrayList<FluorescentMolecule> cm, int startB, int endB){
		ArrayList<FluorescentMolecule> toBleach =new ArrayList<FluorescentMolecule>(); // needed to see which molecules to bleach
		for (FluorescentMolecule fm : cm) {
			if (fm.minPos<endB) {
				toBleach.add(fm);
			} else if (fm.maxPos>startB) {
				toBleach.add(fm);
			}
		}
		return toBleach;
	}
	
	public ArrayList<ArrayList<FluorescentMolecule>> diffuseMolecules(ArrayList<FluorescentMolecule> cm, int nIttD, int bc, int difleft, int difright) {
		// We need to simulate diffusion and mark the min and max position
		// Also if the max position>100 or the min position<0 we need to move it to another compartment based on the difleft, and or difright chances
		 // needed to move between compartmentds
		ArrayList<FluorescentMolecule> toremove =new ArrayList<FluorescentMolecule>(); // needed to see which molecules to remove
		ArrayList<FluorescentMolecule> toclean =new ArrayList<FluorescentMolecule>(); 
		ArrayList<ArrayList<FluorescentMolecule>> toreturn= new ArrayList<ArrayList<FluorescentMolecule>>();
		for (FluorescentMolecule fm:cm) {// This diffuses the molecule
			int cComp=bc;
			//System.out.println("New molecule");
			int cminpos=fm.position; // if at any time during diffusion the molecule is in this region, it can be bleached
			int cmaxpos=fm.position;
			cComp=bc;
			for (int j=0;j<nIttD;j++) {
				//System.out.print(fm.position+", ");
				if (Math.random()>diffD/100) { //move right
					fm.position++;
					if (fm.position>99) {
						//check if it gets moved right or whether is bounces of the wall
						if (Math.random()*100<difright) {
							
							if (cComp+1<h.comps.length) {// checks if it can diffuse right
								fm.position=0;
								cmaxpos=0;
								cminpos=0;
								//System.out.println("Trying right "+j+", "+fm.toString());
								if (!h.comps[cComp+1].fms.contains(fm)) {
									//System.out.println((cComp+1)+" Does not contain "+fm.toString());
									h.comps[cComp+1].fms.add(fm); //moves it to the start of the next compartment
								} else {
									
									//System.out.println((cComp+1)+" Does contain "+fm.toString());
								}
								// Does not work, add it to a remove list, which will be removed later on. h.comps[cComp].fms.remove(fm); //removes it from the active compartment
								if(!toremove.contains(fm)) {	
									toremove.add(fm);
								}
								cComp++;;
							} 
						} else {
							fm.position-=2;//bounced so it moves back to position 99
						}
					}
					if (fm.position>cmaxpos && cComp==bc) { 
						cmaxpos=fm.position;
						fm.maxPos=cmaxpos;						
					}
				} else { //move left
					fm.position--;
					if (fm.position<0) {
						//check if it gets moved right or whether is bounces of the wall
						if (Math.random()*100<difleft) {
							if (cComp-1>=0) {// checks if it can diffuse left
								fm.position=99; 
								cmaxpos=99;
								cminpos=99;
								//System.out.println("Trying left "+j+", "+fm.toString());
								if (!h.comps[cComp-1].fms.contains(fm)) {
									//System.out.println((cComp-1)+" Does not contain "+fm.toString());
									h.comps[cComp-1].fms.add(fm); //moves it to the start of the next compartment
									//moves it to the start of the next compartment
								} else {
									//System.out.println((cComp-1)+" Does contain "+fm.toString());
								} 
								if(!toremove.contains(fm)) {	
									toremove.add(fm);
								}
								//h.comps[cComp].fms.remove(fm); //removes it from the active 
								cComp--;
							} 
						} else {
							fm.position+=2;//bounced so it moves back to position 1
						}
					}
					if (fm.position<cminpos &&cComp==bc) {
						cminpos=fm.position;
						fm.minPos=cminpos;
					}
				}

			}
			//done checking the molecule
			
			if (cComp==bc) { // this means it moved back in to the original compartment
				// no need to remove, so clean list
				if (toremove.contains(fm)) {
					toremove.remove(fm);
//					System.out.println("Cancelling removal of "+h.comps[bc].fms.indexOf(fm));
				// This also means we need to remove it from other compartments
					for (int k=0;k<h.comps.length;k++) { // cannot be done like this due to concurrent modification, thus we need to return a list of objects to remove from all compartments except current one
						toclean.add(fm);
					}
				}
			}
			//System.out.println("maxpos"+ fm.maxPos+", minpos"+fm.minPos);
		}
		toreturn.add(toremove);
		toreturn.add(toclean);
		return toreturn;
	}

}
