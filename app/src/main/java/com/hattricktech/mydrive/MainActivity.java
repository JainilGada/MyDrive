package com.hattricktech.mydrive;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SyncStatusObserver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;

import jcifs.Config;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;
import jcifs.smb.SmbFileOutputStream;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener{

    private static final int UPLOAD_FILE_REQUEST_CODE = 10;
    private static final int BROWSE_REQUEST = 1 ;
    private static String HOST_NAME = "192.168.101.65";
    private static String SHARE = "MyDrive_12";
    private static String PASSWORD = "jainilgada";
    private static String USERNAME = "jainil";
    private static String FOLDER_NAME = "2014130012";
    private static String DOMAIN = "SDrive";
    private static NtlmPasswordAuthentication auth;
    String URL = String.format("smb://%s/%s/", HOST_NAME, SHARE);
    ArrayList<SmbFile> smbFileArrayList;
    FileSystemCustomAdapter fileSystemCustomAdapter;
    ListView fileSystemListView;

    // String url = "smb://172.16.30.15/homes/yash"
    //+ String.valueOf(y) + ".txt";


    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        if(intent.getStringExtra("url")!=null)
        {
            URL = intent.getStringExtra("url");
        }
        System.out.println("onCreate : url "+URL);
        smbFileArrayList = new ArrayList<SmbFile>();

        fileSystemCustomAdapter = new FileSystemCustomAdapter();
        fileSystemListView = (ListView) findViewById(R.id.lv_fileSystem);
        fileSystemListView.setOnItemClickListener(this);
        verifyStoragePermissions(this);

        auth = new NtlmPasswordAuthentication(DOMAIN, USERNAME, PASSWORD);
        new BrowseFile().execute();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_menu,menu);
        return true;
    }

    public void uploadFile(MenuItem menuItem)
    {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent,UPLOAD_FILE_REQUEST_CODE);
    }

    /*public void browseFile(View view)
    {
        new BrowseFile().execute();
    }*/

    public void downloadFile(MenuItem menuItem)
    {
        new DownloadFile().execute("");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK)
        {
            switch (requestCode)
            {
                case UPLOAD_FILE_REQUEST_CODE :
                    Uri uri = data.getData();
                  new UploadFile().execute(uri);
                    break;
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    SmbFile smb_file = smbFileArrayList.get(position);

        try{

            System.out.println("PAth in on click "+smb_file.getPath());
            System.out.println("Tag :" + view.getTag() );
            String tag = (String) view.getTag();
            if(tag.charAt(0)=='F')
            {
                Toast.makeText(this, "File", Toast.LENGTH_SHORT).show();
            }else
            {

                Toast.makeText(this, "Directory", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this,MainActivity.class);
                intent.putExtra("url",smb_file.getPath());
                startActivity(intent);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    class UploadFile extends AsyncTask<Uri,String,String>
    {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Uri... params) {
            Uri uri = params[0];
            System.out.println("In do in background");
            try {
                String URL = String.format("smb://%s/%s/%s", HOST_NAME, SHARE,getFileName(uri));
                System.out.println(URL);

                InputStream iStream =   getContentResolver().openInputStream(uri);
                byte[] inputData = getBytes(iStream);
                SmbFile smbFile = new SmbFile(URL,auth);
                smbFile.createNewFile();
                SmbFileOutputStream smbFileOutputStream = new SmbFileOutputStream(smbFile,true);
                smbFileOutputStream.write(inputData);

                /*File fromCopyFile = new File(uri.getPath().toString());
                FileInputStream fileInputStream = new FileInputStream(fromCopyFile);
                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                SmbFile smbFile = new SmbFile(URL,auth);
                smbFile.createNewFile();
                SmbFileOutputStream out = new SmbFileOutputStream(smbFile, true);



                StringBuffer stringBuffer = new StringBuffer("");
                String dataline;
                while((dataline = bufferedReader.readLine())!=null)
                {
                    stringBuffer.append(dataline);
                    System.out.println(dataline);
                    out.write(dataline.getBytes());
                }
                bufferedReader.close();
                inputStreamReader.close();
                fileInputStream.close();*/


                //System.out.println("File Location : " + fromCopyFile);


            }
            catch (Exception e)
            {
                e.printStackTrace();
                System.out.println(e.getMessage().toString());
               // Toast.makeText(MainActivity.this, e.getMessage().toString(), Toast.LENGTH_SHORT).show();
            }


            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
        }
    }

    class BrowseFile extends AsyncTask<Void,String,String>
    {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {
            try {

                System.out.println("Browse File url : "+URL);
                SmbFile smbFile = new SmbFile(URL,auth);
                if (smbFile.isDirectory())
                {
                    System.out.println(smbFile.getName());
                    SmbFile smbFiles[] = smbFile.listFiles();
                    for(int i = 0; i<smbFiles.length;i++)
                    {
                        smbFileArrayList.add(smbFiles[i]);
                        System.out.println(smbFiles[i].getPath());

                        fileSystemCustomAdapter.notifyDataSetChanged();
                    }

                }



            }catch (Exception e)
            {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            //super.onPostExecute(s);

            fileSystemListView.setAdapter(fileSystemCustomAdapter);
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
        }
    }

    class DownloadFile extends AsyncTask<String,String,String>
    {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            String URL = "smb://"+HOST_NAME+"/MyDrive_12/jj.txt";
            try {
                SmbFile smbFile = new SmbFile(URL,auth);
                SmbFileInputStream smbFileInputStream = new SmbFileInputStream(smbFile);
                byte bytes[] = new byte[10000];
                while (smbFileInputStream.read(bytes)>0)
                {
                    for (int i =0;i<bytes.length;i++)
                        System.out.print(bytes[i]);
                }
                //smbFileInputStream.read();

            }catch (Exception e)
            {

            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
        }
    }

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            System.out.println("Permission is not granted");
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
        else
        {
            System.out.println("Permission Granted");
        }
    }


    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {

                }
                return;
            }

        }
    }


    class FileSystemCustomAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return smbFileArrayList.size();
        }

        @Override
        public Object getItem(int position) {
            return smbFileArrayList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            SmbFile smb_file = smbFileArrayList.get(position);
            LayoutInflater inflater = getLayoutInflater();
            System.out.println("IN");
            View view = null;
            try {
                if(smb_file.isDirectory())
                {
                   view = inflater.inflate(R.layout.list_item_directory,null);
                    TextView directoryTV = (TextView) view.findViewById(R.id.tv_directory_name);
                    directoryTV.setText(smb_file.getName());
                    view.setTag(new String("D"));

                }
                else
                {
                    view = inflater.inflate(R.layout.list_item_file,null);
                    TextView fileTv = (TextView) view.findViewById(R.id.tv_file_name);
                    fileTv.setText(smb_file.getName());

                    view.setTag(new String("F"));
                }
            } catch (SmbException e) {
                e.printStackTrace();
            }

            return view;
        }
    }

    public byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }
}
