package com.hyh.common.json.internal.type;

import com.hyh.common.json.internal.TypeUtil;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;

/**
 * @author Administrator
 * @description
 * @data 2020/7/17
 */
public class WildcardTypeImpl implements WildcardType, Serializable {

    private final Type upperBound;
    private final Type lowerBound;

    public WildcardTypeImpl(Type[] upperBounds, Type[] lowerBounds) {
        TypeUtil.checkArgument(lowerBounds.length <= 1);
        TypeUtil.checkArgument(upperBounds.length == 1);

        if (lowerBounds.length == 1) {
            TypeUtil.checkNotNull(lowerBounds[0]);
            TypeUtil.checkNotPrimitive(lowerBounds[0]);
            TypeUtil.checkArgument(upperBounds[0] == Object.class);
            this.lowerBound = TypeUtil.canonicalize(lowerBounds[0]);
            this.upperBound = Object.class;

        } else {
            TypeUtil.checkNotNull(upperBounds[0]);
            TypeUtil.checkNotPrimitive(upperBounds[0]);
            this.lowerBound = null;
            this.upperBound = TypeUtil.canonicalize(upperBounds[0]);
        }
    }

    public Type[] getUpperBounds() {
        return new Type[]{upperBound};
    }

    public Type[] getLowerBounds() {
        return lowerBound != null ? new Type[]{lowerBound} : TypeUtil.EMPTY_TYPE_ARRAY;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof WildcardType
                && TypeUtil.equals(this, (WildcardType) other);
    }

    @Override
    public int hashCode() {
        // this equals Arrays.hashCode(getLowerBounds()) ^ Arrays.hashCode(getUpperBounds());
        return (lowerBound != null ? 31 + lowerBound.hashCode() : 1)
                ^ (31 + upperBound.hashCode());
    }

    @Override
    public String toString() {
        if (lowerBound != null) {
            return "? super " + TypeUtil.typeToString(lowerBound);
        } else if (upperBound == Object.class) {
            return "?";
        } else {
            return "? extends " + TypeUtil.typeToString(upperBound);
        }
    }

    private static final long serialVersionUID = 0;
}
