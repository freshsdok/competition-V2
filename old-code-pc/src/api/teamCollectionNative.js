import request from '@/utils/request'
export const getMyNativeCollections=()=>request({url:'/system/teamCollectionNative/mine',method:'get'})
export const enterNativeCollection=(id,version)=>request({url:`/system/teamCollectionNative/${encodeURIComponent(String(id))}/entry`,method:'post',data:{version},headers:{repeatSubmit:false}})
