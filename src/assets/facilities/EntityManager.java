package assets.facilities;

import interfaces.IAgent;
import interfaces.IEntityManager;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EntityManager<T extends IAgent> implements IEntityManager<T> {
    private final int capacity;
    private final Set<T> entitySet = new HashSet<>();

    public EntityManager(int capacity){
        this.capacity = capacity;
    }

    @Override
    public void add(T entity) {
        if(entity == null) throw new IllegalArgumentException("Entity cannot be null");
        if(getCount() < capacity) entitySet.add(entity);
        else throw new IllegalArgumentException("Maximum capacity reached");
    }

    @Override
    public void delete(T entity) {
        if(entitySet.contains(entity)) entitySet.remove(entity);
        else throw new IllegalArgumentException("Entity not found");
    }

    @Override
    public long getCount() {
        return entitySet.size();
    }
    @Override
    public List<T> getAll() { return List.copyOf(entitySet); }
}
