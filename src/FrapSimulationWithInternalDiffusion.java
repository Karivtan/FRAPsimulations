import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
//import java.util.Arrays;
import java.util.Random;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JProgressBar;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.time.LocalTime;
import ij.measure.CurveFitter;
/* Done rework frapped molecules so it contains all the molecules that have been in the bleached area
 * TODO implement curve fitting for bleached compartment
 * TODO bleach length setting (line ~540)
 */

public class FrapSimulationWithInternalDiffusion {

	Hyphae h;
	int startB, endB, bleachedCompartment, nMols, nComps, nBleached,nItterations, nIttD, lostL, lostR, nSubComp, CompLength, lambdaL, lambdaR;
	double diffD, difright, difleft;
	boolean diffBleach, refillL, refillR, moveL, moveR, refillLL, refillRR, refillLF, refillRF;
	String title;
	File f;
	int [][] posHist;
	int [] LR = new int[2];
	double [][] fitData;
	double [][] fitSubData;
	public FrapSimulationWithInternalDiffusion() {
		
	};
	
	public FrapSimulationWithInternalDiffusion(
		    String title, File f, int nMols, int nComps, int nSubComp, 
		    int CompLength, int bleachedCompartment, int nBleached, 
		    int startB, int endB, int nItterations, int nIttD, 
		    double diffD, double difleft, double difright, 
		    boolean diffBleach, boolean refillL, boolean refillR, 
		    boolean moveL, boolean moveR, boolean refillLL, boolean refillRR, boolean refillLF, boolean refillRF
		) {
		    // Basic Parameter Initialization
		    this.title = title;
		    this.f = f;
		    this.nMols = nMols;
		    this.nComps = nComps;
		    this.nSubComp = nSubComp;
		    this.CompLength = CompLength;
		    this.bleachedCompartment = bleachedCompartment;
		    this.nBleached = nBleached;
		    this.nItterations = nItterations;
		    this.nIttD = nIttD;
		    
		    // Diffusion and Boundary Logic
		    this.diffD = diffD;
		    this.difleft = difleft;
		    this.difright = difright;
		    this.diffBleach = diffBleach;
		    this.refillL = refillL;
		    this.refillR = refillR;
		    this.moveL = moveL;
		    this.moveR = moveR;
		    
		    // Object and Array Initialization
		    this.posHist = new int[nComps][this.CompLength];
		    this.LR = new int[2]; // [0] = Left, [1] = Right
		    
		    // Create the Hyphae structure
		    // (Assuming Hyphae constructor takes nComps, nMols, and CompLength)
		    this.h = new Hyphae(this.nComps, this.nMols, this.CompLength);
		}
	
