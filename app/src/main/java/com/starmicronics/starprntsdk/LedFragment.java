package com.starmicronics.starprntsdk;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.starmicronics.starprntsdk.Communication.CommunicationResult;

import com.starmicronics.starioextension.ICommandBuilder;
import com.starmicronics.starioextension.ILedCommandBuilder.Led;

import com.starmicronics.starioextension.StarIoExt;
import com.starmicronics.starioextension.StarIoExt.LedModel;
import com.starmicronics.starprntsdk.functions.LedFunctions;
import com.starmicronics.starprntsdk.functions.PrinterFunctions;
import com.starmicronics.starprntsdk.localizereceipts.ILocalizeReceipts;

public class LedFragment extends Fragment implements CommonAlertDialogFragment.Callback {

    private static final String ERROR_DIALOG = "ErrorDialog";

    static final private String AUTOMATIC_BLINK_LED_CONTROL[] = {
            "Printing and Error",
            "Printing",
            "Error",
            "Disable",
    };

    static final private String BLINK_LED_CONTROL[] = {
            "Printing",
            "Error",
    };

    private Spinner mAutomaticBlinkLedControlSpinner;
    private EditText mSetPrintingLedOnTimeEditText;
    private EditText mSetPrintingLedOffTimeEditText;
    private EditText mSetErrorLedOnTimeEditText;
    private EditText mSetErrorLedOffTimeEditText;

    private Spinner mBlinkLedControlSpinner;
    private EditText mRepeatCountEditText;
    private EditText mBlinkLedOnTimeEditText;
    private EditText mBlinkLedOffTimeEditText;

    private ProgressDialog mProgressDialog;

    private PrinterSettings mPrinterSettings;

    private boolean mIsForeground;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        View rootView = inflater.inflate(R.layout.fragment_led, container, false);

        createLedViews(rootView);

        mProgressDialog = new ProgressDialog(getActivity());

        mProgressDialog.setMessage("Communicating...");
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(false);

        PrinterSettingManager settingManager = new PrinterSettingManager(getActivity());
        mPrinterSettings = settingManager.getPrinterSettings();

