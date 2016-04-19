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
package com.feilong.core.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.TreeMap;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.feilong.core.UncheckedIOException;
import com.feilong.core.Validator;
import com.feilong.core.bean.ConvertUtil;
import com.feilong.core.lang.StringUtil;
import com.feilong.core.text.MessageFormatUtil;

/**
 * {@link java.util.ResourceBundle} 工具类.
 * 
 * <h3>如果现在多种资源文件一起出现,该如何访问？</h3>
 * 
 * <blockquote>
 * <p>
 * 如果一个项目中同时存在Message.properties、Message_zh_CN.properties、Message_zh_ CN.class 3个类型的文件,那最终使用的是哪一个?<br>
 * 只会使用一个,按照优先级使用.<br>
 * 顺序为Message_zh_CN.class、Message_zh_CN.properties、Message.properties.<br>
 * </p>
 * <p>
 * 解析原理,参见:<br>
 * {@link "java.util.ResourceBundle#loadBundle(CacheKey, List, Control, boolean)"}<br>
 * {@link java.util.ResourceBundle.Control#newBundle(String, Locale, String, ClassLoader, boolean)}
 * </p>
 * </blockquote>
 * 
 * <h3>关于配置文件格式问题</h3>
 * 
 * <blockquote>
 * 参考 {@link PropertiesUtil}的注释
 * </blockquote>
 *
 * @author feilong
 * @version 1.4.0 2015年8月3日 上午3:18:50
 * @see MessageFormatUtil#format(String, Object...)
 * @see java.util.ResourceBundle
 * @see java.util.PropertyResourceBundle
 * @see java.util.ListResourceBundle
 * @since 1.4.0
 */
public final class ResourceBundleUtil{

