/*
 * Copyright (C) 2008 feilong
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.feilong.core.util.resourcebundleutiltest;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;

import com.feilong.core.bean.ConvertUtil;
import com.feilong.core.util.ResourceBundleUtil;
import com.feilong.test.AbstractThreeParamsAndOneResultParameterizedTest;

import static com.feilong.core.bean.ConvertUtil.toArray;
import static com.feilong.core.bean.ConvertUtil.toList;

/**
 * The Class ResourceBundleUtilGetValueWithBaseNameParameterizedTest.
 *
 * @author <a href="http://feitianbenyue.iteye.com/">feilong</a>
 */
public class ResourceBundleUtilGetValueWithBaseNameParameterizedTest
                extends AbstractThreeParamsAndOneResultParameterizedTest<String, String, Object[], String>{

    /** The base name. */
    private static final String BASE_NAME = "messages/feilong-core-test";

    /**
     * Test get value.
     */
    @Test
    public void testGetValue(){
        assertEquals(expectedValue, ResourceBundleUtil.getValue(input1, input2, input3));
    }

    /**
     * Data.
     *
     * @return the iterable
     */
    @Parameters(name = "index:{index}:ResourceBundleUtil.getValue(\"{0}\",\"{1}\",\"{2}\")={3}")
    public static Iterable<Object[]> data(){
        return toList(//
                        ConvertUtil.<Object> toArray(BASE_NAME, "config_test_array", toArray(), "5,8,7,6"),
                        toArray(BASE_NAME, "test.arguments", toArray("feilong", 28), "my name is feilong,age is 28"),
                        toArray(BASE_NAME, "with_space_value", toArray(), "a "),

                        toArray(BASE_NAME, "emptyValue", null, EMPTY),
                        toArray(BASE_NAME, "emptyValue", toArray(), EMPTY),

                        toArray(BASE_NAME, "wo_bu_cun_zai", toArray(), EMPTY),
                        toArray("messages.empty", "wo_bu_cun_zai", toArray(), EMPTY)
        //  
        );
    }
}