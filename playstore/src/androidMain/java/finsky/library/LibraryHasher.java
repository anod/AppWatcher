package finsky.library;

public interface LibraryHasher {
    void add(final long p0);

    long compute();

    void remove(final long p0);

    void reset();
}