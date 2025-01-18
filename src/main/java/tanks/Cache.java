package tanks;


import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Stream;

public class Cache<T>
{
    private final ArrayList<T> objs = new ArrayList<>();
    public int position = 0;

    public void add(T c)
    {
        if (position >= objs.size())
            objs.add(c);
        else
            objs.set(position, c);
        position++;
    }

    public boolean contains(T c)
    {
        for (int i = 0; i < position; i++)
            if (Objects.equals(objs.get(i), c))
                return true;
        return false;
    }

    public void forEach(Consumer<T> func)
    {
        for (int i = 0; i < position; i++)
            func.accept(objs.get(i));
        reset();
    }

    public void forEach(BiConsumer<T, Integer> func)
    {
        for (int i = 0; i < position; i++)
            func.accept(objs.get(i), i);
        reset();
    }

    public void reset()
    {
        position = 0;
    }

    @SuppressWarnings("unused")
    public Stream<T> stream()
    {
        return objs.stream();
    }
}
