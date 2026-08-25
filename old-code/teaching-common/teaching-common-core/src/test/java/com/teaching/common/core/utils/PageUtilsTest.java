package com.teaching.common.core.utils;

import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class PageUtilsTest
{
    @Test
    public void shouldReturnEmptyPageWhenRequestedPageExceedsResultSet()
    {
        List<Integer> page = PageUtils.paginate(List.of(1, 2, 3), 99, 10);

        Assert.assertTrue(page.isEmpty());
    }

    @Test
    public void shouldNormalizeInvalidPaginationParameters()
    {
        List<Integer> page = PageUtils.paginate(List.of(1, 2, 3), 0, 0);

        Assert.assertEquals(List.of(1), page);
    }
}
