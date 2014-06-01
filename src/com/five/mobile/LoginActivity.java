package com.five.mobile;
/**
 * The first time one uses five, they have to login to the server using this
 * activity
 */

import java.io.*;
import java.util.*;
import java.net.*;
import android.os.*;
import android.app.*;
import android.view.*;
import android.content.*;
import android.location.*;
import android.widget.*;
import android.util.Log;
import com.google.android.gms.common.*;
import com.google.android.gms.location.*;

public class LoginActivity extends Activity
{
    private EditText handleEdit = null;
    private EditText passwordEdit = null;
    private FiveClient fiveClient = null;
    private SharedPreferences sharedPrefs = null;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        handleEdit = (EditText) findViewById(R.id.handle_edit);
        Log.d("five", "handleEdit: " + handleEdit);
        passwordEdit = (EditText) findViewById(R.id.password_edit);
        Log.d("five", "passwordEdit: " + passwordEdit);
        sharedPrefs = getSharedPreferences(Constants.FIVE_SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        fiveClient = Utilities.getFiveClient(sharedPrefs);
    }

    /**
     * Called when the login button is tapped.
     */
    public void login(View view)
    {
        String handle = handleEdit.getText().toString(),
               password = passwordEdit.getText().toString();
        try
        {
            fiveClient.login(handle, password, new FiveClient.ResultHandler() {
                @Override
                public void handle(Object ret) 
                {
                    Response response = (Response) ret;
                    if (response != null)
                    {
                        if (response.statusCode / 100 == 2)
                        {
                            Utilities.setSessionActive(sharedPrefs, true);
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                        }
                        else
                        {
                            Log.d("five", "Invalid login");
                            Toast.makeText(LoginActivity.this, "Invalid login", Toast.LENGTH_SHORT);
                        }
                    }
                    else
                    {
                        Log.d("five", "LoginTask.onPostExecute(): NULL response for login");
                        return;
                    }
                }
            });
            Utilities.setWaiting(this, "Logging in...");
        }
        catch (IOException e)
        {
            setContentView(R.layout.login);
            Log.d("five", "IO error in login: " + e.toString());
        }
    }
}
