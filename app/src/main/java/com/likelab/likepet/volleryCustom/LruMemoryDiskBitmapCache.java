package com.likelab.likepet.volleryCustom;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.v4.util.LruCache;
import android.util.Log;

import com.android.volley.toolbox.ImageLoader.ImageCache;
import com.likelab.likepet.BuildConfig;
import com.jakewharton.disklrucache.DiskLruCache;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
 
public class LruMemoryDiskBitmapCache extends LruCache<String, Bitmap> implements
        ImageCache {
	
	private DiskLruCache mDiskCache;
    private CompressFormat mCompressFormat = CompressFormat.JPEG;
    private static int IO_BUFFER_SIZE = 8*1024;
    private static int DISK_IMAGECACHE_SIZE = 1024*1024*50;
    private int mCompressQuality = 70;
    private static final int APP_VERSION = 1;
    private static final int VALUE_COUNT = 1;
    
    public static int getDefaultLruCacheSize() {
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 8;
 
        return cacheSize;
    }

    public LruMemoryDiskBitmapCache(Context context) {
        this(getDefaultLruCacheSize());
        
        try {
            final File diskCacheDir = getDiskCacheDir(context);
            mDiskCache = DiskLruCache.open( diskCacheDir, APP_VERSION, VALUE_COUNT, DISK_IMAGECACHE_SIZE );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
 
    public LruMemoryDiskBitmapCache(int sizeInKiloBytes) {
        super(sizeInKiloBytes);
    }
 
    @Override
    protected int sizeOf(String key, Bitmap value) {
        return value.getRowBytes() * value.getHeight() / 1024;
    }
 
    @Override
    public Bitmap getBitmap(String url) {

        Bitmap bitmap = get(url);
    	if( bitmap != null ){	//Memory cache hit!
        	return bitmap;
        }
    	
    	DiskLruCache.Snapshot snapshot = null;
    	try {

            snapshot = mDiskCache.get( createKey(url) );
            if ( snapshot == null ) {
                return null;
            }
            final InputStream in = snapshot.getInputStream( 0 );
            if ( in != null ) {
                final BufferedInputStream buffIn = 
                new BufferedInputStream( in, IO_BUFFER_SIZE );
                bitmap = BitmapFactory.decodeStream( buffIn );
            }   
        } catch ( IOException e ) {
            e.printStackTrace();
        } finally {
            if ( snapshot != null ) {
                snapshot.close();
            }
        }

        if ( bitmap != null ) {
            Log.d( "cache_test_DISK_", bitmap == null ? "" : "image read from disk " + createKey(url));
            put(url, bitmap);
        }

        return bitmap;
    }
 
    @Override
    public void putBitmap(String url, Bitmap bitmap) {
        put(url, bitmap);
        
        DiskLruCache.Editor editor = null;
        try {
            editor = mDiskCache.edit( createKey(url) );
            if ( editor == null ) {
                return;
            }

            if( writeBitmapToFile( bitmap, editor ) ) {               
                mDiskCache.flush();
                editor.commit();
                if ( BuildConfig.DEBUG ) {
                   Log.d( "cache_test_DISK_", "image put on disk cache " + createKey(url) );
                }
            } else {
                editor.abort();
                if ( BuildConfig.DEBUG ) {
                    Log.d( "cache_test_DISK_", "ERROR on: image put on disk cache " + createKey(url) );
                }
            }   
        } catch (IOException e) {
            if ( BuildConfig.DEBUG ) {
                Log.d( "cache_test_DISK_", "ERROR on: image put on disk cache " + createKey(url) );
            }
            try {
                if ( editor != null ) {
                    editor.abort();
                }
            } catch (IOException ignored) {
            }           
        }
    }
    
    private boolean writeBitmapToFile(Bitmap bitmap, DiskLruCache.Editor editor )
            throws IOException, FileNotFoundException {
            OutputStream out = null;
            try {
                out = new BufferedOutputStream( editor.newOutputStream( 0 ), IO_BUFFER_SIZE );
                return bitmap.compress( mCompressFormat, mCompressQuality, out );
            } finally {
                if ( out != null ) {
                    out.close();
                }
            }
        }

    public File getDiskCacheDir(Context context) {
    	//Find the dir to save cached images
    	File cacheDir;
    	
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
            cacheDir=context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        else
            cacheDir=context.getCacheDir();
        
        if(!cacheDir.exists())
            cacheDir.mkdirs();
        
        return cacheDir;
    }
        
    public boolean containsKey( String key ) {

        boolean contained = false;
        DiskLruCache.Snapshot snapshot = null;
        try {
            snapshot = mDiskCache.get( key );
            contained = snapshot != null;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if ( snapshot != null ) {
                snapshot.close();
            }
        }

        return contained;

    }

    public void clearCache() {
        if ( BuildConfig.DEBUG ) {
            Log.d( "cache_test_DISK_", "disk cache CLEARED");
        }
        try {
            mDiskCache.delete();
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }

    public File getCacheFolder() {
        return mDiskCache.getDirectory();
    }
    
    private String createKey(String url){
		return String.valueOf(url.hashCode());
	}
}