/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.neethi;

public class CircularPolicyReferenceTest extends PolicyTestCase {

    PolicyRegistry registry = new PolicyRegistryImpl();

    public CircularPolicyReferenceTest() {
        super("CircularPolicyReferenceTest");
    }

    /**
     * Test two policies that mutually reference each other cause unbounded recursion in
     * AbstractPolicyOperator.normalizeOperator() when normalize(true) is called.
     *
     * Policy A contains a PolicyReference to "B".
     * Policy B contains a PolicyReference to "A".
     * Both are registered in the PolicyRegistry before normalization begins.
     *
     * normalizeOperator() resolves each PolicyReference via reg.lookup() and
     * recurses into the resolved policy's components with no cycle detection,
     * ultimately throwing StackOverflowError.
     */
    public void testCircularPolicyReferenceThrowsStackOverflow() {
        // Build Policy A: ExactlyOne > All > PolicyReference("B")
        PolicyReference refToB = new PolicyReference();
        refToB.setURI("B");
        All allA = new All();
        allA.addPolicyComponent(refToB);
        ExactlyOne eoA = new ExactlyOne();
        eoA.addPolicyComponent(allA);
        Policy policyA = new Policy();
        policyA.setId("A");
        policyA.addPolicyComponent(eoA);

        // Build Policy B: ExactlyOne > All > PolicyReference("A")
        PolicyReference refToA = new PolicyReference();
        refToA.setURI("A");
        All allB = new All();
        allB.addPolicyComponent(refToA);
        ExactlyOne eoB = new ExactlyOne();
        eoB.addPolicyComponent(allB);
        Policy policyB = new Policy();
        policyB.setId("B");
        policyB.addPolicyComponent(eoB);

        registry.register("A", policyA);
        registry.register("B", policyB);

        try {
            policyA.normalize(registry, true);
            fail("Expected StackOverflowError or RuntimeException due to circular reference");
        } catch (RuntimeException e) {
            assertTrue(
                "Expected a cycle-detection message but got: " + e.getMessage(),
                e.getMessage() != null && e.getMessage().toLowerCase().contains("circular"));
        }
    }
}
