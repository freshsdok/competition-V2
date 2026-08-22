import request from '@/utils/request'

// 查询进行中实例列表
export function listRunning(query) {
    return request({
        url: '/workflow/process/runList',
        method: 'get',
        params: query
    })
}

// 查询已完成实例列表
export function listFinish(query) {
    return request({
        url: '/workflow/process/finishList',
        method: 'get',
        params: query
    })
}

// 流程实例挂起
export function flowSuspend(id) {
    return request({
        url: `/workflow/process/suspend/${id}`,
        method: 'get'
    })
}

// 流程实例激活
export function flowActivate(id) {
    return request({
        url: `/workflow/process/activate/${id}`,
        method: 'get'
    })
}

// 流程终止
export function stopTask(data) {
    return request({
        url: '/workflow/task/stop',
        method: 'post',
        data: data
    })
}
