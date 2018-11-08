/**
 *    Copyright 2009-2015 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.logging;

/**
 * 这里表示日志级别
 * 这个就是日志模块的抽象接口类
 *
 * @author Clinton Begin
 */
public interface Log {

  /**
   * 是否是 debug 模式
   *
   * @return true/false
   */
  boolean isDebugEnabled();

  boolean isTraceEnabled();

  /**
   * error 日志级别
   *
   * @param s
   * @param e
   */
  void error(String s, Throwable e);

  void error(String s);

  void debug(String s);

  void trace(String s);

  void warn(String s);

}
