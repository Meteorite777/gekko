import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.LinkedList;

public class Individual {
	
	public double fitness = Double.MIN_VALUE;
	
	/*
	//NF's
	private String gekkoCD = "cd /home/osboxes/Documents/Gekko/gekko && ";
	private String gekkoArgs = "node gekko --backtest --config " + "ga-config.js";
	private String configTemplate = "/home/osboxes/Documents/Gekko/gekko/ga-config-template.js";
	private String configTarget = "/home/osboxes/Documents/Gekko/gekko/ga-config.js";
	*/
	
	//SM's
	private String gekkoCD = "cd /home/osboxes/Documents/Gekko/gekko && ";
	private String gekkoArgs = "node gekko --backtest --config " + "ga-config.js";
	private String configTemplate = "/home/osboxes/Documents/Gekko/gekko/ga-config-template.js";
	private String configTarget = "/home/osboxes/Documents/Gekko/gekko/ga-config.js";
	
	public String terminal = "Not ran.";
	
	//Minutes
	public int checkTime;
	private int [] checkTimes = {1, 2, 5, 10, 15}; 
	
	//Minutes
	public int candleLength;
	private int[] candleLengths = {60, 90, 120, 180, 240, 300, 360};
	
	public double stopLossPercentage;
	public double stopLossPercentageMax = 0.09;
	public double stopLossPercentageMin = 0.01;
	public double stopLossPercentageMutation = 0.005;
	
	public double deltaCloseBelowEMA;
	public double deltaCloseBelowEMAMax = 0.15;
	public double deltaCloseBelowEMAMin = 0.005;
	public double deltaCloseBelowEMAMutation = 0.005;
	
	
	public double deltaCloseAboveEMA;
	public double deltaCloseAboveEMAMax = 0.15;
	public double deltaCloseAboveEMAMin = 0.005;
	public double deltaCloseAboveEMAMutation = 0.005;
	
	public double deltaFarAboveEMA;
	public double deltaFarAboveEMAMax = 0.15;
	public double deltaFarAboveEMAMin = 0.05;
	public double deltaFarAboveEMAMutation = 0.01;
	
	public double deltaFarBelowEMA;
	public double deltaFarBelowEMAMax = 0.15;
	public double deltaFarBelowEMAMin = 0.05;
	public double deltaFarBelowEMAMutation = 0.01;
	
	public Individual() {
		
	}
	
	public double getFitness() {
		if(fitness > Double.MIN_VALUE) {
			return fitness;
		}
		
		//Set config file
		writeConfigFile();
		
		//calculate fitness.
		String commandString = gekkoCD + gekkoArgs;
		
		ProcessBuilder pb = new ProcessBuilder();
		StringBuilder sb = new StringBuilder();
		pb.command("bash", "-c", commandString);
		try {
			Process process = pb.start();
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			
			String line;
			while((line = reader.readLine()) != null) {
				sb.append(line);
				sb.append("\n");
			}
			
			process.waitFor();
			
		}catch(Exception e) {
			System.out.println(e.toString());
			fitness = Double.MIN_VALUE + 1; //Error Value
			return fitness;
		}
		
		//Find value from output.
		terminal = sb.toString();
		
		try {
			String tmp = terminal.substring(terminal.indexOf("(PROFIT REPORT) profit:"));
			tmp = tmp.substring("(PROFIT REPORT) profit:".length());
			tmp = tmp.substring(0, tmp.indexOf("USDT"));
			tmp = tmp.trim();
			
			fitness = Double.parseDouble(tmp);
		}catch(Exception e) {
			//System.out.println(e.toString());
			fitness = Double.MIN_VALUE + 1;  //Error value
			return fitness;
		}
		
		return fitness;
	}
	
