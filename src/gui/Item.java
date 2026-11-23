package gui;

record Item<T>(String label, T value) {
    @Override
    public String toString() { return label; }
}
