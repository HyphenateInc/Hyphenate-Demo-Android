package io.agora.easeui.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;


import java.io.File;

import io.agora.chatdemo.PreferenceManager;
import io.agora.util.FileHelper;
import io.agora.util.EMLog;

public class EaseFileUtils {
    private static final String TAG = EaseFileUtils.class.getSimpleName();

    private static boolean isQ() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;
    }

    public static boolean isFileExistByUri(Context context, Uri fileUri) {
        return FileHelper.getInstance().isFileExist(fileUri);
    }

    /**
     * Delete file by uri
     * @param context
     * @param uri
     */
    public static void deleteFile(Context context, Uri uri) {
        if(isFileExistByUri(context, uri)) {
            String filePath = getFilePath(context, uri);
            if(!TextUtils.isEmpty(filePath)) {
                File file = new File(filePath);
                if(file != null && file.exists() && file.isFile()) {
                    file.delete();
                }
            }else {
                try {
                    context.getContentResolver().delete(uri, null, null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Get file name
     * @param context
     * @param fileUri
     * @return
     */
    public static String getFileNameByUri(Context context, Uri fileUri) {
        return FileHelper.getInstance().getFilename(fileUri);
    }

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri     The Uri to query.
     * @author paulburke
     */
    public static String getFilePath(final Context context, final Uri uri) {
        return FileHelper.getInstance().getFilePath(uri);
    }

    /**
     * Check if start with "file"
     * @param fileUri
     * @return
     */
    public static boolean uriStartWithFile(Uri fileUri) {
        return "file".equalsIgnoreCase(fileUri.getScheme()) && fileUri.toString().length() > 7;
    }

    /**
     * Check if start with "content"
     * @param fileUri
     * @return
     */
    public static boolean uriStartWithContent(Uri fileUri) {
        return "content".equalsIgnoreCase(fileUri.getScheme());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * Check if the local FileProvider
     * @param context
     * @param uri
     * @return
     */
    public static boolean isFileProvider(Context context, Uri uri) {
        return (context.getApplicationInfo().packageName + ".fileProvider").equalsIgnoreCase(uri.getAuthority());
    }

    /**
     * Other file provider
     * @param context
     * @param uri
     * @return
     */
    public static boolean isOtherFileProvider(Context context, Uri uri) {
        String scheme = uri.getScheme();
        String authority = uri.getAuthority();
        if(TextUtils.isEmpty(scheme) || TextUtils.isEmpty(authority)) {
            return false;
        }
        return !(context.getApplicationInfo().packageName + ".fileProvider").equalsIgnoreCase(uri.getAuthority())
                && "content".equalsIgnoreCase(uri.getScheme())
                && authority.contains(".fileProvider".toLowerCase());
    }

    public static boolean saveUriPermission(Context context, Uri fileUri, Intent intent) {
        if(context == null || fileUri == null) {
            return false;
        }
        if(!uriStartWithContent(fileUri)) {
            return false;
        }
        int intentFlags = 0;
        if(intent != null) {
            intentFlags = intent.getFlags();
        }
        int takeFlags = intentFlags & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        String last = null;
        try {
            context.getContentResolver().takePersistableUriPermission(fileUri, takeFlags);
            last = getLastSubFromUri(fileUri);
            EMLog.d(TAG, "saveUriPermission last part of Uri: "+last);
        } catch (SecurityException e) {
            EMLog.e("EaseFileUtils", "saveUriPermission failed e: "+e.getMessage());
        }
        if(!TextUtils.isEmpty(last)) {
            PreferenceManager.getInstance().putString(last, fileUri.toString());
            return true;
        }
        return false;
    }

    private static String getLastSubFromUri(Uri fileUri) {
        if(fileUri == null) {
            return "";
        }
        String uri = fileUri.toString();
        if(!uri.contains("/")) {
            return "";
        }
        int lastIndex = uri.lastIndexOf("/");
        return uri.substring(lastIndex + 1);
    }

    /**
     * Get permanent read permission
     * @param context
     * @param uri
     * @return
     */
    public static Uri takePersistableUriPermission(Context context, Uri uri) {
        if(context == null || uri == null) {
            return null;
        }
        if(!uriStartWithContent(uri)) {
            return null;
        }
        String last = getLastSubFromUri(uri);
        if(!TextUtils.isEmpty(last)) {
            String fileUri = PreferenceManager.getInstance().getString(last);
            if(!TextUtils.isEmpty(fileUri)) {
                try {
                    context.getContentResolver().takePersistableUriPermission(Uri.parse(fileUri), Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    return Uri.parse(fileUri);
                } catch (SecurityException e) {
                    EMLog.e("EaseFileUtils", "takePersistableUriPermission failed e: "+e.getMessage());
                    return null;
                }
            }
        }
        try {
            context.getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } catch (SecurityException e) {
            EMLog.e("EaseFileUtils", "takePersistableUriPermission failed e: "+e.getMessage());
            return null;
        }
        return uri;
    }
}

