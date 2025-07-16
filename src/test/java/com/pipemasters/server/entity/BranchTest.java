package com.pipemasters.server.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BranchTest {

    @Test
    void branchLevel_isCalculatedFromParentChain() {
        Branch root = new Branch("root", null);
        Branch child = new Branch("child", root);
        Branch grandChild = new Branch("grand", child);

        assertEquals(0, root.getBranchLevel());
        assertEquals(1, child.getBranchLevel());
        assertEquals(2, grandChild.getBranchLevel());
    }
}