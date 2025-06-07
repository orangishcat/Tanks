package basewindow;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Scanner;

public class ComputerFileManager extends BaseFileManager
{
    @Override
    public BaseFile getFile(String file)
    {
        return new ComputerFile(file);
    }

    @Override
    public ArrayList<String> getInternalFileContents(String file)
    {
        try
        {
            InputStream st = this.getResource(file);

            if (st == null)
                return null;

            Scanner s = new Scanner(new InputStreamReader(st));
            ArrayList<String> al = new ArrayList<>();

            while (s.hasNext())
            {
                al.add(s.nextLine());
            }

            s.close();
            return al;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void openFileManager(String path)
    {
        String[] cmd;

        String url = "file:///" + path.replace("\\", "/");
        String os = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        if (os.contains("win"))
            cmd = new String[]{"rundll32", "url.dll,FileProtocolHandler", url};
        else if (os.contains("mac"))
            cmd = new String[]{"open", url};
        else
            cmd = new String[]{"xdg-open", url};

        try
        {
            Runtime.getRuntime().exec(cmd);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public InputStream getResource(String path) throws IOException
    {
        return getResource(this.overrideLocations, path);
    }

    public static InputStream getResource(ArrayList<String> overrideLocations, String path) throws IOException
    {
        for (String overridesDir: overrideLocations)
        {
            File f = new File(overridesDir + path);

            if (f.exists())
                return Files.newInputStream(f.toPath());
        }

        return ComputerFileManager.class.getResourceAsStream(path);
    }
}