	public LinkedList<Individual> cross(Individual other) {
		LinkedList<Individual> crossed = new LinkedList<Individual>();
		Individual ind1 = new Individual();
		Individual ind2 = new Individual();
		
		crossed.add(ind1);
		crossed.add(ind2);
		
		
		//Check time
		if(Math.random() > 0.5) {
			ind1.checkTime = this.checkTime;
			ind2.checkTime = other.checkTime;
		}else {
			ind2.checkTime = this.checkTime;
			ind1.checkTime = other.checkTime;
		}
		
		//Candle len
		if(Math.random() > 0.5) {
			ind1.candleLength = this.candleLength;
			ind2.candleLength = other.candleLength;
		}else {
			ind2.candleLength = this.candleLength;
			ind1.candleLength = other.candleLength;
		}
		
		//Stoploss
		if(Math.random() > 0.5) {
			ind1.stopLossPercentage = this.stopLossPercentage;
			ind2.stopLossPercentage = other.stopLossPercentage;
		}else {
			ind2.stopLossPercentage = this.stopLossPercentage;
			ind1.stopLossPercentage = other.stopLossPercentage;
		}
		
		//DeltaCloseBelowEMA
		if(Math.random() > 0.5) {
			ind1.deltaCloseBelowEMA = this.deltaCloseBelowEMA;
			ind2.deltaCloseBelowEMA = other.deltaCloseBelowEMA;
		}else {
			ind2.deltaCloseBelowEMA = this.deltaCloseBelowEMA;
			ind1.deltaCloseBelowEMA = other.deltaCloseBelowEMA;
		}
		
		//DeltaCloseAboveEMA
		if(Math.random() > 0.5) {
			ind1.deltaCloseAboveEMA = this.deltaCloseAboveEMA;
			ind2.deltaCloseAboveEMA = other.deltaCloseAboveEMA;
		}else {
			ind2.deltaCloseAboveEMA = this.deltaCloseAboveEMA;
			ind1.deltaCloseAboveEMA = other.deltaCloseAboveEMA;
		}
		
		//DeltaFarAboveEMA
		if(Math.random() > 0.5) {
			ind1.deltaFarAboveEMA = this.deltaFarAboveEMA;
			ind2.deltaFarAboveEMA = other.deltaFarAboveEMA;
		}else {
			ind2.deltaFarAboveEMA = this.deltaFarAboveEMA;
			ind1.deltaFarAboveEMA = other.deltaFarAboveEMA;
		}
		
		//DeltaFarAboveEMA
		if(Math.random() > 0.5) {
			ind1.deltaFarBelowEMA = this.deltaFarBelowEMA;
			ind2.deltaFarBelowEMA = other.deltaFarBelowEMA;
		}else {
			ind2.deltaFarBelowEMA = this.deltaFarBelowEMA;
			ind1.deltaFarBelowEMA = other.deltaFarBelowEMA;
		}
		
		return crossed;
	}
	
	/**
	 * mutate() will return a newly created individual that has been mutated.
	 */
	public Individual mutate() {
		

		Individual indMutant = new Individual();
		
		if(Math.random() > 0.3) {
			//Change check time
			indMutant.checkTime = checkTimes[(int) (Math.random() * checkTimes.length)];
		}else {
			indMutant.checkTime = this.checkTime;
		}
		
		if(Math.random() > 0.3) {
			//Change our candle length
			candleLength = candleLengths[(int) (Math.random() * candleLengths.length)];
		}else {
			indMutant.candleLength = this.candleLength;
		}
		
		if(Math.random() > 0.3) {
			if(Math.random() > 0.5) {
				indMutant.stopLossPercentage += indMutant.stopLossPercentageMutation;
			}else {
				indMutant.stopLossPercentage -= indMutant.stopLossPercentageMutation;
				indMutant.stopLossPercentage = Math.max(indMutant.stopLossPercentageMin, indMutant.stopLossPercentage);
			}
		}else {
			indMutant.stopLossPercentage = this.stopLossPercentage;
		}
		
		if(Math.random() > 0.3) {
			if(Math.random() > 0.5) {
				indMutant.deltaCloseBelowEMA += indMutant.deltaCloseBelowEMAMutation;
			}else {
				indMutant.deltaCloseBelowEMA -= indMutant.deltaCloseBelowEMAMutation;
				indMutant.deltaCloseBelowEMA = Math.max(indMutant.deltaCloseBelowEMAMin, indMutant.deltaCloseBelowEMA);
			}
		}else {
			indMutant.deltaCloseBelowEMA = this.deltaCloseBelowEMA;
		}
		
		if(Math.random() > 0.3) {
			if(Math.random() > 0.5) {
				indMutant.deltaFarAboveEMA += indMutant.deltaFarAboveEMAMutation;
			}else {
				indMutant.deltaFarAboveEMA -= indMutant.deltaFarAboveEMAMutation;
				indMutant.deltaFarAboveEMA = Math.max(indMutant.deltaFarAboveEMAMin, indMutant.deltaFarAboveEMA);
			}
		}else {
			indMutant.deltaFarAboveEMA = this.deltaFarAboveEMA;
		}
		
		if(Math.random() > 0.3) {
			if(Math.random() > 0.5) {
				indMutant.deltaFarBelowEMA += indMutant.deltaFarBelowEMAMutation;
			}else {
				indMutant.deltaFarBelowEMA -= indMutant.deltaFarBelowEMAMutation;
				indMutant.deltaFarBelowEMA = Math.max(indMutant.deltaFarBelowEMAMin, indMutant.deltaFarBelowEMA);
			}
		}else {
			indMutant.deltaFarBelowEMA = this.deltaFarBelowEMA;
		}
		
		
		return indMutant;
	}
	
