import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Comparator;

public class Banker {
	private int numberOfCustomers;	// the number of customers
	private int numberOfResources;	// the number of resources

	private int[] available; 	// the available amount of each resource
	private int[][] maximum; 	// the maximum demand of each customer
	private int[][] allocation;	// the amount currently allocated
	private int[][] need;		// the remaining needs of each customer

	/**
	 * Constructor for the Banker class.
	 * @param resources          An array of the available count for each resource.
	 * @param numberOfCustomers  The number of customers.
	 */
	public Banker (int[] resources, int numberOfCustomers) {
		// TODO: set the number of resources
		this.numberOfResources = resources.length;
		// TODO: set the number of customers
		this.numberOfCustomers = numberOfCustomers;
		// TODO: set the value of bank resources to available
		this.available = resources.clone();
		// TODO: set the array size for maximum, allocation, and need
		this.maximum = new int[this.numberOfResources][this.numberOfCustomers];
		this.allocation = new int[this.numberOfResources][this.numberOfCustomers];
		this.need = new int[this.numberOfCustomers][this.numberOfResources];
	}

	/**
	 * Sets the maximum number of demand of each resource for a customer.
	 * @param customerIndex  The customer's index (0-indexed).
	 * @param maximumDemand  An array of the maximum demanded count for each resource.
	 */
	public void setMaximumDemand(int customerIndex, int[] maximumDemand) {
		// TODO: add customer, update maximum and need
		this.maximum[customerIndex] = maximumDemand;
		this.need[customerIndex] = maximumDemand;
	}

	/**
	 * Prints the current state of the bank.
	 */
	public void printState() {
        System.out.println("\nCurrent state:");
        // print available
        System.out.println("Available:");
        System.out.println(Arrays.toString(available));
        System.out.println("");

        // print maximum
        System.out.println("Maximum:");
        for (int[] aMaximum : maximum) {
            System.out.println(Arrays.toString(aMaximum));
        }
        System.out.println("");
        // print allocation
        System.out.println("Allocation:");
        for (int[] anAllocation : allocation) {
            System.out.println(Arrays.toString(anAllocation));
        }
        System.out.println("");
        // print need
        System.out.println("Need:");
        for (int[] aNeed : need) {
            System.out.println(Arrays.toString(aNeed));
        }
        System.out.println("");
	}

	/**
	 * Requests resources for a customer loan.
	 * If the request leave the bank in a safe state, it is carried out.
	 * @param customerIndex  The customer's index (0-indexed).
	 * @param request        An array of the requested count for each resource.
	 * @return true if the requested resources can be loaned, else false.
	 */
	public synchronized boolean requestResources(int customerIndex, int[] request) throws IllegalArgumentException{
		// TODO: print the request
		System.out.println("Customer " + customerIndex + " requesting");
        System.out.println(Arrays.toString(request));
		// TODO: check if request larger than need
		if (!compare(this.need[customerIndex], request)){
			throw new IllegalArgumentException();
		}
		// TODO: check if request larger than available
		if (!compare(this.available, request)){
			return false;
		}
		// TODO: check if the state is safe or not
		boolean state = checkSafe(customerIndex, request);
		// TODO: if it is safe, allocate the resources to customer customerNumber
		if (state){
			minus(this.available, request);
			minus(this.need[customerIndex], request);
			plus(this.allocation[customerIndex], request);
			return true;
		}
		return false;
	}

	//If every element in a1 is larger or equal than a2, return true; otherwise, return false.
	public boolean compare(int[] a1, int[] a2){
		for (int i=0;i<a1.length;i++){
			if (a1[i]<a2[i]){
				return false;
			}
		}
		return true;
	}

	//For every element a1[i], replace it by a1[i]-a2[i]
	public void minus(int[] a1, int[] a2){
		for (int i=0;i<a1.length;i++){
			a1[i] = a1[i]-a2[i];
		}
	}

	//For every element a1[i], replace it by a1[i]+a2[i]
	public void plus(int[] a1, int[] a2){
		for (int i=0;i<a1.length;i++){
			a1[i] = a1[i]+a2[i];
		}
	}

	/**
	 * Releases resources borrowed by a customer. Assume release is valid for simplicity.
	 * @param customerIndex  The customer's index (0-indexed).
	 * @param release        An array of the release count for each resource.
	 */
	public synchronized void releaseResources(int customerIndex, int[] release) {
		// TODO: print the release
		System.out.println("Customer " + customerIndex + " releasing");
		System.out.println(Arrays.toString(release));
		// TODO: release the resources from customer customerNumber
		plus(this.available, release);
		plus(this.need[customerIndex], release);
		minus(this.allocation[customerIndex], release);
	}

