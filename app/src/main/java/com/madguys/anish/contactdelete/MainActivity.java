package com.madguys.anish.contactdelete;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    EditText query;
    TextView result;
    Button button;
    int count;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        query = (EditText) findViewById(R.id.edit_query);
        result = (TextView) findViewById(R.id.result);
        button = (Button) findViewById(R.id.button);
        progressBar = (ProgressBar) findViewById(R.id.progress);
        count = 0;
        if (Build.VERSION.SDK_INT >= 23)
            insertDummyContactWrapper();
        else
            insertDummyContact();


    }

    public void onResume() {
        super.onResume();
        query = (EditText) findViewById(R.id.edit_query);
        result = (TextView) findViewById(R.id.result);
        button = (Button) findViewById(R.id.button);
        count = 0;
    }

    public void insertDummyContact() {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!query.getText().toString().toLowerCase().equals("")) {
                    //progressBar.setVisibility(View.VISIBLE);
                    count = 0;
                    new LongOperation().execute(query.getText().toString().toLowerCase());
                } else {
                    Toast.makeText(MainActivity.this, "Fill some substring, else all contacts can be deleted", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    final private int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124;

    //  @TargetApi(Build.VERSION_CODES.M)


    public class LongOperation extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... params) {

            Log.i("Here", "inside");
            ContentResolver cr = getContentResolver();
            Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

            int total = 0;
            ArrayList<Uri> urii = new ArrayList<>();
            Log.i("Total", total + "");
            try {
                while (cur.moveToNext()) {
                    try {
                        String lookupKey = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
                        Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookupKey);
                        //System.out.println("The uri is " + uri.toString());
                        String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                        if (name.toLowerCase().contains(params[0])) {
//

                            publishProgress(-1);
                            urii.add(uri);
                            total++;

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        //System.out.println(e.getStackTrace());
                    }
                }
                if(total > 0) {
                    for (int i = 0; i < total; i++) {
                        //
                        Uri uri = urii.get(i);
                        System.out.println("The uri is " + uri);
                        cr.delete(uri, null, null);
                        count++;
                        publishProgress((count * 100) / total, count);

                    }
                }else{


                    publishProgress(-2);
                }

            } catch (Exception e1) {
                e1.printStackTrace();
            }

            return "";
        }

        @Override
        protected void onPostExecute(String res) {
           // result.setText(count + " contacts deleted");
            progressBar.setVisibility(View.GONE);
            button.setClickable(true);
        }

        @Override
        protected void onPreExecute() {

            progressBar.setVisibility(View.VISIBLE);
            button.setClickable(false);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            //txt.setText("Running..."+ values[0]);
            progressBar.setMax(100);
            if (values[0] >= 0) {
                progressBar.setIndeterminate(false);
                progressBar.setProgress(values[0]);
                result.setText(values[1] + " contacts deleted...");
            } else if (values[0] == -1){
                progressBar.setIndeterminate(true);
                result.setText("Fetching details..");

            }else{
                result.setText("No contact deleted");
                progressBar.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, "No contact found", Toast.LENGTH_SHORT).show();
            }
        }


    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    private void insertDummyContactWrapper() {
        List<String> permissionsNeeded = new ArrayList<String>();

        final List<String> permissionsList = new ArrayList<String>();

        if (!addPermission(permissionsList, Manifest.permission.READ_CONTACTS))
            permissionsNeeded.add("Read Contacts");
        if (!addPermission(permissionsList, Manifest.permission.WRITE_CONTACTS))
            permissionsNeeded.add("Write Contacts");

        if (permissionsList.size() > 0) {
            if (permissionsNeeded.size() > 0) {
                // Need Rationale
                String message = "You need to grant access to " + permissionsNeeded.get(0);
                for (int i = 1; i < permissionsNeeded.size(); i++)
                    message = message + ", " + permissionsNeeded.get(i);

                requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
                        REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);

                return;
            }
            requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
                    REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
            return;
        }

        insertDummyContact();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean addPermission(List<String> permissionsList, String permission) {
        if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);
            // Check for Rationale Option
            if (!shouldShowRequestPermissionRationale(permission))
                return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS: {
                Map<String, Integer> perms = new HashMap<String, Integer>();
                // Initial
                perms.put(Manifest.permission.READ_CONTACTS, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.WRITE_CONTACTS, PackageManager.PERMISSION_GRANTED);
                // Fill with results
                for (int i = 0; i < permissions.length; i++)
                    perms.put(permissions[i], grantResults[i]);
                // Check for ACCESS_FINE_LOCATION
                if (perms.get(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
                        && perms.get(Manifest.permission.WRITE_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                    // All Permissions Granted
                    insertDummyContact();
                } else {
                    // Permission Denied
                    Toast.makeText(MainActivity.this, "Some Permission is Denied", Toast.LENGTH_SHORT)
                            .show();
                }
            }
            break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


}
