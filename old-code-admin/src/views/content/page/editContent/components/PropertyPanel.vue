<template>
  <div class="property-panel-container">
    <div class="panel-header">
      <h3>属性配置</h3>
    </div>
    
    <div class="panel-content" v-if="selectedComponent">
      <!-- 基本信息 -->
      <div class="property-section">
        <h4 class="section-title">基本信息</h4>
        <div class="property-item">
          <label class="property-label">组件类型</label>
          <div class="property-value">{{ selectedComponent.type }}</div>
        </div>
        <div class="property-item">
          <label class="property-label">组件ID</label>
          <div class="property-value">{{ selectedComponent.id }}</div>
        </div>
      </div>
      
      <!-- 文本内容属性 -->
      <template v-if="['text', 'title', 'paragraph', 'hyperlink'].includes(selectedComponent.type)">
        <div class="property-section">
          <h4 class="section-title">文本内容</h4>
          <div class="property-item">
            <label class="property-label">内容</label>
            <el-input
              v-model="componentData.content"
              type="textarea"
              :rows="3"
              @change="updateComponent"
            ></el-input>
          </div>
          <div class="property-item">
            <label class="property-label">字体大小</label>
            <el-input-number
              v-model.number="fontSizeValue"
              :min="8"
              :max="72"
              @change="updateFontSize"
            ></el-input-number>
            <span class="unit">px</span>
          </div>
          <div class="property-item">
            <label class="property-label">文字颜色</label>
            <el-color-picker
              v-model="componentData.style.color"
              @change="updateComponent"
              :predefine="[]"
            ></el-color-picker>
          </div>
          <div class="property-item">
            <label class="property-label">文本对齐</label>
            <el-radio-group v-model="componentData.style.textAlign" @change="updateComponent">
              <el-radio label="left">左对齐</el-radio>
              <el-radio label="center">居中</el-radio>
              <el-radio label="right">右对齐</el-radio>
            </el-radio-group>
          </div>
          <div class="property-item" v-if="selectedComponent.type === 'title'">
            <label class="property-label">标题级别</label>
            <el-select v-model="componentData.level" @change="updateComponent">
              <el-option label="H1" :value="1"></el-option>
              <el-option label="H2" :value="2"></el-option>
              <el-option label="H3" :value="3"></el-option>
              <el-option label="H4" :value="4"></el-option>
              <el-option label="H5" :value="5"></el-option>
              <el-option label="H6" :value="6"></el-option>
            </el-select>
          </div>
        </div>
        
        <!-- 超链接特有属性 -->
        <div class="property-section" v-if="selectedComponent.type === 'hyperlink'">
          <h4 class="section-title">链接设置</h4>
          <div class="property-item">
            <label class="property-label">是否展示下划线</label>
            <el-switch
              v-model="componentData.showUnderline"
              active-text="是"
              inactive-text="否"
              @change="updateComponent"
            ></el-switch>
          </div>
          <div class="property-item">
            <label class="property-label">跳转地址</label>
            <el-input
              v-model="componentData.href"
              placeholder="请输入跳转地址"
              @change="updateComponent"
            ></el-input>
          </div>
        </div>
      </template>
      
      <!-- 首页标签组件属性 -->
      <template v-if="selectedComponent.type === 'min_home_tabs'">
        <div class="property-section">
          <h4 class="section-title">标签显示设置</h4>
          <div class="property-item">
            <label class="property-label">是否显示赛事中心</label>
            <el-switch
              v-model="componentData.showTournamentCenter"
              :default="true"
              active-text="显示"
              inactive-text="隐藏"
              @change="updateComponent"
              active-color="#13ce66"
              inactive-color="#ff4949"
            ></el-switch>
          </div>
          <div class="property-item">
            <label class="property-label">是否显示学习中心</label>
            <el-switch
              v-model="componentData.showLearningCenter"
              :default="true"
              active-text="显示"
              inactive-text="隐藏"
              @change="updateComponent"
              active-color="#13ce66"
              inactive-color="#ff4949"
            ></el-switch>
          </div>
          <div class="property-item">
            <label class="property-label">是否显示资讯中心</label>
            <el-switch
              v-model="componentData.showNewsCenter"
              :default="true"
              active-text="显示"
              inactive-text="隐藏"
              @change="updateComponent"
              active-color="#13ce66"
              inactive-color="#ff4949"
            ></el-switch>
          </div>
          <div class="property-item">
            <label class="property-label">是否显示技术支持</label>
            <el-switch
              v-model="componentData.showTechSupport"
              :default="true"
              active-text="显示"
              inactive-text="隐藏"
              @change="updateComponent"
              active-color="#13ce66"
              inactive-color="#ff4949"
            ></el-switch>
          </div>
          <div class="property-item">
            <label class="property-label">是否显示大唐杯</label>
            <el-switch
              v-model="componentData.showDatangCup"
              :default="true"
              active-text="显示"
              inactive-text="隐藏"
              @change="updateComponent"
              active-color="#13ce66"
              inactive-color="#ff4949"
            ></el-switch>
          </div>
        </div>
      </template>
      
      <!-- 赛事列表组件属性 -->
      <template v-if="selectedComponent.type === 'mini_tournament_list'">
        <div class="property-section">
          <h4 class="section-title">基本设置</h4>
          <!-- 无需标题设置 -->
        </div>
        <div class="property-section">
          <h4 class="section-title">数据源设置</h4>
          <div class="property-item">
            <label class="property-label">数据源</label>
            <el-select
              v-model="componentData.dataSourceId"
              filterable
              remote
              reserve-keyword
              placeholder="输入数据源名称搜索，选择数据源"
              :remote-method="remoteMethod"
              :loading="epLoading"
              remote-show-suffix
              style="width: 100%"
              @change="handleDataSourceSelect"
            >
              <el-option
                v-for="item in epOptions"
                :key="item.dataId"
                :label="item.dataName"
                :value="item.dataId"
              />
            </el-select>
          </div>
        </div>
      </template>

      <!-- 赛事标签组件属性 -->
      <template v-if="selectedComponent.type === 'mini_tournament_tabs'">
        <div class="property-section">
          <h4 class="section-title">显示控制</h4>
          <div class="property-item">
            <label class="property-label">是否显示我的团队</label>
            <el-switch
              v-model="componentData.showMyTeam"
              :default="true"
              active-text="显示"
              inactive-text="隐藏"
              @change="updateComponent"
              active-color="#13ce66"
              inactive-color="#ff4949"
            ></el-switch>
          </div>
          <div class="property-item">
            <label class="property-label">是否显示我的赛事</label>
            <el-switch
              v-model="componentData.showMyTournament"
              :default="true"
              active-text="显示"
              inactive-text="隐藏"
              @change="updateComponent"
              active-color="#13ce66"
              inactive-color="#ff4949"
            ></el-switch>
          </div>
          <div class="property-item">
            <label class="property-label">是否显示成绩查询</label>
            <el-switch
              v-model="componentData.showScoreQuery"
              :default="true"
              active-text="显示"
              inactive-text="隐藏"
              @change="updateComponent"
              active-color="#13ce66"
              inactive-color="#ff4949"
            ></el-switch>
          </div>
        </div>
      </template>
      
      <!-- 搜索框组件属性 -->
      <template v-if="selectedComponent.type === 'min_home_search'">
        <div class="property-section">
          <h4 class="section-title">搜索框设置</h4>
          <!-- Placeholder设置 -->
          <div class="property-item">
            <label class="property-label">搜索提示文本</label>
            <el-input
              v-model="componentData.placeholder"
              placeholder="请输入搜索提示文本"
              @change="updateComponent"
            ></el-input>
          </div>
          <!-- 按钮文本设置 -->
          <div class="property-item">
            <label class="property-label">按钮文本</label>
            <el-input
              v-model="componentData.buttonText"
              placeholder="请输入按钮文本"
              @change="updateComponent"
            ></el-input>
          </div>
        </div>
      </template>
      
      <!-- Mini赛事组件属性 -->
      <template v-if="selectedComponent.type === 'mini_home_tournament'">
        <div class="property-section">
          <h4 class="section-title">基本设置</h4>
          <div class="property-item">
            <label class="property-label">标题</label>
            <el-input
              v-model="componentData.title"
              placeholder="请输入赛事中心标题"
              @change="updateComponent"
            ></el-input>
          </div>
        </div>
        <div class="property-section">
          <h4 class="section-title">数据源设置</h4>
          <div class="property-item">
            <label class="property-label">数据源</label>
            <el-select
              v-model="componentData.dataSourceId"
              filterable
              remote
              reserve-keyword
              placeholder="输入数据源名称搜索，选择数据源"
              :remote-method="remoteMethod"
              :loading="epLoading"
              remote-show-suffix
              style="width: 100%"
              @change="handleDataSourceSelect"
            >
              <el-option
                v-for="item in epOptions"
                :key="item.dataId"
                :label="item.dataName"
                :value="item.dataId"
              />
            </el-select>
          </div>
        </div>
      </template>
      
      <!-- Mini学习中心组件属性 -->
      <template v-if="selectedComponent.type === 'mini_home_learn'">
        <div class="property-section">
          <h4 class="section-title">基本设置</h4>
          <div class="property-item">
            <label class="property-label">标题</label>
            <el-input
              v-model="componentData.title"
              placeholder="请输入学习中心标题"
              @change="updateComponent"
            ></el-input>
          </div>
        </div>
        <div class="property-section">
          <h4 class="section-title">数据源设置</h4>
          <div class="property-item">
            <label class="property-label">数据源</label>
            <el-select
              v-model="componentData.dataSourceId"
              filterable
              remote
              reserve-keyword
              placeholder="输入数据源名称搜索，选择数据源"
              :remote-method="remoteMethod"
              :loading="epLoading"
              remote-show-suffix
              style="width: 100%"
              @change="handleDataSourceSelect"
            >
              <el-option
                v-for="item in epOptions"
                :key="item.dataId"
                :label="item.dataName"
                :value="item.dataId"
              />
            </el-select>
          </div>
        </div>
      </template>
      
      <!-- PC学习中心组件属性 -->
      <template v-if="selectedComponent.type === 'pc_home_learn'">
        <div class="property-section">
          <h4 class="section-title">基本设置</h4>
          <div class="property-item">
            <label class="property-label">标题</label>
            <el-input
              v-model="componentData.title"
              placeholder="请输入学习中心标题"
              @change="updateComponent"
            ></el-input>
          </div>

        </div>
        <div class="property-section">
          <h4 class="section-title">数据源设置</h4>
          <div class="property-item">
            <label class="property-label">数据源</label>
            <el-select
              v-model="componentData.dataSourceId"
              filterable
              remote
              reserve-keyword
              placeholder="输入数据源名称搜索，选择数据源"
              :remote-method="remoteMethod"
              :loading="epLoading"
              remote-show-suffix
              style="width: 100%"
              @change="handleDataSourceSelect"
            >
              <el-option
                v-for="item in epOptions"
                :key="item.dataId"
                :label="item.dataName"
                :value="item.dataId"
              />
            </el-select>
          </div>
        </div>
      </template>
      
      <!-- PC资讯中心组件属性 -->
      <template v-if="selectedComponent.type === 'pc_home_information'">
        <div class="property-section">
          <h4 class="section-title">基本设置</h4>
          <div class="property-item">
            <label class="property-label">标题</label>
            <el-input
              v-model="componentData.title"
              placeholder="请输入资讯中心标题"
              @change="updateComponent"
            ></el-input>
          </div>
        </div>
        <!-- <div class="property-section">
          <h4 class="section-title">最新资讯数据源设置</h4>
          <div class="property-item">
            <label class="property-label">数据源</label>
            <el-select
              v-model="componentData.dataSourceId"
              filterable
              remote
              reserve-keyword
              placeholder="输入数据源名称搜索，选择数据源"
              :remote-method="remoteMethod"
              :loading="epLoading"
              remote-show-suffix
              style="width: 100%"
              @change="handleInfoDataSourceSelect"
            >
              <el-option
                v-for="item in epOptions"
                :key="item.dataId"
                :label="item.dataName"
                :value="item.dataId"
              />
            </el-select>
          </div>
        </div> -->
        <div class="property-section">
          <h4 class="section-title">公告通知数据源设置</h4>
          <div class="property-item">
            <label class="property-label">数据源</label>
            <el-select
              v-model="componentData.dataSourceTwoId"
              filterable
              remote
              reserve-keyword
              placeholder="输入数据源名称搜索，选择数据源"
              :remote-method="remoteMethod"
              :loading="epLoading"
              remote-show-suffix
              style="width: 100%"
              @change="handleNoticeDataSourceSelect"
            >
              <el-option
                v-for="item in epOptions"
                :key="item.dataId"
                :label="item.dataName"
                :value="item.dataId"
              />
            </el-select>
          </div>
        </div>
      </template>
      
      <!-- 移动端信息展示组件属性 -->
      <template v-if="selectedComponent.type === 'mini_information'">
        <div class="property-section">
          <h4 class="section-title">基本设置</h4>
          <div class="property-item">
            <label class="property-label">标题</label>
            <el-input
              v-model="componentData.title"
              placeholder="请输入信息展示标题"
              @change="updateComponent"
            ></el-input>
          </div>
        </div>
        <!-- <div class="property-section">
          <h4 class="section-title">最新资讯数据源设置</h4>
          <div class="property-item">
            <label class="property-label">数据源</label>
            <el-select
              v-model="componentData.dataSourceId"
              filterable
              remote
              reserve-keyword
              placeholder="输入数据源名称搜索，选择数据源"
              :remote-method="remoteMethod"
              :loading="epLoading"
              remote-show-suffix
              style="width: 100%"
              @change="handleInfoDataSourceSelect"
            >
              <el-option
                v-for="item in epOptions"
                :key="item.dataId"
                :label="item.dataName"
                :value="item.dataId"
              />
            </el-select>
          </div>
          <div class="property-item">
            <label class="property-label">置顶轮播最新资讯</label>
            <el-switch
              v-model="componentData.topInfoCarousel"
              :default="true"
              active-text="开启"
              inactive-text="关闭"
              @change="updateComponent"
              active-color="#13ce66"
              inactive-color="#ff4949"
            ></el-switch>
          </div>
        </div> -->
        <div class="property-section">
          <h4 class="section-title">公告通知数据源设置</h4>
          <div class="property-item">
            <label class="property-label">数据源</label>
            <el-select
              v-model="componentData.dataSourceTwoId"
              filterable
              remote
              reserve-keyword
              placeholder="输入数据源名称搜索，选择数据源"
              :remote-method="remoteMethod"
              :loading="epLoading"
              remote-show-suffix
              style="width: 100%"
              @change="handleNoticeDataSourceSelect"
            >
              <el-option
                v-for="item in epOptions"
                :key="item.dataId"
                :label="item.dataName"
                :value="item.dataId"
              />
            </el-select>
          </div>
          <div class="property-item">
            <label class="property-label">置顶轮播公告通知</label>
            <el-switch
              v-model="componentData.topNoticeCarousel"
              :default="true"
              active-text="开启"
              inactive-text="关闭"
              @change="updateComponent"
              active-color="#13ce66"
              inactive-color="#ff4949"
            ></el-switch>
          </div>
        </div>
      </template>
      
      <!-- PC技术支持组件属性 -->
      <template v-if="selectedComponent.type === 'pc_support'">
        <div class="property-section">
          <h4 class="section-title">基本设置</h4>
          <div class="property-item">
            <label class="property-label">标题</label>
            <el-input
              v-model="componentData.title"
              placeholder="请输入技术支持标题"
              @change="updateComponent"
            ></el-input>
          </div>
        </div>
        <div class="property-section">
          <h4 class="section-title">数据源设置</h4>
          <div class="property-item">
            <label class="property-label">数据源</label>
            <el-select
              v-model="componentData.dataSourceId"
              filterable
              remote
              reserve-keyword
              placeholder="输入数据源名称搜索，选择数据源"
              :remote-method="remoteMethod"
              :loading="epLoading"
              remote-show-suffix
              style="width: 100%"
              @change="handleDataSourceSelect"
            >
              <el-option
                v-for="item in epOptions"
                :key="item.dataId"
                :label="item.dataName"
                :value="item.dataId"
              />
            </el-select>
          </div>
        </div>
      </template>
      
      <!-- PC赛事列表组件属性 -->
      <template v-if="selectedComponent.type === 'pc_tournament_list'">
        <div class="property-section">
          <h4 class="section-title">基本设置</h4>
          <div class="property-item">
            <label class="property-label">标题</label>
            <el-input
              v-model="componentData.title"
              placeholder="请输入赛事列表标题"
              @change="updateComponent"
            ></el-input>
          </div>
        </div>
        <div class="property-section">
          <h4 class="section-title">数据源设置</h4>
          <div class="property-item">
            <label class="property-label">数据源</label>
            <el-select
              v-model="componentData.dataSourceId"
              filterable
              remote
              reserve-keyword
              placeholder="输入数据源名称搜索，选择数据源"
              :remote-method="remoteMethod"
              :loading="epLoading"
              remote-show-suffix
              style="width: 100%"
              @change="handleDataSourceSelect"
            >
              <el-option
                v-for="item in epOptions"
                :key="item.dataId"
                :label="item.dataName"
                :value="item.dataId"
              />
            </el-select>
          </div>
        </div>
      </template>
      
      <!-- Mini资讯中心组件属性 -->
      <template v-if="selectedComponent.type === 'mini_home_information'">
        <div class="property-section">
          <h4 class="section-title">基本设置</h4>
          <div class="property-item">
            <label class="property-label">标题</label>
            <el-input
              v-model="componentData.title"
              placeholder="请输入资讯中心标题"
              @change="updateComponent"
            ></el-input>
          </div>

        </div>
        <!-- <div class="property-section">
          <h4 class="section-title">最新资讯数据源设置</h4>
          <div class="property-item">
            <label class="property-label">数据源</label>
            <el-select
              v-model="componentData.dataSourceId"
              filterable
              remote
              reserve-keyword
              placeholder="输入数据源名称搜索，选择数据源"
              :remote-method="remoteMethod"
              :loading="epLoading"
              remote-show-suffix
              style="width: 100%"
              @change="handleInfoDataSourceSelect"
            >
              <el-option
                v-for="item in epOptions"
                :key="item.dataId"
                :label="item.dataName"
                :value="item.dataId"
              />
            </el-select>
          </div>
        </div> -->
        <div class="property-section">
          <h4 class="section-title">公告通知数据源设置</h4>
          <div class="property-item">
            <label class="property-label">数据源</label>
            <el-select
              v-model="componentData.dataSourceTwoId"
              filterable
              remote
              reserve-keyword
              placeholder="输入数据源名称搜索，选择数据源"
              :remote-method="remoteMethod"
              :loading="epLoading"
              remote-show-suffix
              style="width: 100%"
              @change="handleNoticeDataSourceSelect"
            >
              <el-option
                v-for="item in epOptions"
                :key="item.dataId"
                :label="item.dataName"
                :value="item.dataId"
              />
            </el-select>
          </div>
        </div>
      </template>

      <!-- 信息展示组件属性 -->
      <template v-if="selectedComponent.type === 'pc_information'">
        <div class="property-section">
          <h4 class="section-title">基本设置</h4>
          <div class="property-item">
            <label class="property-label">标题</label>
            <el-input
              v-model="componentData.title"
              placeholder="请输入信息展示标题"
              @change="updateComponent"
            ></el-input>
          </div>
        </div>
        <!-- <div class="property-section">
          <h4 class="section-title">最新资讯数据源设置</h4>
          <div class="property-item">
            <label class="property-label">数据源</label>
            <el-select
              v-model="componentData.dataSourceId"
              filterable
              remote
              reserve-keyword
              placeholder="输入数据源名称搜索，选择数据源"
              :remote-method="remoteMethod"
              :loading="epLoading"
              remote-show-suffix
              style="width: 100%"
              @change="handleInfoDataSourceSelect"
            >
              <el-option
                v-for="item in epOptions"
                :key="item.dataId"
                :label="item.dataName"
                :value="item.dataId"
              />
            </el-select>
          </div>
          <div class="property-item">
            <label class="property-label">是否置顶轮播最新资讯前三条</label>
            <el-switch
              v-model="componentData.topInfoCarousel"
              :default="true"
              active-text="是"
              inactive-text="否"
              @change="updateComponent"
              active-color="#13ce66"
              inactive-color="#ff4949"
            ></el-switch>
          </div>
        </div> -->
        <div class="property-section">
          <h4 class="section-title">公告通知数据源设置</h4>
          <div class="property-item">
            <label class="property-label">数据源</label>
            <el-select
              v-model="componentData.dataSourceTwoId"
              filterable
              remote
              reserve-keyword
              placeholder="输入数据源名称搜索，选择数据源"
              :remote-method="remoteMethod"
              :loading="epLoading"
              remote-show-suffix
              style="width: 100%"
              @change="handleNoticeDataSourceSelect"
            >
              <el-option
                v-for="item in epOptions"
                :key="item.dataId"
                :label="item.dataName"
                :value="item.dataId"
              />
            </el-select>
          </div>
          <div class="property-item">
            <label class="property-label">是否置顶轮播公告通知前三条</label>
            <el-switch
              v-model="componentData.topNoticeCarousel"
              :default="true"
              active-text="是"
              inactive-text="否"
              @change="updateComponent"
              active-color="#13ce66"
              inactive-color="#ff4949"
            ></el-switch>
          </div>
        </div>
      </template>
      
      <!-- 赛事模块属性 -->
      <template v-if="selectedComponent.type === 'pc_home_tournament'">
        <div class="property-section">
          <h4 class="section-title">赛事模块设置</h4>
          <!-- 标题设置 -->
          <div class="property-item">
            <label class="property-label">模块标题</label>
            <el-input
              v-model="componentData.title"
              placeholder="请输入赛事模块标题"
              @change="updateComponent"
            ></el-input>
          </div>
          <!-- 数据源设置 -->
          <div class="property-item">
            <label class="property-label">数据源</label>
            <el-select
              v-model="componentData.dataSourceId"
              filterable
              remote
              reserve-keyword
              placeholder="输入数据源名称搜索，选择数据源"
              :remote-method="remoteMethod"
              :loading="epLoading"
              remote-show-suffix
              style="width: 100%"
              @change="handleTournamentDataSourceSelect"
            >
              <el-option
                v-for="item in epOptions"
                :key="item.dataId"
                :label="item.dataName"
                :value="item.dataId"
              />
            </el-select>
          </div>

        </div>
      </template>

      <!-- 轮播图属性 -->
      <template v-if="selectedComponent.type === 'global_banner'">
        <div class="property-section">
          <h4 class="section-title">轮播图设置</h4>
          <div class="property-item">
            <label class="property-label">数据源</label>
            <el-select
              v-model="componentData.dataSourceId"
              filterable
              remote
              reserve-keyword
              placeholder="输入数据源名称搜索，选择数据源"
              :remote-method="remoteMethod"
              :loading="epLoading"
              remote-show-suffix
              style="width: 100%"
              @change="handleDataSourceSelect"
            >
              <el-option
                v-for="item in epOptions"
                :key="item.dataId"
                :label="item.dataName"
                :value="item.dataId"
              />
            </el-select>
          </div>
          <div class="property-item">
            <label class="property-label">是否自动轮播</label>
            <el-switch
              v-model="componentData.autoPlay"
              @change="updateComponent"
              active-color="#13ce66"
              inactive-color="#ff4949"
            ></el-switch>
          </div>
          <div class="property-item">
            <label class="property-label">轮播间隔时间(ms)</label>
            <el-input-number
              v-model.number="componentData.interval"
              :min="1000"
              :max="10000"
              :step="500"
              @change="updateComponent"
            ></el-input-number>
          </div>
          <div class="property-item">
            <label class="property-label">是否循环播放</label>
            <el-switch
              v-model="componentData.loop"
              @change="updateComponent"
              active-color="#13ce66"
              inactive-color="#ff4949"
            ></el-switch>
          </div>
          <div class="property-item">
            <label class="property-label">轮播高度(px)</label>
            <el-input-number
              v-model.number="carouselHeight"
              :min="50"
              :max="1000"
              @change="updateCarouselHeight"
            ></el-input-number>
          </div>
        </div>
      </template>

      <!-- 图片属性 -->
      <template v-if="selectedComponent.type === 'image'">
        <div class="property-section">
          <h4 class="section-title">图片设置</h4>
          <div class="property-item">
            <label class="property-label">图片URL</label>
            <el-input
              v-model="componentData.src"
              placeholder="请输入图片URL"
              @change="updateComponent"
            ></el-input>
          </div>
          <div class="property-item">
            <label class="property-label">图片描述</label>
            <el-input
              v-model="componentData.alt"
              placeholder="请输入图片描述"
              @change="updateComponent"
            ></el-input>
          </div>
          <div class="property-item">
            <label class="property-label">宽度</label>
            <el-input-number
              v-model.number="widthValue"
              :min="1"
              @change="updateWidth"
            ></el-input-number>
            <span class="unit">px</span>
          </div>
          <div class="property-item">
            <label class="property-label">高度</label>
            <el-input-number
              v-model.number="heightValue"
              :min="1"
              @change="updateHeight"
            ></el-input-number>
            <span class="unit">px</span>
          </div>
        </div>
      </template>
      
      <!-- 按钮属性 -->
      <template v-if="selectedComponent.type === 'button'">
        <div class="property-section">
          <h4 class="section-title">按钮设置</h4>
          <div class="property-item">
            <label class="property-label">按钮文本</label>
            <el-input
              v-model="componentData.text"
              @change="updateComponent"
            ></el-input>
          </div>
          <div class="property-item">
            <label class="property-label">按钮类型</label>
            <el-select v-model="componentData.type" @change="updateComponent">
              <el-option label="默认" :value="''"></el-option>
              <el-option label="主要" :value="'primary'"></el-option>
              <el-option label="成功" :value="'success'"></el-option>
              <el-option label="警告" :value="'warning'"></el-option>
              <el-option label="危险" :value="'danger'"></el-option>
              <el-option label="信息" :value="'info'"></el-option>
            </el-select>
          </div>
          <div class="property-item">
            <label class="property-label">按钮尺寸</label>
            <el-select v-model="componentData.size" @change="updateComponent">
              <el-option label="默认" :value="''"></el-option>
              <el-option label="小型" :value="'small'"></el-option>
              <el-option label="大型" :value="'large'"></el-option>
            </el-select>
          </div>
          <div class="property-item">
            <label class="property-label">点击动作</label>
            <el-input
              v-model="componentData.action"
              placeholder="请输入点击动作"
              @change="updateComponent"
            ></el-input>
          </div>
        </div>
      </template>
      
      <!-- 卡片属性 -->
      <template v-if="selectedComponent.type === 'card'">
        <div class="property-section">
          <h4 class="section-title">卡片设置</h4>
          <div class="property-item">
            <label class="property-label">卡片标题</label>
            <el-input
              v-model="componentData.title"
              @change="updateComponent"
            ></el-input>
          </div>
          <div class="property-item">
            <label class="property-label">卡片内容</label>
            <el-input
              v-model="componentData.content"
              type="textarea"
              :rows="3"
              @change="updateComponent"
            ></el-input>
          </div>
          <div class="property-item">
            <label class="property-label">阴影效果</label>
            <el-select v-model="componentData.shadow" @change="updateComponent">
              <el-option label="默认" :value="''"></el-option>
              <el-option label="永远" :value="'always'"></el-option>
              <el-option label="悬浮" :value="'hover'"></el-option>
              <el-option label="不显示" :value="'never'"></el-option>
            </el-select>
          </div>
        </div>
      </template>
      
      <!-- 容器属性 -->
      <template v-if="['container', 'row', 'column'].includes(selectedComponent.type)">
        <div class="property-section">
          <h4 class="section-title">容器设置</h4>
          <div class="property-item" v-if="selectedComponent.type === 'container'">
            <label class="property-label">容器标题</label>
            <el-input
              v-model="componentData.title"
              @change="updateComponent"
            ></el-input>
          </div>
          <div class="property-item" v-if="selectedComponent.type === 'row'">
            <label class="property-label">间距</label>
            <el-input-number
              v-model="componentData.gutter"
              :min="0"
              :max="50"
              @change="updateComponent"
            ></el-input-number>
          </div>
          <div class="property-item" v-if="selectedComponent.type === 'column'">
            <label class="property-label">列宽</label>
            <el-input-number
              v-model="componentData.span"
              :min="1"
              :max="24"
              @change="updateComponent"
            ></el-input-number>
          </div>
        </div>
      </template>
      
      <!-- 空白组件特殊属性 -->
      <div v-if="props.selectedComponent && props.selectedComponent.type === 'blank_spacing'" class="property-section">
        <h4 class="section-title">空白组件设置</h4>
        <div class="property-item">
          <label class="property-label">高度</label>
          <el-input-number
            v-model="heightValue"
            :min="1"
            :max="500"
            @change="updateHeight"
          ></el-input-number>
          <span class="unit">px</span>
        </div>
      </div>
      
      <!-- 通用样式属性 -->
      <div class="property-section">
        <h4 class="section-title">样式设置</h4>
        <div class="property-item">
          <label class="property-label">背景颜色</label>
          <div style="display: flex; align-items: center; gap: 10px;">
            <el-switch
              v-model="showBackgroundColor"
              active-text="设置"
              inactive-text="不设置"
              @change="handleBackgroundColorToggle"
              active-color="#13ce66"
              inactive-color="#ff4949"
            ></el-switch>
            <el-color-picker
              v-if="showBackgroundColor"
              v-model="componentData.style.backgroundColor"
              @change="updateComponent"
              show-alpha
            ></el-color-picker>
          </div>
        </div>
        <div class="property-item">
          <label class="property-label">内边距</label>
          <el-input-number
            v-model="paddingValue"
            :min="0"
            @change="updatePadding"
          ></el-input-number>
          <span class="unit">px</span>
        </div>
        <div class="property-item">
          <label class="property-label">外边距</label>
          <el-input-number
            v-model="marginValue"
            :min="0"
            @change="updateMargin"
          ></el-input-number>
          <span class="unit">px</span>
        </div>
        <div class="property-item">
          <label class="property-label">边框圆角</label>
          <el-input-number
            v-model="borderRadiusValue"
            :min="0"
            @change="updateBorderRadius"
          ></el-input-number>
          <span class="unit">px</span>
        </div>
      </div>
    </div>
    
    <!-- 未选中组件时的提示 -->
    <div class="empty-state" v-else>
      <i class="el-icon-setting"></i>
      <p>请选择一个组件进行编辑</p>
    </div>
  </div>
