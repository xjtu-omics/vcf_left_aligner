package vcf_left_aligner;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Aligns indels in a VCF file to the leftmost position (not all SV-callers do so)
 * 
 * @author Eric-Wubbo Lameijer, Xi'an Jiaotong University, eric_wubbo@hotmail.com
 *
 */
public class VcfLeftAligner {
	
	/**
	 * 
	 * @param args 
	 * 		the command-line arguments, which should be the name of the input vcf file,
	 * 		the reference fasta file, and the output vcf-file
	 */
	public static void main(String[] args) {
		if (args.length != 3) {
			System.out.println("VcfLeftAligner.main error: three arguments are required, " + 
					"the name of the input vcf file, the name of the reference (fasta) file, and " + 
					"the name of the output vcf file that is to be created.");
			System.exit(-1);
		}
		String nameOfInputVcf = args[0];
		String nameOfReference = args[1];
		String nameOfOutputVcf = args[2];
		
		try ( BufferedReader inputVcf = new BufferedReader(new FileReader(nameOfInputVcf));
				BufferedWriter outputVcf = new BufferedWriter(new FileWriter(nameOfOutputVcf));){
			
			BufferedReader referenceGenome = new BufferedReader(new FileReader(nameOfReference));
			
		    String line;
		    
		    String nameOfCurrentChromosome = "";
		    String sequenceOfCurrentChromosome = "";
		    while ((line = inputVcf.readLine()) != null)
		    {
		    	//System.out.println(line);
		    	if (line.startsWith("#")) {
		    		// copy comment lines directly to the output
		    		outputVcf.write(line);
		    		outputVcf.newLine();
		    	} else {
		    		// apparently, we've reached the first event
		    		Event event = new Event(line);
		    		String chromosomeOfEvent = event.getChromosome();
		    		if (!chromosomeOfEvent.equals(nameOfCurrentChromosome)) {
		    			sequenceOfCurrentChromosome = load(chromosomeOfEvent, referenceGenome);
		    			nameOfCurrentChromosome = chromosomeOfEvent;
		    			if (sequenceOfCurrentChromosome.length() == 0) {
		    				referenceGenome.close();
		    				referenceGenome = new BufferedReader(new FileReader(nameOfReference));
		    				sequenceOfCurrentChromosome = load(chromosomeOfEvent, referenceGenome);
		    			}
		    		}
		    		//System.out.println("Ready with chromosome loading");
		    		event.leftAlign(sequenceOfCurrentChromosome);
		    		outputVcf.write(event.asLine());
		    		outputVcf.newLine();
		    	}
		    }
		    
		  }
		  catch (Exception e)
		  {
		    System.err.format("Exception occurred trying to read '%s' or '%s'.", nameOfInputVcf, nameOfReference);
		    e.printStackTrace();
		  }
	}

	/**
	 * Loads a chromosome with the specified name into memory.
	 * @param nameOfChromosome
	 * 		The name of the chromosome which is sought
	 * @param genomeFile
	 * 		The file which houses the reference genome
	 * @return
	 * 		The sequence of the sought chromosome.
	 */
	private static String load(String nameOfChromosome, BufferedReader genomeFile) {
		StringBuilder chromosomeSequence = new StringBuilder();
		System.out.println("Loading chromosome " + nameOfChromosome);
		String soughtStartSequence = ">" + nameOfChromosome;
		int startSequenceLength = soughtStartSequence.length();
		String line;
		int counter = 1;
		try {
		  boolean copyLines = false;
		  while ((line = genomeFile.readLine()) != null) {
			 if (line.startsWith(">")) {
				 System.out.println(line);
				 // two cases: either turn copying on, or turn it off.
				 if (line.startsWith(soughtStartSequence) && Character.isWhitespace(line.charAt(startSequenceLength))) {
					 copyLines = true;
					 chromosomeSequence.append("N"); // for easy conversion of Java coordinates to 1-based coordinates of most
					 // references
				 } else { // >otherChromosomeName
					 if (copyLines) {
						 genomeFile.reset();
						 return chromosomeSequence.toString(); // done
					 }
				 }
			 } else {
				 // is just a normal chromosome sequence
				 if (copyLines) {
					 genomeFile.mark(1000);
					 counter++;
					 if (counter % 10000 == 0) {
						 System.out.println(counter);
					 }
					 
					 chromosomeSequence.append(line);
				 }
			 }			
		 }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//Utilities.require(chromosomeSequence.length() > 0 , "VcfLeftAligner.load error: sought chromosome " + nameOfChromosome + " not found.");
		return chromosomeSequence.toString();
	}

}
