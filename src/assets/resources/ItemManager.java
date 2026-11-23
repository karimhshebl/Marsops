package assets.resources;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static universalHelpers.Validation.validatePositive;

public class ItemManager<T> {
    private final Map<T, Integer> stock = new HashMap<>();
    public int getQuantity(T item){
        return stock.getOrDefault(item , 0);
    }
    public void addResource(T item , int quantity){
       validatePositive(quantity , "Quantity");
       int originalQuantity = getQuantity(item);
       stock.put(item , originalQuantity + quantity);
    }
    public void removeResource(T item , int quantity){
        validatePositive(quantity , "Quantity");
        if(getQuantity(item) == 0) throw new IllegalArgumentException(item.toString() + " is empty");
        else if (quantity > getQuantity(item)) throw new IllegalArgumentException("Insufficient " + item.toString());
        else {
            stock.put(item, getQuantity(item) - quantity);
        }
    }
    public Map<T , Integer> getStockMap(){
        return Collections.unmodifiableMap(stock);
    }

}
