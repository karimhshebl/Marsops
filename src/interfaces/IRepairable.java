package interfaces;

public interface IRepairable {
    boolean isDamaged();

    /** @throws IllegalStateException if this is already damaged */
    void damage();

    /** @throws IllegalStateException if this is not damaged */
    void repair();
}
