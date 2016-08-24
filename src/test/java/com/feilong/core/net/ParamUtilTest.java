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
package com.feilong.core.net;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.feilong.tools.jsonlib.JsonUtil;

import static com.feilong.core.CharsetType.UTF8;
import static com.feilong.core.util.SortUtil.sortByKeyAsc;

/**
 * The Class ParamUtilTest.
 *
 * @author <a href="http://feitianbenyue.iteye.com/">feilong</a>
 */
public class ParamUtilTest{

    /** The Constant LOGGER. */
    private static final Logger LOGGER    = LoggerFactory.getLogger(ParamUtilTest.class);

    /** <code>{@value}</code>. */
    private static String       uriString = "http://www.feilong.com:8888/esprit-frontend/search.htm?keyword=%E6%81%A4&page=";

    //***************com.feilong.core.net.ParamUtil.toNaturalOrderingQueryString(Map<String, String>)**********

    /**
     * Test to natural ordering string null map.
     */
    @Test
    public void testToNaturalOrderingStringNullMap(){
        assertEquals(EMPTY, ParamUtil.toNaturalOrderingQueryString(null));
    }

    /**
     * Test to natural ordering string empty map.
     */
    @Test
    public void testToNaturalOrderingStringEmptyMap(){
        assertEquals(EMPTY, ParamUtil.toNaturalOrderingQueryString(new HashMap<String, String>()));
    }

    /**
     * Test to natural ordering string.
     */
    @Test
    public void testToNaturalOrderingString(){
        Map<String, String> map = new HashMap<String, String>();
        map.put("service", "create_salesorder");
        map.put("_input_charset", "gbk");
        map.put("totalActual", "210.00");
        map.put("address", "江苏南通市通州区888组888号");

        assertEquals(
                        "_input_charset=gbk&address=江苏南通市通州区888组888号&service=create_salesorder&totalActual=210.00",
                        ParamUtil.toNaturalOrderingQueryString(map));
    }

    /**
     * Test to natural ordering string 3.
     */
    @Test
    public void testToNaturalOrderingStringNullValue(){
        Map<String, String> map = new HashMap<String, String>();
        map.put("service", null);
        map.put("totalActual", "210.00");
        map.put("province", "江苏省");
        assertEquals("province=江苏省&service=&totalActual=210.00", ParamUtil.toNaturalOrderingQueryString(map));
    }

    /**
     * Test to natural ordering string null key.
     */
    @Test
    public void testToNaturalOrderingStringNullKey(){
        Map<String, String> map = new HashMap<>();
        map.put("totalActual", null);
        map.put(null, "create_salesorder");
        map.put("province", "江苏省");
        assertEquals("=create_salesorder&province=江苏省&totalActual=", ParamUtil.toNaturalOrderingQueryString(map));
    }

    //***************************************************************************************************
    /**
     * Test join values.
     */
    @Test
    public void testJoinValues(){
        String value = "create_salesorder";
        String value2 = "unionpay_mobile";

        Map<String, String> map = new HashMap<String, String>();
        map.put("service", value);
        map.put("paymentType", value2);

        assertEquals(EMPTY, ParamUtil.joinValuesOrderByIncludeKeys(map, "a", "b"));
        assertEquals(value, ParamUtil.joinValuesOrderByIncludeKeys(map, "service"));
        assertEquals(value + value2, ParamUtil.joinValuesOrderByIncludeKeys(map, "service", "paymentType"));
        assertEquals(value2 + value, ParamUtil.joinValuesOrderByIncludeKeys(map, "paymentType", "service"));
    }

    /**
     * Test join values order by include keys.
     */
    @Test
    public void testJoinValuesOrderByIncludeKeys(){
        Map<String, String> map = new HashMap<String, String>();
        map.put("service", "create_salesorder");
        map.put("paymentType", "unionpay_mobile");

        LOGGER.debug(ParamUtil.joinValuesOrderByIncludeKeys(map, "service", "paymentType"));
    }

    /**
     * Adds the parameter1.
     */
    @Test
    public void addParameter1(){
        String pageParamName = "page";
        Object prePageNo = "";
        LOGGER.debug(ParamUtil.addParameter(uriString, pageParamName, prePageNo, UTF8));
    }

    /**
     * Adds the parameter.
     */
    @Test
    public void addParameter(){
        String pageParamName = "label";
        String prePageNo = "2-5-8-12";
        LOGGER.debug(ParamUtil.addParameter(uriString, pageParamName, prePageNo, UTF8));
    }

    /**
     * Test join single value map.
     */
    @Test
    public void testJoinSingleValueMap(){
        Map<String, String> map = new HashMap<String, String>();
        map.put(null, null);
        //        map.put("a", "");
        //        map.put("b", null);
        //        map.put("c", "jim");
        LOGGER.debug(ParamUtil.toQueryStringUseSingleValueMap(map));
    }

