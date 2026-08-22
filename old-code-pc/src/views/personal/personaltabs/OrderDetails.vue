<template>
  <div style="background-color: #f5f5f5">
    <div class="container-custom">
      <Breadcrumbar />

      <el-card class="kapian">
        <div class="title">
          <div class="titleleft">订单详情</div>
          <div>
            <el-button type="primary" style="margin-right: 10px" v-if="
              xinxi.payMethod == 'offline' && xinxi.payStatus == 'pending'
            " @click="saomazhifu">扫码支付</el-button>
            <el-tag :type="pay_status.find((item) => {
              return item.value == xinxi.payStatus;
            })?.elTagType
              " size="large">
              {{
                pay_status.find((item) => {
                  return item.value == xinxi.payStatus;
                })?.label
              }}
            </el-tag>
          </div>
        </div>
        <div class="jbxx">基本信息</div>

        <el-row>
          <el-col :span="8">
            <div class="zhxx">
              <div>订单号</div>
              <div>{{ xinxi.id }}</div>
            </div>
          </el-col>

          <el-col :span="8">
            <div class="zhxx">
              <div>创建时间</div>
              <div>{{ xinxi.createTime }}</div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="zhxx">
              <div>支付方式</div>
              <div>
                {{
                  pay_method.find((item) => {
                    return item.value == xinxi.payMethod;
                  })?.label
                }}
              </div>
            </div>
          </el-col>
          <!-- <el-col :span="6">
            <div class="zhxx">
              <div>订单金额</div>
              <div style="color: red">¥{{ xinxi.amount }}</div>
            </div>
          </el-col> -->
        </el-row>
      </el-card>
      <el-card class="kapian" v-if="xinxi.payStatus == 'approve_rejected'">
        <div class="zzxx">审核意见</div>
        <div style="font-size: 16px; text-indent: 30px">
          {{ xinxi.auditOpinion }}
        </div>
      </el-card>
      <el-card class="kapian" v-if="xinxi.payMethod != 'online' && xinxi.commodityType !== 'cert'">
        <div class="zzxx">转账信息</div>
        <div class="lankuang">
          <el-row>
            <el-col :span="12">
              <div class="zhuanzhangtitle">收款单位</div>
              <div class="zhuanzhangvalue">
                {{ offline.merName }}
              </div>
            </el-col>
            <el-col :span="12">
              <div class="zhuanzhangtitle">银行账号</div>
              <div class="zhuanzhangvalue">
                {{ offline.account }}
              </div>
            </el-col>
            <el-col :span="12">
              <div class="zhuanzhangtitle">开户行</div>
              <div class="zhuanzhangvalue">
                {{ offline.bank }}
              </div>
            </el-col>
            <el-col :span="12">
              <div class="zhuanzhangtitle">转账金额</div>
              <div class="zhuanzhangvalue" style="color: red; font-weight: bold">
                ¥{{ xinxi.amount }}
              </div>
            </el-col>
          </el-row>
          <div class="beizhu">
            <div>
              <span style="font-weight: bold">必填备注：</span>订单号+学校名称
            </div>
            <div>（该备注将用于系统自动精准匹配订单，请务必准确无误。）</div>
          </div>
        </div>
      </el-card>
      <el-card class="kapian" v-if="xinxi.payMethod != 'online' && xinxi.commodityType !== 'cert'">
        <div class="zzxx">上传转账凭证</div>
        <div class="sc">
          <div class="shangchuanbt">请上传转账凭证</div>
          <div style="margin: 20px 0">
            支持{{ upload.worksFormat }} 格式文件。文件大小不超过
            {{ upload.worksFormatSize }} MB。
          </div>
          <el-upload :disabled="xinxi.payStatus != 'pending' &&
            xinxi.payStatus != 'approve_rejected'
            " ref="uploadRef" :accept="upload.worksFormat" :headers="upload.headers" :action="upload.url"
            :on-error="handleFileError" :on-success="onUploadSuccess" list-type="picture-card" :file-list="fileList"
            :auto-upload="true" :on-preview="handlePictureCardPreview" :on-remove="handleRemove"
            :before-upload="beforeUpload">
            <el-icon>
              <Plus />
            </el-icon>
            <!-- <el-button class="sctx" type="primary">选择文件</el-button> -->
          </el-upload>
        </div>
        <el-button class="sctx" type="primary" style="float: right" @click="tijiao" v-if="
          xinxi.payMethod != 'online' &&
          (xinxi.payStatus == 'pending' ||
            xinxi.payStatus == 'approve_rejected')
        ">上传凭证</el-button>
        <div style="clear: both"></div>
        <div class="notes">
          <p style="margin-bottom: 10px; color: #856404">上传说明</p>
          <div>请上传清晰的转账凭证，包含转账金额、时间、备注等关键信息</div>
          <div>上传后我们将在1-2个工作日内审核，请耐心等待</div>
          <div>如有疑问，请联系客服：010-12345678</div>
        </div>
      </el-card>
      <el-card class="kapian">
        <div class="zzxx">{{ COMMODITY_TYPE[xinxi.commodityType] }}</div>
        <!-- 赛事报名-团队信息 -->
        <template v-if="xinxi.commodityType === 'competition'">
          <div v-for="(x, i) in xinxi.teamInfoLists" :key="i" class="product-item">
            <div class="product-main">
              <div class="product-info">
                <div class="team-code" style="font-weight: 500; margin-bottom: 8px">
                  团队编号：{{ x.teamCode }}
                </div>
                <div class="product-name mt-[6px] font-[600] text-[16px]">
                  <span class="text-[#3169f8]">{{ x.competitionName }}</span>
                  <span>-</span>
                  <span class="text-[#FF8800]">{{ x.competitionTrackName }}</span>
                  <span>-</span>
                  <span class="text-[#51C512]">{{ x.secondLevelName }}</span>
                </div>

                <div class="mt-[10px] text-[14px]">
                  <p v-if="x && x.playersList && x.playersList.length > 0">
                    <span class="text-[#666666] font-[500]">队员：</span>
                    <span class="text-[#666666] font-[400]" v-for="(item, index) in x.playersList" :key="index">
                      {{ item.userName }}（{{ item.idCard }}）
                      <span v-if="item.delFlag != 0" class='status-deleted'>（已删除）</span>
                    </span>
                  </p>
                  <p v-if="x && x.instructorList && x.instructorList.length > 0">
                    <span class="text-[#666666] font-[500]">指导教师：</span>
                    <span class="text-[#666666] font-[400]" v-for="(item, index) in x.instructorList" :key="index">
                      {{ item.userName }}
                      <span v-if="item.guideTeacherPhone">（{{ item.guideTeacherPhone }}）</span>
                    </span>
                  </p>
                </div>
                <div class="players-info" style="font-size: 14px; color: #666; margin-top: 8px">
                  {{ x.teamSize }} 名队员 × ￥ {{ x.fee }}/人
                </div>
              </div>
              <div class="product-price" style="font-size: 18px; color: #e53935; font-weight: 600">
                ￥{{ x.subtotal ? x.subtotal : 0 }}
              </div>
            </div>
            <el-divider border-style="dashed" />
          </div>
        </template>
        <!-- 赛证互通 -->
        <template v-if="xinxi.commodityType === 'cert'">
          <div class="flex items-center justify-between">
            <div class="flex-1 text-[#444]">
              <div class="mb-[5px]">赛证互通：{{ xinxi.teamInfoLists?.rulerName }}</div>
              <div class="mb-[5px]">
                <span>源证书：</span>
                <span class="text-[#000]" v-for="cert in xinxi.teamInfoLists?.originCertList"> 【{{ cert.certConfigName
                }}】</span>
              </div>
              <div class="mb-[5px]">
                <span>目标证书：</span>
                <span class="text-[#3169f8]" v-for="cert in xinxi.teamInfoLists?.targetCertList">【{{ cert.certConfigName
                }}】</span>
              </div>
            </div>
            <div class="text-[16px] text-[#e53935]">￥ {{ xinxi.amount }}</div>
          </div>
        </template>
      </el-card>
      <el-card class="kapian">
        <div class="zzxx">金额明细</div>
        <div class="mingxi">
          <div class="xiang">
            <div>商品总额</div>
            <div>¥{{ xinxi.amount }}</div>
          </div>
          <!-- <div v-if="xinxi.numDetailList">
            <div
              v-for="(item, index) in xinxi.numDetailList"
              :key="index"
              class="xiang"
            >
              <div>
                <div v-if="index == 0">队员人数*单价</div>
              </div>
              <div>{{ item.num }}人 × ¥{{ item.price }}/人</div>
            </div>
          </div> -->

          <div class="xiang">
            <div style="font-size: 20px; font-weight: bold">实付金额</div>
            <div style="font-size: 20px; font-weight: bold; color: red">
              ¥{{ xinxi.amount }}
            </div>
          </div>
        </div>
      </el-card>
      <el-card class="kapian">
        <div style="margin-top: 20px; display: flex; justify-content: flex-end">
          <el-button class="sctx" @click="zhanghaoshezhi">返回订单列表</el-button>
        </div>
      </el-card>
    </div>
    <el-dialog v-model="dialogVisible">
      <img w-full :src="dialogImageUrl" alt="Preview Image" />
    </el-dialog>
  </div>