</template>

<script setup name="PropertyPanel">
import { ref, watch, computed } from 'vue'
import { cloneDeep } from 'lodash'
import { listSource } from '@/api/content/source'

// 定义props
const props = defineProps({
  selectedComponent: {
    type: Object,
    default: null
  },
  componentIndex: {
    type: Number,
    default: -1
  }
})

// 定义emits
const emit = defineEmits(['update:component'])

// 组件数据副本，用于编辑
const componentData = reactive({})

// 数值型属性（用于处理带单位的属性）
const fontSizeValue = ref(16)
const widthValue = ref(0)
const heightValue = ref(20) // 空白组件默认高度
const paddingValue = ref(0)
const marginValue = ref(0)
const borderRadiusValue = ref(0)

// 背景色控制
const showBackgroundColor = ref(false)

// 监听选中组件变化
watch(() => props.selectedComponent, (newComponent) => {
  if (newComponent) {
    // 关键修复：清空componentData，避免旧组件属性残留
    Object.keys(componentData).forEach(key => {
      delete componentData[key]
    })
    
    // 深拷贝新组件数据
    Object.assign(componentData, cloneDeep(newComponent))
    
    // 初始化样式对象
    if (!componentData.style) {
      componentData.style = {}
    }
    
    // 确保style对象存在
    if (!componentData.style) {
      componentData.style = {}
    }
    
    // 为所有文本类组件设置默认的左对齐
    if (['text', 'title', 'paragraph', 'hyperlink'].includes(newComponent.type)) {
      // 只有当style.textAlign不存在时才设置默认值
      if (!componentData.style.textAlign) {
        componentData.style.textAlign = 'left'
      }
    }
    
    // 移除可能存在的直接属性，确保所有样式都在style对象中
    delete componentData.fontSize
    delete componentData.color
    delete componentData.textAlign
    
    // 针对超链接组件的特殊处理，确保href等特有属性被正确设置
    if (newComponent.type === 'hyperlink') {
      componentData.href = newComponent.href || ''
      componentData.target = newComponent.target || '_blank'
    }
    
    // 关键修复：当切换到轮播图组件时，确保正确设置数据源ID
    // 直接从当前选中的组件获取数据源信息，避免使用之前组件的数据
    if (newComponent.type === 'global_banner') {
      // 确保componentData中的数据源信息与当前选中组件完全一致
      componentData.dataSourceId = newComponent.dataSourceId || ''
      componentData.dataSourceName = newComponent.dataSourceName || ''
      
      // 清空数据源选项，避免交叉污染
      epOptions = []
      // 如果当前组件有数据源，则添加到选项中
      if (newComponent.dataSourceId && newComponent.dataSourceName) {
        epOptions.push({
          dataId: newComponent.dataSourceId,
          dataName: newComponent.dataSourceName
        })
      }
    }
    
    // 赛事模块组件初始化
    if (newComponent.type === 'pc_home_tournament') {
      // 设置默认标题为"赛事中心"
      componentData.title = newComponent.title || '赛事中心'
      // 确保数据源信息正确设置
      componentData.dataSourceId = newComponent.dataSourceId || ''
      componentData.dataSourceName = newComponent.dataSourceName || ''
      
      // 初始化样式，确保背景图片正确设置
      if (!componentData.style) {
        componentData.style = {
          minHeight: '400px',
          padding: '0',
          borderRadius: '0'
        }
      }
      
      // 清空数据源选项，避免交叉污染
      epOptions = []
      // 如果当前组件有数据源，则添加到选项中
      if (newComponent.dataSourceId && newComponent.dataSourceName) {
        epOptions.push({
          dataId: newComponent.dataSourceId,
          dataName: newComponent.dataSourceName
        })
      }
    }
    
    // mini_home_tournament组件初始化
    if (newComponent.type === 'mini_home_tournament') {
      // 确保数据源信息正确设置
      componentData.dataSourceId = newComponent.dataSourceId || ''
      componentData.dataSourceName = newComponent.dataSourceName || ''
      
      // 清空数据源选项，避免交叉污染
      epOptions = []
      // 如果当前组件有数据源，则添加到选项中
      if (newComponent.dataSourceId && newComponent.dataSourceName) {
        epOptions.push({
          dataId: newComponent.dataSourceId,
          dataName: newComponent.dataSourceName
        })
      }
    }
    
    // mini_home_learn组件初始化
    if (newComponent.type === 'mini_home_learn') {
      // 确保数据源信息正确设置
      componentData.dataSourceId = newComponent.dataSourceId || ''
      componentData.dataSourceName = newComponent.dataSourceName || ''
      
      // 清空数据源选项，避免交叉污染
      epOptions = []
      // 如果当前组件有数据源，则添加到选项中
      if (newComponent.dataSourceId && newComponent.dataSourceName) {
        epOptions.push({
          dataId: newComponent.dataSourceId,
          dataName: newComponent.dataSourceName
        })
      }
    }
    
    // pc_home_learn组件初始化
    if (newComponent.type === 'pc_home_learn') {
      // 确保数据源信息正确设置
      componentData.dataSourceId = newComponent.dataSourceId || ''
      componentData.dataSourceName = newComponent.dataSourceName || ''
      
      // 清空数据源选项，避免交叉污染
      epOptions = []
      // 如果当前组件有数据源，则添加到选项中
      if (newComponent.dataSourceId && newComponent.dataSourceName) {
        epOptions.push({
          dataId: newComponent.dataSourceId,
          dataName: newComponent.dataSourceName
        })
      }
    }
    
    // pc_home_information组件初始化
    if (newComponent.type === 'pc_home_information') {
      // 确保数据源信息正确设置
      componentData.dataSourceId = newComponent.dataSourceId || ''
      componentData.dataSourceName = newComponent.dataSourceName || ''
      componentData.dataSourceTwoId = newComponent.dataSourceTwoId || ''
      componentData.dataSourceTwoName = newComponent.dataSourceTwoName || ''
      
      // 清空数据源选项，避免交叉污染
      epOptions = []
      // 如果当前组件有数据源，则添加到选项中（去重处理）
      const dataSourceIds = new Set()
      if (newComponent.dataSourceId && newComponent.dataSourceName) {
        dataSourceIds.add(newComponent.dataSourceId)
        epOptions.push({
          dataId: newComponent.dataSourceId,
          dataName: newComponent.dataSourceName
        })
      }
      if (newComponent.dataSourceTwoId && newComponent.dataSourceTwoName && !dataSourceIds.has(newComponent.dataSourceTwoId)) {
        epOptions.push({
          dataId: newComponent.dataSourceTwoId,
          dataName: newComponent.dataSourceTwoName
        })
      }
    }
    
    // pc_support组件初始化
    if (newComponent.type === 'pc_support') {
      // 设置默认标题为"技术支持"
      componentData.title = newComponent.title || '技术支持'
      // 确保数据源信息正确设置
      componentData.dataSourceId = newComponent.dataSourceId || ''
      componentData.dataSourceName = newComponent.dataSourceName || ''
      
      // 清空数据源选项，避免交叉污染
      epOptions = []
      // 如果当前组件有数据源，则添加到选项中
      if (newComponent.dataSourceId && newComponent.dataSourceName) {
        epOptions.push({
          dataId: newComponent.dataSourceId,
          dataName: newComponent.dataSourceName
        })
      }
    }
    
    // pc_tournament_list组件初始化
    if (newComponent.type === 'pc_tournament_list') {
      // 设置默认标题为"赛事列表"
      componentData.title = newComponent.title || '赛事列表'
      // 确保数据源信息正确设置
      componentData.dataSourceId = newComponent.dataSourceId || ''
      componentData.dataSourceName = newComponent.dataSourceName || ''
      
      // 清空数据源选项，避免交叉污染
      epOptions = []
      // 如果当前组件有数据源，则添加到选项中
      if (newComponent.dataSourceId && newComponent.dataSourceName) {
        epOptions.push({
          dataId: newComponent.dataSourceId,
          dataName: newComponent.dataSourceName
        })
      }
    }
    
    // mini_home_information组件初始化
    if (newComponent.type === 'mini_home_information') {
      // 设置默认标题为"资讯中心"
      componentData.title = newComponent.title || '资讯中心'
      // 确保数据源信息正确设置
      componentData.dataSourceId = newComponent.dataSourceId || ''
      componentData.dataSourceName = newComponent.dataSourceName || ''
      componentData.dataSourceTwoId = newComponent.dataSourceTwoId || ''
      componentData.dataSourceTwoName = newComponent.dataSourceTwoName || ''
      
      // 清空数据源选项，避免交叉污染
      epOptions = []
      // 如果当前组件有数据源，则添加到选项中（去重处理）
      const dataSourceIds = new Set()
      if (newComponent.dataSourceId && newComponent.dataSourceName) {
        dataSourceIds.add(newComponent.dataSourceId)
        epOptions.push({
          dataId: newComponent.dataSourceId,
          dataName: newComponent.dataSourceName
        })
      }
      if (newComponent.dataSourceTwoId && newComponent.dataSourceTwoName && !dataSourceIds.has(newComponent.dataSourceTwoId)) {
        epOptions.push({
          dataId: newComponent.dataSourceTwoId,
          dataName: newComponent.dataSourceTwoName
        })
      }
    }
    
    // pc_information组件初始化
    if (newComponent.type === 'pc_information') {
      // 设置默认标题为"信息展示"
      componentData.title = newComponent.title || '信息展示'
      // 确保数据源信息正确设置
      componentData.dataSourceId = newComponent.dataSourceId || ''
      componentData.dataSourceName = newComponent.dataSourceName || ''
      componentData.dataSourceTwoId = newComponent.dataSourceTwoId || ''
      componentData.dataSourceTwoName = newComponent.dataSourceTwoName || ''
      // 设置轮播开关默认值
      componentData.topInfoCarousel = newComponent.topInfoCarousel !== undefined ? newComponent.topInfoCarousel : true
      componentData.topNoticeCarousel = newComponent.topNoticeCarousel !== undefined ? newComponent.topNoticeCarousel : true
      
      // 清空数据源选项，避免交叉污染
      epOptions = []
      // 如果当前组件有数据源，则添加到选项中（去重处理）
      const dataSourceIds = new Set()
      if (newComponent.dataSourceId && newComponent.dataSourceName) {
        dataSourceIds.add(newComponent.dataSourceId)
        epOptions.push({
          dataId: newComponent.dataSourceId,
          dataName: newComponent.dataSourceName
        })
      }
      if (newComponent.dataSourceTwoId && newComponent.dataSourceTwoName && !dataSourceIds.has(newComponent.dataSourceTwoId)) {
        epOptions.push({
          dataId: newComponent.dataSourceTwoId,
          dataName: newComponent.dataSourceTwoName
        })
      }
    }
    
    // 首页标签组件初始化
    if (newComponent.type === 'min_home_tabs') {
      // 设置所有标签显示属性默认值为true
      componentData.showTournamentCenter = newComponent.showTournamentCenter !== false
      componentData.showLearningCenter = newComponent.showLearningCenter !== false
      componentData.showNewsCenter = newComponent.showNewsCenter !== false
      componentData.showTechSupport = newComponent.showTechSupport !== false
      componentData.showDatangCup = newComponent.showDatangCup !== false
    }
    
    // 赛事列表组件初始化
    if (newComponent.type === 'mini_tournament_list') {
      // 确保数据源信息正确设置
      componentData.dataSourceId = newComponent.dataSourceId || ''
      componentData.dataSourceName = newComponent.dataSourceName || ''
      
      // 清空数据源选项，避免交叉污染
      epOptions = []
      // 如果当前组件有数据源，则添加到选项中
      if (newComponent.dataSourceId && newComponent.dataSourceName) {
        epOptions.push({
          dataId: newComponent.dataSourceId,
          dataName: newComponent.dataSourceName
        })
      }
    }

    // 赛事标签组件初始化
    if (newComponent.type === 'mini_tournament_tabs') {
      // 设置所有标签显示属性默认值为true
      componentData.showMyTeam = newComponent.showMyTeam !== false
      componentData.showMyTournament = newComponent.showMyTournament !== false
      componentData.showScoreQuery = newComponent.showScoreQuery !== false
    }
    
    // 搜索框组件初始化
    if (newComponent.type === 'min_home_search') {
      // 设置默认placeholder
      componentData.placeholder = newComponent.placeholder || '请输入搜索内容'
      // 设置默认按钮文本
      componentData.buttonText = newComponent.buttonText || '搜索'
      
      // 初始化样式，移除背景图片，设置圆角为0
      if (!componentData.style) {
        componentData.style = {
          minHeight: '70px',
          padding: '0',
          borderRadius: '0',
          backgroundColor: '#f0f0f0'
        }
      }
    }
    
    // 解析数值型属性
    parseNumericProperties()
    
    // 处理空白组件高度
    if (newComponent.type === 'blank_spacing') {
      // 确保style对象存在
      if (!componentData.style) {
        componentData.style = {}
      }
      // 设置默认高度为20px
      if (!componentData.style.height) {
        componentData.style.height = '20px'
      }
      const height = componentData.style.height || '20px'
      heightValue.value = parseInt(height) || 20
    }
    
    // 处理背景色显示状态
    showBackgroundColor.value = !!componentData.style?.backgroundColor && componentData.style.backgroundColor !== 'transparent'
    // 默认不设置背景色时不添加该属性
    if (!showBackgroundColor.value && componentData.style?.backgroundColor === 'transparent') {
      delete componentData.style.backgroundColor
    }
  }
}, { immediate: true })

