package com.gw.buildsrc.utils

import org.gradle.api.Project
import java.io.InputStream;
import java.util.Properties;

/**
 * 我们在构建脚本中, 通常来说, 会使用到4种属性: 环境变量, 系统属性, gradle属性, project属性
 * 这个工具类, 就是用于获取这4种属性的属性值.
 * 这里分别介绍一下这4种属性.
 * 参考自: https://docs.gradle.org/current/userguide/build_environment.html
 *
 * (1) 关于 环境变量
 *          在linux中, 可以 通过 `export <环境变量名>=<环境变量值>` 方式 添加 一个环境变量.
 *          在使用jenkins时:
 *                  一方面: 我们有时需要读取系统中的一些环境变量;
 *                          这是常用的方式
 *                  一方面: jenkins会为构建过程添加一些环境变量;
 *                          这是常用的方式
 *                  一方面: 我们也常常会在jenkins中的hook脚本中添加一些环境变量.
 *                          这是常用的方式
 *          在build.gradle中, 读取环境变量的办法: `System.getenv(<环境变量名>)`
 * (2) 关于 系统属性
 *          添加系统属性的办法:
 *                  一种是: 通过 java的SystemProperty类 添加 系统属性
 *                          这是常用的方式
 *                  一种是: 通过 `gradle -D<系统属性名>=<系统属性值> 选项` 来 向 这个构建过程中的虚拟机 添加 系统属性
 *                          这是常用的方式
 *                  一种是: 通过 在 `<项目根目录>/gradle.properties` 中的 `systemProp.<系统属性名>=<系统属性值>` 来 向 这个构建过程中的虚拟机 添加 系统属性
 *                          这是常用的方式
 *          在build.gradle中, 读取系统属性的办法: `System.properties[<系统属性名>]`
 * (3) 关于 gradle属性
 *          添加gradle属性的办法:
 *                  一种是: 通过 在 `<项目根目录>/gradle.properties` 中的 `<gradle属性名>=<系统属性值>` 来 添加 gradle属性
 *                          这是常用的方式
 *                  一种是: 在 `<gradle安装目录>/gradle.properties` 中的 `<gradle属性名>=<gradle属性值>` 来 添加 gradle属性
 *                          不常用的方式
 *                  一种是: 在 `~/.gradle/gradle.properties` 中的 `<gradle属性名>=<gradle属性值>` 来 添加 gradle属性
 *                          不常用的方式
 *                  一种是: 通过 `gradle -Dgradle.user.home=<gradle.user.home的属性> 选项` 来 添加 `gradle.user.home属性`
 *                          不常用的方式
 *          读取gradle属性的办法:
 *                  gradle属性 会 直接 做为 project的属性.
 *                  比如: 我们 向 `<项目根目录>/gradle.properties` 中 添加 `helloGradle=helloGradle_value`
 *                          在build.gradle中, 我们 可以:
 *                                  一种: 直接 通过 `project.helloGradle` 来读取
 *                                  一种: 通过 `project.properties['helloGradle']` 来读取
 * (4) 关于 project属性
 *          添加project属性的办法:
 *                  一种是: 通过 `gradle -P<project属性名>=<project属性值> 选项`/`gradle --project-prop <project属性名>=<project属性值> 选项` 来 添加 project属性
 *                          这是常用的方式
 *                  一种是: 通过 `gradle -Dorg.gradle.project.<project属性名>=<project属性值> 选项`/`gradle --system-prop org.gradle.project.<project属性名>=<project属性值> 选项` 来 添加 project属性
 *                          不常用的方式
 *                  一种是: 通过 在 `<项目根目录>/gradle.properties` 中的 `org.gradle.project.<project属性名>=<project属性值>` 来 添加 project属性
 *                          不常用的方式
 *                  一种是: 通过 `export ORG_GRADLE_PROJECT_<project属性名>=<project属性值>` 添加 环境变量 的 方式 来 添加 project属性
 *                          不常用的方式
 *          读取project属性的办法:
 *                  project属性 本身 就是 project的属性 啊! (废话)
 *                  比如: `gradle -PhelloGradle=helloGradle_value` 添加了 `名为helloGradle的project属性`
 *                          在build.gradle中, 我们 可以:
 *                                  一种: 直接 通过 `project.helloGradle` 来读取
 *                                  一种: 通过 `project.properties['helloGradle']` 来读取
 *
 * by caizhiyong, 2022年2月10日
 */
class PropertyUtils {

    /**
     * 读取参数指定的环境变量的值
     * @param propertyName 环境变量名
     * @return 这个环境变量的值
     * @throws IllegalArgumentException 如果参数propertyName为空, 则抛出这个异常
     */
    static String getStringEnv(String propertyName) {
        if (StringUtils.isEmpty(propertyName)) {
            throw new IllegalArgumentException("参数propertyName 值 为 空, 不合法!")
        } else {
            return System.getenv(propertyName)
        }
    }