</template>
<script setup>
  import Breadcrumbar from "@/components/breadcrumbar.vue";
  import { updatePaymentProof, orderchak } from "@/api/personal/index";
  import { getToken } from "@/utils/auth";
  import { ElMessage, genFileId } from "element-plus";
  import { useRoute, useRouter } from "vue-router";
  const { proxy } = getCurrentInstance();
  import { updatePayMethod, getOfflineBankInfo } from "@/api/pay.js";
  const { pay_status, pay_method } = proxy.useDict("pay_status", "pay_method");
  const router = useRouter();
  const route = useRoute();
  const COMMODITY_TYPE = {
    competition: '团队信息',
    cert: '赛证互通'
  }
  const offline = ref({});
  const getOfflineBankInfolist = () => {
    getOfflineBankInfo(route.query.id).then((res) => {
      if (res.code == 200) {
        console.log(res, 123456);
        offline.value = res.data;
      }
    });
  };
  getOfflineBankInfolist();

  const saomazhifu = () => {
    const params = {
      id: route.query.id,
      payMethod: "online", //online-线上转账，offline-线下转账
    };
    updatePayMethod(params).then((res) => {
      router.push({
        path: "/personal/paymentrecords/payment",
        query: {
          id: route.query.id,
        },
      });
    });
  };
  // 返回
  const zhanghaoshezhi = () => {
    router.push({
      path: "/personal/paymentrecords",
    });
  };
  const dialogVisible = ref(false);
  const dialogImageUrl = ref(null);
  // 文件上传前校验
  const beforeUpload = (file) => {
    const allowedTypes = ["image/jpeg", "image/png"];
    const isValidType = allowedTypes.includes(file.type);

    if (!isValidType) {
      ElMessage.error("上传图片只能是 JPG、JPEG 或 PNG 格式！");
      return false; // 阻止上传
    }
    const maxSize = 50 * 1024 * 1024; // 1MB（单位：字节）
    if (file.size > maxSize) {
      ElMessage.error("上传文件不能超过 1MB！");
      return false; // 阻止上传
    }
    return true; // 允许上传
  };
  const handlePictureCardPreview = (uploadFile) => {
    dialogImageUrl.value = uploadFile.url;
    dialogVisible.value = true;
  };
  /*** 用户导入参数 */
  const upload = reactive({
    // 是否显示弹出层（用户导入）
    open: false,
    // 文件类型
    worksFormat: ".jpg,.jpeg,.png",
    // 文件大小
    worksFormatSize: "50",
    // 弹出层标题（用户导入）
    title: "",
    // 是否禁用上传
    isUploading: false,
    // 设置上传的请求头部
    headers: { Authorization: "Bearer " + getToken() },
    // 上传的地址
    url: import.meta.env.VITE_APP_BASE_API + "/file/upload",
  });
  const fileList = ref([]);

  /** 文件上传失败 */
  const handleFileError = (error, file, fileList) => {
    ElMessage.error("上传失败");
  };
  const zuopiin = ref([]);
  const onUploadSuccess = (response, file) => {
    if (response.code == 200) {
      zuopiin.value.push(response.data.url);

      // ElMessage.success("上传成功");
    } else {
      ElMessage.error(response.msg);
    }
  };
  const handleRemove = (uploadFile, uploadFiles) => {
    zuopiin.value = [];

    uploadFiles.forEach((item) => {
      if (item.response) {
        zuopiin.value.push(item.response.data.url);
      } else {
        zuopiin.value.push(item.url);
      }
    });
  };

  const xinxi = ref({
    payStatus: "paid",
  });

  const orderchaklist = () => {
    orderchak(route.query.id).then((res) => {
      xinxi.value = res.data;
      xinxi.value.teamInfoLists = JSON.parse(xinxi.value.teamInfoList || '[]')
      console.log(xinxi.value.teamInfoLists, 123456)
      zuopiin.value = xinxi.value.paymentProofFiles?.split(",") || [];
      fileList.value = xinxi.value.paymentProofFiles?.split(",").map((url) => {
        // 从 URL 中提取文件名（最后一部分）
        const name = url.substring(url.lastIndexOf("/") + 1);
        return {
          name: name,
          url: url,
        };
      });
    });
  };
  orderchaklist();
  const tijiao = () => {
    if (zuopiin.value && zuopiin.value.length > 0) {
      const params = {
        id: route.query.id,
        paymentProofFiles: zuopiin.value.join(","),
      };
      updatePaymentProof(params).then((res) => {
        if (res.code == 200) {
          router.push({
            path: "/personal/paymentrecords",
          });
        } else if (res.code == 300) {
          ElMessage.error(res.msg);
        }
      });
    } else {
      ElMessage.error("请上传凭证");
    }
  };
