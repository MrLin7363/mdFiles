package com.coolpad.partner.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
public class BeanConvertUtils {

    private static final int MAX_LEVEL = 5; //最多递归执行五层

    public static final int STRING_CONVERT_NONE = 0;
    public static final int STRING_CONVERT_NULL_TO_EMPTY = 1;
    public static final int STRING_CONVERT_EMPTY_TO_NULL = 2;

    /**
     * object convert
     * @param <T>
     * @param <V>
     * @return
     */
    public static  <T,V> List<T> convertTo(List<V> list, Function<V,T> mapper){
        return list.stream()
                .map(mapper)
                .collect(Collectors.toList());
    }


    /**
     * 转换bean
     *
     * @param orig 源对象
     * @param <T>  目标类型
     * @return
     */
    public static <T> T convertTo(Object orig, Class<T> destClazz) {
        return convertRecurs(orig, destClazz, MAX_LEVEL, STRING_CONVERT_NONE);
    }

    /**
     * 转换bean
     *
     * @param orig              源对象
     * @param <T>               目标类型
     * @param stringConvert
     * @return
     */
    public static <T> T convertTo(Object orig, Class<T> destClazz, int stringConvert) {
        return convertRecurs(orig, destClazz, MAX_LEVEL, stringConvert);
    }

    /**
     * 转换List
     *
     * @param orig      源对象
     * @param destClazz 转换目标对象的类型
     * @return
     */
    public static <A, B> List<B> convertTo(List<A> orig, Class<B> destClazz) {
        return convertListRecurs(orig, destClazz, MAX_LEVEL, STRING_CONVERT_NONE);
    }

    /**
     * 转换Set
     *
     * @param orig      源对象
     * @param destClazz 目标类型
     * @return 泛型为目标类型的Set集合
     */
    public static <A, B> Set<B> convertTo(Set<A> orig, Class<B> destClazz) {
        return convertSetRecurs(orig, destClazz, MAX_LEVEL, STRING_CONVERT_NONE);
    }

