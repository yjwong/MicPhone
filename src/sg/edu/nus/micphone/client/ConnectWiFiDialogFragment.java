package sg.edu.nus.micphone.client;

import org.androidannotations.annotations.EFragment;

import sg.edu.nus.micphone.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;

/**
 * The dialog fragment that asks the user to connect to a WiFi network.
 * This dialog fragment is shown when the user is not connected.
 */
@EFragment
public class ConnectWiFiDialogFragment extends DialogFragment {
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Use the builder class for convenient dialog construction.
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage(R.string.must_connect_wifi)
			.setTitle(R.string.wifi_disconnected)
			.setPositiveButton(R.string.connect, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
					startActivity(intent);
				}
			})
			.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					getActivity().finish();
				}
			});
		
		return builder.create();
	}
	
	@Override
	public void onCancel(DialogInterface dialog) {
		getActivity().finish();
	}
}