	public void randomize() {
		
		//Choose random candle size and check times.
		checkTime = checkTimes[(int) (Math.random() * checkTimes.length)];
		candleLength = candleLengths[(int) (Math.random() * candleLengths.length)];
		
		
		//random number for stoploss
		stopLossPercentage = Math.random();
		while(stopLossPercentage < stopLossPercentageMin || stopLossPercentage > stopLossPercentageMax) {
			//The random number was not in our range. example: 0.0.
			stopLossPercentage = Math.random();
		}
		
		deltaCloseBelowEMA = Math.random();
		while(deltaCloseBelowEMA > deltaCloseBelowEMAMax || deltaCloseBelowEMA < deltaCloseBelowEMAMin) {
			deltaCloseBelowEMA = Math.random();
		}
		
		deltaCloseAboveEMA = Math.random();
		while(deltaCloseAboveEMA > deltaCloseAboveEMAMax || deltaCloseAboveEMA < deltaCloseAboveEMAMin) {
			deltaCloseAboveEMA = Math.random();
		}
		
		deltaFarAboveEMA = Math.random();
		while(deltaFarAboveEMA > deltaFarAboveEMAMax || deltaFarAboveEMA < deltaFarAboveEMAMin) {
			deltaFarAboveEMA = Math.random();
		}
		
		deltaFarBelowEMA = Math.random();
		while(deltaFarBelowEMA > deltaFarBelowEMAMax || deltaFarBelowEMA < deltaFarBelowEMAMin) {
			deltaFarBelowEMA = Math.random();
		}
	}
	
	private String readConfigFile() 
	{
	    StringBuilder contentBuilder = new StringBuilder();
	    try (BufferedReader br = new BufferedReader(new FileReader(configTemplate))) 
	    {
	 
	        String sCurrentLine;
	        while ((sCurrentLine = br.readLine()) != null) 
	        {
	            contentBuilder.append(sCurrentLine).append("\n");
	        }
	    } 
	    catch (IOException e) 
	    {
	        e.printStackTrace();
	        System.exit(0);
	    }
	    return contentBuilder.toString();
	}
	
	public void writeConfigFile() {
		String str;
		str = readConfigFile();
		
		str = str.replace("#checkTime#", checkTime + ""); //Replace in config.TradingAdvisor{}
		str = str.replace("#checkTime#", checkTime + ""); //Replace in config.20and200{}
		
		str = str.replace("#candleLength#", candleLength + "");
		
		str = str.replace("#stopLossPercent#", stopLossPercentage + "");
		str = str.replace("#deltaCloseBelowEMA#", deltaCloseBelowEMA + "");
		str = str.replace("#deltaCloseAboveEMA#", deltaCloseAboveEMA + "");
		str = str.replace("#deltaFarBelowEMA#", deltaFarBelowEMA + "");
		str = str.replace("#deltaFarAboveEMA#", deltaFarAboveEMA + "");
		
		try {
			PrintWriter out = new PrintWriter(new FileOutputStream(configTarget, false));
			out.print(str);
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	public String toString() {
		String ret = "";
		if(fitness == Double.MIN_VALUE) {
			ret = "Fitness not (yet) calculated";
		}else {
			ret = "Fitness: ($)" + fitness;
		}
		return ret + "\n\tCheck Time: " + checkTime + ", CandleLength: " + candleLength 
				+ "\n\t Stoploss: " + stopLossPercentage 
				+ "\n\t DeltaCloseBelowEMA: " + deltaCloseBelowEMA 
				+ "\n\t DeltaCloseAboveEMA: " + deltaCloseAboveEMA
				+ "\n\t DeltaFarAboveEMA: " + deltaFarAboveEMA
				+ "\n\t DeltaFarBelowEMA: " + deltaFarBelowEMA;
	}
}
