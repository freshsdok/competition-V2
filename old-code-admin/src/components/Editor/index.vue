<template>
  <div class="editor">
    <div style="border: 1px solid #ccc;">
      <Toolbar
        :editor="editorRef"
        :defaultConfig="toolbarConfig"
        mode="default"
        style="border-bottom: 1px solid #ccc"
      />
      <Editor
        :defaultConfig="editorConfig"
        mode="default"
        class="editor-wangeditor-com"
        v-model="valueHtml"
        :style="editorStyle"
        @onCreated="handleCreated"
        @onChange="handleChange"
        @onDestroyed="handleDestroyed"
        @onFocus="handleFocus"
        @onBlur="handleBlur"
        @customAlert="customAlert"
        @customPaste="customPaste"
      />
    </div>
  </div>
</template>

<script>
import '@wangeditor/editor/dist/css/style.css';
import { onBeforeUnmount, ref, shallowRef, onMounted, computed, watch } from 'vue';
import { Editor, Toolbar } from '@wangeditor/editor-for-vue';
import { getToken } from '@/utils/auth';
import { replaceFileOrigin } from '@/utils/fileOrigin';
import axios from 'axios';

export default {
  components: { Editor, Toolbar },
  props: {
    /* 编辑器的内容 */
    modelValue: {
      type: String,
    },
    /* 高度 */
    height: {
      type: Number,
      default: null,
    },
    /* 最小高度 */
    minHeight: {
      type: Number,
      default: null,
    },
    /* 只读 */
    readOnly: {
      type: Boolean,
      default: false,
    },
    /* 上传文件大小限制(MB) */
    fileSize: {
      type: Number,
      default: 5,
    },
    /* 类型（base64格式、url格式） */
    type: {
      type: String,
      default: "url",
    },
    disp: {
      type: Boolean,
      default: false,
    }
  },
  emits: ['update:modelValue'],
  setup(props, { emit }) {
    // 编辑器实例，必须用 shallowRef，重要！
    const editorRef = shallowRef();

    // 内容 HTML
    const valueHtml = ref(props.modelValue || '');

    // 监听modelValue变化
    watch(() => props.modelValue, (newVal) => {
      // 保持空字符串的默认值
      const normalizedNewVal = newVal || '';
      // 只有当值真正改变时才更新
      if (normalizedNewVal !== valueHtml.value) {
        valueHtml.value = normalizedNewVal;
      }
    }, { immediate: true, deep: true });
    
    // 计算编辑器样式
    const editorStyle = computed(() => {
      let style = { overflowY: 'hidden' };
      if (props.minHeight) {
        style.minHeight = `${props.minHeight}px`;
      } else {
        style.height = '400px'; // 默认高度
      }
      if (props.height) {
        style.height = `${props.height}px`;
      }
      return style;
    });

    // 上传配置
    const uploadUrl = import.meta.env.VITE_APP_BASE_API + '/file/upload';
    const headers = {
      Authorization: 'Bearer ' + getToken()
    };

    // 工具栏配置
    const toolbarConfig = {};
    // 编辑器配置
    const editorConfig = {
      placeholder: '请输入内容...',
      readOnly: props.readOnly,
      MENU_CONF: {
        // 配置图片上传
        uploadImage: {
          server: uploadUrl,
          fieldName: 'file',
          headers: headers,
          maxFileSize: props.fileSize * 1024 * 1024, // 最大文件大小
          allowedFileTypes: ['image/jpeg', 'image/jpg', 'image/png', 'image/svg'], // 允许的图片类型
          // 自定义插入图片，因为服务端返回格式与wangeditor要求不一致
          customInsert: (res, insertFn) => {
            // 调试信息，查看服务端返回的具体数据结构
            console.log('图片上传服务端返回：', res);
            // 检查服务端返回的code字段
            if (res.code === 200 && res.data && res.data.url) {
              // 插入图片，res.data.url是图片地址
              insertFn(replaceFileOrigin(res.data.url), '', '');
            } else {
              console.error('图片上传失败：服务端返回格式不正确', res);
            }
          },
          // 上传失败处理
          onFailed(file, res) {
            console.error('图片上传失败', res);
            return '图片上传失败';
          },
          // 上传错误处理
          onError(file, err, res) {
            console.error('图片上传错误', err);
            return '图片上传错误';
          }
        },
        // 配置视频上传
        uploadVideo: {
          server: uploadUrl,
          fieldName: 'file',
          headers: headers,
          maxFileSize: props.fileSize * 1024 * 1024, // 最大文件大小
          allowedFileTypes: ['video/mp4', 'video/webm', 'video/ogg'], // 允许的视频类型
          // 自定义插入视频，因为服务端返回格式与wangeditor要求不一致
          customInsert: (res, insertFn) => {
            // 调试信息，查看服务端返回的具体数据结构
            console.log('视频上传服务端返回：', res);
            // 检查服务端返回的code字段
            if (res.code === 200 && res.data && res.data.url) {
              // 插入视频，res.data.url是视频地址
              insertFn(replaceFileOrigin(res.data.url), '');
            } else {
              console.error('视频上传失败：服务端返回格式不正确', res);
            }
          },
          // 上传失败处理
          onFailed(file, res) {
            console.error('视频上传失败', res);
            return '视频上传失败';
          },
          // 上传错误处理
          onError(file, err, res) {
            console.error('视频上传错误', err);
            return '视频上传错误';
          }
        }
      }
    };

    // 销毁编辑器的方法
    const destroyEditor = () => {
      const editor = editorRef.value;
      if (editor == null) return;

      editor.destroy();
      editorRef.value = null;
    };

    // 组件销毁时，也及时销毁编辑器，重要！
    onBeforeUnmount(() => {
      destroyEditor();
    });

    // 编辑器回调函数
    const handleCreated = (editor) => {
      editorRef.value = editor; // 记录 editor 实例，重要！
      // 设置只读状态
      if (props.readOnly) {
        editor.disable();
      }
      // 编辑器初始化完成后，显式同步内容，防止异步时序导致内容丢失
      if (valueHtml.value && editor.getHtml() !== valueHtml.value) {
        editor.setHtml(valueHtml.value);
      }
    };
    const handleChange = (editor) => {
      const html = editor.getHtml();
      // 只有当编辑器内容真正改变时才更新，避免循环更新
      valueHtml.value = html;
      emit('update:modelValue', html);
    };
    
    const handleDestroyed = (editor) => {
    };
    const handleFocus = (editor) => {
      console.log('focus', editor);
    };
    const handleBlur = (editor) => {
      console.log('blur', editor);
    };
    const customAlert = (info, type) => {
      alert(`【自定义提示】${type} - ${info}`);
    };
    const customPaste = (editor, event, callback) => {
      console.log('ClipboardEvent 粘贴事件对象', event);
      const clipboard = event.clipboardData || window.clipboardData;
      
      // 检查是否有图片粘贴
      if (clipboard && clipboard.items) {
        const imageFiles = [];
        let hasNonImageItems = false;
        
        // 遍历所有粘贴项
        for (let i = 0; i < clipboard.items.length; i++) {
          const item = clipboard.items[i];
          if (item.type.indexOf('image') !== -1) {
            // 收集图片文件
            const file = item.getAsFile();
            if (file) {
              imageFiles.push(file);
            }
          } else {
            // 有非图片项
            hasNonImageItems = true;
          }
        }
        
        // 如果只有图片，处理图片上传和插入
        if (imageFiles.length > 0 && !hasNonImageItems) {
          event.preventDefault();
          callback(false);
          
          // 异步处理图片上传和插入
          imageFiles.forEach(file => {
            const formData = new FormData();
            formData.append('file', file);
            
            axios.post(uploadUrl, formData, {
              headers: {
                'Content-Type': 'multipart/form-data',
                ...headers
              }
            }).then(res => {
              if (res.data.code === 200) {
                // 使用wangeditor v5官方推荐的方式插入图片
                // 通过创建图片节点并插入
                const imgNode = {
                  type: 'image',
                  src: replaceFileOrigin(res.data.data.url),
                  alt: '',
                  style: {}
                };
                editor.insertNode(imgNode);
              } else {
                console.error('粘贴图片上传失败', res.data);
              }
            }).catch(err => {
              console.error('粘贴图片上传错误', err);
            });
          });
          
          return;
        }
        
        // 如果有图片和其他内容混合，允许默认粘贴行为
        // 编辑器会自动处理文本和图片的粘贴
        if (imageFiles.length > 0 && hasNonImageItems) {
          // 允许默认粘贴行为，让编辑器处理混合内容
          callback(true);
          return;
        }
      }
      
      callback(true); // 允许默认粘贴行为
    };
    let cssMinHeight = computed(() => {
      return props.minHeight ? `${props.minHeight}px` : '';
    });
    return {
      editorRef,
      valueHtml,
      toolbarConfig,
      editorConfig,
      editorStyle,
      cssMinHeight,
      handleCreated,
      handleChange,
      handleDestroyed,
      handleFocus,
      handleBlur,
      customAlert,
      customPaste,
      destroyEditor, // 暴露手动销毁方法给父组件
    };
  },
};

</script>
<style scoped lang="scss">
// 使用js变量面的变量
:deep(.w-e-text-container) {
   min-height: v-bind(cssMinHeight);
}
</style>
