package com.cool.app.puppetandroid;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.widget.Toast;

import com.googlecode.android_scripting.Exec;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class MyService extends Service {









    public MyService()
    {

    }

    @Override
    public IBinder onBind(Intent intent)
    {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        try {
            //Socket cs = new Socket("127.0.0.1",7777);
            //PrintWriter out = new PrintWriter(cs.getOutputStream());
            //BufferedReader in = new BufferedReader(new InputStreamReader(cs.getInputStream()));

            //out.println("Android wants to connect");

            //String pythonScript = in.readLine();
            String pythonScript = "import android, time\n" +
                    "\n" +
                    "droid = android.Android()\n" +
                    "\n" +
                    "while 1:\n" +
                    "\tdroid.makeToast(\"Hello from Python 2.7 for Android\")\n" +
                    "\ttime.sleep(5)";

            Toast.makeText(this,"Creating script..",Toast.LENGTH_SHORT).show();//
            File f = new File(this.getFilesDir().getAbsolutePath() + "/myscript.py");
            PrintWriter fo = new PrintWriter(new BufferedWriter(new FileWriter(f, true)));
            fo.write(pythonScript);
            fo.close();

            String[] args = {"myscript.py", "--foreground"};

            File pythonBinary = new File(this.getFilesDir().getAbsolutePath() + "/python/bin/python");


            Map<String, String> environmentVariables = null;
            environmentVariables = new HashMap<String, String>();
            environmentVariables.put("PYTHONPATH", Environment.getExternalStorageDirectory().getAbsolutePath()+ "/" + this.getPackageName() + "/extras/python" + ":" + this.getFilesDir().getAbsolutePath() + "/python/lib/python2.7/lib-dynload" + ":" + this.getFilesDir().getAbsolutePath() + "/python/lib/python2.7");
            environmentVariables.put("TEMP", Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + this.getPackageName() + "/extras/tmp");
            environmentVariables.put("PYTHONHOME", this.getFilesDir().getAbsolutePath() + "/python");
            environmentVariables.put("LD_LIBRARY_PATH", this.getFilesDir().getAbsolutePath() + "/python/lib" + ":" + this.getFilesDir().getAbsolutePath() + "/python/lib/python2.7/lib-dynload");
            List<String> environmentV = new ArrayList<String>();
            for (Map.Entry<String, String> entry : environmentVariables.entrySet()) {
                environmentV.add(entry.getKey() + "=" + entry.getValue());
            }
            String[] environment = environmentV.toArray(new String[environmentVariables.size()]);

            int[] pid = new int[1];

            Toast.makeText(this,"Executing script..",Toast.LENGTH_SHORT).show();//
            FileDescriptor a = Exec.createSubprocess(f.getAbsolutePath(), args, environment, null, pid);
            Toast.makeText(this,a.toString(),Toast.LENGTH_SHORT).show();//

            //int myPid = pid[0];

        } catch (IOException e) {
            e.printStackTrace();
        }

        Toast.makeText(this,"DONE!!!",Toast.LENGTH_SHORT).show();//
        return Service.START_STICKY;
    }
}
