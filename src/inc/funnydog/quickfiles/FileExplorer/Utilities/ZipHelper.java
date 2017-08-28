package inc.funnydog.quickfiles.FileExplorer.Utilities;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class ZipHelper {
    
    public static final int BUFFER_SIZE = 2048;
    
    private static void compressFile(ZipOutputStream zos, File file, String path) 
            throws IOException {
        
        if (!file.isDirectory()){
            byte[] buf = new byte[BUFFER_SIZE];
            int len;
            FileInputStream in = new FileInputStream(file);
            if(path.length() > 0)
                zos.putNextEntry(new ZipEntry(path + "/" + file.getName()));
            else
                zos.putNextEntry(new ZipEntry(file.getName()));
            while ((len = in.read(buf)) > 0) {
                zos.write(buf, 0, len);
            }
            in.close();
            return;
        }
        if (file.list() == null){
            return;
        }
        for (String fileName: file.list()){
            File f = new File(file.getAbsolutePath() + File.separator + fileName);
            compressFile(zos, f, path + File.separator + file.getName());
        }
    }
    
    public static boolean compress(List<File> files, File zipFile) {
        ZipOutputStream zos = null;
        try {
            zos = new ZipOutputStream(new FileOutputStream(zipFile));
        
            for(File file : files) {
                compressFile(zos, file, "");
            } 
        } catch(Exception ex) {
            ex.printStackTrace();
            return false;
        } finally {
            try {
                zos.close();
            } catch(Exception e) {}
        }
        
        return true;
    }
    
    public static boolean decompress(File file, String targetName) {
        
        return extract(file, file.getParent() + File.separator + targetName);
    }
    
    public static boolean extract(File archive, String destinationPath) {
        try {
            ZipFile zipfile = new ZipFile(archive);
            for (Enumeration<?> e = zipfile.entries(); e.hasMoreElements();) {
                ZipEntry entry = (ZipEntry) e.nextElement();
                unzipEntry(zipfile, entry, destinationPath);
            }
            
            return true;
        } catch (Exception e) {
            return false;
        }
    }   
    
    private static void createDir(File dir) {
        if (dir.exists()) {
            return;
        }
        if (!dir.mkdirs()) {
            throw new RuntimeException("Can not create dir " + dir);
        }
    }   
    
    private static void unzipEntry(ZipFile zipfile, ZipEntry entry,
            String outputDir) throws IOException {
        if (entry.isDirectory()) {
            createDir(new File(outputDir, entry.getName()));
            return;
        }
        File outputFile = new File(outputDir, entry.getName());
        if (!outputFile.getParentFile().exists()) {
            createDir(outputFile.getParentFile());
        }
        
        BufferedInputStream inputStream = new BufferedInputStream(zipfile.getInputStream(entry));
        BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFile));
        try {
            int len;
            byte buf[] = new byte[BUFFER_SIZE];
            while ((len = inputStream.read(buf)) > 0) {
                outputStream.write(buf, 0, len);
            }
        } finally {
            outputStream.close();
            inputStream.close();
        }
    }
}
