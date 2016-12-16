package com.google.android.finsky.library;

import android.util.Log;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

public class HashMapLibrary extends HashingLibrary
{
    private final int mBackendId;
    private final Map<LibraryEntry, LibraryEntry> mEntries;
    
    public HashMapLibrary(final int mBackendId, final LibraryHasher libraryHasher) {
        super(libraryHasher);
        this.mEntries = new HashMap<>();
        this.mBackendId = mBackendId;
    }
    
    @Override
    public void add(final LibraryEntry libraryEntry) {
        synchronized (this) {
            super.add(libraryEntry);
            this.mEntries.put(libraryEntry, libraryEntry);
        }
    }
    
    @Override
    public final boolean contains(final LibraryEntry libraryEntry) {
        synchronized (this) {
            return this.mEntries.containsKey(libraryEntry);
        }
    }
    
    @Override
    public void dumpState(final String s, final String s2) {
        Log.d("FinskyLibrary", s2 + "Library (" + s + ") {");
        Log.d("FinskyLibrary", s2 + "  backend=" + this.mBackendId);
        Log.d("FinskyLibrary", s2 + "  entryCount=" + this.mEntries.size());
        Log.d("FinskyLibrary", s2 + "}");
    }
    
    @Override
    public final LibraryEntry get(final LibraryEntry libraryEntry) {
        return this.mEntries.get(libraryEntry);
    }
    
    @Override
    public Iterator<LibraryEntry> iterator() {
        synchronized (this) {
            return this.mEntries.values().iterator();
        }
    }
    
    @Override
    public void remove(final LibraryEntry libraryEntry) {
        synchronized (this) {
            super.remove(libraryEntry);
            this.mEntries.remove(libraryEntry);
        }
    }
    
    @Override
    public void reset() {
        synchronized (this) {
            super.reset();
            this.mEntries.clear();
        }
    }
    
    @Override
    public final int size() {
        synchronized (this) {
            return this.mEntries.size();
        }
    }
    
    @Override
    public String toString() {
        return String.format(Locale.US, "{backend=%d, num entries=%d}", this.mBackendId, this.size());
    }
}