package tanks.tankson;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.function.Function;

public class Compatibility {
    public static final HashMap<String, Function<Object, Object>> compatibility_table = new HashMap<>();
    public static final HashMap<String, String> field_table = new HashMap<>();

    public static Object convert(Field f, Object o) {
        try
        {
            if (o instanceof Boolean)
                return ((Boolean) o) ? f.getType().getConstructor().newInstance() : null;
        }
        catch (Exception ignored)
        {

        }

        return compatibility_table.get(Serializer.getid(f)).apply(o);
    }

    public static String convert(String f) {
        return field_table.get(f);
    }
}
