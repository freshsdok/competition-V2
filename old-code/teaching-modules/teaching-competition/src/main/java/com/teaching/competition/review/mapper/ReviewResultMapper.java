package com.teaching.competition.review.mapper;

import com.teaching.competition.review.domain.ReviewResult;
import com.teaching.competition.review.dto.ReviewResultGenerateDTO;
import com.teaching.competition.review.dto.ReviewResultQueryDTO;
import com.teaching.competition.review.vo.ReviewResultGenerateItemVO;
import com.teaching.competition.review.vo.ReviewResultListVO;
import com.teaching.competition.review.vo.ReviewResultRecordVO;
import com.teaching.competition.review.vo.ReviewScoreDetailReadonlyVO;
import com.teaching.competition.review.support.ReviewCrudMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Review Result Mapper接口。
 */
public interface ReviewResultMapper extends ReviewCrudMapper<ReviewResult> {
    @Override
    ReviewResult selectById(Long id);

    @Override
    List<ReviewResult> selectList(ReviewResult query);

    @Override
    int insert(ReviewResult entity);

    @Override
    int update(ReviewResult entity);

    @Override
    int deleteByIds(@Param("ids") Long[] ids, @Param("updateBy") String updateBy);

    List<ReviewResultGenerateItemVO> selectGenerateItems(ReviewResultGenerateDTO dto);

    List<ReviewResultListVO> selectResultList(ReviewResultQueryDTO query);

    List<ReviewResultRecordVO> selectRecordList(@Param("activityId") Long activityId,
                                                @Param("roundId") Long roundId,
                                                @Param("objectId") Long objectId);

    List<ReviewScoreDetailReadonlyVO> selectScoreDetailList(@Param("recordId") Long recordId);

    int updateRank(@Param("id") Long id, @Param("rank") Integer rank, @Param("updateBy") String updateBy);
}
