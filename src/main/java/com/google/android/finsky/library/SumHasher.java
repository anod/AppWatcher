package com.google.android.finsky.library;

public final class SumHasher implements LibraryHasher {
    private long mHash;

    @Override
    public final void add(final long n) {
        this.mHash += n;
    }

    @Override
    public final long compute() {
        return this.mHash;
    }

    @Override
    public final void remove(final long n) {
        this.mHash -= n;
    }

    @Override
    public final void reset() {
        this.mHash = 0L;
    }
}