// 轮播图高度
const carouselHeight = ref(300)

// 监听选中组件变化，更新轮播图高度值
watch(() => props.selectedComponent, (newComponent) => {
  if (newComponent && newComponent.type === 'global_banner') {
    // 优先从根级别的height属性获取值
    if (newComponent.height) {
      carouselHeight.value = parseInt(newComponent.height) || 300
    } else if (newComponent.style?.height) {
      // 兼容旧数据，从style中获取
      carouselHeight.value = parseInt(newComponent.style.height) || 300
    } else {
      // 默认值
      carouselHeight.value = 300
    }
  }
}, { immediate: true })



// 查询数据源
// 获取数据源列表
let epLoading = $ref(false)
let epOptions = $ref([])
const remoteMethod = (query) => {
  if (query) {
    epLoading = true
    listSource({pageNum: 1,pageSize: 100,dataName:query}).then(res => {
      if(res.code === 200){
        let rows = res.rows;
        // 使用reduce进行数组合并和去重（代码更简洁，但在大数据量下可能略慢于直接for循环）
        const tempMap = [...epOptions, ...rows].reduce((map, item) => {
          // 只有当Map中不存在该enterpriseId时才添加，确保保留第一个出现的元素
          if (!map.has(item.dataId)) {
            map.set(item.dataId, item);
          }
          return map;
        }, new Map());
        // 转换Map值为数组
        epOptions = Array.from(tempMap.values());
        epLoading = false
      }else{
        epLoading = false
        epOptions = []
      }
    }).catch(err => {
      epLoading = false
    })
  } else {
    epOptions = []
  }
}

