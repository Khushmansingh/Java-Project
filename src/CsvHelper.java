import java.io.*;
import java.util.*;
@SuppressWarnings("unused")
public class CsvHelper
{
    public static void importDrivers(String file,DriverService service) throws IOException
    {
        int imported=0,rejected=0,skipped=0,row=0;
        try(BufferedReader reader=new BufferedReader(new FileReader(file)))
        {
            String line;
            while((line=reader.readLine())!=null)
            {
                row++;
                if(row==1&&line.toLowerCase().startsWith("driverid"))
                {
                    skipped++;
                    continue;
                }
                String[] v=line.split(",",-1);
                try
                {
                    if(v.length<8)throw new IllegalArgumentException("expected 8 columns");
                    int id=Integer.parseInt(v[0].trim());
                    double rating=Double.parseDouble(v[4].trim()),salary=Double.parseDouble(v[5].trim()),value=Double.parseDouble(v[6].trim());
                    Driver d=new Driver(id,v[1].trim(),v[2].trim(),v[3].trim(),rating,salary,value,null,v[7].trim().toUpperCase());
                    service.register(d);
                    imported++;
                }
                catch(Exception e)
                {
                    rejected++;
                    System.out.println("Row "+row+" rejected: "+e.getMessage());
                }
            }
        }
        System.out.println("CSV summary: imported="+imported+", rejected="+rejected+", skipped="+skipped);
    }
    public static void exportReport(String file,String report) throws IOException
    {
        try(BufferedWriter writer=new BufferedWriter(new FileWriter(file)))
        {
            writer.write("Report line");
            writer.newLine();
            for(String line:report.split("\\R"))
            {
                writer.write("\""+line.replace("\"","\"\"")+"\"");
                writer.newLine();
            }
        }
    }
}
