import java.io.IOException;
/*
 * added this change in the working copy
 * and want to merge with the master 
 */

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		if(args.length == 0)
		{
			System.out.println("No files specified for analysis");
			System.out.println("Exiting Program");
		}
		else
		{
			try
			{
				new AnalyzeVMStats().AnalyseStats(args);
			} catch (IOException e) 
			{
				// TODO Auto-generated catch block
				System.out.println(e.toString());
				e.printStackTrace();
			}
		}
	}

}
