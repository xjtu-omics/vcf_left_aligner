package vcf_left_aligner;
/**
 * 'Event' represents a genetic event (so insertion or deletion).
 * 
 * @author Eric-Wubbo Lameijer, Xi'an Jiaotong University, eric_wubbo@hotmail.com
 *
 */
public class Event {
	
	String[] allFields; // saves all fields of the VCF line, handy for output
	String alternativeAllele; // the alt allele/alternative allele: the sequence of DNA in the individual that is other than
		// expected by consulting the reference chromosome.
	String chromosomeName; // the name of the chromosome in which this event takes place
	int position; // position of the event
	String referenceAllele; // the expected sequence at this place in this chromosome, as indicated in the reference genome
	boolean isPacBio; // PacBio VCF files have a different format, like T <DEL> instead of TTCG T
	
	/**
	 * Event constructor; creates an event out of a line of a VCF file.
	 * 
	 * @param vcfLine
	 * 		The line of the VCF file to be turned into an event.
	 */
	public Event(String vcfLine) {
		allFields = vcfLine.split("\\t");
		chromosomeName = allFields[0];
		//System.out.println("Chromosome name: " + chromosomeName);
		position = Integer.parseInt(allFields[1]);
		// id = vcfDataItems[2];
		referenceAllele = allFields[3];
		alternativeAllele = allFields[4];
		isPacBio =  (alternativeAllele.equals("<INS>") || alternativeAllele.equals("DEL"));
		if (isPacBio) {
			if (alternativeAllele.equals("<INS>")) {
				String info = allFields[7];
				int sequencePosition = info.indexOf("SEQ=") + 4;
				alternativeAllele = "";
				char base = info.charAt(sequencePosition);
				while (isValidDnaBase(base)) {
					alternativeAllele += Character.toUpperCase(base);
					++sequencePosition;
					base = info.charAt(sequencePosition);
				}
			} else if (alternativeAllele.equals("<DEL>")) {
				alternativeAllele = referenceAllele;
				String info = allFields[7];
				int sequencePosition = info.indexOf("SEQ=") + 4;
				referenceAllele = "";
				char base = info.charAt(sequencePosition);
				while (isValidDnaBase(base)) {
					referenceAllele += Character.toUpperCase(base);
					++sequencePosition;
					base = info.charAt(sequencePosition);
				}
				
			} else {
				Utilities.require(false, "Event constructor error: event type " + alternativeAllele + " is unknown.");
			}
		}
		
	}

	/**
	 * Is this a valid base of DNA?
	 * 
	 * @param base the base (or character) to be checked
	 * @return whether the base is a valid DNA base
	 */
	private boolean isValidDnaBase(char base) {
		char baseInUpperCase = Character.toUpperCase(base);
		return (baseInUpperCase == 'A' || baseInUpperCase == 'C' || baseInUpperCase == 'G' || baseInUpperCase == 'T');
	}

	/**
	 * Returns the name of the chromosome in which this event takes place.
	 * 
	 * @return the name of the chromosome in which this event takes place.
	 */
	public String getChromosome() {
		return chromosomeName;
	}
	
	/**
	 * Does this event have multiple alternative alleles?
	 * 
	 * @return whether the event has multiple alternative alleles.
	 */
	private boolean hasMultipleAltAlleles() {
		return alternativeAllele.contains(",");
	}
	
	/**
	 * Returns whether the event is a deletion.
	 * 
	 * @return whether the event is a deletion
	 */
	private boolean isDeletion() {
		return (referenceAllele.length() > 1 && alternativeAllele.length() == 1);
	}
	
	/**
	 * Returns whether the event is an insertion.
	 * 
	 * @return whether the event is an insertion
	 */
	private boolean isInsertion() {
		return (referenceAllele.length() == 1 && alternativeAllele.length() > 1 && !hasMultipleAltAlleles());
	}

	/**
	 * Left-aligns the event
	 * 
	 * @param sequenceOfCurrentChromosome
	 */
	public void leftAlign(String sequenceOfCurrentChromosome) {
		if (isInsertion()) {
			Utilities.require(referenceAllele.charAt(0) == sequenceOfCurrentChromosome.charAt(position), 
					"Event.leftAlign error: reference chromosome and reference sequence don't seem to match.");
			char referenceBase = referenceAllele.charAt(0);
			while (alternativeAllele.charAt(alternativeAllele.length() - 1) == referenceBase && position > 1) {
				System.out.println("shifting " + chromosomeName + position);
				--position;
				referenceBase = sequenceOfCurrentChromosome.charAt(position);
				referenceAllele = "" + referenceBase;
				alternativeAllele = referenceAllele + alternativeAllele.substring(0, alternativeAllele.length() - 1);
				if (position == 23213479) {
					System.exit(0);
				}
			}
		} else if (isDeletion()){
			Utilities.require(referenceAllele.charAt(0) == sequenceOfCurrentChromosome.charAt(position), 
					"Event.leftAlign error: reference chromosome and reference sequence don't seem to match.");
			int eventLength = referenceAllele.length() - alternativeAllele.length();
			int lastPositionOfDeletion = position + eventLength;
			char referenceBase = referenceAllele.charAt(0);
			while (sequenceOfCurrentChromosome.charAt(lastPositionOfDeletion) == referenceBase && position > 1) {
				System.out.println("shifting " + chromosomeName + position);
				--position;
				referenceBase = sequenceOfCurrentChromosome.charAt(position);
				referenceAllele = referenceBase + referenceAllele.substring(0, referenceAllele.length() - 1);
				alternativeAllele = "" + referenceBase;
				lastPositionOfDeletion = position + eventLength;			
			}
			
		} else {
			System.out.println("Can't handle " + asLine());
		}
	}

	/** 
	 * Returns the event as a line.
	 * 
	 * @return the event as a line/String.
	 */
	public String asLine() {
		allFields[1] = String.valueOf(position);
		allFields[3] = referenceAllele;
		allFields[4] = alternativeAllele;
		if (isPacBio) {
			if (referenceAllele.length() > alternativeAllele.length()) {
				// deletion
				allFields[3] = alternativeAllele;
				allFields[4]  = "<DEL>";
			} else {
				// insertion
				allFields[3] = referenceAllele;
				allFields[4] = "<INS>";
			}
		}
		return String.join("\t", allFields);
	}
	
	
	
}