	public static void main(String[] args) {
		// eventueel grootte/lengte hyphae
		// deeltjes formaat
		// andere deeltjes in hyphae
		// first create a hyphae
		FrapSimulationWithInternalDiffusion fs = new FrapSimulationWithInternalDiffusion();
		for (int i=0;i<fs.nComps;i++) {
			for (int j=0;j<fs.CompLength; j++) {
				fs.posHist[i][j]=0;
			}
		}
		JTextField titleF = new JTextField("Frap3C_OR");
		JTextField nMolsF = new JTextField("4000");
		JTextField nCompsF = new JTextField("3");
		JTextField CL = new JTextField("100");
		JTextField startB = new JTextField("0");
		JTextField diffDF = new JTextField("50");
		JTextField endB = new JTextField("100");
		JTextField BCF = new JTextField("1");
		JTextField nBleachedF = new JTextField("100");
		JTextField nIttF = new JTextField("200");
		JTextField nIttD = new JTextField("100");
		JTextField difLeftF = new JTextField("50");
		JTextField difRightF = new JTextField("50");
		JTextField nSubCompF = new JTextField("3");
		JCheckBox diffB= new JCheckBox("Simmulate diffusion during bleaching (selected), or instantaneous bleach",true);
		JCheckBox refL= new JCheckBox("Refill molecules lost on the right towards the left",true);
		JCheckBox refR= new JCheckBox("Refill molecules lost on the left towards the right",true);
		JCheckBox refLL= new JCheckBox("Refill molecules lost on the left back to the left",false);
		JCheckBox refRR= new JCheckBox("Refill molecules lost on the right back to the right",false);
		JCheckBox refLF= new JCheckBox("Refill molecules lost on the left back to the left",false);
		JCheckBox refRF= new JCheckBox("Refill molecules lost on the right back to the right",false);
		
		JCheckBox moveL= new JCheckBox("Hyphae is closed on the left side",false);
		JCheckBox moveR= new JCheckBox("Hyphae is closed on the right side",false);
		JFileChooser ch = new JFileChooser();
		ch.setDialogTitle("Choose where to store the data");
		ch.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		
    	File cDir=new File("E:\\Owncloud\\IBL_Streptomyces_cell_division (Projectfolder)\\Diffusion kinetics\\Simulations");
    	// Use a simple check and invokeLater to prevent the RowSorter crash
    	if (cDir.exists()) {
    	    SwingUtilities.invokeLater(() -> ch.setCurrentDirectory(cDir));
    	}
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
				"What is your compartment length", CL,
				"Start position of bleach in the compartment (0-compartment length)", startB,
				"End position of bleach in the compartment (0-compartment length)", endB,
				"Chance of molecules diffusing to the left", diffDF,
				"Percentage of molecules in the compartment are bleached", nBleachedF,
				"Number of measurement itterations", nIttF,
				"Per how many itterations a measurement is performed", nIttD,
				"Chance to diffuse to left compartment", difLeftF,
				"Chance to diffuse to right compartment", difRightF,
				diffB,
				refL,
				refR,
				refLL,
				refRR,
				refLF,
				refRF,
				moveL,
				moveR,
		};
		int option =JOptionPane.showConfirmDialog(null, message, "Enter FRAP settings", JOptionPane.OK_CANCEL_OPTION);
		if (option== JOptionPane.OK_OPTION) {
			fs.title=titleF.getText();
			fs.nMols=Integer.parseInt(nMolsF.getText());
			fs.nComps=Integer.parseInt(nCompsF.getText());
			fs.CompLength=Integer.parseInt(CL.getText());
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
			fs.refillLL=refLL.isSelected();
			fs.refillRR=refRR.isSelected();
			fs.refillLF=refLF.isSelected();
			fs.refillRF=refRF.isSelected();

			fs.moveL=moveL.isSelected();
			fs.moveR=moveR.isSelected();
			fs.nSubComp=Integer.parseInt(nSubCompF.getText());
			fs.diffD=Double.parseDouble(diffDF.getText());
			fs.posHist=new int[fs.nComps][fs.CompLength];
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
		h = new Hyphae(ncomp,nmol,CompLength); // creates new hyphae with 2 empty compartments on each side
	}
	
