package inc.funnydog.quickfiles.FileExplorer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.apache.commons.io.FileUtils;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Parcelable;
import android.text.format.Formatter;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.j256.ormlite.dao.RuntimeExceptionDao;
import inc.funnydog.quickfiles.R;
import inc.funnydog.quickfiles.DB.BookMark;
import inc.funnydog.quickfiles.DB.DatabaseHelper;
import inc.funnydog.quickfiles.FileExplorer.Utilities.ZipHelper;
import inc.funnydog.quickfiles.Helpers.ProgressableTask;
import inc.funnydog.quickfiles.Helpers.StringsHelper;

public class FileHelper {

    public final static String ZIP_EXTENTION = ".zip";
    
    public interface IOperationCallback {
        void onFinished(Context context, Object obj);
    }
    
    public static boolean isZipFile(File file) {
        if(file == null)
            return false;
        
        if(file.getName().endsWith(ZIP_EXTENTION)) {
            return true;
        }
        return false;
    }
    
    private static String generateFolderName(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if(dotIndex > 0 && dotIndex < fileName.length()) {
            return fileName.substring(0, dotIndex);
        }
        
        return StringsHelper.getString(R.string.filemgr_default_zip_folder);
    }
    
    public static void extractZipFile(List<DisplayableFile> files, 
            final Context context, final IOperationCallback callback) {
        final File firstFile = files.get(0).getFile();
        new ProgressableTask<Void, Void, Void>(context) {
            boolean isOk = false;
            @Override
            protected Void doInBackground(Void... params) {
                isOk = ZipHelper.decompress(firstFile, generateFolderName(firstFile.getName()));
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
                if(isOk) {
                    Toast.makeText(context, R.string.filemgr_extract_zip_success, Toast.LENGTH_SHORT).show();
                    if(callback != null) {
                        callback.onFinished(context, null);
                    }
                } else {
                    Toast.makeText(context, R.string.filemgr_extract_zip_failed, Toast.LENGTH_SHORT).show();
                }
            }
            
        }.execute();
    }
    private static void zipFiles(List<DisplayableFile> files, 
            final Context context, final IOperationCallback callback, final File zipFile) {
        final List<File> tempFiles = new ArrayList<File>();
        for(DisplayableFile file : files) {
            if(file.getFile() != null && file.getFile().exists()) {
                tempFiles.add(file.getFile());
            }
        }
        new ProgressableTask<Void, Void, Void>(context) {
            boolean isOk = false;
            @Override
            protected Void doInBackground(Void... params) {
                isOk = ZipHelper.compress(tempFiles, zipFile);
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
                if(isOk) {
                    Toast.makeText(context, R.string.filemgr_compress_success, 
                            Toast.LENGTH_SHORT).show();
                    if(callback != null) {
                        callback.onFinished(context, null);
                    }
                } else {
                    Toast.makeText(context, R.string.filemgr_compress_failed, 
                            Toast.LENGTH_SHORT).show();
                }
            }
            
        }.execute();
    }
    
