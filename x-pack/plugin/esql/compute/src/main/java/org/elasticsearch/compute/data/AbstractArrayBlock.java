/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License
 * 2.0; you may not use this file except in compliance with the Elastic License
 * 2.0.
 */

package org.elasticsearch.compute.data;

import org.elasticsearch.core.Nullable;

import java.util.BitSet;

abstract class AbstractArrayBlock extends AbstractBlock {

    private final MvOrdering mvOrdering;

    /**
     * @param positionCount the number of values in this block
     */
    protected AbstractArrayBlock(int positionCount, MvOrdering mvOrdering) {
        super(positionCount);
        this.mvOrdering = mvOrdering;
    }

    /**
     * @param positionCount the number of values in this block
     */
    protected AbstractArrayBlock(int positionCount, @Nullable int[] firstValueIndexes, @Nullable BitSet nullsMask, MvOrdering mvOrdering) {
        super(positionCount, firstValueIndexes, nullsMask);
        this.mvOrdering = mvOrdering;
    }

    @Override
    public final MvOrdering mvOrdering() {
        return mvOrdering;
    }
}
