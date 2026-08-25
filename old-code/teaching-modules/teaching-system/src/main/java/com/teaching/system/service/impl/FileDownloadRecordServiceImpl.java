package com.teaching.system.service.impl;

import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.redis.service.RedisService;
import com.teaching.system.domain.FileDownloadRecord;
import com.teaching.system.mapper.FileDownloadRecordMapper;
import com.teaching.system.service.IFileDownloadRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 文件下载记录Service业务层处理
 *
 * @author teaching
 * @date 2026-01-09
 */
@Service
public class FileDownloadRecordServiceImpl implements IFileDownloadRecordService {
    @Autowired
    private FileDownloadRecordMapper fileDownloadRecordMapper;

    @Autowired
    private RedisService redisService;

    /**
     * 查询文件下载记录
     *
     * @param id 文件下载记录主键
     * @return 文件下载记录
     */
    @Override
    public FileDownloadRecord selectFileDownloadRecordById(Long id) {
        return fileDownloadRecordMapper.selectFileDownloadRecordById(id);
    }

    /**
     * 查询文件下载记录列表
     *
     * @param fileDownloadRecord 文件下载记录
     * @return 文件下载记录
     */
    @Override
    public List<FileDownloadRecord> selectFileDownloadRecordList(FileDownloadRecord fileDownloadRecord) {
        return fileDownloadRecordMapper.selectFileDownloadRecordList1(fileDownloadRecord);
    }

    /**
     * 新增文件下载记录
     *
     * @param fileDownloadRecord 文件下载记录
     * @return 结果
     */
    @Override
    public int insertFileDownloadRecord(FileDownloadRecord fileDownloadRecord) {
        // 用redis自增功能
        if (!redisService.hasKey("fileDownRecord:" + fileDownloadRecord.getUserId() + ":" + fileDownloadRecord.getTaskId())) {
            // 用redis自增功能
            if (redisService.hasKey("fileDownRecord:" + fileDownloadRecord.getTaskId())) {
                redisService.increment("fileDownRecord:" + fileDownloadRecord.getTaskId(), 1);
            } else {
                redisService.setCacheObject("fileDownRecord:" + fileDownloadRecord.getTaskId(), 1);
            }
            redisService.setCacheObject("fileDownRecord:" + fileDownloadRecord.getUserId() + ":" + fileDownloadRecord.getTaskId(), 1);
        }
        Date nowDate = DateUtils.getNowDate();
        fileDownloadRecord.setCreateTime(nowDate);
        fileDownloadRecord.setDownloadTime(nowDate);
        return fileDownloadRecordMapper.insertFileDownloadRecord(fileDownloadRecord);
    }

    /**
     * 修改文件下载记录
     *
     * @param fileDownloadRecord 文件下载记录
     * @return 结果
     */
    @Override
    public int updateFileDownloadRecord(FileDownloadRecord fileDownloadRecord) {
        fileDownloadRecord.setUpdateTime(DateUtils.getNowDate());
        return fileDownloadRecordMapper.updateFileDownloadRecord(fileDownloadRecord);
    }

    /**
     * 批量删除文件下载记录
     *
     * @param ids 需要删除的文件下载记录主键
     * @return 结果
     */
    @Override
    public int deleteFileDownloadRecordByIds(Long[] ids) {
        return fileDownloadRecordMapper.deleteFileDownloadRecordByIds(ids);
    }

    /**
     * 删除文件下载记录信息
     *
     * @param id 文件下载记录主键
     * @return 结果
     */
    @Override
    public int deleteFileDownloadRecordById(Long id) {
        return fileDownloadRecordMapper.deleteFileDownloadRecordById(id);
    }
}
