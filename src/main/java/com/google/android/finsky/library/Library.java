package com.google.android.finsky.library;

public interface Library extends Iterable<LibraryEntry>
{
    boolean contains(final LibraryEntry p0);
    
    LibraryEntry get(final LibraryEntry p0);
    
    void remove(final LibraryEntry p0);
    
    int size();
}