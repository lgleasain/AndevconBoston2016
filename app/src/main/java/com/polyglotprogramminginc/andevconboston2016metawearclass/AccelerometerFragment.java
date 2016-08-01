package com.polyglotprogramminginc.andevconboston2016metawearclass;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.mbientlab.metawear.AsyncOperation;
import com.mbientlab.metawear.Message;
import com.mbientlab.metawear.MetaWearBoard;
import com.mbientlab.metawear.RouteManager;
import com.mbientlab.metawear.UnsupportedModuleException;
import com.mbientlab.metawear.data.CartesianFloat;
import com.mbientlab.metawear.module.Accelerometer;
import com.mbientlab.metawear.module.Bmi160Accelerometer;


/**
 * A simple {@link Fragment} subclass.
 */
public class AccelerometerFragment extends Fragment {

    private MetaWearBoard metaWearBoard;
    private Accelerometer accelerometer;
    private boolean accelerometerStarted = false;
    private boolean stepCounterStarted = false;

    public AccelerometerFragment() {
        // Required empty public constructor
    }

    public void setMetaWearBoard(MetaWearBoard metaWearBoard) {
        this.metaWearBoard = metaWearBoard;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_accelerometer, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        ((Button) getView().findViewById(R.id.start_accelerometer)).setOnClickListener(
                new Button.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (accelerometerStarted == false) {
                            accelerometerStarted = true;
                            ((Button) getView().findViewById(R.id.start_accelerometer)).setText(R.string.stop_accelerometer);
                            startAccelerometer();
                        } else {
                            accelerometerStarted = false;
                            ((Button) getView().findViewById(R.id.start_accelerometer)).setText(R.string.start_accelerometer);
                            accelerometer.stop();
                        }
                    }
                }
        );

    }

    private void startAccelerometer() {
        try {
            accelerometer = metaWearBoard.getModule(Accelerometer.class);
            accelerometer.routeData()
                    .fromAxes().stream("accel_stream_key")
                    .commit().onComplete(new AsyncOperation.CompletionHandler<RouteManager>() {
                @Override
                public void success(RouteManager result) {
                    result.subscribe("accel_stream_key", new RouteManager.MessageHandler() {
                        @Override
                        public void process(Message msg) {
                            final CartesianFloat axes = msg.getData(CartesianFloat.class);
                            Log.i("MainActivity", axes.toString());
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ((TextView) getView().findViewById(R.id.x_reading)).setText(String.valueOf(axes.x()));
                                    ((TextView) getView().findViewById(R.id.y_reading)).setText(String.valueOf(axes.y()));
                                    ((TextView) getView().findViewById(R.id.z_reading)).setText(String.valueOf(axes.z()));
                                }
                            });

                        }
                    });
                }
            });
            accelerometer.enableAxisSampling();
            accelerometer.start();
        } catch (UnsupportedModuleException e) {
            Log.e("MainActivity", "No accelerometer present on this board", e);
        }
    }
}
