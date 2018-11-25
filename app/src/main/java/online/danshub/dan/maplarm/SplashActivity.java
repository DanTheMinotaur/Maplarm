package online.danshub.dan.maplarm;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v("SPLASH", "I'm in the splash screen");
        //Toast.makeText(getApplicationContext(), "I'm in the SPLASH SCREEN!", Toast.LENGTH_LONG).show();
        startActivity(new Intent(SplashActivity.this, MapsActivity.class));
        finish();
    }
}
