package online.danshub.dan.maplarm;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

/**
 * A simple {@link Fragment} subclass.
 */
public class MarkerDialogFragment extends DialogFragment {
    private final static String TAG = "MarkerSaveDialog";
    private EditText markerName;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();

        View dialogView = inflater.inflate(R.layout.add_marker_dialog, null);

        markerName = dialogView.findViewById(R.id.markername);

        //Log.v(TAG, markerName.getText().toString());

        builder.setView(dialogView)
                .setPositiveButton(R.string.add_marker, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String markerText = markerName.getText().toString();
                        Log.v(TAG, "Marker Positive Dialog Clicked: " +
                                "Position: " + MapsActivity.currentMarker.getPosition()
                            + " Name: " + markerText
                        );

                        Marker marker = new Marker();
                        marker.latitude = MapsActivity.currentMarker.getPosition().latitude;
                        marker.longitude = MapsActivity.currentMarker.getPosition().longitude;
                        marker.markerName = markerText;

                        MapsActivity.db.markerDao().insertMarker(marker);
                    }
                })
                .setNegativeButton(R.string.close_add_marker, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        MarkerDialogFragment.this.getDialog().cancel();
                    }
                });
        return builder.create();

    }
}