        return rootView;
    }

    private void createLedViews(View rootView) {
        mAutomaticBlinkLedControlSpinner = rootView.findViewById(R.id.automaticBlinkLedControlSpinner);

        ArrayAdapter<String> automaticBlinkArrayAdapter = new ArrayAdapter<>(
                getActivity(),
                android.R.layout.simple_spinner_item,
                AUTOMATIC_BLINK_LED_CONTROL);

        automaticBlinkArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mAutomaticBlinkLedControlSpinner.setAdapter(automaticBlinkArrayAdapter);

        mSetPrintingLedOnTimeEditText = rootView.findViewById(R.id.setPrintingLedOnTimeEditText);
        mSetPrintingLedOffTimeEditText = rootView.findViewById(R.id.setPrintingLedOffTimeEditText);

        mSetErrorLedOnTimeEditText = rootView.findViewById(R.id.setErrorLedOnTimeEditText);
        mSetErrorLedOffTimeEditText = rootView.findViewById(R.id.setErrorLedOffTimeEditText);

        Button testPrintButton = rootView.findViewById(R.id.testPrintButton);
        testPrintButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                print();
            }
        });

        mBlinkLedControlSpinner = rootView.findViewById(R.id.blinkLedControlSpinner);

        ArrayAdapter<String> blinkArrayAdapter = new ArrayAdapter<>(
                getActivity(),
                android.R.layout.simple_spinner_item,
                BLINK_LED_CONTROL);

        blinkArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mBlinkLedControlSpinner.setAdapter(blinkArrayAdapter);

        mRepeatCountEditText = rootView.findViewById(R.id.repeatCountEditText);
        mBlinkLedOnTimeEditText = rootView.findViewById(R.id.blinkLedOnTimeEditText);
        mBlinkLedOffTimeEditText = rootView.findViewById(R.id.blinkLedOffTimeEditText);

        Button blinkButton = rootView.findViewById(R.id.blinkButton);
        blinkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                blinkLed();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        mIsForeground = true;
    }

    @Override
    public void onPause() {
        super.onPause();

        mIsForeground = false;

        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

    private void print() {
        mProgressDialog.show();

        // Create commands for LED setting.
        int printingLedOnTime;

        try {
            printingLedOnTime = Integer.parseInt(mSetPrintingLedOnTimeEditText.getText().toString());
        }
        catch (NumberFormatException e) {
            printingLedOnTime = 100;
        }

        int printingLedOffTime;

        try {
            printingLedOffTime = Integer.parseInt(mSetPrintingLedOffTimeEditText.getText().toString());
        }
        catch (NumberFormatException e) {
            printingLedOffTime = 100;
        }

        int errorLedOnTime;

        try {
            errorLedOnTime = Integer.parseInt(mSetErrorLedOnTimeEditText.getText().toString());
        }
        catch (NumberFormatException e) {
            errorLedOnTime = 100;
        }

        int errorLedOffTime;

        try {
            errorLedOffTime = Integer.parseInt(mSetErrorLedOffTimeEditText.getText().toString());
        }
        catch (NumberFormatException e) {
            errorLedOffTime = 100;
        }

        byte[] settingCommands;

        try {
            switch (mAutomaticBlinkLedControlSpinner.getSelectedItemPosition()) {
                default:
                case 0:
                    settingCommands = LedFunctions.createSetAutomaticBlink(LedModel.Star, printingLedOnTime, printingLedOffTime, errorLedOnTime, errorLedOffTime, Led.Printing, Led.Error);
                    break;
                case 1:
                    settingCommands = LedFunctions.createSetAutomaticBlink(LedModel.Star, printingLedOnTime, printingLedOffTime, errorLedOnTime, errorLedOffTime, Led.Printing);
                    break;
                case 2:
                    settingCommands = LedFunctions.createSetAutomaticBlink(LedModel.Star, printingLedOnTime, printingLedOffTime, errorLedOnTime, errorLedOffTime, Led.Error);
                    break;
                case 3:
                    settingCommands = LedFunctions.createSetAutomaticBlink(LedModel.Star, printingLedOnTime, printingLedOffTime, errorLedOnTime, errorLedOffTime);
                    break;
            }
        }
        catch (IllegalArgumentException e) {
            if (mProgressDialog != null) {
                mProgressDialog.dismiss();
            }

            CommonAlertDialogFragment dialog = CommonAlertDialogFragment.newInstance(ERROR_DIALOG);
            dialog.setTitle("Error");
            dialog.setMessage(e.getMessage());
            dialog.setPositiveButton("OK");
            dialog.show(getChildFragmentManager());
            return;
        }

        // Create commands for printing.
        byte[] printCommands;

        PrinterSettingManager settingManager = new PrinterSettingManager(getActivity());
        PrinterSettings       settings       = settingManager.getPrinterSettings();

        StarIoExt.Emulation emulation = ModelCapability.getEmulation(settings.getModelIndex());
        int paperSize = settings.getPaperSize();

        ILocalizeReceipts localizeReceipts = ILocalizeReceipts.createLocalizeReceipts(PrinterSettingConstant.LANGUAGE_ENGLISH, paperSize);

        printCommands = PrinterFunctions.createCouponData(emulation, localizeReceipts, getResources(), paperSize, ICommandBuilder.BitmapConverterRotation.Right90);

        byte[] commands = new byte[settingCommands.length + printCommands.length];

        System.arraycopy(settingCommands, 0, commands, 0, settingCommands.length);
        System.arraycopy(printCommands, 0, commands, settingCommands.length, printCommands.length);

        Communication.sendCommands(this, commands, settings.getPortName(), settings.getPortSettings(), 10000, 150000, getActivity(), mSendCallback);     // 10000mS!!!
    }

    private void blinkLed() {
        mProgressDialog.show();

        Led led;

        switch (mBlinkLedControlSpinner.getSelectedItemPosition()) {
            default:
            case 0:
                led = Led.Printing;
                break;
            case 1:
                led = Led.Error;
                break;
        }

        int repeat;

        try {
            repeat = Integer.parseInt(mRepeatCountEditText.getText().toString());
        }
        catch (NumberFormatException e) {
            repeat = 1;
        }

        int onTime;

        try {
            onTime = Integer.parseInt(mBlinkLedOnTimeEditText.getText().toString());
        }
        catch (NumberFormatException e) {
            onTime = 100;
        }

        int offTime;

        try {
            offTime = Integer.parseInt(mBlinkLedOffTimeEditText.getText().toString());
        }
        catch (NumberFormatException e) {
            offTime = 100;
        }

        byte[] commands;

        try {
            commands = LedFunctions.createBlinkLed(LedModel.Star, led, repeat, onTime, offTime);
        }
        catch (IllegalArgumentException e) {
            if (mProgressDialog != null) {
                mProgressDialog.dismiss();
            }

            CommonAlertDialogFragment dialog = CommonAlertDialogFragment.newInstance(ERROR_DIALOG);
            dialog.setTitle("Error");
            dialog.setMessage(e.getMessage());
            dialog.setPositiveButton("OK");
            dialog.show(getChildFragmentManager());
            return;
        }

        Communication.sendCommands(this, commands, mPrinterSettings.getPortName(), mPrinterSettings.getPortSettings(), 10000, 150000, getActivity(), mSendCallback);     // 10000mS!!!
    }

    private final Communication.SendCallback mSendCallback = new Communication.SendCallback() {
        @Override
        public void onStatus(CommunicationResult communicationResult) {
            if (!mIsForeground) {
                return;
            }

            if (mProgressDialog != null) {
                mProgressDialog.dismiss();
            }

            CommonAlertDialogFragment dialog = CommonAlertDialogFragment.newInstance("CommResultDialog");
            dialog.setTitle("Communication Result");
            dialog.setMessage(Communication.getCommunicationResultMessage(communicationResult));
            dialog.setPositiveButton("OK");
            dialog.show(getChildFragmentManager());
        }
    };

    @Override
    public void onDialogResult(String tag, Intent data) {
        // do nothing
    }
}