    /** The Constant LOGGER. */
    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceBundleUtil.class);

    /** Don't let anyone instantiate this class. */
    private ResourceBundleUtil(){
        //AssertionError不是必须的. 但它可以避免不小心在类的内部调用构造器. 保证该类在任何情况下都不会被实例化.
        //see 《Effective Java》 2nd
        throw new AssertionError("No " + getClass().getName() + " instances for you!");
    }

    /**
     * 获取Properties配置文件键值,转换成指定的 typeClass 类型返回.
     * 
     * @param <T>
     *            the generic type
     * @param baseName
     *            配置文件的包+类全名<span style="color:red">(不要尾缀)</span>,the base name of the resource bundle, a fully qualified class name
     * @param key
     *            the key
     * @param typeClass
     *            指明返回类型, 如果是String.class,则转换成String返回; 如果是Integer.class,则转换成Integer返回
     * @return the value
     * @see #getValue(String, String)
     * @see ConvertUtil#convert(Object, Class)
     */
    public static <T> T getValue(String baseName,String key,Class<T> typeClass){
        String value = getValue(baseName, key);
        return ConvertUtil.convert(value, typeClass);
    }

    /**
     * 获取Properties配置文件键值,按照typeClass 返回对应的类型.
     * 
     * @param <T>
     *            the generic type
     * @param resourceBundle
     *            the resource bundle
     * @param key
     *            the key
     * @param typeClass
     *            指明返回类型,<br>
     *            如果是String.class,则返回的是String <br>
     *            如果是Integer.class,则返回的是Integer
     * @return the value
     * @see #getValue(ResourceBundle, String)
     * @see com.feilong.core.bean.ConvertUtil#convert(Object, Class)
     */
    public static <T> T getValue(ResourceBundle resourceBundle,String key,Class<T> typeClass){
        String value = getValue(resourceBundle, key);
        return ConvertUtil.convert(value, typeClass);
    }

    /**
     * 获取Properties配置文件键值 ,采用 {@link java.util.ResourceBundle#getBundle(String)} 方法来读取.
     *
     * @param baseName
     *            配置文件的包+类全名<span style="color:red">(不要尾缀)</span>,the base name of the resource bundle, a fully qualified class name
     * @param key
     *            Properties配置文件键名
     * @return 该键的值
     * @see #getResourceBundle(String)
     * @see #getValue(ResourceBundle, String)
     */
    public static String getValue(String baseName,String key){
        ResourceBundle resourceBundle = getResourceBundle(baseName);
        return getValue(resourceBundle, key);
    }

    /**
     * 获取Properties配置文件键值 ,采用 {@link java.util.ResourceBundle#getBundle(String)} 方法来读取.
     *
     * @param baseName
     *            配置文件的包+类全名<span style="color:red">(不要尾缀)</span>,the base name of the resource bundle, a fully qualified class name
     * @param key
     *            Properties配置文件键名
     * @param locale
     *            the locale for which a resource bundle is desired,如果是null,将使用 {@link Locale#getDefault()}
     * @return 该键的值
     * @see #getResourceBundle(String, Locale)
     * @see #getValue(ResourceBundle, String)
     */
    public static String getValue(String baseName,String key,Locale locale){
        ResourceBundle resourceBundle = getResourceBundle(baseName, locale);
        return getValue(resourceBundle, key);
    }

    /**
     * 带参数的 配置文件.
     * 
     * <p>
     * 格式如:name={0}.
     * </p>
     * 
     * @param baseName
     *            配置文件的包+类全名<span style="color:red">(不要尾缀)</span>,the base name of the resource bundle, a fully qualified class name
     * @param key
     *            the key
     * @param locale
     *            the locale for which a resource bundle is desired,如果是null,将使用 {@link Locale#getDefault()}
     * @param arguments
     *            此处可以传递Object[]数组过来
     * @return the value with arguments
     * @see #getResourceBundle(String, Locale)
     * @see #getValueWithArguments(ResourceBundle, String, Object...)
     */
    public static String getValueWithArguments(String baseName,String key,Locale locale,Object...arguments){
        ResourceBundle resourceBundle = getResourceBundle(baseName, locale);
        return getValueWithArguments(resourceBundle, key, arguments);
    }

    /**
     * 获取Properties配置文件键值 ,采用 {@link java.util.ResourceBundle#getBundle(String)} 方法来读取.
     * 
     * @param resourceBundle
     *            配置文件的包+类全名(不要尾缀)
     * @param key
     *            Properties配置文件键名
     * @return 该键的值<br>
     *         如果配置文件中,
     *         <ul>
     *         <li>key不存在,LOGGER.warn 输出警告,然后返回null</li>
     *         <li>key存在,但value是null 或者 empty,LOGGER.warn 输出警告,然后返回value</li>
     *         </ul>
     * @see java.util.ResourceBundle#getString(String)
     */
    public static String getValue(ResourceBundle resourceBundle,String key){
        if (!resourceBundle.containsKey(key)){
            LOGGER.debug("resourceBundle:[{}] don't containsKey:[{}]", resourceBundle, key);
            return StringUtils.EMPTY;
        }

        try{
            String value = resourceBundle.getString(key);
            if (Validator.isNullOrEmpty(value)){
                LOGGER.debug("resourceBundle has key:[{}],but value is null/empty", key);
            }
            return value;
        }catch (Exception e){
            LOGGER.error(e.getMessage(), e);
        }
        return StringUtils.EMPTY;
    }

    /**
     * 带参数的 配置文件.
     * <p>
     * 格式如:name={0}.
     * </p>
     * 
     * @param resourceBundle
     *            the resource bundle
     * @param key
     *            如上面的 name
     * @param arguments
     *            此处可以传递Object[]数组过来
     * @return 支持 arguments 为null,原样返回
     * @see MessageFormatUtil
     * @see MessageFormatUtil#format(String, Object...)
     */
    public static String getValueWithArguments(ResourceBundle resourceBundle,String key,Object...arguments){
        String value = getValue(resourceBundle, key);
        if (Validator.isNullOrEmpty(value)){
            return StringUtils.EMPTY;
        }
        // 支持 arguments 为null,原样返回
        return MessageFormatUtil.format(value, arguments);
    }

    // *****************************************************************************
    /**
     * 读取值,转成数组.
     * <p>
     * 默认调用 {@link #getArray(ResourceBundle, String, String, Class)} 形式
     * </p>
     * 
     * @param baseName
     *            配置文件的包+类全名<span style="color:red">(不要尾缀)</span>,the base name of the resource bundle, a fully qualified class name
     * @param key
     *            the key
     * @param delimiters
     *            分隔符,参见 {@link StringUtil#tokenizeToStringArray(String, String)} <code>delimiters</code> 参数
     * @return 如果资源值不存在,返回null
     * @see #getArray(ResourceBundle, String, String, Class)
     */
    public static String[] getArray(String baseName,String key,String delimiters){
        return getArray(baseName, key, delimiters, String.class);
    }

    /**
     * 读取值,转成数组.
     * <p>
     * 默认调用 {@link #getArray(ResourceBundle, String, String, Class)} 形式
     * </p>
     * 
     * @param resourceBundle
     *            the resource bundle
     * @param key
     *            the key
     * @param delimiters
     *            分隔符,参见 {@link StringUtil#tokenizeToStringArray(String, String)} <code>delimiters</code> 参数
     * @return 如果 资源值不存在,返回null
     * @see #getArray(ResourceBundle, String, String, Class)
     */
    public static String[] getArray(ResourceBundle resourceBundle,String key,String delimiters){
        return getArray(resourceBundle, key, delimiters, String.class);
    }

    /**
     * 读取值,转成数组.
     * <h3>示例:</h3>
     * <blockquote>
     * 
     * <pre>
     * 
     * 在 messages/feilong-core-test.properties 配置文件中, 有以下配置
     * {@code
         config_test_array=5,8,7,6
         }
     * 
     * 此时调用
     * {@code    
         LOGGER.info(JsonUtil.format(ResourceBundleUtil.getArray(resourceBundle, "config_test_array", ",", String.class)));
     }
     * 
     * 返回:[
     * "5",
     * "8",
     * "7",
     * "6"
     * ]
     * 
     * 调用
     * {@code 
      LOGGER.info(JsonUtil.format(ResourceBundleUtil.getArray(resourceBundle, "config_test_array", ",", Integer.class)));
     }
     * 
     * 返回:
     * [
     * 5,
     * 8,
     * 7,
     * 6
     * ]
     * 
     * </pre>
     * 
     * </blockquote>
     * 
     * @param <T>
     *            the generic type
     * @param baseName
     *            配置文件的包+类全名<span style="color:red">(不要尾缀)</span>,the base name of the resource bundle, a fully qualified class name
     * @param key
     *            the key
     * @param delimiters
     *            分隔符,参见 {@link StringUtil#tokenizeToStringArray(String, String)} <code>delimiters</code> 参数
     * @param typeClass
     *            指明返回类型,<br>
     *            如果是String.class,则返回的是String []数组<br>
     *            如果是Integer.class,则返回的是Integer [] 数组
     * @return 如果 资源值不存在,返回null
     * @see #getResourceBundle(String)
     * @see #getArray(ResourceBundle, String, String, Class)
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] getArray(String baseName,String key,String delimiters,Class<?> typeClass){
        ResourceBundle resourceBundle = getResourceBundle(baseName);
        return (T[]) getArray(resourceBundle, key, delimiters, typeClass);
    }

    /**
     * 读取值,转成数组.
     * 
     * <h3>示例:</h3>
     * <blockquote>
     * 
     * <pre>
     * 
     * 在 messages/feilong-core-test.properties 配置文件中, 有以下配置
     * {@code
         config_test_array=5,8,7,6
         }
     * 
     * 此时调用
     * {@code    
         LOGGER.info(JsonUtil.format(ResourceBundleUtil.getArray(resourceBundle, "config_test_array", ",", String.class)));
     }
     * 
     * 返回:[
     * "5",
     * "8",
     * "7",
     * "6"
     * ]
     * 
     * 调用
     * {@code 
      LOGGER.info(JsonUtil.format(ResourceBundleUtil.getArray(resourceBundle, "config_test_array", ",", Integer.class)));
     }
     * 
     * 返回:
     * [
     * 5,
     * 8,
     * 7,
     * 6
     * ]
     * 
     * </pre>
     * 
     * </blockquote>
     * 
     * @param <T>
     *            the generic type
     * @param resourceBundle
     *            the resource bundle
     * @param key
     *            the key
     * @param delimiters
     *            分隔符,参见 {@link StringUtil#tokenizeToStringArray(String, String)} <code>delimiters</code> 参数
     * @param typeClass
     *            指明返回类型,<br>
     *            如果是String.class,则返回的是String []数组<br>
     *            如果是Integer.class,则返回的是Integer [] 数组
     * @return 如果 资源值不存在,返回null
     * @see #getValue(ResourceBundle, String)
     * @see StringUtil#tokenizeToStringArray(String, String)
     */
    public static <T> T[] getArray(ResourceBundle resourceBundle,String key,String delimiters,Class<T> typeClass){
        String value = getValue(resourceBundle, key);
        String[] array = StringUtil.tokenizeToStringArray(value, delimiters);
        return ConvertUtil.convert(array, typeClass);
    }

    // **************************************************************************
    /**
     * Read prefix as map({@link TreeMap}).
     * 
     * <p>
     * 注意:JDK实现通常是 java.util.PropertyResourceBundle,内部是使用 hashmap来存储数据的,<br>
     * 本方法出于log以及使用方便,返回的是TreeMap
     * </p>
     * 
     * <h3>示例:</h3>
     * <blockquote>
     * 
     * <pre>
     * 比如 messages/feilong-core-test.properties 里面有
     * 
     * FileType.image=Image
     * FileType.video=Video
     * FileType.audio=Audio
     * 
     * 配置,
     * 
     * 此时调用 
     * 
     * {@code 
     * Map<String, String> map = ResourceBundleUtil.readPrefixAsMap(BASE_NAME, "FileType", ".", Locale.CHINA);
     * LOGGER.info(JsonUtil.format(map));
     * }
     * 
     * 返回 :
     * 
     * {
     * "audio": "Audio",
     * "image": "Image",
     * "video": "Video"
     * }
     * 
     * </pre>
     * 
     * </blockquote>
     *
     * @param baseName
     *            配置文件的包+类全名<span style="color:red">(不要尾缀)</span>,the base name of the resource bundle, a fully qualified class name
     * @param prefix
     *            前缀
     * @param delimiters
     *            the delimiters
     * @param locale
     *            the locale for which a resource bundle is desired,如果是null,将使用 {@link Locale#getDefault()}
     * @return 如果 baseName 没有key value,则返回null,否则解析所有的key和value转成HashMap
     * @see #readAllPropertiesToMap(String, Locale)
     */
    public static Map<String, String> readPrefixAsMap(String baseName,String prefix,String delimiters,Locale locale){
        Map<String, String> propertyMap = readAllPropertiesToMap(baseName, locale);
        if (Validator.isNullOrEmpty(propertyMap)){
            return Collections.emptyMap();
        }

        Map<String, String> result = new TreeMap<String, String>();
        for (Map.Entry<String, String> entry : propertyMap.entrySet()){
            String key = entry.getKey();
            String value = entry.getValue();
            // 以 prefix 开头
            if (key.startsWith(prefix)){
                String[] values = StringUtil.tokenizeToStringArray(key, delimiters);
                if (values.length >= 2){
                    result.put(values[1], value);
                }
            }
        }
        return result;
    }

    /**
     * 读取配置文件,将k/v 统统转成map.
     * 
     * <p>
     * 注意:JDK实现通常是 java.util.PropertyResourceBundle,内部是使用 hashmap来存储数据的,<br>
     * 本方法出于log以及使用方便,返回的是TreeMap
     * </p>
     * 
     * @param baseName
     *            配置文件的包+类全名<span style="color:red">(不要尾缀)</span>,the base name of the resource bundle, a fully qualified class name
     * @return 如果 baseName 没有key value,则返回null,否则,解析所有的key和value转成 {@link TreeMap}
     * @see #readAllPropertiesToMap(String, Locale)
     * @since 1.2.1
     */
    public static Map<String, String> readAllPropertiesToMap(String baseName){
        return readAllPropertiesToMap(baseName, null);
    }

    /**
     * 读取配置文件,将k/v 统统转成map.
     * 
     * <p>
     * 注意:JDK实现通常是 java.util.PropertyResourceBundle,内部是使用 hashmap来存储数据的,<br>
     * 本方法出于log以及使用方便,返回的是<span style="color:red"> TreeMap</span>
     * </p>
     * 
     * @param baseName
     *            配置文件的包+类全名<span style="color:red">(不要尾缀)</span>,the base name of the resource bundle, a fully qualified class name
     * @param locale
     *            the locale for which a resource bundle is desired,如果是null,将使用 {@link Locale#getDefault()}
     * @return 如果 baseName 没有key value,则返回null,否则,解析所有的key和value转成 {@link TreeMap}
     * @see #getResourceBundle(String, Locale)
     * @see java.util.ResourceBundle#getKeys()
     * @see MapUtils#toMap(ResourceBundle)
     */
    public static Map<String, String> readAllPropertiesToMap(String baseName,Locale locale){
        ResourceBundle resourceBundle = getResourceBundle(baseName, locale);
        return readAllPropertiesToMap(resourceBundle);
    }

    /**
     * 读取配置文件,将k/v 统统转成map.
     * 
     * <p>
     * 注意:JDK实现通常是 java.util.PropertyResourceBundle,内部是使用 hashmap来存储数据的,<br>
     * 本方法出于log以及使用方便,返回的是TreeMap
     * </p>
     *
     * @param resourceBundle
     *            the resource bundle
     * @return 如果 baseName 没有key value,则返回null,否则,解析所有的key和value转成 {@link TreeMap}
     * @see #getResourceBundle(String, Locale)
     * @see java.util.ResourceBundle#getKeys()
     * @see MapUtils#toMap(ResourceBundle)
     * @since 1.5.0
     */
    public static Map<String, String> readAllPropertiesToMap(ResourceBundle resourceBundle){
        Validate.notNull(resourceBundle, "resourceBundle can't be null!");

        Enumeration<String> enumeration = resourceBundle.getKeys();
        if (Validator.isNullOrEmpty(enumeration)){
            return Collections.emptyMap();
        }

        Map<String, String> propertyMap = new TreeMap<String, String>();
        while (enumeration.hasMoreElements()){
            String key = enumeration.nextElement();
            String value = resourceBundle.getString(key);
            propertyMap.put(key, value);
        }
        return propertyMap;
    }

    /**
     * 获得ResourceBundle.
     * 
     * @param baseName
     *            配置文件的包+类全名<span style="color:red">(不要尾缀)</span>,the base name of the resource bundle, a fully qualified class name
     * @return the resource bundle
     * @see java.util.Locale#getDefault()
     * @see #getResourceBundle(String, Locale)
     */
    public static ResourceBundle getResourceBundle(String baseName){
        return getResourceBundle(baseName, null);
    }

    /**
     * 获得ResourceBundle.
     * 
     * @param baseName
     *            配置文件的包+类全名<span style="color:red">(不要尾缀)</span>,the base name of the resource bundle, a fully qualified class name
     * @param locale
     *            the locale for which a resource bundle is desired,如果是null,将使用 {@link Locale#getDefault()}
     * @return the resource bundle,may be null
     * @see java.util.ResourceBundle#getBundle(String, Locale)
     */
    public static ResourceBundle getResourceBundle(String baseName,Locale locale){
        Validate.notEmpty(baseName, "baseName can't be null/empty!");

        // Locale enLoc = new Locale("en", "US"); // 表示美国地区
        Locale useLocale = null == locale ? Locale.getDefault() : locale;

        ResourceBundle resourceBundle = ResourceBundle.getBundle(baseName, useLocale);
        if (null == resourceBundle){
            LOGGER.warn("resourceBundle is null,baseName:{},locale:{}", resourceBundle, baseName, useLocale);
        }
        return resourceBundle;
    }

    //*****************************************************************************

    /**
     * 获得ResourceBundle({@link PropertyResourceBundle}),新增这个方法的初衷是为了能读取任意的资源(包括本地file等).
     * 
     * <p>
     * 参数 <code>fileName</code>是路径全地址
     * </p>
     * 
     * <pre>
     * 
     * Example 1:
    {@code
    String mailReadFile = "E:\\DataCommon\\Files\\Java\\config\\mail-read.properties";
    
        ResourceBundle resourceBundleRead = ResourceBundleUtil.getResourceBundleByFileName(mailReadFile);
        String mailServerHost = resourceBundleRead.getString("incoming.pop.hostname");
    }
     * </pre>
     *
     * @param fileName
     *            Example 1: "E:\\DataCommon\\Files\\Java\\config\\mail-read.properties"
     * @return the resource bundle,may be null
     * @see java.util.PropertyResourceBundle#PropertyResourceBundle(InputStream)
     * @see ResourceBundleUtil#getResourceBundle(InputStream)
     * @since 1.0.9
     */
    public static ResourceBundle getResourceBundleByFileName(String fileName){
        Validate.notEmpty(fileName, "fileName can't be null/empty!");
        try{
            InputStream inputStream = new FileInputStream(fileName);
            return getResourceBundle(inputStream);
        }catch (FileNotFoundException e){
            throw new UncheckedIOException(e);
        }
    }

    /**
     * 获得ResourceBundle({@link PropertyResourceBundle}),新增这个方法的初衷是为了能读取任意的资源(包括本地file等).
     *
     * @param inputStream
     *            the input stream
     * @return the resource bundle,may be null
     * @see java.util.PropertyResourceBundle#PropertyResourceBundle(InputStream)
     * @since 1.0.9
     */
    public static ResourceBundle getResourceBundle(InputStream inputStream){
        Validate.notNull(inputStream, "inputStream can't be null!");
        try{
            return new PropertyResourceBundle(inputStream);
        }catch (IOException e){
            throw new UncheckedIOException(e);
        }
    }

    /**
     * 获得 resource bundle({@link PropertyResourceBundle}),新增这个方法的初衷是为了能读取任意的资源(包括本地file等).
     *
     * @param reader
     *            the reader
     * @return the resource bundle
     * @see java.util.PropertyResourceBundle#PropertyResourceBundle(Reader)
     * @since 1.0.9
     */
    public static ResourceBundle getResourceBundle(Reader reader){
        Validate.notNull(reader, "reader can't be null!");
        try{
            return new PropertyResourceBundle(reader);
        }catch (IOException e){
            throw new UncheckedIOException(e);
        }
    }
}