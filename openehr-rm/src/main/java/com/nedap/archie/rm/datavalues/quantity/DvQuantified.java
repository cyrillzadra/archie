package com.nedap.archie.rm.datavalues.quantity;


import com.google.common.collect.Sets;
import com.nedap.archie.rm.datatypes.CodePhrase;
import com.nedap.archie.rm.datavalues.quantity.datetime.DvTemporal;
import com.nedap.archie.rminfo.Invariant;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Created by pieter.bos on 04/11/15.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DV_QUANTIFIED", propOrder = {
        "magnitudeStatus"
})
@XmlSeeAlso({
        DvTemporal.class,
        DvAmount.class
})
public abstract class DvQuantified<AccuracyType, MagnitudeType extends Comparable> extends DvOrdered<MagnitudeType> {

    private static final Set<String> VALID_MAGNITUDE_STATUS_CODES = Sets.newHashSet("=", "<", ">", "<=", ">=", "~");

    @Nullable
    @XmlElement(name = "magnitude_status")
    private String magnitudeStatus;

    public DvQuantified() {
    }

    public DvQuantified(@Nullable List<ReferenceRange> otherReferenceRanges, @Nullable DvInterval normalRange, @Nullable CodePhrase normalStatus, @Nullable String magnitudeStatus) {
        super(otherReferenceRanges, normalRange, normalStatus);
        this.magnitudeStatus = magnitudeStatus;
    }

    @Nullable
    public String getMagnitudeStatus() {
        return magnitudeStatus;
    }

    public void setMagnitudeStatus(@Nullable String magnitudeStatus) {
        this.magnitudeStatus = magnitudeStatus;
    }

    @Nullable
    public abstract AccuracyType getAccuracy();

    public abstract MagnitudeType getMagnitude();

    @Override
    public int compareTo(MagnitudeType other) {
        return getMagnitude().compareTo(other);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        DvQuantified<?, ?> that = (DvQuantified<?, ?>) o;
        return Objects.equals(magnitudeStatus, that.magnitudeStatus);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), magnitudeStatus);
    }

    @Invariant("Magnitude_status_valid")
    public boolean magnitudeStatusValid() {
        return magnitudeStatus == null || VALID_MAGNITUDE_STATUS_CODES.contains(magnitudeStatus);
    }
}
