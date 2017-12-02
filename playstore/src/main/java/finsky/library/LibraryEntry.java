package finsky.library;


public class LibraryEntry
{
    public static final String UNKNOWN_ACCOUNT;
    public final String mAccountName;
    public int mBackendId;
    public String mDocId;
    public int mDocType;
    final long mDocumentHash;
    public String mLibraryId;
    public int mOfferType;
    public final boolean mPreordered;
    public final boolean mSharedByMe;
    public final String mSharerPersonDocid;
    public final long mValidUntilTimestampMs;
    
    static {
        UNKNOWN_ACCOUNT = null;
    }

    public LibraryEntry(final String accountName, final String libraryId, final int backendId, final String docId, final int docType, final int offerType, final long documentHash, final long validUntilTimestampMs, final boolean preordered, final boolean sharedByMe, final String sharerPersonDocid) {
        if (docId == null) {
            throw new NullPointerException();
        }
        this.mAccountName = accountName;
        this.mLibraryId = libraryId;
        this.mBackendId = backendId;
        this.mDocId = docId;
        this.mDocType = docType;
        this.mOfferType = offerType;
        this.mDocumentHash = documentHash;
        this.mValidUntilTimestampMs = validUntilTimestampMs;
        this.mPreordered = preordered;
        this.mSharedByMe = sharedByMe;
        this.mSharerPersonDocid = sharerPersonDocid;
    }
    
//    public static LibraryEntry fromDocId(final String s, final String s2, final Common.Docid docid, final int n) {
//        return new LibraryEntry(s, s2, docid.backend, docid.backendDocid, docid.type, n);
//    }
    
    @Override
    public boolean equals(final Object o) {
        if (this != o) {
            if (!(o instanceof LibraryEntry)) {
                return false;
            }
            final LibraryEntry libraryEntry = (LibraryEntry)o;
            if (this.mBackendId != libraryEntry.mBackendId) {
                return false;
            }
            if (this.mDocType != libraryEntry.mDocType) {
                return false;
            }
            if (this.mOfferType != libraryEntry.mOfferType) {
                return false;
            }
            if (this.mAccountName != LibraryEntry.UNKNOWN_ACCOUNT && libraryEntry.mAccountName != LibraryEntry.UNKNOWN_ACCOUNT && !this.mAccountName.equals(libraryEntry.mAccountName)) {
                return false;
            }
            if (!this.mDocId.equals(libraryEntry.mDocId)) {
                return false;
            }
            if (!this.mLibraryId.equals(libraryEntry.mLibraryId)) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public int hashCode() {
        int hashCode = 0;
        int hashCode2;
        if (this.mLibraryId != null) {
            hashCode2 = this.mLibraryId.hashCode();
        }
        else {
            hashCode2 = 0;
        }
        if (this.mDocId != null) {
            hashCode = this.mDocId.hashCode();
        }
        return ((hashCode2 * 31 + hashCode) * 31 + this.mDocType) * 31 + this.mOfferType;
    }
}