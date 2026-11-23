package universalHelpers;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class IdGenerator {
    private static final Map<String, AtomicInteger> counters = new ConcurrentHashMap<>();

    public static void reset(){
        counters.clear();
    }

    public static String generateId(String prefix){
        return prefix + counters.computeIfAbsent(prefix, key -> new AtomicInteger()).incrementAndGet();
    }
}
