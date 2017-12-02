package finsky.api.utils;

import android.content.res.AssetManager;
import android.content.res.Resources;

import org.keyczar.exceptions.KeyczarException;
import org.keyczar.interfaces.KeyczarReader;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

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
