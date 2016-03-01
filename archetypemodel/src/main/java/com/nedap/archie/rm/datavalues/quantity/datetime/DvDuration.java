package com.nedap.archie.rm.datavalues.quantity.datetime;

import com.nedap.archie.rm.datavalues.SingleValuedDataValue;
import com.nedap.archie.rm.datavalues.quantity.DvAmount;

import java.time.LocalTime;
import java.time.temporal.TemporalAmount;

/**
 * TODO: magnitude of duration is not defined properly
 * Created by pieter.bos on 04/11/15.
 */
public class DvDuration extends DvAmount<Long> implements SingleValuedDataValue<TemporalAmount> {

    private TemporalAmount value;

    @Override
    public TemporalAmount getValue() {
        return value;
    }

    @Override
    public void setValue(TemporalAmount value) {
        this.value = value;
    }

}