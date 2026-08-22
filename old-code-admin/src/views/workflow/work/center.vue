<template>
    <div class="app-container">
        <ul class="grid" v-if="dataStatus==1">
            <li class="grid-item" v-for="item in processList" :key="item.processKey">
                <el-card>
                    <template #header>
                        <div class="card-header">
                            <span class="process-name">{{ item.processName }}</span>
                        </div>
                    </template>
                    <el-scrollbar height="30px">
                        
                        <div class="content">
                            <el-link
                                type="primary"
                                :underline="false"
                                v-hasPermi="['workflow:process:start']"
                                @click="handleStart(item)">
                                >> 发起流程
                            </el-link>
                        </div>
                    </el-scrollbar>
                </el-card>
            </li>
        </ul>
        <empty v-if="dataStatus==2" description="暂无上线流程"></empty>
    </div>
</template>
<script setup name="WorkCenter">
import { listProcess } from "@/api/workflow/process";

const router = useRouter();

const loading = ref(true) // 遮罩层
const processList = ref([]) // 流程定义表格数据
const total = ref(0) // 总条数
const dataStatus = ref(0)

// 查询参数
const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 100,
    processKey: undefined,
    processName: undefined
  },
})

const { queryParams } = toRefs(data);

/** 查询列表 */
const getList = () => {
    loading.value = true
    listProcess(queryParams.value).then(response => {
        processList.value = response.rows
        total.value = response.total
        if (total.value > 0) {
            dataStatus.value = 1
        } else {
            dataStatus.value = 2
        }
        loading.value = false
        // handleStart(processList.value[0])
    })
}

/** 发起流程 */
function handleStart(item) {
  router.push({
    path: '/wentiflow/process/start/' + item.deploymentId,
    query: {
      definitionId: item.definitionId,
    }
  })
}

getList();

</script>
<style lang="scss" scoped>
.grid {
    margin: 0;
    padding: 0;
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    grid-gap: 20px;
    :deep(.el-card__header) {
        background: #0062b1;
        color: #ffffff;
    }
    .card-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        font-size: 14px;
        .process-name {
            font-weight: bold;
        }
    }
    .content {
        margin-top: 5px;
        font-size: 14px;
        text-align: right;
    }

}
</style>