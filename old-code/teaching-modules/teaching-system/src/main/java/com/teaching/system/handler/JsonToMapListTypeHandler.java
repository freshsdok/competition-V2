package com.teaching.system.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@MappedTypes(List.class)
public class JsonToMapListTypeHandler extends BaseTypeHandler<List<Map<String, Object>>> {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // 写入数据库：直接存储原始字符串（不转换）
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i,
                                    List<Map<String, Object>> parameter,
                                    JdbcType jdbcType) throws SQLException {
        try {
            ps.setString(i, objectMapper.writeValueAsString(parameter));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    // 从数据库读取：将 JSON 字符串转为 List<Map>
    @Override
    public List<Map<String, Object>> getNullableResult(ResultSet rs, String columnName)
            throws SQLException {
        return parseJson(rs.getString(columnName));
    }

    @Override
    public List<Map<String, Object>> getNullableResult(ResultSet rs, int columnIndex)
            throws SQLException {
        return parseJson(rs.getString(columnIndex));
    }

    @Override
    public List<Map<String, Object>> getNullableResult(CallableStatement cs, int columnIndex)
            throws SQLException {
        return parseJson(cs.getString(columnIndex));
    }

    private List<Map<String, Object>> parseJson(String json) {
        try {
            if (json == null || json.isEmpty()) return null;
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (Exception e) {
            throw new RuntimeException("JSON 解析失败: " + json, e);
        }
    }
}
