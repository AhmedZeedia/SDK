package com.starmicronics.starprntsdk.functions;

import com.starmicronics.starioextension.ILedCommandBuilder;
import com.starmicronics.starioextension.ILedCommandBuilder.Led;
import com.starmicronics.starioextension.StarIoExt;
import com.starmicronics.starioextension.StarIoExt.LedModel;

import java.util.Arrays;
import java.util.List;

public class LedFunctions {

    public static byte[] createSetAutomaticBlink(LedModel model, int printingLedOnTime, int printingLedOffTime, int errorLedOnTime, int errorLedOffTime, Led... leds) {
        ILedCommandBuilder builder = StarIoExt.createLedCommandBuilder(model);

        builder.appendAutomaticBlinkMode(leds);

        List ledList = Arrays.asList(leds);

        if (ledList.contains(Led.Printing)) {
            builder.appendAutomaticBlinkInterval(Led.Printing, printingLedOnTime, printingLedOffTime);
        }

        if (ledList.contains(Led.Error)) {
            builder.appendAutomaticBlinkInterval(Led.Error, errorLedOnTime, errorLedOffTime);
        }

        return builder.getCommands();
    }

    public static byte[] createBlinkLed(LedModel model, Led led, int repeat, int onTime, int offTime) {
        ILedCommandBuilder builder = StarIoExt.createLedCommandBuilder(model);

        builder.appendBlink(led, repeat, onTime, offTime);

        return builder.getCommands();
    }
}
