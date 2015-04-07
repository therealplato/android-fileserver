// Thx komamitsu!
// https://gist.github.com/komamitsu/1893396

package com.therealplato.fileserver;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import fi.iki.elonen.NanoHTTPD;

public class AndroidWebServerActivity extends ActionBarActivity {
    //private static final int PORT = 8765;
    private TextView hello;
    private MyHTTPD server;
    //private Handler handler = new Handler();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_android_web_server);
        hello = (TextView) findViewById(R.id.hello);
        listFiles();

    }


    public void listFiles(){
        //EditText editText = (EditText) findViewById(R.id.root_folder);
        //String path = editText.getText().toString();
        //Uri selectedUri = Uri.parse(+"");
        File root = Environment.getExternalStorageDirectory();
        String[] filenames = root.list();
        Log.i("FileserverActivity", "First file: " + filenames[0]);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, filenames);
        ListView listView = (ListView) findViewById(R.id.filenameslist);
        listView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        TextView textIpaddr = (TextView) findViewById(R.id.ipaddr);

        /*
        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        int ipAddress = wifiManager.getConnectionInfo().getIpAddress();
        final String formatedIpAddress = String.format("%d.%d.%d.%d", (ipAddress & 0xff), (ipAddress >> 8 & 0xff),
                (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));

        */

        String ipAddress = null;
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        //ipAddress = inetAddress.getHostAddress().toString();
                        ipAddress = inetAddress.getHostAddress();
                        textIpaddr.setText("Please access! http://" + ipAddress + ":" + "8765");
                    }
                }
            }
        } catch (SocketException ex) {}


        try {
            server = new MyHTTPD();
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (server != null)
            server.stop();
    }

    private class MyHTTPD extends NanoHTTPD {
        private static final String QUERY_STRING_PARAMETER = "NanoHttpd.QUERY_STRING";

        public MyHTTPD() throws IOException {
            super("0.0.0.0", 8765);
        }

        @Override
        public Response serve(IHTTPSession session) {
            Map<String, String> files = new HashMap<String, String>();
            Method method = session.getMethod();
            if (Method.PUT.equals(method) || Method.POST.equals(method)) {
                try {
                    session.parseBody(files);
                } catch (IOException ioe) {
                    return new Response(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, "SERVER INTERNAL ERROR: IOException: " + ioe.getMessage());
                } catch (ResponseException re) {
                    return new Response(re.getStatus(), MIME_PLAINTEXT, re.getMessage());
                }
            }

            Map<String, String> parms = session.getParms();
            parms.put(QUERY_STRING_PARAMETER, session.getQueryParameterString());
            //return serve(session.getUri(), method, session.getHeaders(), parms, files);
            return new Response(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "Not Found");

        }
    }
}