package vcf_left_aligner;

/**
 * Contains utility functions that aid programming yet are not in the Java standard library
 * 
 * @author Eric-Wubbo Lameijer, Xi'an Jiaotong University, eric_wubbo@hotmail.com
 *
 */
public class Utilities {
	
	/**
	 * If the requirement is not met, aborts the program.
	 * 
	 * @param requirementMet 
	 * 		whether the requirement is met
	 * @param errorMessage 	
	 * 		the message to give when exiting with an error
	 */
public static void require(boolean requirementMet, String errorMessage) {
	if (!requirementMet) {
		System.out.println(errorMessage);
		System.exit(-1);
	}
}
	
}

