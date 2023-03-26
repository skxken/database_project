import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;

public class api_test2 {
    public static void main(String[] args)
    {
        SimpleDateFormat str_to_date = new SimpleDateFormat("yyyy-MM-dd");
        try {
            DataManipulation dm = new DataFactory().createDataManipulation();
            dm.openDatasource();
            try(BufferedReader infile= new BufferedReader(new FileReader("task2_test_data_final_public.tsv")))
            {
                String line;
                String[] parts;
                line = infile.readLine();
                while ((line = infile.readLine()) != null)
                {
                    parts=line.split("\t");
                    dm.placeOrder(parts[0],parts[1],parts[2],Integer.parseInt(parts[3]),parts[4],parts[5],parts[6],parts[7],parts[8],parts[9]);
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