// 处理轮播图数据源选择
const handleDataSourceSelect = (value) => {
  // 确保正确设置dataSourceId和dataSourceName
  componentData.dataSourceId = value
  let item = epOptions.find(opt => opt.dataId === value)
  componentData.dataSourceName = item ? item.dataName : ''
  updateComponent()
}

// 处理最新资讯数据源选择
const handleInfoDataSourceSelect = (value) => {
  // 确保正确设置dataSourceId和dataSourceName
  componentData.dataSourceId = value
  let item = epOptions.find(opt => opt.dataId === value)
  componentData.dataSourceName = item ? item.dataName : ''
  updateComponent()
}

// 处理公告通知数据源选择
const handleNoticeDataSourceSelect = (value) => {
  // 确保正确设置dataSourceTwoId和dataSourceTwoName
  componentData.dataSourceTwoId = value
  let item = epOptions.find(opt => opt.dataId === value)
  componentData.dataSourceTwoName = item ? item.dataName : ''
  updateComponent()
}

// 处理赛事模块数据源选择
const handleTournamentDataSourceSelect = (value) => {
  // 确保正确设置dataSourceId和dataSourceName
  componentData.dataSourceId = value
  let item = epOptions.find(opt => opt.dataId === value)
  componentData.dataSourceName = item ? item.dataName : ''
  updateComponent()
}

