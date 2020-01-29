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
	
	private String gekkoCD = "cd /home/osboxes/Documents/Gekko/gekko && ";
	private String gekkoArgs = "node gekko --backtest --config " + "ga-config.js";
	private String configTemplate = "/home/osboxes/Documents/Gekko/gekko/ga-config-template.js";
	private String configTarget = "/home/osboxes/Documents/Gekko/gekko/ga-config.js";
	
	public String terminal = "Not ran.";
	
	public double stopLossPercentage;
	public double stopLossPercentageMax = 0.09;
	public double stopLossPercentageMin = 0.01;
	public double stopLossPercentageMutation = 0.01;
	
	public double deltaCloseBelowEMA;
	public double deltaCloseBelowEMAMax = 0.25;
	public double deltaCloseBelowEMAMin = 0.01;
	public double deltaCloseBelowEMAMutation = 0.01;
	
	
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
			sb.append(e.toString());
		}
		
		//Find value from output.
		terminal = sb.toString();
		
		String tmp = terminal.substring(terminal.indexOf("(PROFIT REPORT) profit:"));
		tmp = tmp.substring("(PROFIT REPORT) profit:".length());
		tmp = tmp.substring(0, tmp.indexOf("USDT"));
		tmp = tmp.trim();
		
		fitness = Double.parseDouble(tmp);
		
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
		
		return crossed;
	}
	
	public void randomize() {
		//random number for stoploss
		stopLossPercentage = (int) (Math.random() * 10); //int between 
		stopLossPercentage = stopLossPercentage / 10; // 0.0 - 0.9
		
		while(stopLossPercentage < 0.1 || stopLossPercentage > 0.9) {
			//The random number was not in our range. example: 0.0.
			stopLossPercentage = (int) (Math.random() * 10); //int between 
			stopLossPercentage = stopLossPercentage / 10; // 0.0 - 0.9
		}
		
		deltaCloseBelowEMA = Math.random();
		while(deltaCloseBelowEMA > deltaCloseBelowEMAMax || deltaCloseBelowEMA < deltaCloseBelowEMAMin) {
			deltaCloseBelowEMA = Math.random();
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
		return ", Stoploss: " + stopLossPercentage + ", DeltaCloseBelowEMA: " + deltaCloseBelowEMA;
	}
}