    /**
     * 转换bean
     *
     * @param orig
     * @param destClazz
     * @param level             层数
     * @param stringConvert
     * @return
     */
    private static <T> T convertRecurs(Object orig, Class<T> destClazz, int level, int stringConvert) {
        if (level <= 0 || orig == null) {
            return null;
        }
        try {
            // 如果是基本类型
            if (isBaseDataType(destClazz)) {
                return (T) orig;
            }

            Map<String, Field> origFieldMap = new HashMap<>();
            for (Field field : getAllFields(orig.getClass())) {
                origFieldMap.put(field.getName(), field);
            }

            T dest = destClazz.newInstance();
            List<Field> destFields = getAllFields(destClazz);
            for (Field destField : destFields) {
                Class<?> destFieldType = destField.getType();
                Field origField = findFieldIgnoreCase(origFieldMap, destField.getName());

                // final字段不做处理
                if (origField == null || Modifier.isFinal(destField.getModifiers()) || Modifier.isFinal(origField.getModifiers())) {
                    continue;
                }

                origField.setAccessible(true);
                Object paramObj = null;
                Object origFieldValue = origField.get(orig);
                if ((isBaseDataType(destFieldType) ^ isBaseDataType(origField.getType())) && !destFieldType.isAssignableFrom(origField.getType())) {
                    log.warn("忽略字段:{}, 基本类型与组合类型不能互相转换, {} -> {}", destField.getName(), origField.getType().getName(), destFieldType.getName());
                } else if(Time.class.equals(destFieldType)){
                    paramObj = origFieldValue == null ? null : ((Time)origFieldValue).clone();
                } else if(Timestamp.class.equals(destFieldType)){
                    paramObj = origFieldValue == null ? null : ((Timestamp)origFieldValue).clone();
                } else if (destFieldType.isArray()){
                    int length = Array.getLength(origFieldValue);
                    paramObj = Array.newInstance(destFieldType.getComponentType(), length);
                    System.arraycopy(origFieldValue, 0, paramObj,  0, length);
                } else if (isBaseDataType(destFieldType)) {
                    if(stringConvert > 0 && String.class.equals(destFieldType)){
                        if (origFieldValue == null && stringConvert == STRING_CONVERT_NULL_TO_EMPTY){
                            paramObj = StringUtils.EMPTY;
                        } else if (StringUtils.isBlank((CharSequence) origFieldValue) && stringConvert == STRING_CONVERT_EMPTY_TO_NULL) {
                            paramObj = null;
                        } else {
                            paramObj = origFieldValue;
                        }
                    } else {
                        paramObj = origFieldValue;
                    }
                } else {
                    //非基本类型，则直接转换
                    paramObj = getParamObject(destField, origFieldValue, level, stringConvert);
                }

                destField.setAccessible(true);

                try {
                    destField.set(dest, paramObj);
                }catch (IllegalArgumentException e){
                    log.warn("忽略字段:{},类型不符合, {} -> {}",destField.getName(),origField.getType().getName(), destFieldType.getName(),e.getMessage());
                    continue;
                }
            }
            return dest;
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取字段，忽略字段大小写
     *
     * @param map
     * @param field
     * @return
     */
    private static Field findFieldIgnoreCase(Map<String, Field> map, String field) {
        for (Map.Entry<String, Field> entry : map.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(field)) {
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     * 获取非基本类型对象的参数对象
     */
    private static Object getParamObject(Field destField, Object paramObj, int level, int stringConvert) throws ClassNotFoundException {
        Object returnParamObj;
        Class<?> destFieldType = destField.getType();

        //对象为list
        if (destFieldType.isAssignableFrom(List.class)) {
            Type type = ((ParameterizedType) destField.getGenericType()).getActualTypeArguments()[0];
            returnParamObj = convertListRecurs((List) paramObj, Class.forName(((Class) type).getName()), level - 1, stringConvert);
        }
        //对象为set
        else if (destFieldType.isAssignableFrom(Set.class)) {
            Type type = ((ParameterizedType) destField.getGenericType()).getActualTypeArguments()[0];
            returnParamObj = convertSetRecurs((Set) paramObj, Class.forName(((Class) type).getName()), level - 1, stringConvert);
        }
        //对象为普通对象类型
        else {
            returnParamObj = convertRecurs(paramObj, destField.getType(), level - 1, stringConvert);
        }

        return returnParamObj;
    }

    /**
     * 转换List
     *
     * @param orig  源对象
     * @param level 最多递归几层
     */
    private static <A, B> List<B> convertListRecurs(List<A> orig, Class<B> destClazz, int level, int stringConvert) {
        if(orig == null){
            return null;
        }
        if (level <= 0) {
            return Collections.emptyList();
        }
        return orig.stream().map(a -> convertRecurs(a, destClazz, level, stringConvert)).collect(Collectors.toList());
    }

    /**
     * 转换Set
     *
     * @param orig  源对象
     * @param level 最多递归几层
     */
    private static <A, B> Set<B> convertSetRecurs(Set<A> orig, Class<B> destClazz, int level, int stringConvert) {
        if(orig == null){
            return null;
        }
        if (level <= 0) {
            return Collections.emptySet();
        }
        return orig.stream().map(a -> convertRecurs(a, destClazz, level, stringConvert)).collect(Collectors.toSet());
    }

    /**
     * 获取clazz所有的字段field
     *
     * @param clazz
     * @return
     */
    private static List<Field> getAllFields(Class clazz) {
        List<Field> fieldList = new ArrayList<>();
        while (clazz != null && clazz != Object.class) {
            fieldList.addAll(Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }
        return fieldList;
    }

    /**
     * 判断一个Class是否为基本数据类型。
     *
     * @param clazz 要判断的类。
     * @return true 表示为基本数据类型。
     */
    private static boolean isBaseDataType(Class clazz) {
        return
                (
                        clazz.isPrimitive() || clazz == String.class ||
                                clazz == Integer.class ||
                                clazz == Byte.class ||
                                clazz == Long.class ||
                                clazz == Double.class ||
                                clazz == Float.class ||
                                clazz == Character.class ||
                                clazz == Short.class ||
                                clazz == BigDecimal.class ||
                                clazz == BigInteger.class || clazz == Boolean.class ||
                                clazz == Date.class
                );
    }
}
