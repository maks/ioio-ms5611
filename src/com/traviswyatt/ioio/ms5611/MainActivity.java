package com.traviswyatt.ioio.ms5611;

import ioio.lib.api.IOIO;
import ioio.lib.api.TwiMaster;
import ioio.lib.api.TwiMaster.Rate;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;
import android.os.Bundle;
import android.view.Menu;
import android.widget.TextView;

import com.traviswyatt.ioio.ms5611.MS5611.OversamplingRatio;

public class MainActivity extends IOIOActivity {

	private TextView ioioStatusText;
	private TextView pressureText;
	private TextView temperatureText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		ioioStatusText = (TextView) findViewById(R.id.ioio_status);
		pressureText = (TextView) findViewById(R.id.pressure);
		temperatureText = (TextView) findViewById(R.id.temperature);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	/**
	 * A method to create our IOIO thread.
	 * 
	 * @see ioio.lib.util.AbstractIOIOActivity#createIOIOThread()
	 */
	@Override
	protected IOIOLooper createIOIOLooper() {
		int twiNum = 0;
		Rate rate = TwiMaster.Rate.RATE_100KHz;
		OversamplingRatio osr = OversamplingRatio.OSR_4096;
		final MS5611 ms5611 = new MS5611(twiNum, rate, osr);
		ms5611.setListener(new MS5611.MS5611Listener() {
			@Override
			public void onData(int P, int TEMP) {
				updateTextView(pressureText, "Pressure = " + (P / 100f) + " mbar");
				updateTextView(temperatureText, "Temperature = " + (TEMP / 100f) + " C");
			}
			@Override
			public void onError(String message) {
				System.out.println(message);
			}
		});
		return new DeviceLooper(ms5611);
	}
	
	private void updateTextView(final TextView textView, final String text) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				textView.setText(text);
			}
		});
	}
	
	/**
	 * This is the thread on which all the IOIO activity happens. It will be run
	 * every time the application is resumed and aborted when it is paused. The
	 * method setup() will be called right after a connection with the IOIO has
	 * been established (which might happen several times!). Then, loop() will
	 * be called repetitively until the IOIO gets disconnected.
	 */
	class DeviceLooper implements IOIOLooper {
		
		private IOIOLooper device;

		public DeviceLooper(IOIOLooper device) {
			this.device = device;
		}
		
		@Override
		public void setup(IOIO ioio) throws ConnectionLostException, InterruptedException {
			updateTextView(ioioStatusText, "IOIO Connected");
			device.setup(ioio);
		}

		/**
		 * Called repetitively while the IOIO is connected.
		 * 
		 * @throws ConnectionLostException
		 *             When IOIO connection is lost.
		 * @throws InterruptedException 
		 * 
		 * @see ioio.lib.util.AbstractIOIOActivity.IOIOThread#loop()
		 */
		@Override
		public void loop() throws ConnectionLostException, InterruptedException {
			device.loop();
		}

		@Override
		public void disconnected() {
			updateTextView(ioioStatusText, "IOIO Disconnected");
			device.disconnected();
		}

		@Override
		public void incompatible() {
			// deprecated
		}

		@Override
		public void incompatible(IOIO ioio) {
			updateTextView(ioioStatusText, "IOIO Incompatible");
			device.incompatible(ioio);
		}
	}

}
