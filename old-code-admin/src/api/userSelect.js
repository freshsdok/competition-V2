import request from '@/utils/request';

// 查询部门下拉树结构懒加载
export function deptTreeSelect1(query) {
    return request({
        url: '/system/user/deptTreeSecondary',
        method: 'get',
        params: query
    })
}

//选择人员组件中查询人员，不带层级
export function getlistUser(query) {
    return request({
        url: '/system/user/getUserList',
        method: 'get',
        params: query
    })
}

// 根据工号和姓名查询所有用户
export function getUserWhere(query) {
    return request({
        url: '/system/user/getUserWhere',
        method: 'get',
        params: query
    })
}

// 根据ids查询用户信息列表
export function selectUserByIds(userIds) {
    return request({
        url: '/system/user/selectUserByIds/' + userIds,
        method: 'get'
    })
}