    /**
     * Test join single value map1.
     */
    @Test
    public void testJoinSingleValueMap1(){
        Map<String, String> singleValueMap = new LinkedHashMap<String, String>();

        singleValueMap.put("province", "江苏省");
        singleValueMap.put("city", "南通市");

        assertEquals("province=江苏省&city=南通市", ParamUtil.toQueryStringUseSingleValueMap(singleValueMap));
    }

    /**
     * Test join array value map.
     */
    @Test
    public void testJoinArrayValueMap(){
        Map<String, String[]> keyAndArrayMap = new LinkedHashMap<String, String[]>();

        keyAndArrayMap.put("province", new String[] { "江苏省", "浙江省" });
        keyAndArrayMap.put("city", new String[] { "南通市" });

        LOGGER.debug(ParamUtil.toQueryStringUseArrayValueMap(keyAndArrayMap));
    }

    /**
     * Combine query string.
     */
    @Test
    public void combineQueryString(){
        Map<String, String[]> keyAndArrayMap = new HashMap<String, String[]>();
        keyAndArrayMap.put("name", new String[] { "jim", "feilong", "鑫哥" });
        keyAndArrayMap.put("age", new String[] { "18" });
        keyAndArrayMap.put("love", new String[] { "sanguo" });
        LOGGER.debug(ParamUtil.toSafeQueryString(keyAndArrayMap, UTF8));
        LOGGER.debug(ParamUtil.toSafeQueryString(null, UTF8));
        LOGGER.debug(ParamUtil.toSafeQueryString(null, null));
        LOGGER.debug(ParamUtil.toSafeQueryString(keyAndArrayMap, null));
    }

    /**
     * Parses the query to value map.
     */
    @Test
    public void parseQueryToValueMap(){
        LOGGER.debug(JsonUtil.format(ParamUtil.toSingleValueMap("a=1&b=2&a=3", UTF8)));
        LOGGER.debug(JsonUtil.format(ParamUtil.toSingleValueMap("a=", UTF8)));
        LOGGER.debug(JsonUtil.format(ParamUtil.toSingleValueMap("a=1&", UTF8)));
        LOGGER.debug(JsonUtil.format(ParamUtil.toSingleValueMap("", UTF8)));

    }

