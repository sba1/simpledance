package de.sonumina.simpledance.core.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Class providing some static methods.
 *
 * @author Sebastian Bauer
 */
public class Util
{
	/**
	 * Return a list of all patterns grouped by type of the dance.
	 *
	 * @param directoryName the directory that should be scanned.
	 *
	 * @return the list of patterns grouped by the type of the dance.
	 */
	public static ArrayList<LinkedList<Pattern.PatternInfo>> listAllPatterns(String directoryName)
	{
		ArrayList<LinkedList<Pattern.PatternInfo>> allPatternsArrayList = new ArrayList<>();
		for (int i=0;i<Pattern.DANCE_MAX;i++)
			allPatternsArrayList.add(new LinkedList<Pattern.PatternInfo>());

		File directory = new File(directoryName);
		if (!directory.exists())
			return allPatternsArrayList;

		String [] list = directory.list(new FilenameFilter()
		{
			public boolean accept(File file, String name)
			{
				return name.endsWith(".sdn");
			}
		});

		for (int i=0;i<list.length;i++)
		{
			File file = new File(directory,list[i]);

			FileInputStream fis = null;
			try
			{
				fis = new FileInputStream(file);
				byte [] input = new byte[fis.available()];
				fis.read(input);
				String str = new String(input);
				Pattern.PatternInfo pi = Pattern.getPatternInfo(str);
				pi.data = file;
				allPatternsArrayList.get(pi.type).add(pi);
			}
			catch (FileNotFoundException e)
			{
			}
			catch (IOException e)
			{
			}
			finally
			{
				try
				{
					if (fis != null)
					{
						fis.close();
					}
				}
				catch (IOException e)
				{
				}
			}
		}
		return allPatternsArrayList;
	}


}