	public void Bleach (int BleachedCompartment, int nBleaches, int startB, int endB, int nIttD, boolean diffB, double difleft, double difright) { // this does the actual experiment
		Compartment bc = h.comps[BleachedCompartment]; // select the bleached compartment
		ArrayList<FluorescentMolecule> CompartmentMolecules = bc.fms; // get the molecules in there
		int nToBleach=nMols*nBleaches/100; // needed to stop the bleach when we also diffuse, calculates how many molecules to bleach in total
			
		if (!diffB) { // for instantaneous bleach
			ArrayList<FluorescentMolecule> ListToBleach = new ArrayList<FluorescentMolecule>();
			for (FluorescentMolecule i: CompartmentMolecules) { // go through all molecules in the compartment
				if (i.position>startB && i.position<endB ) { // checks where the molecule is and if it is in the bleaching zone
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
				pw.println("Compartment length,"+ CompLength);
				pw.println("Number of subcompartments for analysis,"+nSubComp);
				pw.println("Which compartment should be bleached (count starts at 0),"+(BleachedCompartment-1));
				pw.println("Start position of bleach in the compartment (0-100),"+startB);
				pw.println("End position of bleach in the compartment (0-100),"+endB);
				pw.println("Percentage of molecules in the compartment are bleached,"+nBleached);
				pw.println("Number of measurement itterations,"+nItterations);
				pw.println("Number of diffusion itterations per measurement itteration,"+nIttD);
				pw.println("Chance of molecules diffusing to the left,"+diffD);
				pw.println("Chance to diffuse to left compartment,"+difleft);
				pw.println("Chance to diffuse to right compartment,"+difright);
				pw.println("Refill molecules lost on the right from the left,"+refillL);
				pw.println("Refill molecules lost on the left from the right,"+refillR);
				pw.println("Refill molecules lost on the right on the right ,"+refillRR);
				pw.println("Refill molecules lost on the left on the left,"+refillLL);
				pw.println("Refill molecules lost on the right back to nMols,"+refillLF);
				pw.println("Refill molecules lost on the left back to nMols,"+refillRF);
				pw.println("Hyphae is closed on the left side,"+moveL);
				pw.println("Hyphae is closed on the right side,"+moveR);
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
				
				for (int i=0;i<nItterations;i++) { // this does the actual recovery
					pb.setValue(i+1);
					diffuseCycle(); 
					if (nItterations%nIttD==0) { // makes sure the output is only written ever so often
						pw.print((i+1)+",");
						pw2.print((i+1)+",");
					}
					MoleculeCounter(pw,i);
					SplitCompartmentCounter(pw2,nSubComp, i);
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
			//ArrayList<FluorescentMolecule> toremove=diffuseMolecules(CompartmentMolecules, nIttD, BleachedCompartment, difleft, difright); // like this we only diffuse the molecules in the bleached compartment, but that is not correct we need to create a bleachCycle
			bleachCycle();
			
			//now we know where all molecules have moved around in the compartment
			//Now we have the cleaned list without the ones that left the compartment
			//Now we can bleach the molecules in the current compartment

			ArrayList<FluorescentMolecule> toBleach = getBleachables(BleachedCompartment,startB, endB);
			//Make sure bleaching will not try to bleach more molecules then are currenty possible
			if (nToBleach>toBleach.size()) { // find all molecules that have been in the bleaching area within nIttD
				nToBleach=toBleach.size();
			}
			// since start positions are random we can just bleach the first n ones. This changed when incuding diffusing from neighbour compartments so random has to be integrated
			Random rand = new Random();
			for (FluorescentMolecule cm:toBleach) {
				if (rand.nextDouble()*100<nBleached) {
					cm.bleached=1;
				}
			}
			//then we need to check the molecules that left the compartment and see if they are bleached
			//For now not implemented since most molecules stay within the compartment

			//Now we need to diffuse the molecules for nIttF*nIttD
			//After every nIttF we need to count the molecules per compartment
			File fs = new File(f+"/"+title+".csv");
			File fs2 = new File(f+"/"+title+"Split.csv");
			File fs3 = new File(f+"/"+title+"Histo.csv");
			int counter=1; 
			while (fs.exists()) {
				fs = new File(f+"/"+title+counter+".csv");
				fs2 = new File(f+"/"+title+counter+"Split.csv");
				fs3 = new File(f+"/"+title+counter+"Histo.csv");
				counter++;
			}
			try {
				PrintWriter pw = new PrintWriter(fs);
				PrintWriter pw2 = new PrintWriter(fs2);
				//PrintWriter pw3 = new PrintWriter(fs3);
				pw.println("sep=,");
				pw2.println("sep=,");
				pw.println("Number of molecules per compartment,"+nMols);
				pw.println("Number of compartments,"+nComps);
				pw.println("Compartment length,"+ CompLength);
				pw.println("Number of subcompartments for analysis,"+nSubComp);
				pw.println("Which compartment should be bleached (count starts at 0),"+bleachedCompartment);
				pw.println("Start position of bleach in the compartment (0-100),"+startB);
				pw.println("End position of bleach in the compartment (0-100),"+endB);
				pw.println("Percentage of molecules in the compartment are bleached,"+nBleached);
				pw.println("Chance of molecules diffusing to the left,"+diffD);
				pw.println("Number of measurement itterations,"+nItterations);
				pw.println("Number of diffusion itterations per measurement itteration,"+nIttD);
				
				pw.println("Chance to diffuse left,"+difleft);
				pw.println("Chance to diffuse right,"+difright);
				pw.println("Refill molecules lost on the right from the left,"+refillL);
				pw.println("Refill molecules lost on the left from the right,"+refillR);
				pw.println("Refill molecules lost on the right on the right ,"+refillRR);
				pw.println("Refill molecules lost on the left on the left,"+refillLL);
				pw.println("Refill molecules lost on the right back to nMols,"+refillLF);
				pw.println("Refill molecules lost on the left back to nMols,"+refillRF);
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
				pw.print(0+",");
				
				//TODO
				// here we can collect the data that needs to go into the curveFitter later on!
				fitData = new double [nComps][nItterations+1];
				MoleculeCounter(pw, 0);
				
				fitSubData = new double [nSubComp*nComps+1][nItterations+1];
				pw2.print(0+",");
				SplitCompartmentCounter(pw2,nSubComp,0);
				//getDetailedHistogram(pw3);
				for (int i=0;i<nItterations;i++) { // this is done every itteration, and we need the diffuse cycle in there
					pb.setValue(i+1);
					// this checks the internal distribution within a compartment
					
					//System.out.println(posHist.toString());
					diffuseCycle(); 
					pw.print((i+1)+",");
					MoleculeCounter(pw,i+1);
					pw2.print((i+1)+",");
					SplitCompartmentCounter(pw2,nSubComp,i+1);
					
					// For histogram
					//getDetailedHistogram(pw3);
				}
				// now we have all the data to fit as well //TODO
				double [][] fits = fitCurves(fitData);
				double [][] subfits = fitSubCurves(fitSubData);
				// now we need to write the data into the files
				for (int i=0;i<5;i++) {
					switch (i) {
					case 0:
						pw.print("RSQ,");
						pw2.print("RSQ,");
						break;
					case 1:
						pw.print("A,");
						pw2.print("A,");
						break;
					case 2:
						pw.print("B,");
						pw2.print("B,");
						break;
					case 3:
						pw.print("T1/2,");
						pw2.print("T1/2,");
						break;
					case 4:
						pw.print("C,");
						pw2.print("C,");
						break;
					}
					
					for (int j=0;j<fits[0].length;j++) {
						pw.print(fits[i][j]+",");
					}
					for (int j=0;j<subfits[0].length;j++) {
						pw2.print(subfits[i][j]+",");
					}
					pw.println();
					pw2.println();
				}
				pw.println("y="+nMols+"*c-a*exp(-b*x)");
				pw2.println("y="+nMols+"*c-a*exp(-b*x)");
				pw.close();
				pw2.close();
				//pw3.close();
				jf.setVisible(false);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
	
	}
	
	public double [][] fitCurves(double [][] fitDats){
		double [][] fits = new double [5][fitDats.length];
		double [] xs= new double[fitDats[0].length];
		for (int i=0;i<fitDats[0].length;i++) {
			xs[i]=i;
		}
		for (int i=0; i<fitData.length;i++) {
			CurveFitter cf = new CurveFitter(xs,fitDats[i]);
			cf.doCustomFit("y="+nMols+"*c-a*exp(-b*x)", new double [] {nMols,1,1} , false);
			double [] fit = cf.getParams();
			double rsq = cf.getRSquared();
			//System.out.println(fit[0]+","+fit[1]+","+ fit.length);
			fits[0][i]=rsq;
			fits[1][i]=fit[0];
			fits[2][i]=fit[1];
			fits[3][i]=Math.log(2)/fit[1];
			fits[4][i]=fit[2];
		}
		return fits;
	}
	
	
	public double [][] fitSubCurves(double [][] fitDats){
		double [][] fits = new double [5][fitDats.length];
		double [] xs= new double[fitDats[0].length];
		for (int i=0;i<fitDats[0].length;i++) {
			xs[i]=i;
		}
		for (int i=0; i<fitSubData.length;i++) {
			CurveFitter cf = new CurveFitter(xs,fitDats[i]);
			cf.doCustomFit("y="+nMols+"*c-a*exp(-b*x)", new double [] {nMols,1,1} , false);
			double [] fit = cf.getParams();
			double rsq = cf.getRSquared();
			//System.out.println(fit[0]+","+fit[1]+","+ fit.length);
			fits[0][i]=rsq;
			fits[1][i]=fit[0];
			fits[2][i]=fit[1];
			fits[3][i]=Math.log(2)/fit[1];
			fits[4][i]=fit[2];
		}
		return fits;
	}
	
	public void getDetailedHistogram(PrintWriter pw3) {
		//File fs3 = new File(f+"/"+title+"Histo.csv");
		try {
			//PrintWriter pw3 = new PrintWriter(fs3);
			for (int j=0;j<nComps;j++) {
				for (int k=0;k<CompLength;k++) {
					posHist[j][k]=0;
				}
			}
			for (int k=1;k<h.comps.length-1;k++) {
				ArrayList<FluorescentMolecule> fms = h.comps[k].fms;
				for (FluorescentMolecule counting:fms) {
					posHist[k-1][counting.position]+=1;
				}
			}
			for (int k=0;k<posHist.length;k++) {
				for (int l=0;l<posHist[0].length;l++) {
					pw3.print(posHist[k][l]+",");
				}
				
			}
			pw3.println();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public void SplitCompartmentCounter(PrintWriter pw, int nparts, int cItt) {
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
					nmols[counting.position/(CompLength/nparts)+(i-1)*nparts]+=1;
					// counting.position is between 0 and 99, with 5C it gets divided by 20
					
				}
			}
			//end up with a cleaned compartment list of molecules
		}
		for (int i=0;i<nmols.length;i++) { // initialize on 0
			pw.print(nmols[i]+", ");
			fitSubData[i][cItt]=nmols[i];
		}
		pw.println();
	}
	

	public void MoleculeCounter(PrintWriter pw, int crow) {
		for (int i=1;i<h.comps.length-1;i++) {
			ArrayList<FluorescentMolecule> cm=h.comps[i].fms;
			int count=0;
			for (FluorescentMolecule counting:cm) {
				if (counting.bleached==0) {
					count++;
				}
			}
			pw.print(count+", ");
			fitData[i-1][crow]=count;
			//end up with a cleaned compartment list of molecules
		}
		for (int i=1;i<h.comps.length-1;i++) {
			pw.print(h.comps[i].fms.size()+", "); // reports the number of molecules
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
				cm.remove(deleting); // remove all overflown molecules
			}
		}
		//System.out.println(LR[0]+", "+LR[1]);
		lostL=h.comps[0].fms.size(); // 
		lostR=h.comps[h.comps.length-1].fms.size();
		h.comps[0].fms=new ArrayList<FluorescentMolecule>(); // resets the diffused out molecules
		h.comps[h.comps.length-1].fms=new ArrayList<FluorescentMolecule>(); // resets the diffused out molecules right
		
		for (int i=1;i<h.comps.length-1;i++) { // resets the diffused boolean
			ArrayList<FluorescentMolecule> cm=h.comps[i].fms;
			for (FluorescentMolecule fm:cm) {
				fm.diffused=false;
			}
		}
		Random rand = new Random();
		if (refillL) { // because of refilling we create a flux. Thus refilling to what is lost on the right side
			for (int i = 0; i < lostR; i++) {
				double displacement = Math.abs(rand.nextGaussian()) * Math.sqrt(nIttD);
				int pos = (int) Math.min(CompLength - 1, Math.max(0, displacement));
			    h.comps[1].fms.add(new FluorescentMolecule(1, CompLength,pos));
			}
			
		}
		if (refillLL) { // because of refilling we create a flux. Thus refilling what was lost on the left side
			for (int i = 0; i < lostL; i++) {
			    h.comps[1].fms.add(new FluorescentMolecule(1, CompLength));
			}
			
		}
		if (refillR) {
			
			for (int i = 0; i < lostL; i++) {
				double displacement = Math.abs(rand.nextGaussian()) * Math.sqrt(nIttD);
		        int pos = (int) Math.min(CompLength - 1, Math.max(0, (CompLength - 1) - displacement));
			    h.comps[h.comps.length-2].fms.add(new FluorescentMolecule(h.comps.length-2, CompLength,pos));
			}
		}
		if (refillRR) {
			for (int i = 0; i < lostR; i++) {
			    h.comps[h.comps.length-2].fms.add(new FluorescentMolecule(h.comps.length-2, CompLength));
			}
		}
		if (refillLF) { // because of refilling we create a flux. Thus refilling to the original number of molecules
			for (int i = 0; i < nMols-h.comps[1].fms.size(); i++) {
				double displacement = Math.abs(rand.nextGaussian()) * Math.sqrt(nIttD);
				int pos = (int) Math.min(CompLength - 1, Math.max(0, displacement));
			    h.comps[1].fms.add(new FluorescentMolecule(1, CompLength,pos));
			}
		}
		if (refillRF) {
			for (int i = 0; i < nMols-h.comps[h.comps.length-2].fms.size(); i++) {
				double displacement = Math.abs(rand.nextGaussian()) * Math.sqrt(nIttD);
		        int pos = (int) Math.min(CompLength - 1, Math.max(0, (CompLength - 1) - displacement));
			    h.comps[h.comps.length-2].fms.add(new FluorescentMolecule(h.comps.length-2, CompLength,pos));
			}
		}
	}
	
	public void bleachCycle(){ // here we need to do the diffusion, and add new molecules when running out on the left and right
		lostL=0;
		lostR=0;
		for (int i=1;i<h.comps.length-1;i++) { // calculates diffusion from compartment 1 to n-1
			ArrayList<FluorescentMolecule> cm=h.comps[i].fms;
			ArrayList<FluorescentMolecule> toChange;
			if (moveL&&i==1) { // here we need to change to bleachMolecules, can be a different number of cycles
				toChange = bleachMolecules(cm,nIttD,i,0,difright);
			} else if (moveR &&i==h.comps.length-2) {
				toChange = bleachMolecules(cm,nIttD,i,difleft,0);
			} else {
				toChange = bleachMolecules(cm,nIttD,i,difleft,difright);
			}
			for (FluorescentMolecule deleting:toChange) {
				cm.remove(deleting); // remove all overflown molecules
			}
		}
		//System.out.println(LR[0]+", "+LR[1]);
		lostL=h.comps[0].fms.size(); // 
		lostR=h.comps[h.comps.length-1].fms.size();
		h.comps[0].fms=new ArrayList<FluorescentMolecule>(); // resets the diffused out molecules
		h.comps[h.comps.length-1].fms=new ArrayList<FluorescentMolecule>(); // resets the diffused out molecules right
		
		for (int i=1;i<h.comps.length-1;i++) { // resets the diffused boolean
			ArrayList<FluorescentMolecule> cm=h.comps[i].fms;
			for (FluorescentMolecule fm:cm) {
				fm.diffused=false;
			}
		}
		Random rand = new Random();
		if (refillL) { // because of refilling we create a flux. Thus refilling to what is lost on the right side
			for (int i = 0; i < lostR; i++) {
				double displacement = Math.abs(rand.nextGaussian()) * Math.sqrt(nIttD);
				int pos = (int) Math.min(CompLength - 1, Math.max(0, displacement));
			    h.comps[1].fms.add(new FluorescentMolecule(1, CompLength,pos));
			}
			
		}
		if (refillLL) { // because of refilling we create a flux. Thus refilling what was lost on the left side
			for (int i = 0; i < lostL; i++) {
			    h.comps[1].fms.add(new FluorescentMolecule(1, CompLength));
			}
			
		}
		if (refillR) {
			
			for (int i = 0; i < lostL; i++) {
				double displacement = Math.abs(rand.nextGaussian()) * Math.sqrt(nIttD);
		        int pos = (int) Math.min(CompLength - 1, Math.max(0, (CompLength - 1) - displacement));
			    h.comps[h.comps.length-2].fms.add(new FluorescentMolecule(h.comps.length-2, CompLength,pos));
			}
		}
		if (refillRR) {
			for (int i = 0; i < lostR; i++) {
			    h.comps[h.comps.length-2].fms.add(new FluorescentMolecule(h.comps.length-2, CompLength));
			}
		}
		if (refillLF) { // because of refilling we create a flux. Thus refilling to the original number of molecules
			for (int i = 0; i < nMols-h.comps[1].fms.size(); i++) {
				double displacement = Math.abs(rand.nextGaussian()) * Math.sqrt(nIttD);
				int pos = (int) Math.min(CompLength - 1, Math.max(0, displacement));
			    h.comps[1].fms.add(new FluorescentMolecule(1, CompLength,pos));
			}
		}
		if (refillRF) {
			for (int i = 0; i < nMols-h.comps[h.comps.length-2].fms.size(); i++) {
				double displacement = Math.abs(rand.nextGaussian()) * Math.sqrt(nIttD);
		        int pos = (int) Math.min(CompLength - 1, Math.max(0, (CompLength - 1) - displacement));
			    h.comps[h.comps.length-2].fms.add(new FluorescentMolecule(h.comps.length-2, CompLength,pos));
			}
		}
	}
	
	
	int samplePoisson(double lambda) {
		Random rand = new Random();
	    double L = Math.exp(-lambda);
	    int k = 0;
	    double p = 1.0;
	    do {
	        k++;
	        p *= rand.nextDouble();
	    } while (p > L);
	    return k - 1;
	}
	
	public ArrayList<FluorescentMolecule> getBleachables(int bleachedCompartment, int startB, int endB){
		ArrayList<FluorescentMolecule> toBleach =new ArrayList<FluorescentMolecule>(); // needed to see which molecules to bleach
		
		for (int i=1;i<h.comps.length-1;i++) { // check all compartments
			ArrayList<FluorescentMolecule> cm=h.comps[i].fms;
			for (FluorescentMolecule fm : cm) { // check all molecules
				for (int [] pos:fm.positionlist) {// check all positions that have been occupied during the bleach
					if (pos[0]==bleachedCompartment) {
						if (pos[1]<=endB) {
							if (pos[1]>=startB) {
								toBleach.add(fm);
								break; // stop checking this molecule, it has been in the bleaching area so it gets added to the list
							}
						} else if (pos[1]>=startB) {
							if (pos[1]<=endB) {
								toBleach.add(fm);
								break; // stop checking this molecule, it has been in the bleaching area so it gets added to the list
							}
						}
					}
					
				}
			}
		}
		
		return toBleach;
	}
	
	public ArrayList<FluorescentMolecule> bleachMolecules(ArrayList<FluorescentMolecule> cm, int nIttD, int bc, double difleft, double difright) {
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
								fm.position=CompLength-1; 
								cmaxpos=CompLength-1;
								cminpos=CompLength-1;
								fm.compartment--;
								// apparently now we need to stop the for loop
							} else {
								fm.position=0;//bounced so it stays in position
							} 
						}
						// 
						
						if (fm.position<cminpos &&fm.compartment==bc) {
							cminpos=fm.position;
							fm.minPos=cminpos;
						}
						
					} else { //move right
						fm.position++;
						if (fm.position>=CompLength) {
							//check if it gets moved right or whether is bounces of the wall
							if (Math.random()*100<difright) { // it diffuses right over the cell wall
								fm.position=0;
								cmaxpos=0;
								cminpos=0;
								fm.compartment++;
							} else { // it bounced
								fm.position=CompLength-1;//bounced so it stays at the end
							} 
						}
						if (fm.position>cmaxpos && fm.compartment==bc) { 
							cmaxpos=fm.position;
							fm.maxPos=cmaxpos;						
						}
					}
					//here we add the position to the positionlist in the fm
					fm.positionlist.add(new int[] {fm.compartment, fm.position});
				}
				//done checking the molecule
				// the compartment position was added to the molecule, so if the current compartment position == bc
				//if (cComp==bc) { // this means it moved back or stayed in to the original compartment nothing needs to be done
				if (fm.compartment!=bc &&fm.compartment>=0 && fm.compartment<=nComps+1) {
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
								fm.position=CompLength-1; 
								cmaxpos=CompLength-1;
								cminpos=CompLength-1;
								fm.compartment--;
								// apparently now we need to stop the for loop
							} else {
								fm.position=0;//bounced so it stays in position
							} 
						}  
						if (fm.position<cminpos &&fm.compartment==bc) {
							cminpos=fm.position;
							fm.minPos=cminpos;
						}
						
					} else { //move right
						fm.position++;
						if (fm.position>=CompLength) {
							//check if it gets moved right or whether is bounces of the wall
							if (Math.random()*100<difright) { // it diffuses right over the cell wall
								fm.position=0;
								cmaxpos=0;
								cminpos=0;
								fm.compartment++;
							} else { // it bounced
								fm.position=CompLength-1;//bounced so it stays at the end
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
				if (fm.compartment!=bc &&fm.compartment>=0 && fm.compartment<=nComps+1) {
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
