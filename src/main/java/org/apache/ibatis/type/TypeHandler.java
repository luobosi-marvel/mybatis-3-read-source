/**
 * Copyright 2009-2015 the original author or authors.
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
package org.apache.ibatis.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Clinton Begin
 */
public interface TypeHandler<T> {

    /**
     * 用于定义在 MyBatis 设置参数时该如何把 Java 类型的参数转换为对应的数据库类型
     *
     * @param ps        当前 PreparedStatement 对象
     * @param i         当前参数位置
     * @param parameter 当前参数的 Java 对象
     * @param jdbcType  当前参数的数据库类型
     * @throws SQLException
     */
    void setParameter(PreparedStatement ps, int i, T parameter, JdbcType jdbcType) throws SQLException;

    /**
     * 用于在 MyBatis 获取数据库结果集时如何把数据库类型转换为对应的 Java 类型
     *
     * @param rs         当前的结果集
     * @param columnName 当前的字段名称
     * @return 转换之后的 Java 对象
     * @throws SQLException
     */
    T getResult(ResultSet rs, String columnName) throws SQLException;

    /**
     * 用于在 MyBatis 获取数据库结果集时如何把数据库类型转换为对应的 Java 类型
     *
     * @param rs          当前的结果集
     * @param columnIndex 当前字段的位置
     * @return 转换之后的 Java 对象
     * @throws SQLException
     */
    T getResult(ResultSet rs, int columnIndex) throws SQLException;

    /**
     * 用于 Mybatis 在调用存储过程后把数据库类型的数据转换为对应的Java类型
     *
     * @param cs          当前的 CallableStatement 执行后的 CallableStatement
     * @param columnIndex columnIndex 当前输出参数的位置
     * @return 转换之后的 Java 对象
     * @throws SQLException
     */
    T getResult(CallableStatement cs, int columnIndex) throws SQLException;

}