// 更新轮播图高度
const updateCarouselHeight = () => {
  // 将轮播图高度设置在根级别，而不是style对象中，确保正确保存到API
  componentData.height = `${carouselHeight.value}px`
  updateComponent()
}

// 解析数值型属性
const parseNumericProperties = () => {
  // 关键修复：确保每次切换组件时，所有数值属性都被正确重置，不受之前组件影响
  
  // 确保style对象存在
  if (!componentData.style) {
    componentData.style = {}
  }
  
  // 解析字体大小 - 直接从style对象中读取
  if (componentData.style.fontSize) {
    fontSizeValue.value = parseInt(componentData.style.fontSize) || 16
  } else {
    // 根据组件类型设置不同的默认字体大小
    if (props.selectedComponent.type === 'title') {
      fontSizeValue.value = 24 // 标题默认字体大小
    } else if (props.selectedComponent.type === 'paragraph') {
      fontSizeValue.value = 16 // 段落默认字体大小
    } else if (props.selectedComponent.type === 'hyperlink') {
      fontSizeValue.value = 14 // 超链接默认字体大小
    } else if (props.selectedComponent.type === 'text') {
      fontSizeValue.value = 14 // 文本默认字体大小
    } else {
      fontSizeValue.value = 16 // 其他组件默认字体大小
    }
    // 为文本类组件设置默认字体大小到style对象
    if (['title', 'paragraph', 'hyperlink', 'text'].includes(props.selectedComponent.type)) {
      componentData.style.fontSize = `${fontSizeValue.value}px`
    }
  }
  
  // 解析宽度 - 确保使用新组件的宽度值
  widthValue.value = componentData.width && componentData.width !== 'auto' ? (parseInt(componentData.width) || 0) : 0
  
  // 解析高度 - 确保使用新组件的高度值
  heightValue.value = componentData.height && componentData.height !== 'auto' ? (parseInt(componentData.height) || 0) : 0
  
  // 解析内边距 - 确保每次都设置值，避免保留上一个组件的值
  paddingValue.value = componentData.style.padding ? (parseInt(componentData.style.padding) || 0) : 0
  
  // 解析外边距 - 确保每次都设置值，避免保留上一个组件的值
  marginValue.value = componentData.style.margin ? (parseInt(componentData.style.margin) || 0) : 0
  
  // 解析边框圆角 - 确保每次都设置值，避免保留上一个组件的值
  borderRadiusValue.value = componentData.style.borderRadius ? (parseInt(componentData.style.borderRadius) || 0) : 0
  
  // 解析轮播图高度
    if (componentData.style && componentData.style.height) {
      carouselHeight.value = parseInt(componentData.style.height) || 300
    } else {
      carouselHeight.value = 300
    }
    
  // 初始化需要数据源的组件信息
  if (props.selectedComponent && (props.selectedComponent.type === 'global_banner' || props.selectedComponent.type === 'pc_home_information')) {
    // 处理轮播图组件
    if (props.selectedComponent.type === 'global_banner') {
      // 确保componentData中的数据源信息与当前选中组件一致
      componentData.dataSourceId = props.selectedComponent.dataSourceId || ''
      componentData.dataSourceName = props.selectedComponent.dataSourceName || ''
      
      // 如果当前组件有数据源，确保只添加一次到epOptions中
      if (props.selectedComponent.dataSourceId && props.selectedComponent.dataSourceName) {
        // 检查是否已经存在相同的数据源，避免重复添加
        const existingOption = epOptions.find(opt => opt.dataId === props.selectedComponent.dataSourceId)
        if (!existingOption) {
          epOptions = [
            {
              dataName: props.selectedComponent.dataSourceName,
              dataId: props.selectedComponent.dataSourceId,
            },
            ...epOptions
          ]
        }
      }
    }
    // 处理资讯中心组件
    else if (props.selectedComponent.type === 'pc_home_information') {
      // 确保componentData中的数据源信息与当前选中组件一致
      componentData.dataSourceId = props.selectedComponent.dataSourceId || ''
      componentData.dataSourceName = props.selectedComponent.dataSourceName || ''
      componentData.dataSourceTwoId = props.selectedComponent.dataSourceTwoId || ''
      componentData.dataSourceTwoName = props.selectedComponent.dataSourceTwoName || ''
      
      // 处理第一个数据源
      if (props.selectedComponent.dataSourceId && props.selectedComponent.dataSourceName) {
        const existingOption = epOptions.find(opt => opt.dataId === props.selectedComponent.dataSourceId)
        if (!existingOption) {
          epOptions = [
            {
              dataName: props.selectedComponent.dataSourceName,
              dataId: props.selectedComponent.dataSourceId,
            },
            ...epOptions
          ]
        }
      }
      
      // 处理第二个数据源
      if (props.selectedComponent.dataSourceTwoId && props.selectedComponent.dataSourceTwoName) {
        const existingOption = epOptions.find(opt => opt.dataId === props.selectedComponent.dataSourceTwoId)
        if (!existingOption) {
          epOptions = [
            {
              dataName: props.selectedComponent.dataSourceTwoName,
              dataId: props.selectedComponent.dataSourceTwoId,
            },
            ...epOptions
          ]
        }
      }
    }
  }
  
  // 确保轮播图组件有默认值
  if (props.selectedComponent && props.selectedComponent.type === 'global_banner') {
    // 自动轮播默认开启
    if (componentData.autoPlay === undefined) {
      componentData.autoPlay = true
    }
    // 间隔时间默认3000ms
    if (componentData.interval === undefined) {
      componentData.interval = 3000
    }
    // 循环播放默认开启
    if (componentData.loop === undefined) {
      componentData.loop = true
    }
    // 数据源ID默认为空
    if (componentData.dataSourceId === undefined) {
      componentData.dataSourceId = ''
    }
  }
  
  // 处理空白组件高度
  if (props.selectedComponent && props.selectedComponent.type === 'blank_spacing') {
    // 确保style对象存在
    if (!componentData.style) {
      componentData.style = {}
    }
    // 确保有正确的height设置
    if (!componentData.style.height) {
      componentData.style.height = '20px'
    } else {
      // 规范化高度值格式
      const height = componentData.style.height
      componentData.style.height = typeof height === 'string' && height.includes('px') 
        ? height 
        : `${parseInt(height) || 20}px`
    }
    // 同步heightValue
    heightValue.value = parseInt(componentData.style.height) || 20
  }
}

