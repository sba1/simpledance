/**
 * @author Standard
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class GraphicsData
{
	/* 21cm hoch */
	
	static private int ladyBaleX = 0;
	static private int ladyBaleY = -29; 
	static private int [] ladyBale = new int [] 
	{
		12,-7,
		12,-16,
		9,-23,
		6,-28,
		2,-32,
		-2,-34,
		-5,-34,
		-7,-32,
		-8,-30,
		-10,-25,
		-12,-18,
		-12,-4,
		-6,9,
		-4,15,
		-4,22,
		3,22,
		3,14,
		6,5
	};
	
	static private int ladyHeelX = 0;
	static private int ladyHeelY = 29; 
	static private int [] ladyHeel = new int []
	{
		-4,22,
		-6,23,
		-6,30,
		-3,34,
		2,34,
		5,30,
		5,23,
		3,22,
	};


	/* 25cm hoch */
	static private int gentBaleX = 0;
	static private int gentBaleY = -29; 
	static private int [] gentBale = new int []
	{
		-8,14,
		-8,4,
		-11,-3,
		-13,-12,
		-14,-19,
		-14,-24,
		-12,-33,
		-8,-38,
		-5,-40,
		-1,-40,
		4,-37,
		8,-32,
		11,-26,
		13,-20,
		14,-13,
		14,-8,
		13,-2,
		11,4,
		9,8,
		8,11,
		8,14
	};
	
	static private int gentHeelX = 0;
	static private int gentHeelY = 29; 
	static private int [] gentHeel = new int []
	{
		-12,14,
		12,14,
		12,30,
		10,35,
		8,37,
		5,39,
		1,40,
		-1,40,
		-5,39,
		-8,37,
		-10,35,
		-12,30
	};

	public int baleX;
	public int baleY;
	public int [] baleData;
	public int heelX;
	public int heelY;
	public int [] heelData;
	public int feetDataYSize;
	public int realYSize;
			
	public GraphicsData(int type)
	{
		int minY = 0;
		int maxY = 0;

		if (type == 0)
		{
			baleX = gentBaleX;
			baleY = gentBaleY;
			baleData = gentBale;

			heelX = gentHeelX;
			heelY = gentHeelY;
			heelData = gentHeel;
			realYSize = 22;
		} else
		if (type == 1)
		{
			baleX = ladyBaleX;
			baleY = ladyBaleY;
			baleData = ladyBale;

			heelX = ladyHeelX;
			heelY = ladyHeelY;
			heelData = ladyHeel;
			realYSize = 21;
		}
		
		for (int i=0;i<baleData.length;i+=2)
		{
			if (baleData[i+1] < minY) minY = baleData[i+1];
			else if (baleData[i+1] > maxY) maxY = baleData[i+1];  
		}

		for (int i=0;i<baleData.length;i+=2)
		{
			if (baleData[i+1] < minY) minY = heelData[i+1];
			else if (baleData[i+1] > maxY) maxY = heelData[i+1];  
		}
		feetDataYSize = maxY - minY + 1;
	}
}
