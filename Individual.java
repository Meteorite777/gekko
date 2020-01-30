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
	
	private String gekkoCD = "cd /home/gekko/Repos/gekko && ";
	private String gekkoArgs = "node gekko --backtest --config " + "ga-config.js";
	private String configTemplate = "/home/gekko/Repos/gekko/ga-config-template.js";
	private String configTarget = "/home/gekko/Repos/gekko/ga-config.js";
	
	public String terminal = "Not ran.";
	
	public double stopLossPercentage;
	public double stopLossPercentageMax = 0.09;
	public double stopLossPercentageMin = 0.01;
	public double stopLossPercentageMutation = 0.01;
	
	public double deltaCloseBelowEMA;
	public double deltaCloseBelowEMAMax = 0.15;
	public double deltaCloseBelowEMAMin = 0.01;
	public double deltaCloseBelowEMAMutation = 0.01;
	
	
	public double deltaCloseAboveEMA;
	public double deltaCloseAboveEMAMax = 0.15;
	public double deltaCloseAboveEMAMin = 0.01;
	public double deltaCloseAboveEMAMutation = 0.01;
	
	public double deltaFarAboveEMA;
	public double deltaFarAboveEMAMax = 0.15;
	public double deltaFarAboveEMAMin = 0.01;
	public double deltaFarAboveEMAMutation = 0.01;
	
	public double deltaFarBelowEMA;
	public double deltaFarBelowEMAMax = 0.15;
	public double deltaFarBelowEMAMin = 0.01;
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
			fitness = -100000000.0;
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
			fitness = -1000000;
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
	
	public void randomize() {
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
	
	private void writeConfigFile() {
		String str;
		str = readConfigFile();
		
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
			ret = "Fitness not calculated";
		}else {
			ret = "Fitness: $" + fitness;
		}
		return ret + ", Stoploss: " + stopLossPercentage 
				+ ", DeltaCloseBelowEMA: " + deltaCloseBelowEMA 
				+ " DeltaCloseAboveEMA: " + deltaCloseAboveEMA
				+ " DeltaFarAboveEMA: " + deltaFarAboveEMA
				+ " DeltaFarBelowEMA: " + deltaFarBelowEMA;
	}
}
