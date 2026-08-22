import { exportPresignedUrl,getOssClientKey } from '@/api/fileTask'
import modal from "@/plugins/modal";
import OSS from 'ali-oss'
export const ossFileFuc = () => {
  // 文件下载
  const downloadOssFile = (url,filename) => {
    exportPresignedUrl({fileKey: url}).then(res => {
      const ossUrl = res.data
      // 创建隐藏的链接并触发下载
      const a = document.createElement('a');
      a.style.display = 'none';
      a.href = ossUrl;
      a.download = filename || '';
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
    })
  }
  // 获取api的OSS信息
  const getOssClient = async (query) => {
    let keyJson = await getOssClientKey(query)
    if (!keyJson.data || keyJson.code != 200) {
      modal.msgError("获取文件上传路径失败")
      return
    }
    let client = new OSS({
      // yourRegion填写Bucket所在地域。以华东1（杭州）为例，yourRegion填写为oss-cn-hangzhou。
      region: keyJson?.data?.region,
      authorizationV4: true,
      // 从STS服务获取的临时访问密钥（AccessKey ID和AccessKey Secret）。
      accessKeyId: keyJson?.data?.accessKeyId,
      accessKeySecret: keyJson?.data?.accessKeySecret,
      // 从STS服务获取的安全令牌（SecurityToken）。
      stsToken: keyJson?.data?.securityToken,
      // 填写Bucket名称。
      bucket: keyJson?.data?.bucketName,
    })
    return {
      client,
      uri: keyJson?.data?.uri,
      keyJson:keyJson?.data,
    }
  }
  // 前端直传OSS文件
  async function uploadOssFile(file,query,progressCallback) {
    try {
      const apiOss = await getOssClient(query)
      let ossName = `${apiOss.uri}${file.name}`
      // 大文件分片上传配置
      const options = {
        // 分片大小，默认5MB，可根据网络情况调整
        partSize: 1024 * 1024 * 5, // 5MB
        // 并发上传的分片数
        parallel: 3,
        // 上传进度回调
        progress: function(p) {
          // 调用外部传入的进度回调
          if (typeof progressCallback === 'function') {
            progressCallback(p);
          }
        }
      };
     const result = await apiOss.client.multipartUpload(ossName, file, options);
      
      // // 构建完整的文件URL
      const region = apiOss.keyJson?.region;
      const bucket = apiOss.keyJson?.bucketName;
      const objectKey = result.name;
      // // 根据region和bucket构建完整URL，格式：https://bucket.oss-region.aliyuncs.com/object-key
      const ossUrl = `https://${bucket}.${region}.aliyuncs.com/${objectKey}`;
      
      let ossData = {
        ossName:result.name,
        ossUrl:ossUrl,
        fileName:file.name,
      }
      return ossData
    } catch (e) {
      console.log('上传失败:', e);
      throw e;
    }
  }
  return {
    downloadOssFile,
    uploadOssFile
  }
}
