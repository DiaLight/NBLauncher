package dialight.extensions;

import java.util.Collection;
import java.util.Iterator;

public class CollectionEx<T> {
    
    private final Collection<T> collection;

    public CollectionEx(Collection<T> collection) {
        this.collection = collection;
    }

    public T firstOrNull() {
        Iterator<T> iterator = collection.iterator();
        if(!iterator.hasNext()) return null;
        return iterator.next();
    }

    public static <T> CollectionEx<T> of(Collection<T> listCell) {
        return new CollectionEx<T>(listCell);
    }
    
}
