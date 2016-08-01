package com.polyglotprogramminginc.andevconboston2016metawearclass;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.mbientlab.metawear.AsyncOperation;
import com.mbientlab.metawear.Message;
import com.mbientlab.metawear.MetaWearBoard;
import com.mbientlab.metawear.RouteManager;
import com.mbientlab.metawear.UnsupportedModuleException;
import com.mbientlab.metawear.module.Logging;
import com.mbientlab.metawear.module.MultiChannelTemperature;
import com.mbientlab.metawear.module.Timer;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class ThermistorFragment extends Fragment {



    private MetaWearBoard metaWearBoard;
    private ListView temperatureList;
    private ArrayList temperatureItemList;
    private MultiChannelTemperature mcTempModule;
    private Timer.Controller timer;
    private boolean streaming = false;

    public ThermistorFragment() {
        // Required empty public constructor
    }

    private final RouteManager.MessageHandler streamingMessageHandler = new RouteManager.MessageHandler() {
        @Override
        public void process(final Message msg) {
            Log.i("MainActivity", String.format("Ext thermistor: %.3fC",
                    msg.getData(Float.class)));
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((TextView) getView().findViewById(R.id.temperature_reading)).setText(String.valueOf(msg.getData(Float.class)));
                }
            });
        }
    };


    private final AsyncOperation.CompletionHandler<RouteManager> temperatureHandler = new AsyncOperation.CompletionHandler<RouteManager>() {
        @Override
        public void success(RouteManager result) {
            result.subscribe("mystream", streamingMessageHandler);

            // Read temperature from the NRF soc chip
            try {
                AsyncOperation<Timer.Controller> taskResult = metaWearBoard.getModule(Timer.class)
                        .scheduleTask(new Timer.Task() {
                            @Override
                            public void commands() {
                                mcTempModule.readTemperature(mcTempModule.getSources().get(MultiChannelTemperature.MetaWearRChannel.NRF_DIE));
                            }
                        }, 50, false);
                taskResult.onComplete(new AsyncOperation.CompletionHandler<Timer.Controller>() {
                    @Override
                    public void success(Timer.Controller result) {
                        // start executing the task
                        timer = result;
                        result.start();
                    }
                });
            } catch (UnsupportedModuleException e) {
                Log.e("Temperature Fragment", e.toString());
            }

        }
    };


    public void setMetaWearBoard(MetaWearBoard metaWearBoard) {
        this.metaWearBoard = metaWearBoard;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_thermistor, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        final Button startStopTemperature = (Button) getView().findViewById(R.id.startStopTemperature);

        startStopTemperature.setOnClickListener(
                new Button.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (streaming) {
                            startStopTemperature.setText(R.string.start_temperature_stream);
                            stopTemperatureStreaming();
                        } else {
                            startStopTemperature.setText(R.string.stop_temperature_stream);
                            startTemperatureStreaming();
                        }
                    }
                }
        );
    }

    public void startTemperatureStreaming() {
        try {
            streaming = true;
            if (mcTempModule == null) {
                mcTempModule = metaWearBoard.getModule(MultiChannelTemperature.class);
                List<MultiChannelTemperature.Source> tempSources = mcTempModule.getSources();

                MultiChannelTemperature.Source tempSource = tempSources.get(MultiChannelTemperature.MetaWearRChannel.NRF_DIE);
                mcTempModule.routeData().fromSource(tempSource).stream("mystream")
                        .commit().onComplete(temperatureHandler);
            }

        } catch (UnsupportedModuleException e) {
            Log.e("Thermistor Fragment", e.toString());
        }
    }

    public void stopTemperatureStreaming() {
        streaming = false;
        timer.stop();
    }
}
