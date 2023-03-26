import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;

public class api_test4 {
    public static void main(String[] args)
    {
        SimpleDateFormat str_to_date = new SimpleDateFormat("yyyy-MM-dd");
        try {
            DataManipulation dm = new DataFactory().createDataManipulation();
            dm.openDatasource();
            try(BufferedReader infile= new BufferedReader(new FileReader("delete_final.tsv")))
            {
                String line;
                String[] parts;
                line = infile.readLine();
                while ((line = infile.readLine()) != null)
                {
                    parts=line.split(",");
                    dm.deleteOrder(parts[0],parts[1],Integer.parseInt(parts[2]));
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
