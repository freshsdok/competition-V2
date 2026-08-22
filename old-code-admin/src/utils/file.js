import modal from "@/plugins/modal";
export function beforeUpload(file) {
  console.log(file,'file...............')
  // 检查文件类型
  const isJPG = file.type === 'image/jpeg'
  const isPNG = file.type === 'image/png'
  const isGIF = file.type === 'image/gif'
  const isLt10M = file.size / 1024 / 1024 < 10
  
  if (!isJPG && !isPNG && !isGIF) {
    modal.msgError('上传图片只能是 JPG、JPEG、PNG、GIF 格式!')
    return false
  }
  
  // 检查文件大小
  if (!isLt10M) {
    modal.msgError('上传图片大小不能超过 10MB!')
    return false
  }
  
  return true
}
