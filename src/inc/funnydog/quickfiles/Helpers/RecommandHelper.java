package inc.funnydog.quickfiles.Helpers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

public class RecommandHelper {
    
    private static final String CONFIG_FILE = "http://users14.jabry.com/kimikan/apps/controls/engadget2.htm";
    
    
    public static void updateLatestRecommand() throws MalformedURLException, IOException {
        Document document = Jsoup.parse(new URL(CONFIG_FILE), 10000);
        Elements elements = document.select("li#version");

        if(elements.size() > 0) {
            Element versionElement = elements.first();
            VersionCode = Integer.parseInt(versionElement.text().trim());
            
            if(VersionCode > getCurrentVersionCode(null)) {
                elements = document.select("li#apk-location");
                if(elements.size() > 0) {
                    String apkLocation = elements.first().text();
                    Log.i("xxx", apkLocation);
                    
                }
            } //end if
        }
    }
    
    public static int VersionCode;
    
    public static int getCurrentVersionCode(Context context) {
        try {
            return context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        
        return 0;
    }
    
    public static File downLoadApk(String location) {
        
        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection = null;
        try {
            URL url = new URL(location);
            //url.get
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                Log.i("xxxx", connection.getResponseCode() + " ");
                return null;
            }

            input = connection.getInputStream();
            File outFile = new File(Environment.getDownloadCacheDirectory()
                    , location.substring(location.lastIndexOf('/' + 1)));
            output = new FileOutputStream(outFile);

            byte data[] = new byte[4096];
            int count;
            while ((count = input.read(data)) != -1) {
                output.write(data, 0, count);
            }
            
            Log.i("xxxx", outFile.getAbsolutePath());
            return outFile;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (output != null)
                    output.close();
                if (input != null)
                    input.close();
            } 
            catch (IOException ignored) { }

            if (connection != null)
                connection.disconnect();
        }
        
        return null;
    }
    
    public static void doPrecheck(Context context) throws Exception {
        Document document = Jsoup.parse(new URL(CONFIG_FILE), 10000);
        Elements elements = document.select("li#version");

        if(elements.size() > 0) {
            Element versionElement = elements.first();
            VersionCode = Integer.parseInt(versionElement.text().trim());
            
            if(VersionCode > getCurrentVersionCode(context)) {
                elements = document.select("li#apk-location");
                if(elements.size() > 0) {
                    String apkLocation = elements.first().text();
                    Log.i("xxx", apkLocation);
                    
                }
            } //end if
        }
    }
    
    public static void installApk(Context context, File file) {
        if(file != null && file.getName().endsWith(".apk")) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(
                    Uri.parse(file.getAbsolutePath()),
                    "application/vnd.android.package-archive");
            context.startActivity(intent);
        }
    }
}
