/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License
 * 2.0; you may not use this file except in compliance with the Elastic License
 * 2.0.
 */

package org.elasticsearch.compute.data;

import org.apache.lucene.util.BytesRef;
import org.elasticsearch.common.util.BigArrays;
import org.elasticsearch.common.util.BytesRefArray;

/**
 * Block build of BytesRefBlocks.
 * This class is generated. Do not edit it.
 */
final class BytesRefBlockBuilder extends AbstractBlockBuilder implements BytesRefBlock.Builder {

    private static final BytesRef NULL_VALUE = new BytesRef();

    private BytesRefArray values;

    BytesRefBlockBuilder(int estimatedSize) {
        this(estimatedSize, BigArrays.NON_RECYCLING_INSTANCE);
    }

    BytesRefBlockBuilder(int estimatedSize, BigArrays bigArrays) {
        values = new BytesRefArray(Math.max(estimatedSize, 2), bigArrays);
    }

    @Override
    public BytesRefBlockBuilder appendBytesRef(BytesRef value) {
        ensureCapacity();
        values.append(value);
        hasNonNullValue = true;
        valueCount++;
        updatePosition();
        return this;
    }

    @Override
    protected int valuesLength() {
        return Integer.MAX_VALUE; // allow the BytesRefArray through its own append
    }

    @Override
    protected void growValuesArray(int newSize) {
        throw new AssertionError("should not reach here");
    }

    @Override
    public BytesRefBlockBuilder appendNull() {
        super.appendNull();
        return this;
    }

    @Override
    public BytesRefBlockBuilder beginPositionEntry() {
        super.beginPositionEntry();
        return this;
    }

    @Override
    public BytesRefBlockBuilder endPositionEntry() {
        super.endPositionEntry();
        return this;
    }

    @Override
    protected void writeNullValue() {
        values.append(NULL_VALUE);
    }

    @Override
    public BytesRefBlockBuilder copyFrom(Block block, int beginInclusive, int endExclusive) {
        if (block.areAllValuesNull()) {
            for (int p = beginInclusive; p < endExclusive; p++) {
                appendNull();
            }
            return this;
        }
        return copyFrom((BytesRefBlock) block, beginInclusive, endExclusive);
    }

    /**
     * Copy the values in {@code block} from {@code beginInclusive} to
     * {@code endExclusive} into this builder.
     */
    public BytesRefBlockBuilder copyFrom(BytesRefBlock block, int beginInclusive, int endExclusive) {
        if (endExclusive > block.getPositionCount()) {
            throw new IllegalArgumentException("can't copy past the end [" + endExclusive + " > " + block.getPositionCount() + "]");
        }
        BytesRefVector vector = block.asVector();
        if (vector != null) {
            copyFromVector(vector, beginInclusive, endExclusive);
        } else {
            copyFromBlock(block, beginInclusive, endExclusive);
        }
        return this;
    }

    private void copyFromBlock(BytesRefBlock block, int beginInclusive, int endExclusive) {
        BytesRef scratch = new BytesRef();
        for (int p = beginInclusive; p < endExclusive; p++) {
            if (block.isNull(p)) {
                appendNull();
                continue;
            }
            int count = block.getValueCount(p);
            if (count > 1) {
                beginPositionEntry();
            }
            int i = block.getFirstValueIndex(p);
            for (int v = 0; v < count; v++) {
                appendBytesRef(block.getBytesRef(i++, scratch));
            }
            if (count > 1) {
                endPositionEntry();
            }
        }
    }

    private void copyFromVector(BytesRefVector vector, int beginInclusive, int endExclusive) {
        BytesRef scratch = new BytesRef();
        for (int p = beginInclusive; p < endExclusive; p++) {
            appendBytesRef(vector.getBytesRef(p, scratch));
        }
    }

    /**
     * How are multivalued fields ordered? This defaults to {@link Block.MvOrdering#UNORDERED}
     * and operators can use it to optimize themselves. This order isn't checked so don't
     * set it to anything other than {@link Block.MvOrdering#UNORDERED} unless you are sure
     * of the ordering.
     */
    public BytesRefBlockBuilder mvOrdering(Block.MvOrdering mvOrdering) {
        this.mvOrdering = mvOrdering;
        return this;
    }

    @Override
    public BytesRefBlock build() {
        finish();
        if (hasNonNullValue && positionCount == 1 && valueCount == 1) {
            return new ConstantBytesRefVector(values.get(0, new BytesRef()), 1).asBlock();
        } else {
            if (isDense() && singleValued()) {
                return new BytesRefArrayVector(values, positionCount).asBlock();
            } else {
                return new BytesRefArrayBlock(values, positionCount, firstValueIndexes, nullsMask, mvOrdering);
            }
        }
    }
}
