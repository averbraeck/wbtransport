package nl.tudelft.pa.wbtransport.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;

import nl.tudelft.simulation.language.io.URLResource;

/**
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opensource.org/licenses/BSD-3-Clause">BSD 3-Clause License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Sep 29, 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class TestConcurrentRead
{
    private String fileFolder = "E:/BGD";

    /**
     * 
     */
    public TestConcurrentRead()
    {
        URL parameterURL = URLResource.getResource(this.fileFolder + "/parameters.tsv");
        if (parameterURL == null || !new File(parameterURL.getPath()).canRead())
        {
            System.err.println("Cannot read file parameters.tsv with the names (and default values) of the model parameters");
            System.exit(-1);
        }
        try
        {
            BufferedReader br = new BufferedReader(new FileReader(parameterURL.getPath()));
            String line = br.readLine();
            while (line != null)
            {
                if (line.length() > 0 && line.contains("\t"))
                {
                    String[] param = line.split("\t");
                }
                line = br.readLine();
            }
            br.close();
        }
        catch (IOException exception)
        {
            exception.printStackTrace();
            System.exit(-1);
        }
    }

    /**
     * @param args args
     */
    public static void main(String[] args)
    {
        for (int i=0; i<1000; i++)
        {
            final int nr = i;
            new Thread()
            {
                /** {@inheritDoc} */
                @Override
                public void run()
                {
                    new TestConcurrentRead();
                    System.out.println("Success " + nr);
                }
            }.start();
        }
    }

}