    /**
     * Parses the query to value map.
     */
    @Test
    public void parseQueryToValueMap12(){
        String queryString = "subject=%E4%B8%8A%E6%B5%B7%E5%AE%9D%E5%B0%8A%E7%94%B5%E5%95%86&sign_type=MD5&notify_url=http%3A%2F%2Fstage.gymboshop.com%2Fpay%2FdoNotify%2F1.htm&out_trade_no=2015090210099910&return_url=http%3A%2F%2Fstage.gymboshop.com%2Fpay%2FdoReturn%2F1.htm&sign=309d124e35d574c5b5f230dac93e8221&_input_charset=UTF-8&it_b_pay=120m&total_fee=0.01&error_notify_url=http%3A%2F%2Fstage.gymboshop.com%2Fpay%2FnotifyError.htm%3Ftype%3D1&service=create_direct_pay_by_user&paymethod=directPay&partner=2088511258288082&anti_phishing_key=KP3B51bszcIOjOoNpw%3D%3D&seller_email=pay%40gymboree.com.cn&payment_type=1";
        queryString = "subject=CalvinKlein&sign_type=MD5&notify_url=http%3A%2F%2Fstaging-cn.puma.com%2Fpayment%2Falipay%2FaSynReturn.htm&out_trade_no=2015091410000044&return_url=http%3A%2F%2Fstaging-cn.puma.com%2Fpayment%2Falipay%2FsynReturn.htm&sign=c7703845019c2e0bce63cf4b0282f293&_input_charset=UTF-8&it_b_pay=24m&total_fee=0.01&error_notify_url=http%3A%2F%2Fstaging-cn.puma.com%2Fpayment%2Falipay%2FsynReturn.htm&service=create_direct_pay_by_user&paymethod=directPay&partner=2088201564862550&anti_phishing_key=KP3B5KV254mjRM_m-Q%3D%3D&seller_email=alipay-test14%40alipay.com&payment_type=1";
        queryString = "TOKEN=EC%2d4XL82648PV7990539&BILLINGAGREEMENTACCEPTEDSTATUS=0&CHECKOUTSTATUS=PaymentActionNotInitiated&TIMESTAMP=2016%2d08%2d24T05%3a29%3a34Z&CORRELATIONID=7fd76e705eee3&ACK=Success&VERSION=98&BUILD=24604018&EMAIL=ua_test_1%40baozun%2ecn&PAYERID=YHMPXSQNBN4Y2&PAYERSTATUS=verified&FIRSTNAME=Allan&LASTNAME=Chen&COUNTRYCODE=US&SHIPTONAME=Allan%20Chen&SHIPTOSTREET=1%20Main%20St&SHIPTOCITY=San%20Jose&SHIPTOSTATE=CA&SHIPTOZIP=95131&SHIPTOCOUNTRYCODE=US&SHIPTOCOUNTRYNAME=United%20States&ADDRESSSTATUS=Confirmed&CURRENCYCODE=HKD&AMT=11%2e75&ITEMAMT=11%2e75&SHIPPINGAMT=0%2e00&HANDLINGAMT=0%2e00&TAXAMT=0%2e00&DESC=UA%20shop&INVNUM=TWD20008401&NOTIFYURL=http%3a%2f%2flocalhost%3a8091%2fpayment%2fpaypalValidate%2ehtm&INSURANCEAMT=0%2e00&SHIPDISCAMT=0%2e00&INSURANCEOPTIONOFFERED=false&L_NAME0=%e7%94%b7%e5%ad%90UA%20Boxerjock%e2%84%a2%e7%b6%93%e5%85%b8%e7%b3%bb%e5%88%97%e5%b9%b3%e8%a7%92%e5%85%a7%e8%a4%b2&L_NUMBER0=2036&L_QTY0=1&L_TAXAMT0=0%2e00&L_AMT0=11%2e75&PAYMENTREQUEST_0_CURRENCYCODE=HKD&PAYMENTREQUEST_0_AMT=11%2e75&PAYMENTREQUEST_0_ITEMAMT=11%2e75&PAYMENTREQUEST_0_SHIPPINGAMT=0%2e00&PAYMENTREQUEST_0_HANDLINGAMT=0%2e00&PAYMENTREQUEST_0_TAXAMT=0%2e00&PAYMENTREQUEST_0_DESC=UA%20shop&PAYMENTREQUEST_0_INVNUM=TWD20008401&PAYMENTREQUEST_0_NOTIFYURL=http%3a%2f%2flocalhost%3a8091%2fpayment%2fpaypalValidate%2ehtm&PAYMENTREQUEST_0_INSURANCEAMT=0%2e00&PAYMENTREQUEST_0_SHIPDISCAMT=0%2e00&PAYMENTREQUEST_0_SELLERPAYPALACCOUNTID=ua_sell%40baozun%2ecn&PAYMENTREQUEST_0_INSURANCEOPTIONOFFERED=false&PAYMENTREQUEST_0_SHIPTONAME=Allan%20Chen&PAYMENTREQUEST_0_SHIPTOSTREET=1%20Main%20St&PAYMENTREQUEST_0_SHIPTOCITY=San%20Jose&PAYMENTREQUEST_0_SHIPTOSTATE=CA&PAYMENTREQUEST_0_SHIPTOZIP=95131&PAYMENTREQUEST_0_SHIPTOCOUNTRYCODE=US&PAYMENTREQUEST_0_SHIPTOCOUNTRYNAME=United%20States&PAYMENTREQUEST_0_ADDRESSSTATUS=Confirmed&L_PAYMENTREQUEST_0_NAME0=%e7%94%b7%e5%ad%90UA%20Boxerjock%e2%84%a2%e7%b6%93%e5%85%b8%e7%b3%bb%e5%88%97%e5%b9%b3%e8%a7%92%e5%85%a7%e8%a4%b2&L_PAYMENTREQUEST_0_NUMBER0=2036&L_PAYMENTREQUEST_0_QTY0=1&L_PAYMENTREQUEST_0_TAXAMT0=0%2e00&L_PAYMENTREQUEST_0_AMT0=11%2e75&PAYMENTREQUESTINFO_0_ERRORCODE=0";
        LOGGER.debug(JsonUtil.format(sortByKeyAsc(ParamUtil.toSingleValueMap(queryString, UTF8))));
    }

    /**
     * Parses the query to value map1.
     */
    @Test
    public void testToSafeArrayValueMap(){
        LOGGER.debug(JsonUtil.format(ParamUtil.toSafeArrayValueMap("a=1&b=2&a", UTF8)));
        LOGGER.debug(JsonUtil.format(ParamUtil.toSafeArrayValueMap("a=&b=2&a", UTF8)));
        LOGGER.debug(JsonUtil.format(ParamUtil.toSafeArrayValueMap("a=1&b=2&a=5", UTF8)));
        LOGGER.debug(JsonUtil.format(ParamUtil.toSafeArrayValueMap("a=1=2&b=2&a=5", UTF8)));
    }

