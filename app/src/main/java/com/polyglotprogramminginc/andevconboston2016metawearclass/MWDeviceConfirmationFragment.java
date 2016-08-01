package com.polyglotprogramminginc.andevconboston2016metawearclass;


import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.mbientlab.metawear.MetaWearBoard;
import com.mbientlab.metawear.UnsupportedModuleException;
import com.mbientlab.metawear.module.Led;


/**
 * A simple {@link Fragment} subclass.
 */
public class MWDeviceConfirmationFragment extends DialogFragment {

    private Led ledModule = null;
    private Button yesButton = null;
    private Button noButton = null;
    private DeviceConfirmCallback callback = null;

    public interface DeviceConfirmCallback {
        void pairDevice();
        void dontPairDevice();
    }

    public MWDeviceConfirmationFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_mwdevice_confirmation, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        noButton = (Button) view.findViewById(R.id.confirm_no);
        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ledModule.stop(true);
                callback.dontPairDevice();
                dismiss();
            }
        });

        yesButton = (Button) view.findViewById(R.id.confirm_yes);
        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ledModule.stop(true);
                callback.pairDevice();
                dismiss();
            }
        });

    }

    @Override
    public void onAttach(Activity activity) {
        if (!(activity instanceof DeviceConfirmCallback)) {
            throw new RuntimeException("Acitivty does not implement DeviceConfirmationCallback interface");
        }

        callback= (DeviceConfirmCallback) activity;
        super.onAttach(activity);
    }

    public void flashDeviceLight(MetaWearBoard mwBoard, FragmentManager fragmentManager) {
        try {
            ledModule = mwBoard.getModule(Led.class);
        } catch (UnsupportedModuleException e) {
            Log.e("Led Fragment", e.toString());
        }
        ledModule.configureColorChannel(Led.ColorChannel.BLUE)
                .setRiseTime((short) 750).setPulseDuration((short) 2000)
                .setRepeatCount((byte) -1).setHighTime((short) 500)
                .setFallTime((short) 750).setLowIntensity((byte) 0)
                .setHighIntensity((byte) 31).commit();

        ledModule.play(true);

        show(fragmentManager, "device_confirm_callback");
    }


}
