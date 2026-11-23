package interfaces;

import java.util.List;

public interface IEntityManager<T> {
    void add(T entity);
    void delete(T entity);
    long getCount();

    List<T> getAll();
}
