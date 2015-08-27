
/*
		 *  1 - r  - How many processes are waiting for CPU time.
		 *  2 - b - Wait Queue - Process which are waiting for I/O (disk, network, user input,etc..)
		 *  3 - swpd - shows how many blocks are swapped out to disk (paged). Total Virtual memory usage.
		 *  4 - free - Idle Memory
		 *  5 - buff - Memory used as buffers, like before/after I/O operations
		 *  6 - cache - Memory used as cache by the Operating System
		 *  7 - si - How many blocks per second the operating system is swapping in. i.e  Memory swapped in from the disk (Read from swap area to Memory)
		 *  8 - so - How many blocks per second the operating system is swaped Out. i.e Memory swapped to the disk (Written to swap area and cleared from Memory)
		 *  9 - bi - Blocks received from block device - Read (like a hard disk)
		 *  10 - bo - Blocks sent to a block device - Write
		 *  11 - in - The number of interrupts per second, including the clock
		 *  12 - cs - The number of context switches per second
		 *  13 - us - percentage of cpu used for running non-kernel code.
		 *  14 - sy - percentage of cpu used for running kernel code. 
		 *  15 - id - cpu idle time in percentage.
		 *  16 - wa - percentage of time spent by cpu for waiting to IO
		 *  17 - st - 
		 *  18 - date
		 *  19 - time
		 *  20 - zone
		 */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;


public class AnalyzeVMStats
{
	 DateTime dtStartTime, dtEndTime;
	 boolean timeSpecified = false;
	 final int tokenNoCPU = 13;
	final int tokenNoTime = 19;
	
	
	public void AnalyseStats(String args[]) throws IOException
	{
		
		ArrayList<String> arrLogFiles = new ArrayList<String>();
		//If there are more than 1 arguments then that means the user has specified start and end time.
		if(args.length > 1)
		{
				getLogFiles(args, arrLogFiles);
			  	DateTimeFormatter formatter = DateTimeFormat.forPattern("HH:mm:ss");
				dtStartTime = formatter.parseDateTime(args[1]);
				dtEndTime = formatter.parseDateTime(args[2]);
				timeSpecified = true;
		}
		else
		{
			getLogFiles(args, arrLogFiles);
		}
		
		//Start the analysis for each file
		for (String strLogFile : arrLogFiles) 
		{
			System.out.println("Analysing File: " + strLogFile );
			
			ArrayList<String> arrStatsCPU = new ArrayList<String>();
			ArrayList<String> arrStatsTime = new ArrayList<String>();
			
			String currentLine;
			
			File logFile = new File(strLogFile);
			String serverName = logFile.getName().split("_")[0];
			StringBuffer sbOutput = new StringBuffer();
			BufferedReader br = new BufferedReader(new FileReader(logFile));
			sbOutput.append("r,b,swpd,free,buff,cache,si,so,bi,bo,in,cs,us,sy,id,wa,st,date,time,zone"+"\n");

			while((currentLine = br.readLine()) != null)
			{
				try
				{
					StringTokenizer st = new StringTokenizer(currentLine);
					if(st.countTokens() == 20)
					{
						int tokenNo = 0;
						while(st.hasMoreTokens())
						{
							String strStat = st.nextToken();
							tokenNo++;
							sbOutput.append(strStat+",");
							switch(tokenNo)
							{
								case tokenNoCPU:
									arrStatsCPU.add(strStat);
									break;
								case tokenNoTime:
									arrStatsTime.add(strStat);
									break;
									
							}
						}
						sbOutput.append("\n");
					}
					
				}
				catch(Exception e)
				{
				}
			}
			
			br.close();
			//outputToFile(sbOutput);
			JFreeChart lineChart = ChartFactory.createTimeSeriesChart(
					" CPU Usage - "+serverName,
			         "Time",
			         "CPU Usage",
			         createDataset("CPU Usage - "+serverName,arrStatsCPU,arrStatsTime),
			         true,true,false);
			
			XYPlot plot = (XYPlot) lineChart.getPlot();
			lineChart.getXYPlot().getRangeAxis().setRange(0.0,100.0);
			DateAxis axis = (DateAxis) plot.getDomainAxis();
			axis.setAutoRange(true);
			axis.setDateFormatOverride(new SimpleDateFormat("HH:mm:ss"));
	        int width=1500; /* Width of the image */
	        int height=700; /* Height of the image */   
	        String outputFileName = serverName + "_Analysis.png";
	        File fileChart=new File(outputFileName);              
	        ChartUtilities.saveChartAsPNG(fileChart,lineChart,width,height); 
	        System.out.println("Analysis complete for "+strLogFile);
		}
		
	}


	private void getLogFiles(String[] args, ArrayList<String> arrLogFiles) 
	{
		File[] faFiles = new File(args[0]).listFiles();
		for(File file: faFiles)
		 {
				if(file.isFile())
				{
					//System.out.println(file.getAbsolutePath());
				     arrLogFiles.add(file.getAbsolutePath());
				}
		      
		 }
	}


	private  void outputToFile(StringBuffer sbOutput) throws IOException
	{
		FileWriter fw = new FileWriter(new File("T:\\output.csv"));
		fw.write(sbOutput.toString());
		fw.close();
	}
	
	
	 private  TimeSeriesCollection createDataset(String strStatName,ArrayList<String> arrStats,ArrayList<String> arrTime)
	   {
		 	@SuppressWarnings("deprecation")
			final TimeSeries series = new TimeSeries("Per Second Data", Second.class);
		 	if(arrStats.size() == arrTime.size())
		 	{
		 		for(int i=0;i<arrStats.size();i++)
		 		{
		 			DateTimeFormatter formatter = DateTimeFormat.forPattern("HH:mm:ss");
				 	DateTime dt = formatter.parseDateTime(arrTime.get(i));
				 	if(timeSpecified)
				 	{
				 		if(dt.isAfter(dtStartTime) && dt.isBefore(dtEndTime))
					 	{
					 		series.addOrUpdate(new Second(dt.toDate()), Integer.parseInt(arrStats.get(i)));
					 	}
				 	}
				 	else
				 	{
				 		series.addOrUpdate(new Second(dt.toDate()), Integer.parseInt(arrStats.get(i)));
				 	}
		 		}
		 		
		        final TimeSeriesCollection dataset = new TimeSeriesCollection(series);
				return dataset;
		 	}
			return null;
		 
	   }
}
