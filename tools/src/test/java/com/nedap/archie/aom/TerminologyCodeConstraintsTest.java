package com.nedap.archie.aom;

import com.google.common.collect.Sets;
import com.nedap.archie.adlparser.treewalkers.DummyRulesPrimitiveObjectParent;
import com.nedap.archie.aom.primitives.CTerminologyCode;
import com.nedap.archie.aom.terminology.ArchetypeTerminology;
import com.nedap.archie.aom.terminology.ValueSet;
import com.nedap.archie.base.terminology.TerminologyCode;
import com.nedap.archie.rm.datatypes.CodePhrase;
import com.nedap.archie.rm.datavalues.DvCodedText;
import com.nedap.archie.rminfo.ArchieRMInfoLookup;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by pieter.bos on 23/02/16.
 */
public class TerminologyCodeConstraintsTest {

    private Archetype archetype;

    @Before
    public void setupArchetype() {
        archetype = new AuthoredArchetype();
        ArchetypeTerminology terminology = new ArchetypeTerminology();
        archetype.setTerminology(terminology);
        Map<String, ValueSet> valueSets = new LinkedHashMap<>();
        terminology.setValueSets(valueSets);
        ValueSet ac12 = new ValueSet();
        ac12.setId("ac12");
        ac12.setMembers(Sets.newHashSet("at23", "at24"));
        ValueSet ac13 = new ValueSet();
        ac13.setId("ac13");
        ac13.setMembers(Sets.newHashSet("at24"));
        valueSets.put("ac12", ac12 );
        valueSets.put("ac13", ac13 );
    }

    @Test
    public void noConstraint() {
        CTerminologyCode code = new CTerminologyCode();
        assertTrue(code.isValidValue(TerminologyCode.createFromString("[ac12::at23]")));
    }

    @Test
    public void terminologyIdConstraint() {
        CTerminologyCode code = new CTerminologyCode();
        code.setParent(new DummyRulesPrimitiveObjectParent(archetype));
        code.addConstraint("ac12");
        assertTrue(code.isValidValue(TerminologyCode.createFromString("[ac12::at23]")));
        assertTrue(code.isValidValue(TerminologyCode.createFromString("[ac12::at24]")));
        assertFalse(code.isValidValue(TerminologyCode.createFromString("[ac12::at25]")));
        assertTrue(code.isValidValue(TerminologyCode.createFromString("[local::at23]")));
    }

    @Test
    public void externalTerminology() {
        CTerminologyCode code = new CTerminologyCode();
        code.setParent(new DummyRulesPrimitiveObjectParent(archetype));
        code.addConstraint("ac12");
        //this is valid, because we have no idea about the binding currently. Could be invalid later, when we have proper
        //external terminology support
        assertTrue(code.isValidValue(TerminologyCode.createFromString("[snomedct::72489423]")));
        assertTrue(code.isValidValue(TerminologyCode.createFromString("[anything::atall]")));
    }

    @Test
    public void terminologyCodeConstraint() {
        CTerminologyCode code = new CTerminologyCode();
        code.setParent(new DummyRulesPrimitiveObjectParent(archetype));
        code.addConstraint("at23");
        assertTrue(code.isValidValue(TerminologyCode.createFromString("[ac12::at23]")));
        assertTrue(code.isValidValue(TerminologyCode.createFromString("[ac13::at23]")));
        assertFalse(code.isValidValue(TerminologyCode.createFromString("[ac13::at24]")));
    }

    @Test
    public void dvCodedText() {
        //DV_CODED_TEXT can be constrained by a C_TERMINOLOGY_CONSTRAINT, according to lots of DV_ORDINAL usage in the CKM
        CTerminologyCode code = new CTerminologyCode();
        code.setParent(new DummyRulesPrimitiveObjectParent(archetype));
        code.addConstraint("at23");
        DvCodedText text = new DvCodedText();
        text.setValue("does not matter for this validation");
        text.setDefiningCode(new CodePhrase("[ac12::at23]"));
        assertTrue(code.isValidValue(ArchieRMInfoLookup.getInstance(), text));
        text.setDefiningCode(new CodePhrase("[ac13::at24]"));
        assertFalse(code.isValidValue(ArchieRMInfoLookup.getInstance(), text));
        text.setDefiningCode(new CodePhrase());
        assertFalse(code.isValidValue(ArchieRMInfoLookup.getInstance(), text));
        text.setDefiningCode(null);
        assertFalse(code.isValidValue(ArchieRMInfoLookup.getInstance(), text));
        assertFalse(code.isValidValue(ArchieRMInfoLookup.getInstance(), null));
    }
}
