/**
 * Copyright 2009-2018 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ibatis.reflection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.binding.MapperMethod.ParamMap;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

/**
 * 用来处理接口中定义的方法的参数列表
 * 参数名解析器
 */
public class ParamNameResolver {

    private static final String GENERIC_NAME_PREFIX = "param";

    /**
     * <p>
     * The key is the index and the value is the name of the parameter.<br />
     * The name is obtained from {@link Param} if specified. When {@link Param} is not specified,
     * the parameter index is used. Note that this index could be different from the actual index
     * when the method has special parameters (i.e. {@link RowBounds} or {@link ResultHandler}).
     * </p>
     * <ul>
     * <li>aMethod(@Param("M") int a, @Param("N") int b) -&gt; {{0, "M"}, {1, "N"}}</li>
     * <li>aMethod(int a, int b) -&gt; {{0, "0"}, {1, "1"}}</li>
     * <li>aMethod(int a, RowBounds rb, int b) -&gt; {{0, "0"}, {2, "1"}}</li>
     * </ul>
     */
    private final SortedMap<Integer, String> names;

    /**
     * 是否有 @Param 注解
     */
    private boolean hasParamAnnotation;

    /**
     * 构造方法
     * 会通过反射的方式读取 Mapper 接口中对应方法的信息
     *
     * @param config
     * @param method
     */
    public ParamNameResolver(Configuration config, Method method) {
        // 获取参数的 类类型
        final Class<?>[] paramTypes = method.getParameterTypes();
        // 存储参数列表里面的注解，这里为什么要定义成二维数组？
        final Annotation[][] paramAnnotations = method.getParameterAnnotations();
        // 每个方法都会对应一个 TreeMap，里面保存了参数
        final SortedMap<Integer, String> map = new TreeMap<>();
        int paramCount = paramAnnotations.length;
        // get names from @Param annotations
        for (int paramIndex = 0; paramIndex < paramCount; paramIndex++) {
            if (isSpecialParameter(paramTypes[paramIndex])) {
                // skip special parameters
                continue;
            }
            String name = null;
            for (Annotation annotation : paramAnnotations[paramIndex]) {
                if (annotation instanceof Param) {
                    hasParamAnnotation = true;
                    name = ((Param) annotation).value();
                    break;
                }
            }
            if (name == null) {
                // @Param was not specified.
                if (config.isUseActualParamName()) {
                    name = getActualParamName(method, paramIndex);
                }
                if (name == null) {
                    // use the parameter index as the name ("0", "1", ...)
                    // gcode issue #71
                    name = String.valueOf(map.size());
                }
            }
            map.put(paramIndex, name);
        }
        names = Collections.unmodifiableSortedMap(map);
    }

    /**
     * 获取实际参数的名称
     *
     * @param method        方法对象
     * @param paramIndex    参数下标
     * @return 返回实际参数的名称
     */
    private String getActualParamName(Method method, int paramIndex) {
        return ParamNameUtil.getParamNames(method).get(paramIndex);
    }

    /**
     * 是否是指定的参数
     *
     * @param clazz 类类型
     * @return true/false
     */
    private static boolean isSpecialParameter(Class<?> clazz) {
        return RowBounds.class.isAssignableFrom(clazz) || ResultHandler.class.isAssignableFrom(clazz);
    }

    /**
     * Returns parameter names referenced by SQL providers.
     */
    public String[] getNames() {
        return names.values().toArray(new String[0]);
    }

    /**
     * <p>
     * A single non-special parameter is returned without a name.
     * Multiple parameters are named using the naming rule.
     * In addition to the default names, this method also adds the generic names (param1, param2,
     * ...).
     * </p>
     */
    public Object getNamedParams(Object[] args) {
        final int paramCount = names.size();
        if (args == null || paramCount == 0) {
            return null;
        } else if (!hasParamAnnotation && paramCount == 1) {
            return args[names.firstKey()];
        } else {// 使用了 @Param 注解指定了参数名称或有多个参数的情况
            // param 这个 Map 中记录了参数名称与实参之间的对应关系。ParamMap 继承了 HashMap，
            // 如果向 ParamMap 中添加已经存在的 key，会报错，其他行为和 HashMap 相同
            final Map<String, Object> param = new ParamMap<>();
            int i = 0;
            for (Map.Entry<Integer, String> entry : names.entrySet()) {
                // 将参数名与实参对应关系记录到 param 中
                param.put(entry.getValue(), args[entry.getKey()]);
                // add generic param names (param1, param2, ...)
                final String genericParamName = GENERIC_NAME_PREFIX + String.valueOf(i + 1);
                // ensure not to overwrite parameter named with @Param
                if (!names.containsValue(genericParamName)) {
                    param.put(genericParamName, args[entry.getKey()]);
                }
                i++;
            }
            return param;
        }
    }
}