    public static boolean zipFiles(final List<DisplayableFile> files, 
            final Context context, final IOperationCallback callback) {
        LayoutInflater inflater = LayoutInflater.from(context);
        final EditText v = (EditText) inflater.inflate(R.layout.dialog_text_input, null);
        
        final DisplayableFile firstFile = files.get(0);
        v.setText(firstFile.getFile().getParentFile().getName() + ZIP_EXTENTION);
        
        final AlertDialog dialog = new AlertDialog.Builder(context)
        .setInverseBackgroundForced(true)
        .setTitle(R.string.filemgr_compress_title)
        .setIcon(firstFile.getIconDrawable(context))
        .setView(v)
        .setNegativeButton(android.R.string.cancel, null)
        .setPositiveButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                            int which) {
                        String input = v.getText().toString();
                        if(input.length() > 0 ) {
                            File targetFile = new File(firstFile.getFile().getParentFile(), input);
                            if(targetFile.exists()) {
                                targetFile.delete();
                            }
                            zipFiles(files, context, callback, 
                                    targetFile);
                        }
                        
                    }
                }).create();
        v.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
                if (arg1 == EditorInfo.IME_ACTION_GO) {
                    String input = v.getText().toString();
                    if(input.length() > 0 ) {
                        File targetFile = new File(firstFile.getFile().getParentFile(), input);
                        if(targetFile.exists()) {
                            targetFile.delete();
                        }
                        zipFiles(files, context, callback, 
                                targetFile);
                    }
                    dialog.dismiss();
                }
                return false;
            }
        });
        dialog.show();
        return false;
    }
    
    public static String getMimeType(File file) {
        if(file == null) {
            return "*/*";
        }
        
        String[][] mimeTypes = {{".png","image/png"} 
                                , {".gif", "image/gif"} , {".jpg", "image/jpeg"}
                                , {".jpeg", "image/jpeg"} , {".bmp","image/bmp"}
                                , {".mp3", "audio/mpeg"} , {".wav", "audio/x-wav"}
                                , {".ogg", "application/ogg"} , {".mid", "audio/midi"}
                                , {".midi", "audio/midi"} , {".amr", "audio/amr"}
                                , {".aac", "audio/x-aac"} , {".mpeg", "video/mpeg"}
                                , {".3gp", "video/3gpp"}
                                , {".jar", "application/java-archive"}, {".zip", "application/zip"}
                                , {".rar", "application/rar"}, {".gz", "application/gzip"}
                                , {".htm", "text/html"}, {".html", "text/html"}
                                , {".php", "text/php"}, {".txt", "text/plain"}
                                , {".csv", "text/comma-separated-values"}, {".xml","text/xml"}
                                , {".apk", "application/vnd.android.package-archive"} };
        
        String name = file.getName().toLowerCase(Locale.US);
        for(int i = 0; i < mimeTypes.length; ++i) {
            if(name.endsWith(mimeTypes[i][0])) {
                return mimeTypes[i][1];
            }
        }

        String extension = file.getName().substring(file.getName().lastIndexOf((int)'.'));
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
    }
    
    public static void openFile(File file, Context c) {
        if(c == null || file == null) {
            return;
        }
        
        //FileUtils.
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW);

        Uri data = Uri.fromFile(file);
        String type = getMimeType(file);
        
        if ("*/*".equals(type)){
            Toast.makeText(c, R.string.filemgr_open_failed, Toast.LENGTH_SHORT).show();
            return;
        }
        
        intent.setDataAndType(data, type);

        try {
            c.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(c, R.string.filemgr_open_failed, Toast.LENGTH_SHORT).show();
        }
    }
    
    public static void sendFiles(Context context, List<DisplayableFile> files) {
        if(context == null || files == null) {
            return;
        }
        
        ArrayList<Uri> imageUris = new ArrayList<Uri>();
        for(DisplayableFile file : files) {
            imageUris.add(Uri.fromFile(file.getFile()));
        }

        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
        shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris);
        shareIntent.setType("*/*");
        context.startActivity(Intent.createChooser(shareIntent, 
                StringsHelper.getString(R.string.filemgr_share_files_with)));
    }
    
    
    public static boolean isRootFolder(File file) {
        if(file == null)
            return false;
        
        if(file.isDirectory()) {
            return file.getAbsolutePath().equals("/");
        }
        
        return false;
    }
    
    private static DecimalFormat decimalFormat = new DecimalFormat("#,##0.#");
    public static String readableFileSize(long paramLong) {
        if (paramLong <= 0L)
            return "0";
        
        String[] arrayOfString = { "B", "KB", "MB", "GB", "TB" };
        int i = (int)(Math.log10(paramLong) / Math.log10(1024.0D));
        return decimalFormat.format(paramLong / Math.pow(1024.0D, i)) + " " + arrayOfString[i];
    }
    
    public static void sortFiles(File[] files) {

        if(files != null) {
            Arrays.sort(files, new Comparator<File>() {

                @Override
                public int compare(File arg0, File arg1) {
                    boolean bDir1 = arg0.isDirectory();
                    boolean bDir2 = arg1.isDirectory();
                    if(bDir1 && bDir2) {
                        return arg0.getName().compareToIgnoreCase(arg1.getName());
                    } else if(bDir1 && !bDir2) {
                        return -1000;
                    } else if(!bDir1 && bDir2) {
                        return 1000;
                    } else {
                        return arg0.getName().compareToIgnoreCase(arg1.getName());
                    }
                }
                
            });
        }
    }
    
    public static void deleteFiles(final Context context, 
            final List<DisplayableFile> files, 
            final IOperationCallback callback) {

        new AlertDialog.Builder(context)
                .setInverseBackgroundForced(true)
                .setTitle(String.format(StringsHelper.getString(R.string.filemgr_delete_files), files.size()))
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                            new ProgressableTask<File, Void, Void> (context){
                                private int mResult = 1;

                                private void recursiveDelete(File file) {
                                    File[] files = file.listFiles();
                                    if (files != null && files.length != 0) 
                                        for (File childFile : files) {
                                            if (childFile.isDirectory()) {
                                                recursiveDelete(childFile);
                                            } else {
                                                mResult *= childFile.delete() ? 1 : 0;
                                            }
                                        }
                                        
                                        mResult *= file.delete() ? 1 : 0;
                                }
                                @Override
                                protected void onPostExecute(Void result) {
                                    super.onPostExecute(result);
                                    Toast.makeText(context, 
                                            mResult == 0 
                                            ? R.string.filemgr_delete_files_failed 
                                                    : R.string.filemgr_delete_files_success, 
                                                    Toast.LENGTH_LONG).show();
   
                                    if(callback != null) {
                                        callback.onFinished(context, null);
                                    }
                                }

                                @Override
                                protected Void doInBackground(File... arg0) {
                                    for(DisplayableFile fh : files)
                                        recursiveDelete(fh.getFile());
                                    return null;
                                }
                            }.execute();
                    }
                })
                .setIcon(files.get(0).getIconDrawable(context))
                .setNegativeButton(android.R.string.cancel, null)
                .create().show();
        
    }
    
    public static void renameFile(final Context context, 
            final DisplayableFile file, final IOperationCallback callback) {
        LayoutInflater inflater = LayoutInflater.from(context);
        final EditText v = (EditText) inflater.inflate(R.layout.dialog_text_input, null);
        v.setText(file.getName());
        final AlertDialog dialog = new AlertDialog.Builder(context)
        .setInverseBackgroundForced(true)
        .setTitle(R.string.filemgr_rename)
        .setIcon(file.getIconDrawable(context))
        .setView(v)
        .setNegativeButton(android.R.string.cancel, null)
        .setPositiveButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                            int which) {
                        renameFile(context, file.getFile(), v.getText().toString(), callback);

                    }
                }).create();
        v.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
                if (arg1 == EditorInfo.IME_ACTION_GO) {
                    renameFile(context, file.getFile(), v.getText().toString(), callback);
                    dialog.dismiss();
                }
                return false;
            }
        });
        dialog.show();
    }
    
    private static void renameFile(Context context, File file, String to, 
            IOperationCallback callback){
        try {
            if(to.length() > 0){
                File dest = new File(file.getParent() + File.separator + to);
                if(!dest.exists()){
                    if(file.renameTo(dest)) {
                        Toast.makeText(context, R.string.filemgr_rename_success
                                , Toast.LENGTH_SHORT).show();
                        if(callback != null) {
                            callback.onFinished(context, null);
                        }
                    } else {
                        Toast.makeText(context, R.string.filemgr_rename_failed
                                , Toast.LENGTH_SHORT).show();
                    }
                }
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public static void createShortcut(List<DisplayableFile> files, Context context) {
        Intent shortcutintent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
        
        for(DisplayableFile file : files) {
            //shortcutintent.setClassName(context, context.getClass().getName());
            
            shortcutintent.putExtra(Intent.EXTRA_SHORTCUT_NAME, file.getName());
            Parcelable icon = Intent.ShortcutIconResource.fromContext(context.getApplicationContext(), 
                    file.getIconRes());
            shortcutintent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);

            // Intent to load
            Intent itl = new Intent(Intent.ACTION_VIEW);
            Uri uri = Uri.fromFile(file.getFile());
            itl.setData(uri);
            itl.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            
            shortcutintent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, itl);
            context.sendBroadcast(shortcutintent);
            
        }
        
    }
    
    
    public static void showDetails(final Context context, DisplayableFile file) {
        // Inflate the view to display
        final File f = file.getFile();
        LayoutInflater inflater = LayoutInflater.from(context);
        final View v = inflater.inflate(R.layout.dialog_details, null);
        
        // Fill the views
        ((TextView) v.findViewById(R.id.details_type_value))
            .setText((f.isDirectory() ? R.string.filemgr_details_folder_type 
                    : (f.isFile() ? R.string.filemgr_details_file_type 
                            : R.string.filemgr_details_other_type) ));
        
        final TextView mSizeView = (TextView) v.findViewById(R.id.details_size_value);
        new ProgressableTask<Void, Void, String>(context) {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mSizeView.setText(R.string.filemgr_details_loading);
            }
            
            @Override
            protected String doInBackground(Void... params) {
                long size = 0;
                if (f.isDirectory())
                    size = FileUtils.sizeOfDirectory(f);
                else
                    size = f.length();
                
                return Formatter.formatFileSize(context, size);
            }
            
            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);
                mSizeView.setText(result);
            }
        }.execute();
        
        String perms = (f.canRead() ? "R" : "-") + (f.canWrite() ? "W" : "-") + (f.canExecute() ? "X" : "-");
        ((TextView) v.findViewById(R.id.details_permissions_value)).setText(perms);
        
        ((TextView) v.findViewById(R.id.details_hidden_value)).setText(f.isHidden() 
                ? R.string.filemgr_details_hidden_type : R.string.filemgr_details_hidden_type_no);
        
        ((TextView) v.findViewById(R.id.details_lastmodified_value)).setText(file.getLastModifiedTime());
        
        // Finally create the dialog
        new AlertDialog.Builder(context)
                .setInverseBackgroundForced(true)
                .setTitle(file.getName())
                .setIcon(file.getIconDrawable(context))
                .setView(v)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                    int which) {
                                dialog.dismiss();
                            }
                        }).create().show();
    }
    
    
    public static File uri2File(Uri uri) {
        
        return new File(uri.getPath());
    }
    
    public static void newFolder(final File parent, final Context context
            , final IOperationCallback callback) {
        
        LayoutInflater inflater = LayoutInflater.from(context);
        final EditText v = (EditText) inflater.inflate(
                R.layout.dialog_text_input, null);
        
        final AlertDialog dialog = new AlertDialog.Builder(context)
        .setInverseBackgroundForced(true)
        .setTitle(R.string.filemgr_new_folder)
        .setIcon(android.R.drawable.ic_dialog_alert)
        .setView(v)
        .setPositiveButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog,
                            int which) {
                        createFolder(v.getText(), parent, context, callback);
                        dialog.dismiss();
                    }
                }).setNegativeButton(android.R.string.cancel, null)
                  .create();
        
        v.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView text, int actionId,
                    KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    createFolder(text.getText(), parent, context, callback);
                    dialog.dismiss();
                }
                return true;
            }
        });

        dialog.show();
    }

    private static void createFolder(final CharSequence text
            , File parent, Context c
            , final IOperationCallback callback) {
        if (text.length() != 0) {
            File tbcreated = new File(parent, text.toString());
            if (tbcreated.exists()) {
                Toast.makeText(c, R.string.filemgr_folder_exist, Toast.LENGTH_SHORT).show();
            } else {
                if (tbcreated.mkdirs()) {
                    if(callback != null) {
                        callback.onFinished(c, null);
                    }
                    Toast.makeText(c, "Success", Toast.LENGTH_SHORT).show();
                }
                else
                    Toast.makeText(c, "Failed", Toast.LENGTH_SHORT).show();
            }
        }
    }
   
    
    public static void addBookMarks(final Context context, final List<DisplayableFile> files) {
        new ProgressableTask<Void, Void, Boolean>(context) {
            
            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    RuntimeExceptionDao<BookMark, Integer> dao = 
                            DatabaseHelper.instance().getBookMarkDataDao();
                    
                    for(DisplayableFile file : files) {
                        BookMark bookmark = new BookMark();
                        bookmark.BookmarkName = file.getName();
                        bookmark.BookmarkPath = file.getFile().getAbsolutePath();
                        dao.create(bookmark);
                    }
                } catch(Exception ex) {
                    ex.printStackTrace();
                    return false;
                }
                
                return true;
            }
            
            @Override
            protected void onPostExecute(Boolean result) {
                super.onPostExecute(result);
                Toast.makeText(context, result.booleanValue() 
                        ? R.string.bookmarks_add_success : R.string.bookmarks_add_failed
                                , Toast.LENGTH_LONG).show();
            }
        }.execute();
    }
    
    
    public static File createUniqueCopyName(Context context, File path, 
            String fileName) {
        // Does that file exist?
        File file = new File(path, fileName);
        if (!file.exists()) {
            return file;
        }
        
        // Split file's name and extension to fix internationalization issue #307
        int fromIndex = fileName.lastIndexOf('.');
        String extension = "";
        if (fromIndex > 0) {
            extension = fileName.substring(fromIndex);
            fileName = fileName.substring(0, fromIndex);
        }
        
        int copyIndex = 2;
        while(copyIndex < 400) {
            file = new File(path, (fileName + copyIndex) + extension);
            if (!file.exists()) {
                return file;
            }
            ++copyIndex;
        }
        
        return null;
    }   
    
    private final static int COPY_BUFFER_SIZE = 2048;
    private static boolean copyFile(File oldFile, File newFile) {
        try {
            FileInputStream input = new FileInputStream(oldFile);
            FileOutputStream output = new FileOutputStream(newFile);
        
            byte[] buffer = new byte[COPY_BUFFER_SIZE];
            while (true) {
                int bytes = input.read(buffer);
                
                if (bytes <= 0) {
                    break;
                }
                
                output.write(buffer, 0, bytes);
            }
            output.close();
            input.close();
            
        } catch (Exception e) {
            return false;
        }
        return true;
    }
    
    private static boolean copyFolder(File oldFile, File newFile) {
        boolean res = true;
        
        if (oldFile.isDirectory()) {
            // if directory not exists, create it
            if (!newFile.exists()) {
                newFile.mkdir();
            }

            String files[] = oldFile.list();
            for (String file : files) {
                File srcFile = new File(oldFile, file);
                File destFile = new File(newFile, file);
                res &= copyFolder(srcFile, destFile);
            }
        } else {
            res &= copyFile(oldFile, newFile);
        }
        
        return res;
    }
    
    public static void performCopyFiles(final File targetFolder, final Context context
            , final IOperationCallback callback) {
        
        new ProgressableTask<File, Void, Boolean>(context) {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }
            
            @Override
            protected Boolean doInBackground(File... params) {
                boolean res = true;
                for(DisplayableFile fh : ClipBoard.instance().getFiles()){
                    if(fh.getFile().isFile()) {
                        res &= copyFile(fh.getFile(), 
                                createUniqueCopyName(context, targetFolder, fh.getName()));
                    } else {
                        res &= copyFolder(fh.getFile(), 
                                createUniqueCopyName(context, targetFolder, fh.getName()));
                    }
                }
                
                return res;
            }
            
            @Override
            protected void onPostExecute(Boolean result) {
                super.onPostExecute(result);
                Toast.makeText(context, result ? R.string.filemgr_copy_success 
                        : R.string.filemgr_copy_failed, Toast.LENGTH_SHORT).show();
                
                ClipBoard.instance().setFiles(null, false);

                if(callback != null) {
                    callback.onFinished(context, result);
                }
            }
        }.execute();
    }
    
    public static void performCutFiles(final File targetFolder, final Context context
            , final IOperationCallback callback) {
        
        new ProgressableTask<File, Void, Boolean>(context) {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }
            
            @Override
            protected Boolean doInBackground(File... params) {
                boolean res = true;
                for(DisplayableFile fh : ClipBoard.instance().getFiles()){
                    res &= fh.getFile().renameTo(new File(targetFolder, fh.getName()));
                }
                
                return res;
            }
            
            @Override
            protected void onPostExecute(Boolean result) {
                super.onPostExecute(result);
                Toast.makeText(context, result ? R.string.filemgr_cut_success 
                        : R.string.filemgr_cut_failed, Toast.LENGTH_SHORT).show();
                
                ClipBoard.instance().setFiles(null, false);

                if(callback != null) {
                    callback.onFinished(context, result);
                }
            }
        }.execute();
    }
}
