import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;

public class api_test3 {
    public static void main(String[] args)
    {
        SimpleDateFormat str_to_date = new SimpleDateFormat("yyyy-MM-dd");
        try {
            DataManipulation dm = new DataFactory().createDataManipulation();
            dm.openDatasource();
            try(BufferedReader infile= new BufferedReader(new FileReader("update_final_test.tsv")))
            {
                String line;
                String[] parts;
                line = infile.readLine();
                while ((line = infile.readLine()) != null)
                {
                    parts=line.split("\t");
                    dm.updateOrder(parts[0],parts[1],parts[2],Integer.parseInt(parts[3]),parts[4],parts[5]);
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
