package org.openehr.bmm.v2.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.openehr.bmm.core.*;

import java.util.ArrayList;
import java.util.List;

public class PBmmContainerType extends PBmmType<BmmContainerType> {

    private String containerType;
    private PBmmUnitaryType typeDef;
    private String type;

    public String getContainerType() {
        return containerType;
    }

    public void setContainerType(String containerType) {
        this.containerType = containerType;
    }

    public PBmmUnitaryType getTypeDef() {
        return typeDef;
    }

    public void setTypeDef(PBmmUnitaryType typeDef) {
        this.typeDef = typeDef;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * Effective unitary type, ignoring containers and also generic parameters
     */
    @Override
    public String baseType() {
        if (type != null)
            return type;
        else
            return typeDef.baseType();
    }

    /**
     * Formal name of the type for display.
     *
     * @return
     */
    @Override
    public String asTypeString() {
        return containerType + "<" + getTypeRef().asTypeString() + ">";
    }

    @Override
    public List<String> flattenedTypeList() {
        List<String> retVal = new ArrayList<>();
        retVal.add(containerType);
        if (getTypeRef() != null) {
            retVal.addAll(getTypeRef().flattenedTypeList());
        }
        return retVal;
    }

    /**
     * Get the type reference to the contained type
     * @return
     */
    @JsonIgnore
    public PBmmUnitaryType getTypeRef() {
        if (typeDef == null && type != null) {
            if (type.length() == 1) {
                // This is ugly because it basically checks parameter length to see if it's a generic parameter
                // However it's the only way in the current P_BMM version
                return new PBmmOpenType(type);
            }
            else
                return new PBmmSimpleType(type);
        }
        return typeDef;
    }

    @Override
    public void createBmmType(BmmModel schema, BmmClass classDefinition) {
        BmmClass containerClassDef = schema.getClassDefinition(containerType);
        PBmmUnitaryType containedType = getTypeRef();
        if (containerClassDef instanceof BmmGenericClass && containedType != null) {
            containedType.createBmmType(schema, classDefinition);
            if (containedType.bmmType instanceof BmmUnitaryType)
                bmmType = new BmmContainerType ((BmmUnitaryType)containedType.bmmType, (BmmGenericClass)containerClassDef);
        }
        else
            throw new RuntimeException("BmmClass " + containerClassDef.getName() + " is not defined in this model or not a generic type");
    }

}
