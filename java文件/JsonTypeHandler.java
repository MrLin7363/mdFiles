package com.coolpad.basic.magic.config;

import com.alibaba.fastjson.JSONObject;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.springframework.util.StringUtils;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author: chenjunlin
 * @since: 2021/08/27
 * @descripe:
 */
public class JsonTypeHandler<T> extends BaseTypeHandler<T> {
    private Class<T> javaClass = null;

    public JsonTypeHandler() {

    }

    public JsonTypeHandler(Class<T> javaTypeClass) {
        this.javaClass = javaTypeClass;
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, T parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, JSONObject.toJSONString(parameter));
    }

    @Override
    public T getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return toObject(rs.getString(columnName),javaClass);
    }

    @Override
    public T getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return toObject(rs.getString(columnIndex),javaClass);
    }

    @Override
    public T getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return toObject(cs.getString(columnIndex),javaClass);
    }

    private T toObject(String content,Class<?> clazz){
        if (StringUtils.isEmpty(content)){
            return null;
        }
        return (T)JSONObject.parseObject(content,clazz);
    }


}
