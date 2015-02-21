package com.google.android.finsky.api.model;

import java.util.*;
import com.google.android.finsky.protos.*;

public abstract class ContainerList<T> extends PaginatedList<T, Document>
{
    private Document mContainerDocument;
    
    protected ContainerList(final String s) {
        super(s);
    }
    
    protected ContainerList(final String s, final boolean b) {
        super(s, b);
    }
    
    protected ContainerList(final List<UrlOffsetPair> list, final int n, final boolean b) {
        super(list, n, b);
    }
    
    public int getBackendId() {
        return this.getBackendId(0);
    }
    
    public int getBackendId(int backend) {
        if (this.mContainerDocument != null) {
            backend = this.mContainerDocument.getBackend();
        }
        return backend;
    }
    
    public Document getContainerDocument() {
        return this.mContainerDocument;
    }
    
    protected Document[] updateContainerAndGetItems(final DocumentV2.DocV2 docV2) {
        Document[] array;
        if (docV2 != null) {
            this.mContainerDocument = new Document(docV2);
            final int length = docV2.child.length;
            array = new Document[length];
            for (int i = 0; i < length; ++i) {
                array[i] = new Document(docV2.child[i]);
            }
        }
        else {
            array = new Document[0];
        }
        return array;
    }
}