</script>



<style scoped lang="scss">
  .kapian {
    background-color: #fff;
    padding: 0;
    margin: 20px 0;

    .title {
      display: flex;
      align-items: center;
      justify-content: space-between;

      .titleleft {
        font-size: 24px;
        font-weight: bold;
      }
    }

    .jbxx {
      margin-top: 20px;
      font-size: 18px;
      font-weight: 500;
      margin-bottom: 15px;
      border-bottom: 1px solid #e0e0e0;
    }

    .zhxx {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 10px;
      background-color: #f8f9fa;
      border-radius: 4px;
      margin: 10px;
      font-size: 14px;
    }

    .zzxx {
      font-size: 18px;
      font-weight: 500;
      margin-bottom: 15px;
    }

    .lankuang {
      background-color: #e3f2fd;
      padding: 20px;
      border-radius: 4px;
      border-left: 4px solid #2196f3;
      margin: 20px 0;
      padding: 30px;

      .zhuanzhangtitle {
        font-size: 14px;
        color: #666;
      }

      .zhuanzhangvalue {
        font-size: 20px;
        font-weight: 500;
        color: #333;
        margin-top: 10px;
      }

      .beizhu {
        background-color: #fff;
        padding: 15px;
        border-radius: 4px;
        border: 1px solid #b3e5fc;
        margin: 15px 0;
        font-size: 14px;
      }
    }

    .sc {
      background-color: #f8f9fa;
      padding: 20px;
      border-radius: 4px;
      margin: 20px 0;
      border: 2px dashed #dee2e6;
      text-align: center;

      .shangchuanbt {
        font-weight: bold;
      }
    }

    .notes {
      background-color: #fff3cd;
      padding: 15px;
      border-radius: 4px;
      margin: 20px 0;
      border-left: 4px solid #ffc107;
    }

    .mingxi {
      text-align: right;
      margin: 20px 0;
      padding: 20px;
      background-color: #f8f9fa;
      border-radius: 4px;

      .xiang {
        display: flex;
        justify-content: space-between;
        align-items: center;
        line-height: 40px;
      }
    }
  }

  :deep(.el-upload-list__item) {
    width: 200px;
    margin: 0 auto;
  }

  .product-item {
    padding: 15px 0;

    .product-main {
      display: flex;
      align-items: center;
      gap: 20px;
    }

    .product-image {
      width: 100px;
      height: 100px;
      flex-shrink: 0;
      border-radius: 8px;
      overflow: hidden;
      border: 1px solid #e0e0e0;

      .product-img {
        width: 100%;
        height: 100%;
        object-fit: cover;
        transition: transform 0.3s ease;

        &:hover {
          transform: scale(1.05);
        }
      }
    }

    .product-info {
      flex: 1;
      display: flex;
      flex-direction: column;
      gap: 8px;
    }

    .team-code {
      font-size: 14px;
      color: #333;
    }

    .product-name {
      font-size: 16px;
      font-weight: 600;
      color: #333;
      line-height: 1.4;
    }

    .players-info {
      font-size: 14px;
      color: #666;
    }

    .product-price {
      font-size: 18px;
      font-weight: 600;
      color: #e53935;
      margin-left: auto;
    }

    .status-normal {
      color: #67c23a;
    }

    .status-deleted {
      color: #f56c6c;
    }
  }

  :deep(.el-upload-list--picture-card) {
    display: inline-block;
  }
</style>