    /**
     * 读取参数指定的环境变量的值. 如果不存在这个环境变量, 则返回参数二指定的默认值
     * @param propertyName 环境变量名
     * @param defaultValue 默认值
     * @return 这个环境变量的值. 如果不存在参数一指定的环境变量, 那么返回参数二指定的默认值
     * @throws IllegalArgumentException 如果参数propertyName为空, 则抛出这个异常
     */
    static String getStringEnv(String propertyName, String defaultValue) {
        if (StringUtils.isEmpty(propertyName)) {
            throw new IllegalArgumentException("参数propertyName 值 为 空, 不合法!")
        } else {
            String value = System.getenv(propertyName)
            if (!StringUtils.isEmpty(value)) {
                return value
            } else {
                return defaultValue
            }
        }
    }

    /**
     * 读取参数指定的环境变量的值. 如果不存在这个环境变量, 则返回参数二指定的默认值
     * 注意: 这个环境变量的合法的值 只能是: "true", "1", "false", "0"
     *          如果赋为其它值时, 在这个函数内部会抛出 IllegalArgumentException异常
     * @param propertyName 环境变量名
     * @param defaultValue 默认值
     * @return 这个环境变量的值. 如果不存在参数一指定的环境变量, 那么返回参数二指定的默认值
     * @throws IllegalArgumentException 如果参数propertyName为空, 则抛出这个异常; 如果参数propertyName取得的属性值不合法时, 则抛出这个异常
     */
    static boolean getBooleanEnv(String propertyName, boolean defaultValue) {
        if (StringUtils.isEmpty(propertyName)) {
            throw new IllegalArgumentException("参数propertyName 值 为 空, 不合法!")
        } else {
            String value = System.getenv(propertyName)
            if (!StringUtils.isEmpty(value)) {
                if (value == "true" || value == "1") {
                    return true
                } else if (value == "false" || value == "0") {
                    return false
                } else {
                    throw new IllegalArgumentException("参数propertyName(${propertyName}) 的 属性类型 为 boolean, 但是 属性值 为 ${value}. 不合法!")
                }
            } else {
                return defaultValue
            }
        }
    }

    /**
     * 读取参数指定的环境变量的值. 如果不存在这个环境变量, 则返回参数二指定的默认值
     * 注意: 如果通过环境变量来设置int值时, 合法的值 只能是: 整型值
     *          如果赋为其它值时, 在这个函数内部会抛出 IllegalArgumentException异常
     * @param propertyName 环境变量名
     * @param defaultValue 默认值
     * @return 这个环境变量的值. 如果不存在参数一指定的环境变量, 那么返回参数二指定的默认值
     * @throws IllegalArgumentException 如果参数propertyName为空, 则抛出这个异常; 如果参数propertyName取得的属性值不合法时, 则抛出这个异常
     */
    static int getIntEnv(String propertyName, int defaultValue) {
        if (StringUtils.isEmpty(propertyName)) {
            throw new IllegalArgumentException("参数propertyName 值 为 空, 不合法!")
        } else {
            String value = System.getenv(propertyName)
            if (!StringUtils.isEmpty(value)) {
                if (value.isInteger()) {
                    return value.toInteger()
                } else {
                    throw new IllegalArgumentException("参数propertyName(${propertyName}) 的 属性类型 为 int, 但是 属性值 为 ${value}. 不合法!")
                }
            } else {
                return defaultValue
            }
        }
    }

    /**
     * 读取参数指定的系统属性的值
     * @param propertyName 系统属性名
     * @return 这个系统属性的值
     * @throws IllegalArgumentException
     *          如果参数propertyName为空, 则 抛出 IllegalArgumentException异常
     */
    static String getSystemProperty(String propertyName) {
        if (StringUtils.isEmpty(propertyName)) {
            throw new IllegalArgumentException("参数propertyName 值 为 空, 不合法!")
        } else {
            return System.properties[propertyName]
        }
    }

    /**
     * 读取参数指定的gradle属性的值
     * @param propertyName gradle属性名
     * @return 这个gradle属性的值
     * @throws IllegalArgumentException 如果参数project为空, 或者参数propertyName为空, 则 抛出 IllegalArgumentException异常
     */
    static String getGradleProperty(Project project, String propertyName) {
        if (project == null) {
            throw new IllegalArgumentException("参数project 为 空, 不合法!")
        } else if (StringUtils.isEmpty(propertyName)) {
            throw new IllegalArgumentException("参数propertyName 值 为 空, 不合法!")
        } else {
            if (!project.hasProperty(propertyName)) {
                return null
            } else {
                return project.properties[propertyName]
            }
        }
    }

