package online.danshub.dan.maplarm;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.net.URISyntaxException;

public class AlarmChooserActivity extends AppCompatActivity {

    private static final int FILE_SELECT_CODE = 0;
    private static final String TAG = "AUDIO FILE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");

        startActivityForResult(Intent.createChooser(intent, "Choose Alarm Sound"), FILE_SELECT_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case FILE_SELECT_CODE:
                if (resultCode == RESULT_OK) {
                    // Get the Uri of the selected file
                    Uri uri = data.getData();
                    Log.d(TAG, "File Uri: " + uri.toString());
                    // Get the path
                    String path = uri.getPath();
                    Log.d(TAG, "File Path: " + path);

                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

                    preferences.edit().putString("alarm_file_path", uri.toString()).apply();

                    Log.v(TAG, "New Alarm tone set" + path);
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
        startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
    }

}