// 处理背景色开关切换
const handleBackgroundColorToggle = () => {
  if (showBackgroundColor.value) {
    // 如果之前没有设置过背景色，默认设置为白色
    componentData.style.backgroundColor = componentData.style.backgroundColor || '#ffffff'
  } else {
    // 不设置背景色时，删除该属性
    delete componentData.style.backgroundColor
  }
  // 更新组件数据
  updateComponent()
}

// 更新组件数据 - 添加参数以修复Vue事件验证警告
const updateComponent = (newValue) => {
  // 创建一个深拷贝以避免直接修改props
  const updatedData = cloneDeep(componentData)
  
  // 确保style对象存在
  updatedData.style = updatedData.style || {}
  
  // 移除可能存在的直接样式属性，确保所有样式都在style对象中
  delete updatedData.fontSize
  delete updatedData.color
  delete updatedData.textAlign
  
  // 同步超链接下划线样式
  if (updatedData.type === 'hyperlink') {
    if (updatedData.showUnderline) {
      updatedData.style.textDecoration = 'underline'
    } else {
      delete updatedData.style.textDecoration
    }
  }
  
  // 特别处理需要数据源的组件，确保数据源属性被正确包含
  if (updatedData.type === 'global_banner' || updatedData.type === 'pc_home_information') {
    // 确保dataSourceId和dataSourceName属性存在
    if (componentData.dataSourceId !== undefined) {
      updatedData.dataSourceId = componentData.dataSourceId
    }
    if (componentData.dataSourceName !== undefined) {
      updatedData.dataSourceName = componentData.dataSourceName
    }
    
    // 对于资讯中心组件，还需要确保第二个数据源属性存在
    if (updatedData.type === 'pc_home_information') {
      if (componentData.dataSourceTwoId !== undefined) {
        updatedData.dataSourceTwoId = componentData.dataSourceTwoId
      }
      if (componentData.dataSourceTwoName !== undefined) {
        updatedData.dataSourceTwoName = componentData.dataSourceTwoName
      }
    }
  }
  
  emit('update:component', updatedData)
}