    /**
     * 读取参数指定的project属性的值
     * @param propertyName project属性名
     * @return 这个project属性的值
     * @throws IllegalArgumentException
     *          如果参数project为空, 或者参数propertyName为空, 则 抛出 IllegalArgumentException异常
     */
    static String getProjectProperty(Project project, String propertyName) {
        if (project == null) {
            throw new IllegalArgumentException("参数project 为 空, 不合法!")
        } else if (StringUtils.isEmpty(propertyName)) {
            throw new IllegalArgumentException("参数propertyName 值 为 空, 不合法!")
        } else {
            if (!project.hasProperty(propertyName)) {
                return null
            } else {
                return project.properties[propertyName]
            }
        }
    }

    /**
     * 读取参数一指定的属性的值.
     * 会依次以: project属性->gradle属性->系统属性->环境变量->默认值 来 确认 这个属性的属性值
     *          这是一个优先级顺序:
     *                  如果 这个属性 是 project属性, 则 返回 这个 project属性 的 值
     *                  否则, 如果 这个属性 是 gradle属性, 则 返回 这个 gradle属性 的 值
     *                  否则, 如果 这个属性 是 系统属性, 则 返回 这个 系统属性 的 值
     *                  否则, 如果 这个属性 是 环境变量, 则 返回 这个 环境变量 的 值
     *                  否则, 返回 参数三指定的默认值
     * @param project Project对象
     * @param propertyName 这个属性的属性名
     * @param defaultValue 默认值
     * @return 属性值
     * @throws IllegalArgumentException
     *          如果参数project为空, 或者参数propertyName为空, 则 抛出 IllegalArgumentException异常
     */
    static String getProperty(Project project, String propertyName, String defaultValue) {
        String value = getProjectProperty(project, propertyName)
        if (!StringUtils.isEmpty(value)) {
            return value
        }

        value = getGradleProperty(project, propertyName)
        if (!StringUtils.isEmpty(value)) {
            return value
        }

        value = getSystemProperty(propertyName)
        if (!StringUtils.isEmpty(value)) {
            return value
        }

        value = getStringEnv(propertyName)
        if (!StringUtils.isEmpty(value)) {
            return value
        }

        return defaultValue
    }

    /**
     * 读取参数一指定的属性的值.
     * 会依次以: project属性->gradle属性->系统属性->环境变量->默认值 来 确认 这个属性的属性值
     *          这是一个优先级顺序:
     *                  如果 这个属性 是 project属性, 则 返回 这个 project属性 的 值
     *                  否则, 如果 这个属性 是 gradle属性, 则 返回 这个 gradle属性 的 值
     *                  否则, 如果 这个属性 是 系统属性, 则 返回 这个 系统属性 的 值
     *                  否则, 如果 这个属性 是 环境变量, 则 返回 这个 环境变量 的 值
     *                  否则, 返回 参数三指定的默认值
     * 注意: 如果通过上述4种属性来设置int值时, 合法的值 只能是: 整型值
     *          如果赋为其它值时, 在这个函数内部会抛出 IllegalArgumentException异常
     * @param project Project对象
     * @param propertyName 这个属性的属性名
     * @param defaultValue 默认值
     * @return 属性值
     * @throws IllegalArgumentException
     *          如果参数project为空, 或者参数propertyName为空, 则 抛出 IllegalArgumentException异常;
     *          如果参数propertyName取得的属性值不合法时, 则抛出这个异常.
     */
    static int getIntProperty(Project project, String propertyName, int defaultValue) {
        String value = getProperty(project, propertyName, null)
        if (!StringUtils.isEmpty(value)) {
            if (value.isInteger()) {
                return value.toInteger()
            } else {
                throw new IllegalArgumentException("参数propertyName(${propertyName}) 的 属性类型 为 int, 但是 属性值 为 ${value}. 不合法!")
            }
        } else {
            return defaultValue
        }
    }

    /**
     * 读取参数一指定的属性的值.
     * 会依次以: project属性->gradle属性->系统属性->环境变量->默认值 来 确认 这个属性的属性值
     *          这是一个优先级顺序:
     *                  如果 这个属性 是 project属性, 则 返回 这个 project属性 的 值
     *                  否则, 如果 这个属性 是 gradle属性, 则 返回 这个 gradle属性 的 值
     *                  否则, 如果 这个属性 是 系统属性, 则 返回 这个 系统属性 的 值
     *                  否则, 如果 这个属性 是 环境变量, 则 返回 这个 环境变量 的 值
     *                  否则, 返回 参数三指定的默认值
     * 注意: 如果通过上述4种属性来设置int值时, 合法的值 只能是: 整型值
     *          如果赋为其它值时, 在这个函数内部会抛出 IllegalArgumentException异常
     * @param project Project对象
     * @param propertyName 这个属性的属性名
     * @param defaultValue 默认值
     * @return 属性值
     * @throws IllegalArgumentException
     *          如果参数project为空, 或者参数propertyName为空, 则 抛出 IllegalArgumentException异常;
     *          如果参数propertyName取得的属性值不合法时, 则抛出这个异常.
     */
    static int getLongProperty(Project project, String propertyName, long defaultValue) {
        String value = getProperty(project, propertyName, null)
        if (!StringUtils.isEmpty(value)) {
            if (value.isLongInteger()) {
                return value.toLong()
            } else {
                throw new IllegalArgumentException("参数propertyName(${propertyName}) 的 属性类型 为 int, 但是 属性值 为 ${value}. 不合法!")
            }
        } else {
            return defaultValue
        }
    }

