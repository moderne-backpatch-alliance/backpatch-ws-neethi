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
     * Ensures circular references are detected for policies with explicit Id values.
     */
    public void testCircularPolicyReferenceWithIdsIsDetected() {
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
            fail("Expected RuntimeException due to circular reference");
        } catch (RuntimeException e) {
            assertTrue(
                "Expected a cycle-detection message but got: " + e.getMessage(),
                e.getMessage() != null && e.getMessage().toLowerCase().contains("circular"));
        }
    }

    /**
     * Ensures circular references are detected even when referenced policies have no Id.
     *
     * Cycle detection must use a stable non-null resolution token so that
     * no-Id references cannot bypass the resolving set.
     */
    public void testCircularPolicyReferenceWithoutIdsIsDetected() {
        // Policy A references registry key "B" but has no policy Id.
        PolicyReference refToB = new PolicyReference();
        refToB.setURI("B");
        All allA = new All();
        allA.addPolicyComponent(refToB);
        ExactlyOne eoA = new ExactlyOne();
        eoA.addPolicyComponent(allA);
        Policy policyA = new Policy();
        policyA.addPolicyComponent(eoA);

        // Policy B references registry key "A" and also has no policy Id.
        PolicyReference refToA = new PolicyReference();
        refToA.setURI("A");
        All allB = new All();
        allB.addPolicyComponent(refToA);
        ExactlyOne eoB = new ExactlyOne();
        eoB.addPolicyComponent(allB);
        Policy policyB = new Policy();
        policyB.addPolicyComponent(eoB);

        registry.register("A", policyA);
        registry.register("B", policyB);

        try {
            policyA.normalize(registry, true);
            fail("Expected RuntimeException due to circular reference");
        } catch (RuntimeException e) {
            assertTrue(
                "Expected a cycle-detection message but got: " + e.getMessage(),
                e.getMessage() != null && e.getMessage().toLowerCase().contains("circular"));
        }
    }
}
