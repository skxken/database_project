import java.lang.reflect.InvocationTargetException;

public class DataFactory {
    public DataManipulation createDataManipulation() {
        String name="DatabaseManipulation";;
        try {
            return (DataManipulation) Class.forName(name).getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