    /**
     * 读取参数一指定的属性的值.
     * 会依次以: project属性->gradle属性->系统属性->环境变量->默认值 来 确认 这个属性的属性值
     *          这是一个优先级顺序:
     *                  如果 这个属性 是 project属性, 则 返回 这个 project属性 的 值
     *                  否则, 如果 这个属性 是 gradle属性, 则 返回 这个 gradle属性 的 值
     *                  否则, 如果 这个属性 是 系统属性, 则 返回 这个 系统属性 的 值
     *                  否则, 如果 这个属性 是 环境变量, 则 返回 这个 环境变量 的 值
     *                  否则, 返回 参数三指定的默认值
     * 注意: 如果通过上述4种属性来设置boolean值时, 合法的值 只能是: "true", "1", "false", "0"
     *          如果赋为其它值时, 在这个函数内部会抛出 IllegalArgumentException异常
     * @param project Project对象
     * @param propertyName 这个属性的属性名
     * @param defaultValue 默认值
     * @return 属性值
     * @throws IllegalArgumentException
     *          如果参数project为空, 或者参数propertyName为空, 则 抛出 IllegalArgumentException异常;
     *          如果参数propertyName取得的属性值不合法时, 则抛出这个异常.
     */
    static boolean getBooleanProperty(Project project, String propertyName, boolean defaultValue) {
        String value = getProperty(project, propertyName, null)
        if (!StringUtils.isEmpty(value)) {
            if (value == "false" || value == "0") {
                return false
            } else if (value == "true" || value == "1") {
                return true
            } else {
                throw new IllegalArgumentException("参数propertyName(${propertyName}) 的 属性类型 为 boolean, 但是 属性值 为 ${value}. 不合法!")
            }
        } else {
            return defaultValue
        }
    }

    /**
     * 读取 根project下的local.properties文件 中 参数指定的project属性的值
     * @param Project project
     * @param propertyName 属性名
     * @return 这个属性的值
     * @throws IllegalArgumentException
     *          如果参数project为空, 或者参数propertyName为空, 则 抛出 IllegalArgumentException异常
     */
    static String getLocalProperty(Project project, String propertyName) {
        if (project == null) {
            throw new IllegalArgumentException("参数project 为 空, 不合法!")
        } else if (StringUtils.isEmpty(propertyName)) {
            throw new IllegalArgumentException("参数propertyName 值 为 空, 不合法!")
        } else {
            InputStream inputStream = project.rootProject.file("gradle.properties").newDataInputStream()
            Properties properties = new Properties()
            properties.load(inputStream)
            return properties.getProperty(propertyName)
        }
    }

    /**
     * 读取 根project下的local.properties文件 中 参数指定的project属性的值
     * @param Project project
     * @param propertyName 属性名
     * @param defaultValue 如果找不到这个属性, 那么返回这个默认值
     * @return 这个属性的值
     * @throws IllegalArgumentException
     *          如果参数project为空, 或者参数propertyName为空, 则 抛出 IllegalArgumentException异常
     */
    static String getLocalProperty(Project project, String propertyName, String defaultValue) {
        String value = getLocalProperty(project, propertyName)
        if (value == null) {
            value = defaultValue
        }
        return value
    }

    /**
     * 读取 根project下的local.properties文件 中 参数指定的project属性的值
     * @param Project project
     * @param propertyName 属性名
     * @param defaultValue 如果找不到这个属性, 那么返回这个默认值
     * @return 这个属性的值
     * @throws IllegalArgumentException
     *          如果参数project为空, 或者参数propertyName为空, 则 抛出 IllegalArgumentException异常
     */
    static boolean getBooleanLocalProperty(Project project, String propertyName, boolean defaultValue) {
        String value = getLocalProperty(project, propertyName)
        if (!StringUtils.isEmpty(value)) {
            if (value == "true" || value == "1") {
                return true
            } else if (value == "false" || value == "0") {
                return false
            } else {
                throw new IllegalArgumentException("参数propertyName(${propertyName}) 的 属性类型 为 boolean, 但是 属性值 为 ${value}. 不合法!")
            }
        } else {
            return defaultValue
        }
    }
}
