package com.nedap.archie.archetypevalidator.validations;

import com.nedap.archie.aom.Archetype;
import com.nedap.archie.aom.terminology.ArchetypeTerm;
import com.nedap.archie.aom.terminology.ArchetypeTerminology;
import com.nedap.archie.aom.terminology.ValueSet;
import com.nedap.archie.aom.utils.AOMUtils;
import com.nedap.archie.archetypevalidator.ArchetypeValidation;
import com.nedap.archie.archetypevalidator.ArchetypeValidationBase;
import com.nedap.archie.archetypevalidator.ErrorType;
import com.nedap.archie.archetypevalidator.ValidationMessage;
import com.nedap.archie.flattener.ArchetypeRepository;
import com.nedap.archie.rminfo.ModelInfoLookup;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BasicTerminologyValidation extends ArchetypeValidationBase {

    public BasicTerminologyValidation(ModelInfoLookup lookup) {
        super(lookup);
    }

    @Override
    public void validate() {

        validateFormatAndSpecializationLevelOfCodes();
        validateLanguageConsistency();
        validateTerminologyBindings();
        validateValueSets();

    }

    private void validateFormatAndSpecializationLevelOfCodes() {
        int terminologySpecialisationDepth = archetype.getTerminology().specialisationDepth();
        for(Map<String, ArchetypeTerm> languageSpecificTerminology:archetype.getTerminology().getTermDefinitions().values()) {
            for(ArchetypeTerm term:languageSpecificTerminology.values()) {
                if(!AOMUtils.isValidIdCode(term.getCode())) {
                    addMessage(ErrorType.VATCV, String.format("id code %s in terminology is not valid", term.getCode()));
                }
                if(archetype.isDifferential()) {
                    if(terminologySpecialisationDepth != AOMUtils.getSpecializationDepthFromCode(term.getCode())) {
                        addMessage(ErrorType.VTSD, String.format("id code %s in terminology is of a different specialization depth than the archetype", term.getCode()));
                    }
                } else {
                    if(AOMUtils.getSpecializationDepthFromCode(term.getCode()) > terminologySpecialisationDepth) {
                        addMessage(ErrorType.VTSD, String.format("id code %s in terminology is of a different specialization depth than the archetype", term.getCode()));
                    }
                }
            }
        }

    }

    public void validateLanguageConsistency() {
        List<String> codes = archetype.getTerminology().allCodes();
        for(String code:codes) {
            for (String language : archetype.getTerminology().getTermDefinitions().keySet()) {
                if(!archetype.getTerminology().getTermDefinitions().get(language).containsKey(code)) {
                    addMessage(ErrorType.VTLC, "code " + code + " is not present in language " + language);
                }
            }
        }
    }

    private void validateTerminologyBindings() {
        ArchetypeTerminology terminology = archetype.getTerminology();
        Map<String, Map<String, URI>> termBindings = terminology.getTermBindings();
        for(String terminologyId: termBindings.keySet()) {
            for(String constraintCodeOrPath: termBindings.get(terminologyId).keySet()) {
                boolean archetypeHasPath = false;
                try {
                    archetypeHasPath = archetype.hasPath(constraintCodeOrPath);
                } catch (Exception e) {
                    //if not a valid path, fine
                }
                if(!(AOMUtils.isValidIdCode(constraintCodeOrPath) || archetypeHasPath
                                //TODO: || referenceMOdel.hasPath(path)
                    )) {
                    addMessage(ErrorType.VTTBK, String.format("Term binding key %s in path format is not present in archetype", constraintCodeOrPath));
                }
                if(AOMUtils.isValidIdCode(constraintCodeOrPath) &&
                        !(terminology.hasCode(constraintCodeOrPath))
                        || (archetype.isSpecialized() && flatParent != null && !flatParent.getTerminology().hasCode(constraintCodeOrPath)))
                    {
                    addMessage(ErrorType.VTTBK, String.format("Term binding key %s is not present in terminology", constraintCodeOrPath));
                }
                //TODO: two warnings
            }
        }
    }

    private void validateValueSets() {
        ArchetypeTerminology terminology = archetype.getTerminology();
        for(ValueSet valueSet:terminology.getValueSets().values()){
            if(!terminology.hasValueSetCode(valueSet.getId())) {
                addMessage(ErrorType.VTVSID, String.format("value set code %s is not present in terminology", valueSet.getId()));
            }
            for(String value:valueSet.getMembers()) {
                if(!terminology.hasValueCode(value)) {
                    addMessage(ErrorType.VTVSMD, String.format("value set code %s is not present in terminology", valueSet.getId()));
                }
            }
            //TODO: we should check for uniqueness, but valueset is a java.util.Set, so there can be no duplicates by definition

        }
    }


}
