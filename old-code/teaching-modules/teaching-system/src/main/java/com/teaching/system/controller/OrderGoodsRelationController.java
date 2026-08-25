package com.teaching.system.controller;

import java.util.List;
import java.io.IOException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.system.domain.OrderGoodsRelation;
import com.teaching.system.service.IOrderGoodsRelationService;
import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.utils.poi.ExcelUtil;
import com.teaching.common.core.web.page.TableDataInfo;

/**
 * 订单商品关联Controller
 * 
 * @author teaching
 * @date 2025-12-08
 */
@RestController
@RequestMapping("/relation")
public class OrderGoodsRelationController extends BaseController
{
    @Autowired
    private IOrderGoodsRelationService orderGoodsRelationService;

    /**
     * 查询订单商品关联列表
     */
    @RequiresPermissions("system:relation:list")
    @GetMapping("/list")
    public TableDataInfo list(OrderGoodsRelation orderGoodsRelation)
    {
        startPage();
        List<OrderGoodsRelation> list = orderGoodsRelationService.selectOrderGoodsRelationList(orderGoodsRelation);
        return getDataTable(list);
    }

    /**
     * 导出订单商品关联列表
     */
    @RequiresPermissions("system:relation:export")
    @Log(title = "订单商品关联", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, OrderGoodsRelation orderGoodsRelation)
    {
        List<OrderGoodsRelation> list = orderGoodsRelationService.selectOrderGoodsRelationList(orderGoodsRelation);
        ExcelUtil<OrderGoodsRelation> util = new ExcelUtil<OrderGoodsRelation>(OrderGoodsRelation.class);
        util.exportExcel(response, list, "订单商品关联数据");
    }

    /**
     * 获取订单商品关联详细信息
     */
    @RequiresPermissions("system:relation:query")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(orderGoodsRelationService.selectOrderGoodsRelationById(id));
    }

    /**
     * 新增订单商品关联
     */
    @RequiresPermissions("system:relation:add")
    @Log(title = "订单商品关联", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody OrderGoodsRelation orderGoodsRelation)
    {
        return toAjax(orderGoodsRelationService.insertOrderGoodsRelation(orderGoodsRelation));
    }

    /**
     * 修改订单商品关联
     */
    @RequiresPermissions("system:relation:edit")
    @Log(title = "订单商品关联", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody OrderGoodsRelation orderGoodsRelation)
    {
        return toAjax(orderGoodsRelationService.updateOrderGoodsRelation(orderGoodsRelation));
    }

    /**
     * 删除订单商品关联
     */
    @RequiresPermissions("system:relation:remove")
    @Log(title = "订单商品关联", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(orderGoodsRelationService.deleteOrderGoodsRelationByIds(ids));
    }
}
