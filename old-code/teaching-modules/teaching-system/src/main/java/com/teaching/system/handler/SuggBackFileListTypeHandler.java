package com.teaching.system.handler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import com.teaching.system.domain.SuggBackFile;

/**
 * 意见反馈文件列表 JSON 类型处理器
 * 用于将 List<SuggBackFile> 与数据库 JSON 字段进行转换
 * 
 * @author teaching
 */
public class SuggBackFileListTypeHandler extends BaseTypeHandler<List<SuggBackFile>>
{
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<SuggBackFile> parameter, JdbcType jdbcType)
            throws SQLException
    {
        try
        {
            String json = objectMapper.writeValueAsString(parameter);
            ps.setString(i, json);
        }
        catch (Exception e)
        {
            throw new SQLException("Error converting List<SuggBackFile> to JSON", e);
        }
    }

    @Override
    public List<SuggBackFile> getNullableResult(ResultSet rs, String columnName) throws SQLException
    {
        String json = rs.getString(columnName);
        return parseJson(json);
    }

    @Override
    public List<SuggBackFile> getNullableResult(ResultSet rs, int columnIndex) throws SQLException
    {
        String json = rs.getString(columnIndex);
        return parseJson(json);
    }

    @Override
    public List<SuggBackFile> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException
    {
        String json = cs.getString(columnIndex);
        return parseJson(json);
    }

    private List<SuggBackFile> parseJson(String json)
    {
        if (json == null || json.isEmpty())
        {
            return new ArrayList<>();
        }

        try
        {
            return objectMapper.readValue(json, TypeFactory.defaultInstance()
                    .constructCollectionType(List.class, SuggBackFile.class));
        }
        catch (Exception e)
        {
            return new ArrayList<>();
        }
    }
}
