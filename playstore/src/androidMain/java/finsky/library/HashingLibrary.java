package finsky.library;

public abstract class HashingLibrary implements Library
{
    public final LibraryHasher hasher;
    
    public HashingLibrary(final LibraryHasher mHasher) {
        this.hasher = mHasher;
    }
    
    public void add(final LibraryEntry libraryEntry) {
        if (!this.contains(libraryEntry)) {
            this.hasher.add(libraryEntry.mDocumentHash);
        }
    }
    
    public abstract void dumpState(final String p0, final String p1);
    
    @Override
    public void remove(LibraryEntry value) {
        value = this.get(value);
        if (value != null) {
            this.hasher.remove(value.mDocumentHash);
        }
    }
    
    public void reset() {
        this.hasher.reset();
    }
}