    /**
     * Test to safe array value map1.
     */
    @Test
    public void testToSafeArrayValueMap1(){
        LOGGER.debug(JsonUtil.format(ParamUtil.toSafeArrayValueMap(" a& &", UTF8)));
    }

    /**
     * Test to safe array value map2.
     */
    @Test
    public void testToSafeArrayValueMap2(){
        LOGGER.debug(JsonUtil.format(ParamUtil.toSafeArrayValueMap(" a", UTF8)));
    }

    /**
     * Test to single value map.
     */
    @Test
    public void testToSingleValueMap(){
        String queryString = "sec_id=MD5&format=xml&sign=cc945983476d615ca66cee41a883f6c1&v=2.0&req_data=%3Cauth_and_execute_req%3E%3Crequest_token%3E201511191eb5762bd0150ab33ed73976f7639893%3C%2Frequest_token%3E%3C%2Fauth_and_execute_req%3E&service=alipay.wap.auth.authAndExecute&partner=2088011438559510";
        assertThat(ParamUtil.toSingleValueMap(queryString, UTF8), allOf(//
                        hasEntry("sec_id", "MD5"),
                        hasEntry("format", "xml"),
                        hasEntry("sign", "cc945983476d615ca66cee41a883f6c1"),
                        hasEntry("v", "2.0"),
                        hasEntry(
                                        "req_data",
                                        "%3Cauth_and_execute_req%3E%3Crequest_token%3E201511191eb5762bd0150ab33ed73976f7639893%3C%2Frequest_token%3E%3C%2Fauth_and_execute_req%3E"),
                        hasEntry("service", "alipay.wap.auth.authAndExecute"),
                        hasEntry("partner", "2088011438559510")));
    }

    /**
     * Gets the encoded url by array map.
     * 
     */
    @Test
    public void testGetEncodedUrlByArrayMap(){
        String beforeUrl = "www.baidu.com";
        Map<String, String[]> keyAndArrayMap = new LinkedHashMap<String, String[]>();
        keyAndArrayMap.put("a", new String[] { "aaaa", "bbbb" });
        keyAndArrayMap.put("name", new String[] { "aaaa", "bbbb" });
        keyAndArrayMap.put("pa", new String[] { "aaaa" });

        LOGGER.debug(ParamUtil.addParameterArrayValueMap(beforeUrl, keyAndArrayMap, UTF8));
        LOGGER.debug(ParamUtil.addParameterArrayValueMap(beforeUrl, null, UTF8));
        LOGGER.debug(ParamUtil.addParameterArrayValueMap(beforeUrl, null, null));
        beforeUrl = null;
        LOGGER.debug(ParamUtil.addParameterArrayValueMap(beforeUrl, keyAndArrayMap, null));
    }

    /**
     * Test get encoded url by array map1.
     */
    @Test
    public void testGetEncodedUrlByArrayMap1(){
        String beforeUrl = "www.baidu.com";
        Map<String, String[]> keyAndArrayMap = new LinkedHashMap<String, String[]>();

        keyAndArrayMap.put("receiver", new String[] { "鑫哥", "feilong" });
        keyAndArrayMap.put("province", new String[] { "江苏省" });
        keyAndArrayMap.put("city", new String[] { "南通市" });

        LOGGER.debug(ParamUtil.addParameterArrayValueMap(beforeUrl, keyAndArrayMap, UTF8));
    }

    /**
     * Test get encoded url by array map2.
     */
    @Test
    public void testGetEncodedUrlByArrayMap2(){
        String beforeUrl = "www.baidu.com?a=b";
        Map<String, String[]> keyAndArrayMap = new LinkedHashMap<String, String[]>();

        keyAndArrayMap.put("province", new String[] { "江苏省" });
        keyAndArrayMap.put("city", new String[] { "南通市" });

        LOGGER.debug(ParamUtil.addParameterArrayValueMap(beforeUrl, keyAndArrayMap, UTF8));
    }

    /**
     * Test add parameter single value map.
     */
    @Test
    public void testAddParameterSingleValueMap(){
        String beforeUrl = "www.baidu.com";
        Map<String, String> singleValueMap = new LinkedHashMap<String, String>();

        singleValueMap.put("province", "江苏省");
        singleValueMap.put("city", "南通市");

        LOGGER.debug(ParamUtil.addParameterSingleValueMap(beforeUrl, singleValueMap, UTF8));
    }

    /**
     * Test add parameter single value map2.
     */
    @Test
    public void testAddParameterSingleValueMap2(){
        String beforeUrl = "www.baidu.com?a=b";
        Map<String, String> singleValueMap = new LinkedHashMap<String, String>();

        singleValueMap.put("province", "江苏省");
        singleValueMap.put("city", "南通市");

        LOGGER.debug(ParamUtil.addParameterSingleValueMap(beforeUrl, singleValueMap, UTF8));
    }
}