// 更新字体大小
const updateFontSize = () => {
  // 直接更新style对象中的fontSize属性
  componentData.style.fontSize = `${fontSizeValue.value}px`
  updateComponent()
}

// 更新宽度
const updateWidth = () => {
  componentData.width = widthValue.value > 0 ? `${widthValue.value}px` : 'auto'
  updateComponent()
}

// 更新高度
const updateHeight = () => {
  if (props.selectedComponent && props.selectedComponent.type === 'blank_spacing') {
    // 空白组件的高度设置到style中
    componentData.style.height = `${heightValue.value}px`
  } else {
    // 其他组件的高度设置
    componentData.height = heightValue.value > 0 ? `${heightValue.value}px` : 'auto'
  }
  updateComponent()
}

// 更新内边距
const updatePadding = () => {
  componentData.style.padding = `${paddingValue.value}px`
  updateComponent()
}

// 更新外边距
const updateMargin = () => {
  componentData.style.margin = `${marginValue.value}px`
  updateComponent()
}

// 更新边框圆角
const updateBorderRadius = () => {
  componentData.style.borderRadius = `${borderRadiusValue.value}px`
  updateComponent()
}
</script>

<style scoped>
.property-panel-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #fafafa;
}

.panel-header {
  padding: 16px;
  border-bottom: 1px solid #e0e0e0;
  background: #fafafa;
}

