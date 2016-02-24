package com.cool.app.puppetandroid;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);

        File testedFile = new File(this.getFilesDir().getAbsolutePath() + "/python/bin/python");
        if(!testedFile.exists())
        {
            createDirectoryOnExternalStorage(this.getPackageName());
            copyResourcesToLocal();
        }

        startService(new Intent(this, MyService.class));
        finish();
    }


    private static int chmod(File path, int mode) throws Exception
    {
        Class<?> fileUtils = Class.forName("android.os.FileUtils");
        Method setPermissions =
                fileUtils.getMethod("setPermissions", String.class, int.class, int.class, int.class);
        return (Integer) setPermissions.invoke(null, path.getAbsolutePath(), mode, -1, -1);
    }

    private static void createDirectoryOnExternalStorage(String path)
    {
        try {
            File file = new File(Environment.getExternalStorageDirectory(), path);
            if (!file.exists()) {
                try {
                    file.mkdirs();
                } catch (Exception e){
                }
            }

        } catch (Exception e) {

        }

    }

    private void copyResourcesToLocal()
    {
        String name, sFileName;
        InputStream content;

        R.raw a = new R.raw();
        java.lang.reflect.Field[] t = R.raw.class.getFields();
        Resources resources = getResources();

        boolean succeed = true;

        for (int i = 0; i < t.length; i++) {
            try {
                name = resources.getText(t[i].getInt(a)).toString();
                sFileName = name.substring(name.lastIndexOf('/') + 1, name.length());
                content = getResources().openRawResource(t[i].getInt(a));
                content.reset();

                // python -> /data/data/com.android.python27/files/python
                if (sFileName.endsWith("python_27.zip")) {
                    succeed &= unzip(content, this.getFilesDir().getAbsolutePath()+ "/", true);
                    chmod(new File(this.getFilesDir().getAbsolutePath() + "/python/bin/python"), 0755);
                }
                // python extras -> /sdcard/com.android.python27/extras/python
                else if (sFileName.endsWith("python_extras_27.zip")) {
                    createDirectoryOnExternalStorage( this.getPackageName() + "/" + "extras");
                    createDirectoryOnExternalStorage(this.getPackageName() + "/" + "extras" + "/" + "tmp");
                    succeed &= unzip(content, Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + this.getPackageName() + "/extras/", true);
                }

            } catch (Exception e) {
                succeed = false;
            }
        } // end for all files in res/raw
    }

    public static boolean unzip(InputStream inputStream, String dest, boolean replaceIfExists)
    {

        final int BUFFER_SIZE = 4096;

        BufferedOutputStream bufferedOutputStream = null;

        boolean succeed = true;

        try {
            ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(inputStream));
            ZipEntry zipEntry;

            while ((zipEntry = zipInputStream.getNextEntry()) != null){

                String zipEntryName = zipEntry.getName();

                File file2 = new File(dest + zipEntryName);

                if(file2.exists()) {
                    if (replaceIfExists) {

                        try {
                            boolean b = deleteDir(file2);
                        } catch (Exception e) {
                        }
                    }
                }

                // extract
                File file = new File(dest + zipEntryName);

                if (file.exists()){

                } else {
                    if(zipEntry.isDirectory()){
                        file.mkdirs();
                        chmod(file, 0755);

                    }else{

                        // create parent file folder if not exists yet
                        if(!file.getParentFile().exists()) {
                            file.getParentFile().mkdirs();
                            chmod(file.getParentFile(), 0755);
                        }

                        byte buffer[] = new byte[BUFFER_SIZE];
                        bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file), BUFFER_SIZE);
                        int count;

                        while ((count = zipInputStream.read(buffer, 0, BUFFER_SIZE)) != -1) {
                            bufferedOutputStream.write(buffer, 0, count);
                        }

                        bufferedOutputStream.flush();
                        bufferedOutputStream.close();
                    }
                }

                // enable standalone python
                if(file.getName().endsWith(".so") || file.getName().endsWith(".xml") || file.getName().endsWith(".py") || file.getName().endsWith(".pyc") || file.getName().endsWith(".pyo")) {
                    chmod(file, 0755);
                }
            }

            zipInputStream.close();

        }catch (Exception e) {
            succeed = false;
        }
        return succeed;
    }

    private static boolean deleteDir(File dir)
    {
        try {
            if (dir.isDirectory()) {
                String[] children = dir.list();
                for (int i=0; i<children.length; i++) {
                    boolean success = deleteDir(new File(dir, children[i]));
                    if (!success) {
                        return false;
                    }
                }
            }

            return dir.delete();

        } catch (Exception e) {
            return false;
        }
    }
}
