import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class api_test1 {
    public static void main(String[] args)
    {
        SimpleDateFormat str_to_date = new SimpleDateFormat("yyyy-MM-dd");
        try {
            DataManipulation dm = new DataFactory().createDataManipulation();
            dm.openDatasource();
            try(BufferedReader infile= new BufferedReader(new FileReader("in_stoke_test.csv")))
            {
                String line;
                String[] parts;
                line = infile.readLine();
                while ((line = infile.readLine()) != null)
                {
                    parts=line.split(",");
                    if(parts.length==7)
                    {
                        String[] day=parts[4].split("/");
                        String s=day[0]+"-";
                        if(day[1].length()==2)
                            s+="0";
                        s+=day[1];
                        s+="-";
                        if(day[2].length()==2)
                            s+="0";
                        s+=day[2];
                        dm.stockIn(parts[1],parts[2],parts[3],s,Integer.parseInt(parts[5]),Integer.parseInt(parts[6]));
                    }
                    else
                    {
                        String[] day=parts[5].split("/");
                        String s=day[0]+"-";
                        if(day[1].length()==2)
                            s+="0";
                        s+=day[1];
                        s+="-";
                        if(day[2].length()==2)
                            s+="0";
                        s+=day[2];
                        dm.stockIn(parts[1]+parts[2],parts[3],parts[4],s,Integer.parseInt(parts[6]),Integer.parseInt(parts[7]));
                    }
                }
            }catch (IOException e)
            {
                e.printStackTrace();
            }

            dm.closeDatasource();
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
        }
    }
}