.panel-header h3 {
  margin: 0;
  font-size: 16px;
  color: #333;
  font-weight: 500;
}

.panel-content {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

.property-section {
  margin-bottom: 24px;
  background: white;
  border-radius: 4px;
  padding: 16px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.section-title {
  font-size: 14px;
  font-weight: 500;
  color: #606266;
  margin: 0 0 16px 0;
  padding-bottom: 8px;
  border-bottom: 1px solid #ebeef5;
}

.property-item {
  margin-bottom: 16px;
}

.property-label {
  display: block;
  font-size: 12px;
  color: #909399;
  margin-bottom: 8px;
}

.property-value {
  font-size: 14px;
  color: #606266;
  padding: 8px;
  background: #f5f7fa;
  border-radius: 4px;
}

/* 数据源下拉框样式 */
  .data-source-item {
    padding: 8px 12px;
    line-height: 1.5;
  }

  .data-source-id {
    font-size: 13px;
    color: #606266;
    margin-bottom: 4px;
  }

  .data-source-name {
    font-size: 14px;
    color: #303133;
    font-weight: 500;
  }

  /* 轮播图显示样式 */
  .banner-title {
    font-size: 18px;
    font-weight: bold;
    color: #606266;
  }

.unit {
  margin-left: 8px;
  font-size: 12px;
  color: #909399;
}

.empty-state {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #909399;
  font-size: 14px;
}

.empty-state i {
  font-size: 48px;
  margin-bottom: 16px;
  color: #c0c4cc;
}

/* 自定义滚动条 */
.panel-content::-webkit-scrollbar {
  width: 6px;
}

.panel-content::-webkit-scrollbar-track {
  background: #f1f1f1;
}

.panel-content::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.panel-content::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>