	/**
	 * Checks if the request will leave the bank in a safe state.
	 * @param customerIndex  The customer's index (0-indexed).
	 * @param request        An array of the requested count for each resource.
	 * @return true if the requested resources will leave the bank in a
	 *         safe state, else false
	 */
	private synchronized boolean checkSafe(int customerIndex, int[] request) {
		// TODO: check if the state is safe
		// Initialize
		int[] available = this.available.clone();
		minus(available, request);

		int[][] need = this.need.clone();
		for (int i=0;i<need.length;i++){
			need[i]=this.need[i].clone();
			minus(need[i],request);
		}

		int[][] allocation = this.allocation.clone();
		for (int i=0;i<allocation.length;i++){
			allocation[i]=this.allocation[i].clone();
			minus(allocation[i],request);
		}

		boolean[] finish = new boolean[this.numberOfCustomers];

		// Find available index i
		int i = 0;
		while(true){
			if(i>=numberOfCustomers){
				break;
			}
			if (finish[i]==false&&compare(available, need[customerIndex])){
				plus(available, allocation[i]);
				finish[i]=true;
				i = 0;
			}else{
				i++;
			};
		}

		// Check finish[]
		for(boolean pass:finish){
			if(!pass){
				return false;
			}
		}
		return true;
	}

	/**
	 * Parses and runs the file simulating a series of resource request and releases.
	 * Provided for your convenience.
	 * @param filename  The name of the file.
	 */
	public static void runFile(String filename) {

		try {
			BufferedReader fileReader = new BufferedReader(new FileReader(filename));

			String line = null;
			String [] tokens = null;
			int [] resources = null;

			int n, m;

			try {
				n = Integer.parseInt(fileReader.readLine().split(",")[1]);
			} catch (Exception e) {
				System.out.println("Error parsing n on line 1.");
				fileReader.close();
				return;
			}

			try {
				m = Integer.parseInt(fileReader.readLine().split(",")[1]);
			} catch (Exception e) {
				System.out.println("Error parsing n on line 2.");
				fileReader.close();
				return;
			}

			try {
				tokens = fileReader.readLine().split(",")[1].split(" ");
				resources = new int[tokens.length];
				for (int i = 0; i < tokens.length; i++)
					resources[i] = Integer.parseInt(tokens[i]);
			} catch (Exception e) {
				System.out.println("Error parsing resources on line 3.");
				fileReader.close();
				return;
			}

			Banker theBank = new Banker(resources, n);

			int lineNumber = 4;
			while ((line = fileReader.readLine()) != null) {
				tokens = line.split(",");
				if (tokens[0].equals("c")) {
					try {
						int customerIndex = Integer.parseInt(tokens[1]);
						tokens = tokens[2].split(" ");
						resources = new int[tokens.length];
						for (int i = 0; i < tokens.length; i++)
							resources[i] = Integer.parseInt(tokens[i]);
						theBank.setMaximumDemand(customerIndex, resources);
					} catch (Exception e) {
						System.out.println("Error parsing resources on line "+lineNumber+".");
						fileReader.close();
						return;
					}
				} else if (tokens[0].equals("r")) {
					try {
						int customerIndex = Integer.parseInt(tokens[1]);
						tokens = tokens[2].split(" ");
						resources = new int[tokens.length];
						for (int i = 0; i < tokens.length; i++)
							resources[i] = Integer.parseInt(tokens[i]);
						theBank.requestResources(customerIndex, resources);
					} catch (Exception e) {
						System.out.println("Error parsing resources on line "+lineNumber+".");
						fileReader.close();
						return;
					}
				} else if (tokens[0].equals("f")) {
					try {
						int customerIndex = Integer.parseInt(tokens[1]);
						tokens = tokens[2].split(" ");
						resources = new int[tokens.length];
						for (int i = 0; i < tokens.length; i++)
							resources[i] = Integer.parseInt(tokens[i]);
						theBank.releaseResources(customerIndex, resources);
					} catch (Exception e) {
						System.out.println("Error parsing resources on line "+lineNumber+".");
						fileReader.close();
						return;
					}
				} else if (tokens[0].equals("p")) {
					theBank.printState();
				}
			}
			fileReader.close();
		} catch (IOException e) {
			System.out.println("Error opening: "+filename);
		}

	}

	/**
	 * Main function
	 * @param args  The command line arguments
	 */
	public static void main(String [] args) {
		if (args.length > 0) {
			runFile(args[0]);
		}
	}

}