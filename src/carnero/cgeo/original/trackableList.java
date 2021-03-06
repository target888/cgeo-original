package carnero.cgeo.original;

import carnero.cgeo.original.libs.App;
import carnero.cgeo.original.models.Trackable;
import carnero.cgeo.original.libs.Settings;
import carnero.cgeo.original.libs.Base;
import carnero.cgeo.original.libs.Warning;
import java.util.ArrayList;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

public class trackableList extends Activity {
	private ArrayList<Trackable> trackables = new ArrayList<Trackable>();
	private String geocode = null;
	private App app = null;
	private Settings settings = null;
	private Base base = null;
	private Warning warning = null;
	private Activity activity = null;
	private LayoutInflater inflater = null;
	private LinearLayout addList = null;
	private ProgressDialog waitDialog = null;
	private Handler loadInventoryHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			try {
				if (inflater == null) {
					inflater = activity.getLayoutInflater();
				}

				if (addList == null) {
					addList = (LinearLayout) findViewById(R.id.trackable_list);
				}

				if (trackables.isEmpty()) {
					if (waitDialog != null) {
						waitDialog.dismiss();
					}

					warning.showToast("Sorry, c:geo failed to load cache inventory.");

					finish();
					return;
				} else {
					LinearLayout oneTbPre = null;
					for (Trackable trackable : trackables) {
						oneTbPre = (LinearLayout) inflater.inflate(R.layout.trackable_button, null);

						Button oneTb = (Button) oneTbPre.findViewById(R.id.button);

						if (trackable.name != null) {
							oneTb.setText(Html.fromHtml(trackable.name).toString());
						} else {
							oneTb.setText("some trackable");
						}
						oneTb.setClickable(true);
						oneTb.setOnClickListener(new buttonListener(trackable.guid, trackable.geocode, trackable.name));
						addList.addView(oneTbPre);
					}
				}

				if (waitDialog != null) {
					waitDialog.dismiss();
				}
			} catch (Exception e) {
				if (waitDialog != null) {
					waitDialog.dismiss();
				}
				Log.e(Settings.tag, "cgeotrackables.loadInventoryHandler: " + e.toString());
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// init
		activity = this;
		app = (App) this.getApplication();
		settings = new Settings(this, getSharedPreferences(Settings.preferences, 0));
		base = new Base(app, settings, getSharedPreferences(Settings.preferences, 0));
		warning = new Warning(this);

		// set layout
		if (settings.skin == 1) {
			setTheme(R.style.light);
		} else {
			setTheme(R.style.dark);
		}
		setContentView(R.layout.trackables);
		base.setTitle(activity, "Trackables");

		// google analytics
		base.sendAnal(activity, "/trackable/list");

		// get parameters
		Bundle extras = getIntent().getExtras();

		// try to get data from extras
		if (extras != null) {
			geocode = extras.getString("geocode");
		}

		if (geocode == null) {
			warning.showToast("Sorry, c:geo forgot for what cache you want to load trackables.");
			finish();
			return;
		}

		waitDialog = ProgressDialog.show(this, null, "loading cache inventory...", true);
		waitDialog.setCancelable(true);

		(new loadInventory()).start();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		settings.load();
	}

	private class loadInventory extends Thread {

		@Override
		public void run() {
			try {
				trackables = app.loadInventory(geocode);

				loadInventoryHandler.sendMessage(new Message());
			} catch (Exception e) {
				Log.e(Settings.tag, "cgeotrackables.loadInventory.run: " + e.toString());
			}
		}
	}

	private class buttonListener implements View.OnClickListener {

		private String guid = null;
		private String geocode = null;
		private String name = null;

		public buttonListener(String guidIn, String geocodeIn, String nameIn) {
			guid = guidIn;
			geocode = geocodeIn;
			name = nameIn;
		}

		public void onClick(View arg0) {
			Intent trackableIntent = new Intent(activity, trackableDetail.class);
			trackableIntent.putExtra("guid", guid);
			trackableIntent.putExtra("geocode", geocode);
			trackableIntent.putExtra("name", name);
			activity.startActivity(trackableIntent);

			finish();
			return;
		}
	}

	public void goHome(View view) {
		base.goHome(activity);
	}
}