package com.google.android.finsky.api.utils;

import org.keyczar.interfaces.*;
import java.nio.charset.*;
import android.content.res.*;
import org.keyczar.exceptions.*;
import java.io.*;

public class AndroidKeyczarReader implements KeyczarReader
{
    private static final Charset CHARSET_UTF8;
    private final AssetManager mAssetManager;
    private final String mSubDirectory;
    
    static {
        CHARSET_UTF8 = Charset.forName("UTF-8");
    }
    
    public AndroidKeyczarReader(final Resources resources, final String mSubDirectory) {
        super();
        this.mAssetManager = resources.getAssets();
        this.mSubDirectory = mSubDirectory;
    }
    
    private String getFileContentAsString(final String s) throws KeyczarException {
        StringBuilder sb;
        try {
            sb = new StringBuilder();
            final char[] array = new char[1024];
            final InputStreamReader inputStreamReader = new InputStreamReader(this.mAssetManager.open(this.getFullFilename(s)), AndroidKeyczarReader.CHARSET_UTF8);
            while (true) {
                final int read = inputStreamReader.read(array);
                if (read <= 0) {
                    break;
                }
                sb.append(array, 0, read);
            }
        }
        catch (IOException ex) {
            throw new KeyczarException("Couldn't read Keyczar 'meta' file from assets/", ex);
        }
        return sb.toString();
    }
    
    private String getFullFilename(final String s) {
        if (this.mSubDirectory == null) {
            return s;
        }
        return this.mSubDirectory + File.separator + s;
    }
    
    @Override
    public String getKey(final int n) throws KeyczarException {
        return this.getFileContentAsString(String.valueOf(n));
    }

    @Override
    public String getKey() throws KeyczarException {
        return this.getFileContentAsString(String.valueOf(0));
    }

    @Override
    public String getMetadata() throws KeyczarException {
        return this.getFileContentAsString("meta");
    }
}
