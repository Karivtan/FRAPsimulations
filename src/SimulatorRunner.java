import java.io.File;


public class SimulatorRunner {

	public static void main(String[] args) {
		int [] comps = new int[] {5};
		int [] comps2 = new int[] {7,9};
		double [] dirPerc = new double[] {50,50.1,50.2,50.3,50.4,50.5,50.6,50.7,50.8,50.9,51};
		int [][] bleachReg = new int[][] {{0,100},{5,95},{10,90},{15,85},{20,80},{25,75},{30,70},{33,66},{35,65},{40,60},{45,55}};
		//int [][] passChances = new int [][] {{1,1},{2,2},{5,5},{10,10},{25,25},{50,50},{100,100},{1,2},{1,5},{1,10},{1,25},{1,50},{1,100},{2,5},{2,10},{2,25},{2,50},{2,100},{5,10},{5,25},{5,50},{5,100},{10,25},{10,50},{10,100},{25,50},{25,100},{50,100}};
		boolean [] closedL = new boolean [] {false, true, true};
		boolean [] closedR = new boolean [] {false, false, true};
		boolean [] RefillArL = new boolean [] {false, true, true};
		boolean [] RefillArR = new boolean [] {false, false, true};
		int [] lengths = new int[] {10,25,50,100,500};

		FrapSimulationWithInternalDiffusion fs = new FrapSimulationWithInternalDiffusion();
	    //fs.f = new File("E:\\Owncloud\\IBL_Streptomyces_cell_division (Projectfolder)\\Diffusion kinetics\\Simulations\\PassChances\\Detailedv3");
		fs.f = new File("C:/Users/joost/ownCloud - Joost Willemse@universiteitleiden.data.surfsara.nl (2)/IBL_Streptomyces_cell_division (Projectfolder)/Diffusion kinetics/Simulations/BleachRegion");
		for (int a:comps) { // loop through number of compartments 2
			for (int [] b:bleachReg) { //loop through number of percentages 28
				//for (int c=1;c<a;c++) { // loop through different bleach compartments 3+5=8

					fs.title ="FRAP_AbsMols_C"+a+"_Bleach_S"+b[0]+"_E"+b[1]+"_BC"+a/2;
				    fs.nMols = 4000;
				    fs.nSubComp = 3;
				    fs.CompLength = 100;
				    fs.startB=b[0];
				    fs.endB=b[1];
				    fs.nBleached = 100;
				    fs.nItterations = 200;
				    fs.nIttD = 100;
				    
				    //variable in loops
				    fs.nComps = a;
				    fs.diffD = 50;
				    fs.difleft = 50;
				    fs.difright = 50;
				    fs.bleachedCompartment = a/2;
				    
				    // Diffusion and Boundary Logic
				    fs.diffBleach = true;
				    fs.refillL = true;
				    fs.refillR = true;
				    fs.refillLL = false;
				    fs.refillRR = false;
				    fs.refillLF = false;
				    fs.refillRF = false;
				    fs.moveL = false;
				    fs.moveR = false;
				    
				    // Object and Array Initialization
				    fs.posHist = new int[fs.nComps][fs.CompLength];
				    fs.LR = new int[2]; // [0] = Left, [1] = Right
				    
				    fs.createHyphae(fs.nComps,fs.nMols);//this is still fine
					/*+1 on bleachedcompartments needed since we need to bleach one compartment further than actually indicated because of the 2 additional compartments*/
					System.out.println("Starting");
					fs.Bleach(fs.bleachedCompartment+1 , fs.nBleached,fs.startB,fs.endB, fs.nIttD, fs.diffBleach, fs.difleft, fs.difright);// we still need to bleach, however we need to consider the position
					System.out.println("Done");
				//}
			}
		}
/*		for (int a:comps) { // loop through number of compartments 2
			for (int[] c:passChances) {//loop through passChances 28

			}
		}*/

		
		
	}

}
