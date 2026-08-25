package com.teaching.system.service.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.teaching.system.domain.SuggBack;
import com.teaching.system.mapper.SuggBackMapper;
import com.teaching.system.service.ISuggBackService;

/**
 * 意见反馈服务层实现
 *
 * @author teaching
 */
@Service
public class SuggBackServiceImpl implements ISuggBackService
{
    @Autowired
    private SuggBackMapper suggBackMapper;

    /**
     * 查询意见反馈信息
     *
     * @param suggBackId 意见反馈ID
     * @return 意见反馈信息
     */
    @Override
    public SuggBack selectSuggBackBySuggBackId(Long suggBackId)
    {
        return suggBackMapper.selectSuggBackBySuggBackId(suggBackId);
    }

    /**
     * 查询意见反馈列表
     *
     * @param suggBack 意见反馈信息
     * @return 意见反馈集合
     */
    @Override
    public List<SuggBack> selectSuggBackList(SuggBack suggBack)
    {
        return suggBackMapper.selectSuggBackList(suggBack);
    }

    /**
     * 新增意见反馈
     *
     * @param suggBack 意见反馈信息
     * @return 结果
     */
    @Override
    public int insertSuggBack(SuggBack suggBack)
    {
        // 自动生成反馈编码：yyyyMMdd-YJ-xxxxx
        if (suggBack.getBackCode() == null || suggBack.getBackCode().isEmpty())
        {
            String backCode = generateBackCode();
            suggBack.setBackCode(backCode);
        }
        suggBack.setSuggTime(new Date());
        return suggBackMapper.insertSuggBack(suggBack);
    }

    /**
     * 生成反馈编码
     * 格式：yyyyMMdd-YJ-xxxxx（日期-YJ-5位流水号）
     * 示例：20251020-YJ-00001
     *
     * @return 反馈编码
     */
    private String generateBackCode()
    {
        // 获取当前日期
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String dateStr = sdf.format(new Date());
        String prefix = dateStr + "-YJ-";

        // 查询当天最大的编码
        String maxCode = suggBackMapper.selectMaxBackCodeByDatePrefix(prefix);

        int nextNum = 1;
        if (maxCode != null && !maxCode.isEmpty())
        {
            // 从最大编码中提取流水号部分
            String[] parts = maxCode.split("-");
            if (parts.length == 3)
            {
                try
                {
                    int currentNum = Integer.parseInt(parts[2]);
                    nextNum = currentNum + 1;
                }
                catch (NumberFormatException e)
                {
                    // 解析失败，使用默认值1
                    nextNum = 1;
                }
            }
        }

        // 格式化为5位流水号
        String serialNum = String.format("%05d", nextNum);
        return prefix + serialNum;
    }

    /**
     * 修改意见反馈
     *
     * @param suggBack 意见反馈信息
     * @return 结果
     */
    @Override
    public int updateSuggBack(SuggBack suggBack)
    {
        return suggBackMapper.updateSuggBack(suggBack);
    }

    /**
     * 删除意见反馈信息
     *
     * @param suggBackId 意见反馈ID
     * @return 结果
     */
    @Override
    public int deleteSuggBackBySuggBackId(Long suggBackId)
    {
        return suggBackMapper.deleteSuggBackBySuggBackId(suggBackId);
    }

    /**
     * 批量删除意见反馈信息
     *
     * @param suggBackIds 需要删除的意见反馈ID
     * @return 结果
     */
    @Override
    public int deleteSuggBackBySuggBackIds(Long[] suggBackIds)
    {
        return suggBackMapper.deleteSuggBackBySuggBackIds(suggBackIds);
    }

    /**
     * 回复意见反馈
     *
     * @param suggBack 意见反馈信息
     * @return 结果
     */
    @Override
    public int replySuggBack(SuggBack suggBack)
    {
        return suggBackMapper.replySuggBack(suggBack);
    }

    /**
     * 转交意见反馈
     *
     * @param suggBack 意见反馈信息
     * @return 结果
     */
    @Override
    public int transferSuggBack(SuggBack suggBack)
    {
        return suggBackMapper.transferSuggBack(suggBack);
    }
}
