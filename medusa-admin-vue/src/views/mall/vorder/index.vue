<template>
    <div class="app-container">
  
      <el-row :gutter="10" class="mb8">
          <el-col :span="24">
            <el-button type="primary" icon="el-icon-plus" @click="handleNewOrder">New Order</el-button>
            <el-button type="success" icon="el-icon-check" :disabled="!hasSelection" @click="handleStatusUpdate">{{ statusButtonText }}</el-button>
            <el-button type="danger" icon="el-icon-delete" :disabled="!hasSelection" @click="handleBatchDelete">Delete</el-button>
          </el-col>
        </el-row>
        
      <!-- Status Filter Buttons -->
      <el-row :gutter="10" class="mb8">
        <el-col :span="24">
          <div class="status-filter-container">
            <el-button-group>

              <el-button
                v-for="dict in dict.type.order_status.filter(item => item.value !== 5 && item.value !== '5')"
                :key="dict.value"
                :type="queryParams.status === dict.value ? 'primary' : 'default'"
                @click="handleStatusFilter(dict.value)"
              >{{ dict.label }}</el-button>
              <!-- Disputes Button -->
              <el-button
                :type="queryParams.isDispute === 1 ? 'danger' : 'default'"
                @click="handleDisputesFilter"
              >Disputes</el-button>
            </el-button-group>
            
            <el-dropdown @command="handleExportCommand" v-hasPermi="['mall:vorder:export']">
              <el-button
                type="warning"
                plain
                icon="el-icon-download"
                size="mini"
              >
                Export Orders<i class="el-icon-arrow-down el-icon--right"></i>
              </el-button>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item command="excel-acc" :disabled="!isShippedTab">
                  <i class="el-icon-download"></i> Export excel acc
                  <span v-if="!isShippedTab" style="color: #909399; font-size: 12px;"> (Shipped only)</span>
                </el-dropdown-item>
                <el-dropdown-item command="html-ph-2" :disabled="!isFulfilledTab">
                  <i class="el-icon-document"></i> Export HTML (ph)-2
                  <span v-if="!isFulfilledTab" style="color: #909399; font-size: 12px;"> (Fulfilled only)</span>
                </el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </div>
        </el-col>
      </el-row>



      <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" class="search-form">
        <el-form-item label="Order#" prop="orderSn">
          <el-input
            v-model="queryParams.orderSn"
            placeholder="Please enter order number"
            clearable
            style="width: 240px"
            @keyup.enter.native="handleQuery"
          />
        </el-form-item>
       
        <el-form-item label="Order Status" prop="status">
          <el-select
            v-model="queryParams.status"
            placeholder="Please select order status"
            clearable
            style="width: 240px"
          >
            <el-option
              v-for="dict in dict.type.order_status.filter(item => item.value !== 5 && item.value !== '5')"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="Create Time">
          <el-date-picker
            v-model="dateRange"
            style="width: 240px"
            value-format="yyyy-MM-dd"
            type="daterange"
            range-separator="-"
            start-placeholder="Start Date"
            end-placeholder="End Date"
          ></el-date-picker>
        </el-form-item>
        <el-form-item label="Sort By" prop="sortBy">
          <el-select
            v-model="queryParams.sortBy"
            placeholder="Please select sort option"
            clearable
            style="width: 240px"
            @change="handleQuery"
          >
            <el-option label="ProCode" value="procode" />
            <el-option label="Product Name" value="product_name" />
            <el-option label="Channel" value="channel" />
            <el-option label="Coin Type" value="cointype" />
            <el-option label="Date (Latest to Newest)" value="date_asc" />
            <el-option label="Date (Newest to Latest)" value="date_desc" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">Search</el-button>
          <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">Reset</el-button>
        </el-form-item>
      </el-form>

      <el-row :gutter="10" class="mb8">
        <el-col :span="1.5">
          <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
        </el-col>
      </el-row>
  
      <template>
      <div class="app-container">
        
        
        <el-table :data="orderList" border style="width: 100%; margin-top: 20px;" @selection-change="handleSelectionChange">
          <el-table-column type="selection" width="45" align="center" />
          <el-table-column prop="order_sn" label="Order#" width="120">
            <template slot-scope="scope">
              <el-link type="primary" @click="handleView(scope.row)">{{ scope.row.order_sn }}</el-link>
            </template>
          </el-table-column>
          <el-table-column label="📝" width="40" align="center">
            <template slot-scope="scope">
              <el-tooltip 
                v-if="scope.row.customer_comment" 
                effect="dark" 
                placement="right"
                :content="scope.row.customer_comment">
                <i class="el-icon-chat-dot-round" style="color: #409EFF; font-size: 16px; cursor: pointer;"></i>
              </el-tooltip>
            </template>
          </el-table-column>
          <el-table-column prop="create_time" label="Date" width="100">
            <template slot-scope="scope">
              <span>{{ parseTime(scope.row.create_time, '{y}-{m}-{d}') }}</span>
            </template>
          </el-table-column>
          <!-- <el-table-column prop="code" label="Coupon Code" width="120" /> -->
          <el-table-column prop="channel" label="Channel" width="140">
            <template slot-scope="scope">
              <!-- 如果是自定义渠道 (99)，从 remark 提取渠道名称 -->
              <span v-if="scope.row.channel == 99 && scope.row.remark && scope.row.remark.includes('Custom Channel:')">
                <el-tag type="info" size="small">{{ extractCustomChannel(scope.row.remark) }}</el-tag>
              </span>
              <!-- 否则使用字典标签 -->
              <dict-tag v-else :options="dict.type.source_type" :value="scope.row.channel"/>
            </template>
          </el-table-column>
          <el-table-column prop="username" label="Username" width="100" />
          <el-table-column prop="procode" label="ProCode" width="100">
            <template slot-scope="scope">
              <span v-html="scope.row.procodeHtml"></span>
            </template>
          </el-table-column>
          <el-table-column prop="amt" label="Amt" width="80">
            <template slot-scope="scope">
              <span v-html="scope.row.amtHtml"></span>
            </template>
          </el-table-column>
          <el-table-column prop="qty" label="Qty" width="60">
            <template slot-scope="scope">
              <span v-html="scope.row.qtyHtml"></span>
            </template>
          </el-table-column>
          <el-table-column prop="postage" label="Postage" width="80" />
          <el-table-column label="Full name & Address" min-width="250">
            <template slot-scope="scope">
              <div>
                <div><b>{{ scope.row.receiver_name }}</b></div>
                <div>{{ scope.row.address_line1 }}</div>
                <div v-if="scope.row.address_line2">{{ scope.row.address_line2 }}</div>
                <div v-if="scope.row.address_line3">{{ scope.row.address_line3 }}</div>
                <div>
                  {{ scope.row.city }}, {{ scope.row.state }} {{ scope.row.post_code }}
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="tcoin" label="Total Coin" width="80">
            <template slot-scope="scope">
              <span v-html="scope.row.tcoinHtml"></span>
            </template>
          </el-table-column>
          <el-table-column prop="paidcoin" label="Paid Coin" width="80">
            <template slot-scope="scope">
              <span v-html="scope.row.paidcoinHtml"></span>
            </template>
          </el-table-column>
          <el-table-column prop="tamt" label="Total AUD" width="80">
            <template slot-scope="scope">
              <span v-html="scope.row.tamtHtml"></span>
            </template>
          </el-table-column>
          <el-table-column prop="cointype" label="Coin Type" width="80">
            <template slot-scope="scope">
              <dict-tag :options="dict.type.pay_type" :value="scope.row.cointype"/>
            </template>
          </el-table-column>
          
          <el-table-column label="Order Status" prop="status" width="100">
            <template slot-scope="scope">
              <dict-tag :options="filteredOrderStatus" :value="scope.row.status"/>
            </template>
          </el-table-column>
          <el-table-column label="Edit" align="center" width="200" fixed="right">
            <template slot-scope="scope">
              <!-- 图标行 -->
              <div style="display: flex; justify-content: center; margin-bottom: 8px;">
                <el-button
                  size="mini"
                  type="text"
                  icon="el-icon-edit"
                  @click="handleUpdate(scope.row)"
                  v-hasPermi="['mall:order:edit']"
                  style="margin-right: 8px;"
                ></el-button>
                <el-button
                  size="mini"
                  type="text"
                  icon="el-icon-delete"
                  @click="handleDelete(scope.row)"
                  v-hasPermi="['mall:order:edit']"
                ></el-button>
              </div>
              
              <!-- 输入框 -->
              <div style="margin-bottom: 8px;">
                <el-input 
                  :ref="`trackingInput_${scope.row.id}`"
                  v-model="scope.row.trackingNumber" 
                  placeholder="Enter tracking number"
                  size="mini"
                  style="width: 100%;"
                  @keyup.enter.native="focusNextTrackingInput(scope.$index, true)"
                ></el-input>
              </div>
              
              <!-- 按钮行 -->
              <div v-if="isFulfilledTab" style="display: flex; justify-content: center; gap: 4px;">
                <el-button
                  type="primary"
                  size="mini"
                  @click="handleMarkAsShipped(scope.row)"
                  style="font-size: 10px; padding: 4px 8px;"
                >Mark as shipped</el-button>
                <el-button
                  type="danger"
                  size="mini"
                  @click="handleDelete(scope.row)"
                  style="font-size: 10px; padding: 4px 8px;"
                >Delete</el-button>
              </div>
            </template>
          </el-table-column>
  
        </el-table>
        <pagination
          v-show="total>0"
          :total="total"
          :page.sync="queryParams.pageNum"
          :limit.sync="queryParams.pageSize"
          :page-sizes="pageSizes"
          @pagination="getList"
        />
        
        <!-- Bulk Mark as Shipped Button -->
        <div v-if="showBulkShippedButton" class="bulk-shipped-container">
          <el-button
            type="success"
            size="large"
            icon="el-icon-check"
            @click="handleBulkMarkAsShipped"
            class="bulk-shipped-button"
          >
            Mark as Shipped ({{ ordersWithTrackingCount }} orders)
          </el-button>
        </div>
      </div>
    </template>
  
  
      <!-- Add or modify order dialog -->
      <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
        <el-form ref="form" :model="form" :rules="rules" label-width="80px">
          
          <el-form-item label="" prop="remark">
            <el-input v-model="form.remark" type="textarea" placeholder="Please enter remark" />
          </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">
          <el-button type="primary" @click="submitForm">Save</el-button>
          <el-button @click="cancel">Cancel</el-button>
        </div>
      </el-dialog>

      <!-- 添加物流信息对话框 -->
      <el-dialog title="Shipping Information" :visible.sync="shippingDialogVisible" width="500px">
        <el-form :model="shippingForm" label-width="120px">
          <el-form-item label="Tracking Number">
            <el-input v-model="shippingForm.trackingNumber" placeholder="Enter tracking number"></el-input>
          </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">
        
          <el-button type="primary" @click="handleMarkAsShipped">Mark as Shipped</el-button>
          <el-button @click="shippingDialogVisible = false">Cancel</el-button>
        </div>
      </el-dialog>

      <!-- HTML导出配置对话框 -->
      <el-dialog title="HTML Export Settings" :visible.sync="htmlExportDialogVisible" width="650px" append-to-body>
        <el-form :model="htmlExportConfig" label-width="170px" class="html-export-form">
          <el-form-item label="Label Paper Orientation">
            <el-radio-group v-model="htmlExportConfig.labelOrientation" @change="handleOrientationChange">
              <el-radio label="portrait">Portrait (29mm × 90mm) - 3 addresses/label</el-radio>
              <el-radio label="landscape">Landscape (90mm × 29mm) - 1 address/label</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-divider></el-divider>
          <el-form-item label="Card Width">
            <el-input-number 
              v-model="htmlExportConfig.cardWidth" 
              :min="50" 
              :max="1000" 
              :step="5"
              style="width: 100%"
            ></el-input-number>
            <span style="margin-left: 10px; color: #909399;">mm (millimeters)</span>
          </el-form-item>
          <el-form-item label="Card Height">
            <el-input-number 
              v-model="htmlExportConfig.cardHeight" 
              :min="30" 
              :max="500" 
              :step="5"
              style="width: 100%"
            ></el-input-number>
            <span style="margin-left: 10px; color: #909399;">mm (millimeters)</span>
          </el-form-item>
          <el-form-item label="Layout">
            <el-radio-group v-model="htmlExportConfig.layout">
              <el-radio label="single">Single Column</el-radio>
              <el-radio label="two">Two Columns</el-radio>
              <el-radio label="three">Three Columns</el-radio>
              <el-radio label="four">Four Columns</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="Card Padding">
            <el-input-number 
              v-model="htmlExportConfig.cardPadding" 
              :min="0" 
              :max="50" 
              :step="1"
              style="width: 100%"
            ></el-input-number>
            <span style="margin-left: 10px; color: #909399;">mm</span>
          </el-form-item>
          <el-form-item label="Card Margin">
            <el-input-number 
              v-model="htmlExportConfig.cardMargin" 
              :min="0" 
              :max="20" 
              :step="1"
              style="width: 100%"
            ></el-input-number>
            <span style="margin-left: 10px; color: #909399;">mm</span>
          </el-form-item>
          <el-form-item label="Font Size">
            <el-input-number 
              v-model="htmlExportConfig.fontSize" 
              :min="8" 
              :max="24" 
              :step="1"
              style="width: 100%"
            ></el-input-number>
            <span style="margin-left: 10px; color: #909399;">px</span>
          </el-form-item>
          <el-form-item label="Line Height">
            <el-input-number 
              v-model="htmlExportConfig.lineHeight" 
              :min="1.0" 
              :max="3.0" 
              :step="0.1"
              :precision="1"
              style="width: 100%"
            ></el-input-number>
          </el-form-item>
          <el-form-item label="Page Margin">
            <el-input-number 
              v-model="htmlExportConfig.pageMargin" 
              :min="0" 
              :max="50" 
              :step="1"
              style="width: 100%"
            ></el-input-number>
            <span style="margin-left: 10px; color: #909399;">mm</span>
          </el-form-item>
          <el-form-item label="Horizontal Alignment">
            <el-radio-group v-model="htmlExportConfig.horizontalAlign">
              <el-radio label="left">Left</el-radio>
              <el-radio label="center">Center</el-radio>
              <el-radio label="right">Right</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="Vertical Alignment">
            <el-radio-group v-model="htmlExportConfig.verticalAlign">
              <el-radio label="top">Top</el-radio>
              <el-radio label="middle">Middle</el-radio>
              <el-radio label="bottom">Bottom</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="Card Container Alignment">
            <el-radio-group v-model="htmlExportConfig.containerAlign">
              <el-radio label="left">Left</el-radio>
              <el-radio label="center">Center</el-radio>
              <el-radio label="right">Right</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">
          <el-button @click="htmlExportDialogVisible = false">Cancel</el-button>
          <el-button type="primary" @click="confirmHtmlExport">Export</el-button>
        </div>
      </el-dialog>

    </div>
  </template>
  
  <script>
  import { listVOrder, exportVOrderAcc, exportVOrderPh } from '@/api/mall/vorder';
  import { listOrder, getOrder, exportOrder } from "@/api/mall/order";
  import { updateOrderStatus,updateOrder,delOrder } from '@/api/mall/order'
  import { exportVOrder } from '@/api/mall/vorder';
  import request from '@/utils/request';
  
  export default {
    name: "Order",
    dicts: ['order_status', 'source_type', 'pay_type'],
    data() {
      return {
        // Loading mask
        loading: true,
        // Export loading mask
        downloadLoading: false,
        // Selected array
        ids: [],
        // Non-single disabled
        single: true,
        // Non-multiple disabled
        multiple: true,
        // Show search condition
        showSearch: true,
        // Total number
        total: 0,
        // Order table data
        orderList: [],
        // Popup title
        title: "",
        // Whether to show popup
        open: false,
        // Date range
        dateRange: [],
        // Query parameters
        queryParams: {
          pageNum: 1,
          pageSize: 10,
          orderSn: undefined,
          memberId: undefined,
          status: undefined,
          isDispute: undefined,
          sortBy: undefined
        },
        // Form parameters
        form: {},
        // Form validation
        rules: {
          status: [
            { required: true, message: "Order status cannot be empty", trigger: "change" }
          ]
        },
        selectedOrders: [], // 新增：存储选中的订单
        shippingDialogVisible: false,
        shippingForm: {
          orderId: '',
          trackingNumber: ''
        },
        // HTML导出配置
        htmlExportDialogVisible: false,
        htmlExportConfig: {
          labelOrientation: 'landscape',  // 标签纸方向: portrait (29x90竖向) 或 landscape (90x29横向)
          cardWidth: 90,       // 卡片宽度 (mm) - 横向标签宽度
          cardHeight: 29,      // 卡片高度 (mm) - 横向标签高度
          layout: 'single',    // 布局: single, two, three, four
          cardPadding: 0,      // 卡片内边距 (mm) - 无内边距
          cardMargin: 0,       // 卡片外边距 (mm) - 无外边距
          fontSize: 24,        // 字体大小 (px) - 大字体
          lineHeight: 1.3,     // 行高
          pageMargin: 0,       // 页面边距 (mm) - 标签打印无需页边距
          horizontalAlign: 'center',  // 水平对齐: left, center, right
          verticalAlign: 'middle',    // 垂直对齐: top, middle, bottom
          containerAlign: 'center',   // 卡片容器对齐: center
          addressesPerLabel: 1        // 每张标签纸显示1个地址
        },
        pendingOrdersForExport: null,  // 待导出的订单数据
        currentSortBy: 'amount'  // 当前排序方式，默认按amount排序
      };
    },
    computed: {
      hasSelection() {
        return this.ids.length > 0;
      },
      // 过滤掉isDeleted的状态选项，用于表格显示
      filteredOrderStatus() {
        return this.dict.type.order_status.filter(item => item.value !== 5 && item.value !== '5');
      },
      // 新增：根据选中订单的状态计算按钮文本
      statusButtonText() {
        if (this.selectedOrders.length === 0) return 'Update Status';
        
        // 获取所有选中订单的状态
        const statuses = this.selectedOrders.map(order => order.status);
        
        // 如果所有订单都是待付款状态(0)
        if (statuses.every(status => status === 0)) {
          return 'Mark as Paid';
        }
        // 如果所有订单都是已付款状态(1)
        else if (statuses.every(status => status === 1)) {
          return 'Mark as Fulfilled';
        }
        // 如果所有订单都是已完成状态(2)
        else if (statuses.every(status => status === 2)) {
          return 'Mark as Shipped';
        }
        // 如果所有订单都是已取消状态(4)
        else if (statuses.every(status => status === 4)) {
          return 'Reactivate as Paid';
        }
        // 如果订单状态不一致
        else {
          return 'Update Status';
        }
      },
      // 当前是否处于Fulfilled标签页
      isFulfilledTab() {
        return this.queryParams.status == 2;
      },
      // 当前是否处于Shipped标签页
      isShippedTab() {
        return this.queryParams.status == 3;
      },
      // 根据标签页返回不同的分页选项
      pageSizes() {
        // 所有标签页都包含 250 和 500 选项
        return [10, 50, 100, 150, 250, 500];
      },
      // 判断是否显示批量发货按钮（仅在Fulfilled状态下显示）
      showBulkShippedButton() {
        // ✅ 性能优化：快速退出，避免不必要的遍历
        if (!this.isFulfilledTab || !this.orderList.length) {
          return false;
        }
        // some() 会在找到第一个匹配项时立即停止
        return this.orderList.some(order => 
          order.trackingNumber && 
          order.trackingNumber.trim() !== '' &&
          order.status == 2
        );
      },
      // 计算有tracking number的订单数量（仅Fulfilled状态）
      ordersWithTrackingCount() {
        // ✅ 性能优化：快速退出 + 使用 for 循环代替 filter（避免创建新数组）
        if (!this.isFulfilledTab || !this.orderList.length) {
          return 0;
        }
        let count = 0;
        for (let i = 0; i < this.orderList.length; i++) {
          const order = this.orderList[i];
          if (order.trackingNumber && 
              order.trackingNumber.trim() !== '' &&
              order.status == 2) {
            count++;
          }
        }
        return count;
      }
    },
    created() {
      this.getList();
    },
    methods: {
      /** Query order list */
      getList() {
        this.loading = true;
        // ✅ 修改：将日期范围直接添加到 queryParams 中，而不是嵌套在 params 中
        const params = { ...this.queryParams };
        if (this.dateRange && this.dateRange.length === 2) {
          // 确保 params 对象存在
          if (!params.params) {
            params.params = {};
          }
          params.params.beginTime = this.dateRange[0];
          params.params.endTime = this.dateRange[1];
        } else {
          // 如果没有日期范围，确保 params 对象存在但为空
          if (!params.params) {
            params.params = {};
          }
          // 清除日期范围参数
          delete params.params.beginTime;
          delete params.params.endTime;
        }
        return listVOrder(params).then(response => {
          let orderList = response.rows;
          
          // ✅ 性能优化：预处理数据，一次性完成所有字符串替换操作
          // 避免在模板中重复执行 replace 操作（150订单 × 6列 = 900次操作）
          orderList = orderList.map(order => {
            // 计算每个产品的 Paid Coin（按 Total Coin 比例分配）
            let paidcoinHtml = '';
            const tcoinStr = (order.tcoin || '').toString();
            const paidcoinTotal = parseFloat(order.paidcoin) || 0;
            
            if (tcoinStr && paidcoinTotal > 0) {
              // 将 tcoin 按分号分割，得到每个产品的 total coin
              const tcoinArray = tcoinStr.split(';').map(tc => parseFloat(tc.trim()) || 0);
              // 计算总 total coin
              const totalCoinSum = tcoinArray.reduce((sum, tc) => sum + tc, 0);
              
              if (totalCoinSum > 0) {
                // 为每个产品计算 paid coin
                const paidcoinArray = tcoinArray.map(tc => {
                  const percentage = tc / totalCoinSum;
                  const itemPaidcoin = paidcoinTotal * percentage;
                  // 保留8位小数，与币种精度一致
                  return itemPaidcoin.toFixed(8);
                });
                paidcoinHtml = paidcoinArray.join('<br>');
              } else {
                // 如果 total coin 总和为 0，则所有产品的 paid coin 都显示为 0
                paidcoinHtml = tcoinArray.map(() => '0.00000000').join('<br>');
              }
            } else {
              // 如果没有 paid coin 或 total coin，显示原值或 0
              paidcoinHtml = paidcoinTotal > 0 ? paidcoinTotal.toFixed(8) : '0.00000000';
            }
            
            return {
              ...order,
              trackingNumber: order.shipping?.shippingNumber || '',
              // 预处理：将分号替换为HTML换行符（只执行一次）
              procodeHtml: (order.procode || '').replace(/;/g, '<br>'),
              amtHtml: (order.amt || '').toString().replace(/;/g, '<br>'),
              qtyHtml: (order.qty || '').toString().replace(/;/g, '<br>'),
              tcoinHtml: tcoinStr.replace(/;/g, '<br>'),
              paidcoinHtml: paidcoinHtml,
              tamtHtml: (order.tamt || '').toString().replace(/;/g, '<br>')
            };
          });
          
          // Apply sorting if sortBy is selected
          if (this.queryParams.sortBy) {
            orderList = this.sortOrderList(orderList, this.queryParams.sortBy);
          }
          
          this.orderList = orderList;
          this.total = response.total;
          this.loading = false;
          return response;
        });
      },
      /** Sort order list based on selected criteria */
      sortOrderList(orderList, sortBy) {
        // 保存当前排序方式
        this.currentSortBy = sortBy || 'amount';
        
        return orderList.sort((a, b) => {
          let result = 0;
          switch (sortBy) {
            case 'procode':
              // 标准化 ProCode（去空格、转大写）
              const procodeA = ((a.procode || '').split(';')[0] || '').trim().toUpperCase();
              const procodeB = ((b.procode || '').split(';')[0] || '').trim().toUpperCase();
              result = procodeA.localeCompare(procodeB);
              if (result === 0) {
                // 相同 ProCode 时，按 amount 数值排序（取第一个金额）
                const amtA = this.parseAmountForSort(a.amt || '');
                const amtB = this.parseAmountForSort(b.amt || '');
                result = amtA - amtB;
              }
              return result;
            case 'product_name':
              // Since we don't have product_name field, we'll use procode as fallback
              const prodnameA = ((a.procode || '').split(';')[0] || '').trim().toUpperCase();
              const prodnameB = ((b.procode || '').split(';')[0] || '').trim().toUpperCase();
              result = prodnameA.localeCompare(prodnameB);
              if (result === 0) {
                const amtA2 = this.parseAmountForSort(a.amt || '');
                const amtB2 = this.parseAmountForSort(b.amt || '');
                result = amtA2 - amtB2;
              }
              return result;
            case 'channel':
              return (a.channel || 0) - (b.channel || 0);
            case 'cointype':
              return (a.cointype || 0) - (b.cointype || 0);
            case 'date_asc':
              return new Date(a.create_time || 0) - new Date(b.create_time || 0);
            case 'date_desc':
              return new Date(b.create_time || 0) - new Date(a.create_time || 0);
            default:
              return 0;
          }
        });
      },
      // Cancel button
      cancel() {
        this.open = false;
        this.reset();
      },
      // Form reset
      reset() {
        this.form = {
          id: undefined,
          status: undefined,
          remark: undefined
        };
        this.resetForm("form");
      },
      /** Search button operation */
      handleQuery() {
        this.queryParams.pageNum = 1;
        this.getList();
      },
      /** Reset button operation */
      resetQuery() {
        this.dateRange = [];
        this.queryParams.sortBy = undefined;
        this.resetForm("queryForm");
        this.handleQuery();
      },
      // Multiple selection data
      handleSelectionChange(selection) {
        this.ids = selection.map(item => item.id);
        this.selectedOrders = selection;
        this.single = selection.length !== 1;
        this.multiple = !selection.length;
      },
      /** View button operation */
      handleView(row) {
        this.$router.push({ path: '/hide/vorder/detail', query: { id: row.id }});
      },
      /** Edit button operation */
      handleUpdate(row) {
        this.reset();
        const id = row.id || this.ids[0];
        getOrder(id).then(response => {
          this.form = response.data;
          this.open = true;
          this.title = "";
        });
      },
      /** Edit button operation */
      handleUpdateShipping(row) {
        this.reset();
        const id = row.id || this.ids[0];
        getOrder(id).then(response => {
          this.form1 = response.data;
          this.open = true;
          this.title = "";
        });
      },
      /** Submit button */
      submitForm() {
        this.$refs["form"].validate(valid => {
          if (valid) {
            updateOrder(this.form).then(response => {
              this.$modal.msgSuccess("Update successful");
              this.open = false;
              this.getList();
            });


          }
        });
      },

      submitFormShipping() {
        this.$refs["form1"].validate(valid => {
          if (valid) {
          
            
            updateOrder(this.form1).then(response => {
              this.$modal.msgSuccess("Update successful");
              this.open = false;
              this.getList();
            });

            const form2 = {
              id: this.form.id,
              status: 3, // 1 represents "Shipped" status
              updateBy: this.$store.state.user.name
            };
            updateOrder(form2).then(response => {
              this.$modal.msgSuccess("Update successful");
              this.open = false;
              this.getList();
            });


          }
        });
      },

      /** Delete button operation */
      handleDelete(row) {
        this.$modal.confirm('Are you sure to delete this order? This action cannot be undone.').then(() => {
          const form = {
            id: row.id,
            status: 5, // 5 represents "isDeleted" status
            updateBy: this.$store.state.user.name
          };
          return updateOrder(form);
        }).then(() => {
          this.getList();
          this.$modal.msgSuccess("Order deleted successfully");
        }).catch(() => {});
      },
      /** Export command handler */
      handleExportCommand(command) {
        if (command === 'excel-acc') {
          // 只有在 Shipped 标签页才能导出
          if (!this.isShippedTab) {
            this.$modal.msgError('Export Excel (acc) is only available when viewing Shipped orders. Please switch to the Shipped tab first.');
            return;
          }
          this.handleExportExcelAcc();
        } else if (command === 'excel-ph') {
          this.handleExportExcelPh();
        } else if (command === 'html-ph-2') {
          // 只有在 Fulfilled 标签页才能导出
          if (!this.isFulfilledTab) {
            this.$modal.msgError('Export HTML (ph)-2 is only available when viewing Fulfilled orders. Please switch to the Fulfilled tab first.');
            return;
          }
          this.handleExportHTMLPh2();
        }
      },
      /** Export excel acc - Only for Shipped orders */
      handleExportExcelAcc() {
        // 检查是否有选中的订单
        let ordersToExport = [];
        
        if (this.selectedOrders && this.selectedOrders.length > 0) {
          // 如果有选中的订单，只导出 Shipped 状态的订单
          ordersToExport = this.selectedOrders.filter(order => order.status === 3);
          
          if (ordersToExport.length === 0) {
            this.$modal.msgError('No Shipped orders selected. Export Excel (acc) is only available for orders with status: Shipped.');
            return;
          }
          
          if (ordersToExport.length < this.selectedOrders.length) {
            const nonShippedCount = this.selectedOrders.length - ordersToExport.length;
            this.$modal.msgWarning(`${nonShippedCount} non-Shipped order(s) will be excluded. Only ${ordersToExport.length} Shipped order(s) will be exported.`);
          }
          
          // 执行导出选中的 Shipped 订单
          this.exportShippedOrders(ordersToExport);
        } else {
          // 如果没有选中订单，从当前页的订单列表中筛选 Shipped 订单
          const currentPageShippedOrders = this.orderList.filter(order => order.status === 3);
          
          if (currentPageShippedOrders.length === 0) {
            this.$modal.msgError('No Shipped orders found on current page. Export Excel (acc) is only available for orders with status: Shipped.');
            return;
          }
          
          // 直接导出当前页的所有 Shipped 订单，无需确认
          this.exportShippedOrders(currentPageShippedOrders);
        }
      },
      
      /** Export Shipped orders */
      exportShippedOrders(selectedShippedOrders = null) {
        this.downloadLoading = true;
        
        // 构建查询参数
        const exportParams = {
          ...this.addDateRange(this.queryParams, this.dateRange),
          status: 3  // 强制设置为 Shipped 状态
        };
        
        // 如果有选中的 Shipped 订单，传递订单ID列表
        if (selectedShippedOrders && selectedShippedOrders.length > 0) {
          exportParams.orderIds = selectedShippedOrders.map(order => order.id).join(',');
        }
        
        exportVOrderAcc(exportParams).then(response => {
          // 处理blob响应
          if (response && response instanceof Blob) {
            // 创建下载链接
            const blob = response;
            const url = window.URL.createObjectURL(blob);
            const link = document.createElement('a');
            link.href = url;
            link.download = `orders_acc_${new Date().getTime()}.xlsx`;
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
            window.URL.revokeObjectURL(url);
            this.$modal.msgSuccess('Excel ACC file exported successfully');
          } else if (response && response.msg) {
            // 兼容旧版本的下载方式
            this.download(response.msg);
          } else {
            this.$modal.msgError('Export failed: Invalid response');
          }
          this.downloadLoading = false;
        }).catch(error => {
          console.error('Export error:', error);
          this.$modal.msgError('Export failed: ' + (error.message || 'Unknown error'));
          this.downloadLoading = false;
        });
      },
      /** Export excel ph */
      handleExportExcelPh() {
        this.downloadLoading = true;
        exportVOrderPh(this.addDateRange(this.queryParams, this.dateRange)).then(response => {
          if (response && response instanceof Blob) {
            // 直接在新标签页中打开 Blob
            const url = window.URL.createObjectURL(response);
            const newWindow = window.open(url, '_blank');
            
            if (!newWindow || newWindow.closed) {
              // 如果浏览器阻止了弹窗，回退到下载方式
              const link = document.createElement('a');
              link.href = url;
              link.download = `orders_ph_${new Date().getTime()}.xlsx`;
              document.body.appendChild(link);
              link.click();
              document.body.removeChild(link);
              this.$modal.msgWarning('Popup blocked. File downloaded instead.');
            } else {
              // 延迟释放 URL，给浏览器时间加载
              setTimeout(() => {
                window.URL.revokeObjectURL(url);
              }, 1000);
              this.$modal.msgSuccess('Excel PH file opened in new tab');
            }
            this.downloadLoading = false;
          } else if (response && response.msg) {
            this.download(response.msg);
            this.downloadLoading = false;
          } else {
            this.$modal.msgError('Export failed: Invalid response');
            this.downloadLoading = false;
          }
        }).catch(error => {
          console.error('Export error:', error);
          this.$modal.msgError('Export failed: ' + (error.message || 'Unknown error'));
          this.downloadLoading = false;
        });
      },
      /** Export HTML (ph) - Only for Fulfilled orders with checkboxes */
      handleExportHTMLPh() {
        // 检查是否有选中的订单
        let ordersToExport = [];
        
        if (this.selectedOrders && this.selectedOrders.length > 0) {
          // 如果有选中的订单，只导出 Fulfilled 状态的订单
          ordersToExport = this.selectedOrders.filter(order => order.status === 2);
          
          if (ordersToExport.length === 0) {
            this.$modal.msgError('No Fulfilled orders selected. Export HTML (ph) is only available for orders with status: Fulfilled.');
            return;
          }
          
          if (ordersToExport.length < this.selectedOrders.length) {
            const nonFulfilledCount = this.selectedOrders.length - ordersToExport.length;
            this.$modal.msgWarning(nonFulfilledCount + ' non-Fulfilled order(s) will be excluded. Only ' + ordersToExport.length + ' Fulfilled order(s) will be exported.');
          }
        } else {
          // 如果没有选中订单，自动导出所有 Fulfilled 订单
          this.downloadLoading = true;
          return listVOrder({
            ...this.addDateRange(this.queryParams, this.dateRange),
            status: 2
          }).then(response => {
            ordersToExport = response.rows || [];
            this.downloadLoading = false;
            
            if (ordersToExport.length === 0) {
              this.$modal.msgError('No Fulfilled orders found. Export HTML (ph) is only available for orders with status: Fulfilled.');
              return;
            }
            
            this.generateHTMLPhPage(ordersToExport, this.currentSortBy);
          }).catch(error => {
            this.downloadLoading = false;
            console.error('Export error:', error);
            this.$modal.msgError('Export failed: ' + (error.message || 'Unknown error'));
          });
        }
        
        // 如果有选中的订单，直接导出
        if (ordersToExport.length > 0) {
          this.generateHTMLPhPage(ordersToExport, this.currentSortBy);
        }
      },
      /** Export HTML (ph)-2 - Only for Fulfilled orders with one row per item */
      handleExportHTMLPh2() {
        // 检查是否有选中的订单
        let ordersToExport = [];
        
        if (this.selectedOrders && this.selectedOrders.length > 0) {
          // 如果有选中的订单，只导出 Fulfilled 状态的订单
          ordersToExport = this.selectedOrders.filter(order => order.status === 2);
          
          if (ordersToExport.length === 0) {
            this.$modal.msgError('No Fulfilled orders selected. Export HTML (ph)-2 is only available for orders with status: Fulfilled.');
            return;
          }
          
          if (ordersToExport.length < this.selectedOrders.length) {
            const nonFulfilledCount = this.selectedOrders.length - ordersToExport.length;
            this.$modal.msgWarning(nonFulfilledCount + ' non-Fulfilled order(s) will be excluded. Only ' + ordersToExport.length + ' Fulfilled order(s) will be exported.');
          }
        } else {
          // 如果没有选中订单，自动导出所有 Fulfilled 订单
          this.downloadLoading = true;
          return listVOrder({
            ...this.addDateRange(this.queryParams, this.dateRange),
            status: 2
          }).then(response => {
            ordersToExport = response.rows || [];
            this.downloadLoading = false;
            
            if (ordersToExport.length === 0) {
              this.$modal.msgError('No Fulfilled orders found. Export HTML (ph)-2 is only available for orders with status: Fulfilled.');
              return;
            }
            
            this.generateHTMLPh2Page(ordersToExport, this.currentSortBy);
          }).catch(error => {
            this.downloadLoading = false;
            console.error('Export error:', error);
            this.$modal.msgError('Export failed: ' + (error.message || 'Unknown error'));
          });
        }
        
        // 如果有选中的订单，直接导出
        if (ordersToExport.length > 0) {
          this.generateHTMLPh2Page(ordersToExport, this.currentSortBy);
        }
      },
      /** Parse amount string for sorting (handles multiple amounts separated by semicolons) */
      parseAmountForSort(amtStr) {
        if (!amtStr) return 0;
        
        // 如果有多个金额（用分号分隔），取第一个
        const amounts = amtStr.toString().split(';');
        const firstAmount = amounts[0] || '0';
        
        // 移除非数字字符（保留小数点和负号）
        const numericAmount = parseFloat(firstAmount.replace(/[^\d.-]/g, '')) || 0;
        
        return numericAmount;
      },
      /** Generate HTML (ph) page */
      generateHTMLPhPage(orders, sortBy = 'amount') {
        // 不拆分订单项，每个订单保持为一行
        const rows = [];
        
        orders.forEach((order, index) => {
          // 格式化地址 - 根据字段情况按3/4/5行显示
          const addressLines = [];
          
          // 第1行：收件人姓名
          if (order.receiver_name) {
            addressLines.push(order.receiver_name);
          }
          
          // 第2行：Address Line 1
          if (order.address_line1) {
            addressLines.push(order.address_line1);
          }
          
          // 第3行（可选）：Address Line 2
          if (order.address_line2) {
            addressLines.push(order.address_line2);
          }
          
          // 第4行（可选）：Address Line 3
          if (order.address_line3) {
            addressLines.push(order.address_line3);
          }
          
          // 最后一行：City, State, PostCode
          const cityStatePost = [order.city, order.state, order.post_code].filter(x => x).join(' ');
          if (cityStatePost) {
            addressLines.push(cityStatePost);
          }
          
          // 使用<br>连接各行，使其在HTML中分行显示
          const fullAddress = addressLines.join('<br>');
          
          // 保持原始数据格式（分号分隔的多个产品显示在同一单元格）
          rows.push({
            index: index + 1,
            originalIndex: index + 1,  // 保存原始索引，排序后不变
            orderNumber: order.order_sn || '',
            username: order.username || '',
            productCode: (order.procode || '').replace(/;/g, '<br>'), // 使用换行显示多个产品
            productCodeNormalized: (order.procode || '').split(';')[0].trim().toUpperCase(), // 用于排序的标准化版本（取第一个商品代码）
            amount: (order.amt || '').toString().replace(/;/g, '<br>'),
            quantity: (order.qty || '').toString().replace(/;/g, '<br>'),
            postage: order.postage || '',
            address: fullAddress,
            orderSn: order.order_sn || '',
            // 添加数值类型的amount用于排序
            amountNumeric: this.parseAmountForSort(order.amt || '')
          });
        });
        
        // 根据sortBy参数排序
        if (sortBy === 'productCode' || sortBy === 'procode') {
          // 按Product Code排序
          rows.sort((a, b) => {
            const result = a.productCodeNormalized.localeCompare(b.productCodeNormalized);
            if (result === 0) {
              // 相同Product Code时按amount排序
              return a.amountNumeric - b.amountNumeric;
            }
            return result;
          });
        } else if (sortBy === 'orderNumber') {
          // 按Order Number排序
          rows.sort((a, b) => a.orderNumber.localeCompare(b.orderNumber));
        } else {
          // 默认按amount从低到高
          rows.sort((a, b) => a.amountNumeric - b.amountNumeric);
        }
        
        // 排序后使用originalIndex显示，不重新分配
        rows.forEach((row) => {
          row.index = row.originalIndex;
        });
        
        // 生成表格行HTML
        const tableRows = rows.map(row =>
          '<tr style="height:30mm"><td>' + row.index + '</td><td>' + row.orderNumber + 
          '</td><td>' + row.username + '</td><td>' + row.productCode + '</td><td>' + 
          row.amount + '</td><td>' + row.quantity + '</td><td>' + row.postage + 
          '</td><td>' + row.address + '</td></tr>'
        ).join('');
        
        // 构建HTML内容
        const html = '<!DOCTYPE html><html><head><meta charset="UTF-8"><title>Export HTML (ph)</title>' +
          '<style>*{margin:0;padding:0;box-sizing:border-box}body{font-family:Arial,sans-serif;padding:20px;background:#f5f7fa}' +
          '.container{background:white;padding:20px;border-radius:8px;box-shadow:0 2px 12px rgba(0,0,0,0.1)}' +
          '.header{margin-bottom:20px;display:flex;justify-content:space-between;align-items:center;flex-wrap:wrap;gap:15px}' +
          '.header h1{color:#303133;font-size:28px;font-weight:600}' +
          '.controls{display:flex;gap:15px;align-items:center;flex-wrap:wrap}.control-group{display:flex;align-items:center;gap:8px}' +
          '.control-group label{font-weight:500;color:#606266;font-size:14px}' +
          '.control-group select{padding:8px 12px;border:1px solid #dcdfe6;border-radius:4px;cursor:pointer;font-size:14px;background:white;color:#606266;transition:all 0.3s}' +
          '.control-group select:hover{border-color:#409EFF}.control-group select:focus{outline:none;border-color:#409EFF;box-shadow:0 0 0 2px rgba(64,158,255,0.2)}' +
          '.print-btn{padding:10px 20px;background:linear-gradient(135deg,#667eea 0%,#764ba2 100%);color:white;border:none;border-radius:6px;cursor:pointer;font-size:14px;font-weight:600;' +
          'box-shadow:0 4px 12px rgba(102,126,234,0.4);transition:all 0.3s;display:flex;align-items:center;gap:8px}' +
          '.print-btn:hover{transform:translateY(-2px);box-shadow:0 6px 16px rgba(102,126,234,0.5)}' +
          '.print-btn:active{transform:translateY(0);box-shadow:0 2px 8px rgba(102,126,234,0.3)}' +
          '.print-btn::before{content:"🖨️";font-size:18px}' +
          '.address-btn{padding:10px 20px;background:linear-gradient(135deg,#f093fb 0%,#f5576c 100%);color:white;border:none;border-radius:6px;cursor:pointer;font-size:14px;font-weight:600;' +
          'box-shadow:0 4px 12px rgba(245,87,108,0.4);transition:all 0.3s;display:flex;align-items:center;gap:8px}' +
          '.address-btn:hover{transform:translateY(-2px);box-shadow:0 6px 16px rgba(245,87,108,0.5)}' +
          '.address-btn:active{transform:translateY(0);box-shadow:0 2px 8px rgba(245,87,108,0.3)}' +
          '.address-btn::before{content:"📋";font-size:18px}' +
          'table{width:100%;border-collapse:collapse;border:2px solid #333;margin-top:10px}' +
          'thead{background:linear-gradient(135deg,#667eea 0%,#764ba2 100%);color:white}' +
          'th,td{padding:12px 8px;border:1px solid #333;text-align:left}' +
          'th{font-weight:600;font-size:14px}' +
          'th.sortable{cursor:pointer;user-select:none;position:relative;transition:background 0.2s}' +
          'th.sortable:hover{background:rgba(255,255,255,0.2)}' +
          'th.sortable::after{content:" ↕";opacity:0.6;font-size:12px;margin-left:4px}' +
          'th.sort-asc::after{content:" ↑";opacity:1}th.sort-desc::after{content:" ↓";opacity:1}' +
          'tbody tr{height:30mm;transition:background 0.2s}tbody tr:nth-child(even){background:#f9f9f9}tbody tr:hover{background:#ecf5ff}' +
          'td{font-size:13px;color:#606266}' +
          '.pagination{display:flex;justify-content:center;align-items:center;gap:10px;margin-top:20px}' +
          '.pagination button{padding:8px 16px;border:1px solid #dcdfe6;background:white;border-radius:4px;cursor:pointer;font-size:14px;color:#606266;transition:all 0.3s}' +
          '.pagination button:hover:not(:disabled){background:#409EFF;color:white;border-color:#409EFF;transform:translateY(-1px);box-shadow:0 2px 8px rgba(64,158,255,0.3)}' +
          '.pagination button:disabled{opacity:0.5;cursor:not-allowed}' +
          '.pagination-info{color:#606266;font-size:14px;font-weight:500}' +
          '.modal{display:none;position:fixed;z-index:2000;left:0;top:0;width:100%;height:100%;background:rgba(0,0,0,0.5);align-items:center;justify-content:center}' +
          '.modal.show{display:flex}' +
          '.modal-content{background:white;padding:30px;border-radius:12px;max-width:650px;width:90%;max-height:90vh;overflow-y:auto;box-shadow:0 8px 32px rgba(0,0,0,0.3)}' +
          '.modal-header{font-size:20px;font-weight:600;color:#303133;margin-bottom:20px;border-bottom:2px solid #409EFF;padding-bottom:10px}' +
          '.modal-body{margin-bottom:20px}.form-group{margin-bottom:15px}' +
          '.form-group label{display:block;margin-bottom:5px;font-weight:500;color:#606266}' +
          '.form-group input[type="number"],.form-group select{width:100%;padding:8px 12px;border:1px solid #dcdfe6;border-radius:4px;font-size:14px}' +
          '.form-group input[type="radio"]{margin-right:5px}' +
          '.radio-group{display:flex;flex-direction:column;gap:8px}' +
          '.modal-footer{display:flex;justify-content:flex-end;gap:10px;border-top:1px solid #dcdfe6;padding-top:20px}' +
          '.btn{padding:10px 20px;border:none;border-radius:6px;cursor:pointer;font-size:14px;font-weight:600;transition:all 0.3s}' +
          '.btn-primary{background:#409EFF;color:white}.btn-primary:hover{background:#66b1ff}' +
          '.btn-default{background:#f5f7fa;color:#606266;border:1px solid #dcdfe6}.btn-default:hover{background:#e4e7ed}' +
          '@media print{body{padding:0;background:white}.container{box-shadow:none;border-radius:0}.header,.controls,.pagination{display:none}table{border:2px solid #000}' +
          'thead{background:#666!important;-webkit-print-color-adjust:exact;print-color-adjust:exact}' +
          'tbody tr{height:30mm;page-break-inside:avoid}tbody tr:nth-child(even){background:#f5f5f5!important;-webkit-print-color-adjust:exact;print-color-adjust:exact}' +
          'td,th{border:1px solid #000}@page{size:A4 landscape;margin:10mm}}' +
          '</style></head><body><div class="container"><div class="header"><h1>Fulfilled Orders Export</h1>' +
          '<div class="controls"><div class="control-group"><label>Items per page:</label><select id="itemsPerPage">' +
          '<option value="10">10</option><option value="50" selected>50</option><option value="100">100</option><option value="150">150</option>' +
          '</select></div><div class="control-group"><label>Sort by:</label><select id="sortBy">' +
          '<option value="amount" selected>Amount (Default)</option><option value="orderNumber">Order Number</option><option value="productCode">Product Code</option></select></div>' +
          '<button class="print-btn" onclick="window.print()">Print</button>' +
          '<button class="address-btn" onclick="showAddressModal()">Print Address Labels</button></div></div>' +
          '<table><thead><tr><th class="sortable" data-sort="index" onclick="sortTable(\'index\')">Index</th>' +
          '<th class="sortable" data-sort="orderNumber" onclick="sortTable(\'orderNumber\')">Order Number</th><th>Username</th>' +
          '<th class="sortable" data-sort="productCode" onclick="sortTable(\'productCode\')">Product Code</th>' +
          '<th class="sortable" data-sort="amount" onclick="sortTable(\'amount\')">Amount</th><th>Quantity</th><th>Postage</th><th>Address</th></tr></thead>' +
          '<tbody id="tableBody">' + tableRows + '</tbody></table>' +
          '<div class="pagination"><button id="prevBtn" onclick="changePage(-1)">Previous</button>' +
          '<span id="pageInfo">Page 1 of 1</span><button id="nextBtn" onclick="changePage(1)">Next</button></div></div>' +
          '<div id="addressModal" class="modal">' +
          '<div class="modal-content">' +
          '<div class="modal-header">Address Label Settings</div>' +
          '<div class="modal-body">' +
          '<div class="form-group">' +
          '<label>Label Paper Orientation</label>' +
          '<div class="radio-group">' +
          '<label><input type="radio" name="orientation" value="portrait"> Portrait (29mm × 90mm) - 3 addresses/label</label>' +
          '<label><input type="radio" name="orientation" value="landscape" checked> Landscape (90mm × 29mm) - 1 address/label</label>' +
          '</div></div>' +
          '<div class="form-group"><label>Card Width (mm)</label><input type="number" id="cardWidth" value="90" min="50" max="1000" step="5"></div>' +
          '<div class="form-group"><label>Card Height (mm)</label><input type="number" id="cardHeight" value="29" min="30" max="500" step="5"></div>' +
          '<div class="form-group"><label>Card Padding (mm)</label><input type="number" id="cardPadding" value="0" min="0" max="50" step="1"></div>' +
          '<div class="form-group"><label>Card Margin (mm)</label><input type="number" id="cardMargin" value="0" min="0" max="20" step="1"></div>' +
          '<div class="form-group"><label>Font Size (px)</label><input type="number" id="fontSize" value="24" min="8" max="24" step="1"></div>' +
          '<div class="form-group"><label>Line Height</label><input type="number" id="lineHeight" value="1.3" min="1.0" max="3.0" step="0.1"></div>' +
          '<div class="form-group"><label>Page Margin (mm)</label><input type="number" id="pageMargin" value="0" min="0" max="50" step="1"></div>' +
          '<div class="form-group">' +
          '<label>Layout</label>' +
          '<select id="layout">' +
          '<option value="single" selected>Single Column</option>' +
          '<option value="two">Two Columns</option>' +
          '<option value="three">Three Columns</option>' +
          '<option value="four">Four Columns</option>' +
          '</select></div>' +
          '<div class="form-group">' +
          '<label>Horizontal Alignment</label>' +
          '<div class="radio-group">' +
          '<label><input type="radio" name="hAlign" value="left"> Left</label>' +
          '<label><input type="radio" name="hAlign" value="center" checked> Center</label>' +
          '<label><input type="radio" name="hAlign" value="right"> Right</label>' +
          '</div></div>' +
          '<div class="form-group">' +
          '<label>Vertical Alignment</label>' +
          '<div class="radio-group">' +
          '<label><input type="radio" name="vAlign" value="top"> Top</label>' +
          '<label><input type="radio" name="vAlign" value="middle" checked> Middle</label>' +
          '<label><input type="radio" name="vAlign" value="bottom"> Bottom</label>' +
          '</div></div>' +
          '</div>' +
          '<div class="modal-footer">' +
          '<button class="btn btn-default" onclick="closeAddressModal()">Cancel</button>' +
          '<button class="btn btn-primary" onclick="generateAddressLabels()">Generate Labels</button>' +
          '</div></div></div>' +
          '<script>let allRows=' + JSON.stringify(rows) + ';' +
          'let originalOrders=' + JSON.stringify(orders.map(order => ({
            receiver_name: order.receiver_name || '',
            address_line1: order.address_line1 || '',
            address_line2: order.address_line2 || '',
            address_line3: order.address_line3 || '',
            city: order.city || '',
            state: order.state || '',
            post_code: order.post_code || '',
            order_sn: order.order_sn || ''
          }))) + ';' +
          'let currentPage=1;let itemsPerPage=50;let currentSort={field:null,direction:"asc"};' +
          'document.getElementById("itemsPerPage").addEventListener("change",function(e){itemsPerPage=parseInt(e.target.value);currentPage=1;renderTable();});' +
          'document.getElementById("sortBy").addEventListener("change",function(e){sortTable(e.target.value);});' +
          'document.querySelectorAll("input[name=\\"orientation\\"]").forEach(radio=>{radio.addEventListener("change",function(e){' +
          'if(e.target.value==="portrait"){document.getElementById("cardWidth").value=29;document.getElementById("cardHeight").value=30;' +
          'document.getElementById("cardPadding").value=1;document.getElementById("fontSize").value=8;document.getElementById("lineHeight").value=1.2;' +
          'document.querySelector("input[name=\\"hAlign\\"][value=\\"left\\"]").checked=true;' +
          'document.querySelector("input[name=\\"vAlign\\"][value=\\"top\\"]").checked=true;}else{' +
          'document.getElementById("cardWidth").value=90;document.getElementById("cardHeight").value=29;' +
          'document.getElementById("cardPadding").value=0;document.getElementById("cardMargin").value=0;' +
          'document.getElementById("fontSize").value=24;document.getElementById("lineHeight").value=1.3;' +
          'document.querySelector("input[name=\\"hAlign\\"][value=\\"center\\"]").checked=true;' +
          'document.querySelector("input[name=\\"vAlign\\"][value=\\"middle\\"]").checked=true;}});});' +
          'function showAddressModal(){document.getElementById("addressModal").classList.add("show")}' +
          'function closeAddressModal(){document.getElementById("addressModal").classList.remove("show")}' +
          'document.getElementById("addressModal").addEventListener("click",function(e){if(e.target===this)closeAddressModal()});' +
          'function sortTable(field){if(currentSort.field===field){currentSort.direction=currentSort.direction==="asc"?"desc":"asc"}else{currentSort.field=field;currentSort.direction="asc"}' +
          'allRows.sort((a,b)=>{let result=0;if(field==="orderNumber"){let aVal=a.orderNumber||"";let bVal=b.orderNumber||"";result=currentSort.direction==="asc"?aVal.localeCompare(bVal):bVal.localeCompare(aVal)}' +
          'else if(field==="productCode"){let aVal=a.productCodeNormalized||"";let bVal=b.productCodeNormalized||"";result=currentSort.direction==="asc"?aVal.localeCompare(bVal):bVal.localeCompare(aVal);' +
          'if(result===0){result=a.amountNumeric-b.amountNumeric}}' +
          'else if(field==="amount"){result=currentSort.direction==="asc"?(a.amountNumeric-b.amountNumeric):(b.amountNumeric-a.amountNumeric)}' +
          'else{result=currentSort.direction==="asc"?(a.originalIndex-b.originalIndex):(b.originalIndex-a.originalIndex)}' +
          'return result});' +
          'document.querySelectorAll("th.sortable").forEach(th=>{th.classList.remove("sort-asc","sort-desc");if(th.dataset.sort===field){th.classList.add(currentSort.direction==="asc"?"sort-asc":"sort-desc")}});' +
          'currentPage=1;renderTable()}' +
          'function renderTable(){const tbody=document.getElementById("tableBody");const startIndex=(currentPage-1)*itemsPerPage;' +
          'const endIndex=startIndex+itemsPerPage;const pageRows=allRows.slice(startIndex,endIndex);' +
          'tbody.innerHTML=pageRows.map(row=>"<tr style=\\"height:30mm\\"><td>"+row.index+"</td><td>"+row.orderNumber+"</td><td>"+row.username+"</td><td>"+row.productCode+"</td><td>"+row.amount+"</td><td>"+row.quantity+"</td><td>"+row.postage+"</td><td>"+row.address+"</td></tr>").join("");' +
          'const totalPages=Math.ceil(allRows.length/itemsPerPage);document.getElementById("pageInfo").textContent="Page "+currentPage+" of "+totalPages+" ("+allRows.length+" total items)";' +
          'document.getElementById("prevBtn").disabled=currentPage===1;document.getElementById("nextBtn").disabled=currentPage===totalPages||totalPages===0}' +
          'function changePage(direction){const totalPages=Math.ceil(allRows.length/itemsPerPage);const newPage=currentPage+direction;' +
          'if(newPage>=1&&newPage<=totalPages){currentPage=newPage;renderTable();window.scrollTo({top:0,behavior:"smooth"})}}' +
          'function generateAddressLabels(){const config={' +
          'cardWidth:parseFloat(document.getElementById("cardWidth").value),' +
          'cardHeight:parseFloat(document.getElementById("cardHeight").value),' +
          'cardPadding:parseFloat(document.getElementById("cardPadding").value),' +
          'cardMargin:parseFloat(document.getElementById("cardMargin").value),' +
          'fontSize:parseFloat(document.getElementById("fontSize").value),' +
          'lineHeight:parseFloat(document.getElementById("lineHeight").value),' +
          'pageMargin:parseFloat(document.getElementById("pageMargin").value),' +
          'layout:document.getElementById("layout").value,' +
          'horizontalAlign:document.querySelector("input[name=\\"hAlign\\"]:checked").value,' +
          'verticalAlign:document.querySelector("input[name=\\"vAlign\\"]:checked").value,' +
          'orientation:document.querySelector("input[name=\\"orientation\\"]:checked").value,' +
          'addressesPerLabel:document.querySelector("input[name=\\"orientation\\"]:checked").value==="portrait"?3:1};' +
          'const sortedOrders=allRows.map(row=>{const order=originalOrders.find(o=>o.order_sn===row.orderSn);return order||{}});' +
          'createAddressHTML(sortedOrders,config);closeAddressModal()}' +
          'function getAddressLineCount(order){const hasLine2=order.address_line2&&order.address_line2.trim()!=="";' +
          'const hasLine3=order.address_line3&&order.address_line3.trim()!=="";return hasLine3?5:(hasLine2?4:3)}' +
          'function calculateFontSize(lineCount,config){const mmToPx=3.779527559;const cardPaddingPx=config.cardPadding*mmToPx*2;' +
          'const marginSpacingPx=(lineCount-1)*2;const availableHeightPx=(config.cardHeight*mmToPx)-cardPaddingPx-marginSpacingPx;' +
          'const calculatedFontSize=availableHeightPx/(lineCount*config.lineHeight);return Math.max(6,Math.min(12,Math.round(calculatedFontSize)))}' +
          'function formatAddressLines(order){const lastLine=[order.city,order.state,order.post_code].filter(x=>x).join(" ");' +
          'const hasLine2=order.address_line2&&order.address_line2.trim()!=="";const hasLine3=order.address_line3&&order.address_line3.trim()!=="";' +
          'if(hasLine3){return"<div>"+order.receiver_name+"</div><div>"+order.address_line1+"</div><div>"+order.address_line2+"</div><div>"+order.address_line3+"</div><div>"+lastLine+"</div>"}' +
          'else if(hasLine2){return"<div>"+order.receiver_name+"</div><div>"+order.address_line1+"</div><div>"+order.address_line2+"</div><div>"+lastLine+"</div>"}' +
          'else{return"<div>"+order.receiver_name+"</div><div>"+order.address_line1+"</div><div>"+lastLine+"</div>"}}' +
          'function createAddressHTML(orders,config){const mmToPx=3.779527559;const columns=config.layout==="single"?1:config.layout==="two"?2:config.layout==="three"?3:4;' +
          'const containerWidthMm=(config.cardWidth+config.cardMargin*2)*columns+config.cardMargin*2+config.pageMargin*2;' +
          'const containerWidthPx=containerWidthMm*mmToPx;let html="<!DOCTYPE html><html><head><meta charset=\\"UTF-8\\"><title>Address Labels - "+new Date().toLocaleDateString()+"</title>";' +
          'html+="<style>*{margin:0;padding:0;box-sizing:border-box}body{font-family:Arial,sans-serif;margin:"+(config.pageMargin*mmToPx)+"px;background-color:#f5f5f5}";' +
          'html+=".container{width:"+containerWidthPx+"px;margin:0 auto;background-color:white;padding:"+(config.cardMargin*mmToPx)+"px;display:flex;flex-wrap:wrap;gap:"+(config.cardMargin*2*mmToPx)+"px;justify-content:center}";' +
          'html+=".address-card{width:"+(config.cardWidth*mmToPx)+"px;height:"+(config.cardHeight*mmToPx)+"px;border:2px solid #333;border-radius:4px;padding:"+(config.cardPadding*mmToPx)+"px;background-color:white;page-break-inside:avoid;display:flex;flex-direction:column;justify-content:"+(config.verticalAlign==="top"?"flex-start":config.verticalAlign==="middle"?"center":"flex-end")+"}";' +
          'html+=".address-lines{line-height:"+config.lineHeight+";color:#000;font-weight:500;display:flex;flex-direction:column;justify-content:"+(config.verticalAlign==="top"?"flex-start":config.verticalAlign==="middle"?"center":"flex-end")+";width:100%;text-align:"+config.horizontalAlign+"}";' +
          'html+=".address-lines div{margin:2px 0;width:100%}.print-button{position:fixed;top:20px;right:20px;padding:10px 20px;background-color:#409EFF;color:white;border:none;border-radius:5px;cursor:pointer;font-size:14px;z-index:1000}";' +
          'html+=".print-button:hover{background-color:#66b1ff}@media print{body{background-color:white;margin:0}.container{width:100%;max-width:none;padding:"+config.pageMargin+"mm;gap:"+config.cardMargin+"mm;display:flex;flex-wrap:wrap;justify-content:center}";' +
          'html+=".address-card{width:"+config.cardWidth+"mm!important;height:"+config.cardHeight+"mm!important;padding:"+config.cardPadding+"mm;margin:"+config.cardMargin+"mm;border:1px solid #333;page-break-inside:avoid;page-break-after:"+(config.addressesPerLabel===1?"always":"auto")+";flex-shrink:0}";' +
          'html+=(config.addressesPerLabel===3?".address-card:nth-child(3n){page-break-after:always}":"")+".address-card:last-child{page-break-after:auto}.print-button{display:none}@page{size:"+config.cardWidth+"mm "+(config.addressesPerLabel===3?config.cardHeight*3:config.cardHeight)+"mm;margin:0}}";' +
          'html+="</style></head><body><button class=\\"print-button\\" onclick=\\"window.print()\\">🖨️ Print</button><div class=\\"container\\">";' +
          'orders.forEach(order=>{const lineCount=getAddressLineCount(order);const fontSize=calculateFontSize(lineCount,config);' +
          'const formattedAddress=formatAddressLines(order);html+="<div class=\\"address-card\\"><div class=\\"address-lines\\" style=\\"font-size:"+fontSize+"px;\\">"+formattedAddress+"</div></div>"});' +
          'html+="</div></body></html>";const blob=new Blob([html],{type:"text/html;charset=utf-8"});const url=window.URL.createObjectURL(blob);' +
          'const newWindow=window.open(url,"_blank");if(!newWindow||newWindow.closed){const link=document.createElement("a");link.href=url;' +
          'link.download="address_labels_"+new Date().getTime()+".html";document.body.appendChild(link);link.click();document.body.removeChild(link)}' +
          'else{setTimeout(()=>{window.URL.revokeObjectURL(url)},1000)}}' +
          'sortTable("' + (sortBy === 'procode' ? 'productCode' : sortBy === 'orderNumber' ? 'orderNumber' : 'amount') + '");</' + 'script></body></html>';
        
        // 创建并打开HTML文件
        const blob = new Blob([html], { type: 'text/html;charset=utf-8' });
        const url = window.URL.createObjectURL(blob);
        const newWindow = window.open(url, '_blank');
        
        if (!newWindow || newWindow.closed || typeof newWindow.closed === 'undefined') {
          const link = document.createElement('a');
          link.href = url;
          link.download = 'orders_ph_' + new Date().getTime() + '.html';
          document.body.appendChild(link);
          link.click();
          document.body.removeChild(link);
          this.$modal.msgWarning('Popup blocked. File downloaded instead.');
        } else {
          setTimeout(() => {
            window.URL.revokeObjectURL(url);
          }, 1000);
          this.$modal.msgSuccess('HTML (ph) page opened in new tab');
        }
      },
      /** Generate HTML (ph)-2 page with one row per item */
      generateHTMLPh2Page(orders, sortBy = 'amount') {
        // HTML 转义函数
        const escapeHtml = (text) => {
          if (!text) return '';
          const map = {
            '&': '&amp;',
            '<': '&lt;',
            '>': '&gt;',
            '"': '&quot;',
            "'": '&#039;'
          };
          return text.replace(/[&<>"']/g, m => map[m]);
        };
        
        // 拆分订单项，每个商品为一行
        const rows = [];
        let globalIndex = 0;  // 全局索引计数器
        
        orders.forEach((order, orderIndex) => {
          // 格式化地址 - 根据字段情况按3/4/5行显示
          const addressLines = [];
          
          // 第1行：收件人姓名
          if (order.receiver_name) {
            addressLines.push(order.receiver_name);
          }
          
          // 第2行：Address Line 1
          if (order.address_line1) {
            addressLines.push(order.address_line1);
          }
          
          // 第3行（可选）：Address Line 2
          if (order.address_line2) {
            addressLines.push(order.address_line2);
          }
          
          // 第4行（可选）：Address Line 3
          if (order.address_line3) {
            addressLines.push(order.address_line3);
          }
          
          // 最后一行：City, State, PostCode
          const cityStatePost = [order.city, order.state, order.post_code].filter(x => x).join(' ');
          if (cityStatePost) {
            addressLines.push(cityStatePost);
          }
          
          // 使用<br>连接各行，使其在HTML中分行显示
          const fullAddress = addressLines.join('<br>');
          
          // 拆分商品信息（用分号分隔）
          const procodes = (order.procode || '').split(';').filter(x => x.trim());
          const amounts = (order.amt || '').toString().split(';').filter(x => x.trim());
          const quantities = (order.qty || '').toString().split(';').filter(x => x.trim());
          
          // 获取最大长度，确保所有数组对齐
          const maxLength = Math.max(procodes.length, amounts.length, quantities.length, 1);
          
          // 格式化订单来源（Channel），并进行 HTML 转义
          let orderSource = '';
          if (order.channel == 99 && order.remark && order.remark.includes('Custom Channel:')) {
            // 从 remark 中提取自定义渠道
            const match = order.remark.match(/Custom Channel:\s*(.+?)(?:\s*\[|$)/);
            orderSource = escapeHtml(match ? match[1].trim() : 'Other');
          } else {
            // 使用常见的渠道映射
            const channelMap = {
              0: 'OS',
              1: 'Telegram',
              2: 'Market',
              10: 'OS (Admin)',
              11: 'Telegram (Admin)',
              12: 'Market (Admin)',
              99: 'Other'
            };
            const channelLabel = channelMap[order.channel] || `Channel ${order.channel || 'N/A'}`;
            orderSource = escapeHtml(channelLabel);
          }
          
          // 获取备注（Comment），并进行 HTML 转义
          const comment = escapeHtml(order.remark || '');
          
          // 为每个商品创建一行
          for (let i = 0; i < maxLength; i++) {
            globalIndex++;
            const procode = procodes[i] || '';
            const amt = amounts[i] || '';
            const qty = quantities[i] || '';
            
            rows.push({
              index: globalIndex,
              originalIndex: globalIndex,  // 保存原始索引，排序后不变
              orderNumber: order.order_sn || '',
              username: order.username || '',
              productCode: procode,
              productCodeNormalized: procode.trim().toUpperCase(), // 用于排序的标准化版本
              amount: amt,
              quantity: qty,
              itemPosition: (i + 1) + '/' + maxLength, // 显示商品序号，如 1/3, 2/3, 3/3
              postage: i === 0 ? (order.postage || '') : '', // 只在第一行显示邮费
              address: fullAddress,
              comment: comment, // 添加备注列
              orderSource: orderSource, // 添加订单来源列
              orderSn: order.order_sn || '',
              // 添加数值类型的amount用于排序
              amountNumeric: parseFloat((amt || '0').replace(/[^\d.-]/g, '')) || 0,
              // 添加原始订单索引，用于保持订单内商品的顺序
              originalOrderIndex: orderIndex,
              itemIndex: i,
              totalItems: maxLength
            });
          }
        });
        
        // 根据sortBy参数排序
        if (sortBy === 'productCode' || sortBy === 'procode') {
          // 按Product Code排序
          rows.sort((a, b) => {
            const result = a.productCodeNormalized.localeCompare(b.productCodeNormalized);
            if (result === 0) {
              // 相同Product Code时按amount排序
              const amtResult = a.amountNumeric - b.amountNumeric;
              if (amtResult === 0) {
                // amount相同时保持原始顺序
                return (a.originalOrderIndex * 1000 + a.itemIndex) - (b.originalOrderIndex * 1000 + b.itemIndex);
              }
              return amtResult;
            }
            return result;
          });
        } else if (sortBy === 'orderNumber') {
          // 按Order Number排序
          rows.sort((a, b) => {
            const result = a.orderNumber.localeCompare(b.orderNumber);
            if (result === 0) {
              return a.itemIndex - b.itemIndex;
            }
            return result;
          });
        } else {
          // 默认按amount从低到高
          rows.sort((a, b) => {
            const result = a.amountNumeric - b.amountNumeric;
            if (result === 0) {
              return (a.originalOrderIndex * 1000 + a.itemIndex) - (b.originalOrderIndex * 1000 + b.itemIndex);
            }
            return result;
          });
        }
        
        // 排序后使用originalIndex显示，不重新分配
        rows.forEach((row) => {
          row.index = row.originalIndex;
        });
        
        // 生成表格行HTML（添加 Comment 和 Order Source 列）
        const tableRows = rows.map(row =>
          '<tr style="height:30mm"><td>' + row.index + '</td><td>' + row.orderNumber + 
          '</td><td>' + row.username + '</td><td>' + row.productCode + '</td><td>' + 
          row.amount + '</td><td>' + row.quantity + '</td><td>' + row.itemPosition + 
          '</td><td>' + row.postage + '</td><td>' + row.address + '</td><td>' + 
          (row.comment || '') + '</td><td>' + (row.orderSource || '') + '</td></tr>'
        ).join('');
        
        // 构建HTML内容
        const html = '<!DOCTYPE html><html><head><meta charset="UTF-8"><title>Export HTML (ph)-2</title>' +
          '<style>*{margin:0;padding:0;box-sizing:border-box}body{font-family:Arial,sans-serif;padding:20px;background:#f5f7fa}' +
          '.container{background:white;padding:20px;border-radius:8px;box-shadow:0 2px 12px rgba(0,0,0,0.1)}' +
          '.header{margin-bottom:20px;display:flex;justify-content:space-between;align-items:center;flex-wrap:wrap;gap:15px}' +
          '.header h1{color:#303133;font-size:28px;font-weight:600}' +
          '.controls{display:flex;gap:15px;align-items:center;flex-wrap:wrap}.control-group{display:flex;align-items:center;gap:8px}' +
          '.control-group label{font-weight:500;color:#606266;font-size:14px}' +
          '.control-group select{padding:8px 12px;border:1px solid #dcdfe6;border-radius:4px;cursor:pointer;font-size:14px;background:white;color:#606266;transition:all 0.3s}' +
          '.control-group select:hover{border-color:#409EFF}.control-group select:focus{outline:none;border-color:#409EFF;box-shadow:0 0 0 2px rgba(64,158,255,0.2)}' +
          '.print-btn{padding:10px 20px;background:linear-gradient(135deg,#667eea 0%,#764ba2 100%);color:white;border:none;border-radius:6px;cursor:pointer;font-size:14px;font-weight:600;' +
          'box-shadow:0 4px 12px rgba(102,126,234,0.4);transition:all 0.3s;display:flex;align-items:center;gap:8px}' +
          '.print-btn:hover{transform:translateY(-2px);box-shadow:0 6px 16px rgba(102,126,234,0.5)}' +
          '.print-btn:active{transform:translateY(0);box-shadow:0 2px 8px rgba(102,126,234,0.3)}' +
          '.print-btn::before{content:"🖨️";font-size:18px}' +
          '.address-btn{padding:10px 20px;background:linear-gradient(135deg,#f093fb 0%,#f5576c 100%);color:white;border:none;border-radius:6px;cursor:pointer;font-size:14px;font-weight:600;' +
          'box-shadow:0 4px 12px rgba(245,87,108,0.4);transition:all 0.3s;display:flex;align-items:center;gap:8px}' +
          '.address-btn:hover{transform:translateY(-2px);box-shadow:0 6px 16px rgba(245,87,108,0.5)}' +
          '.address-btn:active{transform:translateY(0);box-shadow:0 2px 8px rgba(245,87,108,0.3)}' +
          '.address-btn::before{content:"📋";font-size:18px}' +
          'table{width:100%;border-collapse:collapse;border:2px solid #333;margin-top:10px}' +
          'thead{background:linear-gradient(135deg,#667eea 0%,#764ba2 100%);color:white}' +
          'th,td{padding:12px 8px;border:1px solid #333;text-align:left}' +
          'th{font-weight:600;font-size:14px}' +
          'th.sortable{cursor:pointer;user-select:none;position:relative;transition:background 0.2s}' +
          'th.sortable:hover{background:rgba(255,255,255,0.2)}' +
          'th.sortable::after{content:" ↕";opacity:0.6;font-size:12px;margin-left:4px}' +
          'th.sort-asc::after{content:" ↑";opacity:1}th.sort-desc::after{content:" ↓";opacity:1}' +
          'tbody tr{height:30mm;transition:background 0.2s}tbody tr:nth-child(even){background:#f9f9f9}tbody tr:hover{background:#ecf5ff}' +
          'td{font-size:13px;color:#606266}' +
          '.pagination{display:flex;justify-content:center;align-items:center;gap:10px;margin-top:20px}' +
          '.pagination button{padding:8px 16px;border:1px solid #dcdfe6;background:white;border-radius:4px;cursor:pointer;font-size:14px;color:#606266;transition:all 0.3s}' +
          '.pagination button:hover:not(:disabled){background:#409EFF;color:white;border-color:#409EFF;transform:translateY(-1px);box-shadow:0 2px 8px rgba(64,158,255,0.3)}' +
          '.pagination button:disabled{opacity:0.5;cursor:not-allowed}' +
          '.pagination-info{color:#606266;font-size:14px;font-weight:500}' +
          '.modal{display:none;position:fixed;z-index:2000;left:0;top:0;width:100%;height:100%;background:rgba(0,0,0,0.5);align-items:center;justify-content:center}' +
          '.modal.show{display:flex}' +
          '.modal-content{background:white;padding:30px;border-radius:12px;max-width:650px;width:90%;max-height:90vh;overflow-y:auto;box-shadow:0 8px 32px rgba(0,0,0,0.3)}' +
          '.modal-header{font-size:20px;font-weight:600;color:#303133;margin-bottom:20px;border-bottom:2px solid #409EFF;padding-bottom:10px}' +
          '.modal-body{margin-bottom:20px}.form-group{margin-bottom:15px}' +
          '.form-group label{display:block;margin-bottom:5px;font-weight:500;color:#606266}' +
          '.form-group input[type="number"],.form-group select{width:100%;padding:8px 12px;border:1px solid #dcdfe6;border-radius:4px;font-size:14px}' +
          '.form-group input[type="radio"]{margin-right:5px}' +
          '.radio-group{display:flex;flex-direction:column;gap:8px}' +
          '.modal-footer{display:flex;justify-content:flex-end;gap:10px;border-top:1px solid #dcdfe6;padding-top:20px}' +
          '.btn{padding:10px 20px;border:none;border-radius:6px;cursor:pointer;font-size:14px;font-weight:600;transition:all 0.3s}' +
          '.btn-primary{background:#409EFF;color:white}.btn-primary:hover{background:#66b1ff}' +
          '.btn-default{background:#f5f7fa;color:#606266;border:1px solid #dcdfe6}.btn-default:hover{background:#e4e7ed}' +
          '@media print{body{padding:0;background:white}.container{box-shadow:none;border-radius:0}.header,.controls,.pagination{display:none}table{border:2px solid #000}' +
          'thead{background:#666!important;-webkit-print-color-adjust:exact;print-color-adjust:exact}' +
          'tbody tr{height:30mm;page-break-inside:avoid}tbody tr:nth-child(even){background:#f5f5f5!important;-webkit-print-color-adjust:exact;print-color-adjust:exact}' +
          'td,th{border:1px solid #000}@page{size:A4 landscape;margin:10mm}}' +
          '</style></head><body><div class="container"><div class="header"><h1>Fulfilled Orders Export (ph)-2 - One Row Per Item</h1>' +
          '<div class="controls"><div class="control-group"><label>Items per page:</label><select id="itemsPerPage">' +
          '<option value="10">10</option><option value="50" selected>50</option><option value="100">100</option><option value="150">150</option>' +
          '</select></div><div class="control-group"><label>Sort by:</label><select id="sortBy">' +
          '<option value="amount" selected>Amount (Default)</option><option value="orderNumber">Order Number</option><option value="productCode">Product Code</option></select></div>' +
          '<button class="print-btn" onclick="window.print()">Print</button>' +
          '<button class="address-btn" onclick="showAddressModal()">Print Address Labels</button></div></div>' +
          '<table><thead><tr><th class="sortable" data-sort="index" onclick="sortTable(\'index\')">Index</th>' +
          '<th class="sortable" data-sort="orderNumber" onclick="sortTable(\'orderNumber\')">Order Number</th><th>Username</th>' +
          '<th class="sortable" data-sort="productCode" onclick="sortTable(\'productCode\')">Product Code</th>' +
          '<th class="sortable" data-sort="amount" onclick="sortTable(\'amount\')">Amount</th><th>Quantity</th><th>Item</th><th>Postage</th><th>Address</th><th>Comment</th><th>Channel</th></tr></thead>' +
          '<tbody id="tableBody">' + tableRows + '</tbody></table>' +
          '<div class="pagination"><button id="prevBtn" onclick="changePage(-1)">Previous</button>' +
          '<span id="pageInfo">Page 1 of 1</span><button id="nextBtn" onclick="changePage(1)">Next</button></div></div>' +
          '<div id="addressModal" class="modal">' +
          '<div class="modal-content">' +
          '<div class="modal-header">Address Label Settings</div>' +
          '<div class="modal-body">' +
          '<div class="form-group">' +
          '<label>Label Paper Orientation</label>' +
          '<div class="radio-group">' +
          '<label><input type="radio" name="orientation" value="portrait"> Portrait (29mm × 90mm) - 3 addresses/label</label>' +
          '<label><input type="radio" name="orientation" value="landscape" checked> Landscape (90mm × 29mm) - 1 address/label</label>' +
          '</div></div>' +
          '<div class="form-group"><label>Card Width (mm)</label><input type="number" id="cardWidth" value="90" min="50" max="1000" step="5"></div>' +
          '<div class="form-group"><label>Card Height (mm)</label><input type="number" id="cardHeight" value="29" min="30" max="500" step="5"></div>' +
          '<div class="form-group"><label>Card Padding (mm)</label><input type="number" id="cardPadding" value="0" min="0" max="50" step="1"></div>' +
          '<div class="form-group"><label>Card Margin (mm)</label><input type="number" id="cardMargin" value="0" min="0" max="20" step="1"></div>' +
          '<div class="form-group"><label>Font Size (px)</label><input type="number" id="fontSize" value="24" min="8" max="24" step="1"></div>' +
          '<div class="form-group"><label>Line Height</label><input type="number" id="lineHeight" value="1.3" min="1.0" max="3.0" step="0.1"></div>' +
          '<div class="form-group"><label>Page Margin (mm)</label><input type="number" id="pageMargin" value="0" min="0" max="50" step="1"></div>' +
          '<div class="form-group">' +
          '<label>Layout</label>' +
          '<select id="layout">' +
          '<option value="single" selected>Single Column</option>' +
          '<option value="two">Two Columns</option>' +
          '<option value="three">Three Columns</option>' +
          '<option value="four">Four Columns</option>' +
          '</select></div>' +
          '<div class="form-group">' +
          '<label>Horizontal Alignment</label>' +
          '<div class="radio-group">' +
          '<label><input type="radio" name="hAlign" value="left"> Left</label>' +
          '<label><input type="radio" name="hAlign" value="center" checked> Center</label>' +
          '<label><input type="radio" name="hAlign" value="right"> Right</label>' +
          '</div></div>' +
          '<div class="form-group">' +
          '<label>Vertical Alignment</label>' +
          '<div class="radio-group">' +
          '<label><input type="radio" name="vAlign" value="top"> Top</label>' +
          '<label><input type="radio" name="vAlign" value="middle" checked> Middle</label>' +
          '<label><input type="radio" name="vAlign" value="bottom"> Bottom</label>' +
          '</div></div>' +
          '</div>' +
          '<div class="modal-footer">' +
          '<button class="btn btn-default" onclick="closeAddressModal()">Cancel</button>' +
          '<button class="btn btn-primary" onclick="generateAddressLabels()">Generate Labels</button>' +
          '</div></div></div>' +
          '<script>let allRows=' + JSON.stringify(rows) + ';' +
          'let originalOrders=' + JSON.stringify(orders.map(order => ({
            receiver_name: order.receiver_name || '',
            address_line1: order.address_line1 || '',
            address_line2: order.address_line2 || '',
            address_line3: order.address_line3 || '',
            city: order.city || '',
            state: order.state || '',
            post_code: order.post_code || '',
            order_sn: order.order_sn || ''
          }))) + ';' +
          'let currentPage=1;let itemsPerPage=50;let currentSort={field:null,direction:"asc"};' +
          'document.getElementById("itemsPerPage").addEventListener("change",function(e){itemsPerPage=parseInt(e.target.value);currentPage=1;renderTable();});' +
          'document.getElementById("sortBy").addEventListener("change",function(e){sortTable(e.target.value);});' +
          'document.querySelectorAll("input[name=\\"orientation\\"]").forEach(radio=>{radio.addEventListener("change",function(e){' +
          'if(e.target.value==="portrait"){document.getElementById("cardWidth").value=29;document.getElementById("cardHeight").value=30;' +
          'document.getElementById("cardPadding").value=1;document.getElementById("fontSize").value=8;document.getElementById("lineHeight").value=1.2;' +
          'document.querySelector("input[name=\\"hAlign\\"][value=\\"left\\"]").checked=true;' +
          'document.querySelector("input[name=\\"vAlign\\"][value=\\"top\\"]").checked=true;}else{' +
          'document.getElementById("cardWidth").value=90;document.getElementById("cardHeight").value=29;' +
          'document.getElementById("cardPadding").value=0;document.getElementById("cardMargin").value=0;' +
          'document.getElementById("fontSize").value=24;document.getElementById("lineHeight").value=1.3;' +
          'document.querySelector("input[name=\\"hAlign\\"][value=\\"center\\"]").checked=true;' +
          'document.querySelector("input[name=\\"vAlign\\"][value=\\"middle\\"]").checked=true;}});});' +
          'function showAddressModal(){document.getElementById("addressModal").classList.add("show")}' +
          'function closeAddressModal(){document.getElementById("addressModal").classList.remove("show")}' +
          'document.getElementById("addressModal").addEventListener("click",function(e){if(e.target===this)closeAddressModal()});' +
          'function sortTable(field){if(currentSort.field===field){currentSort.direction=currentSort.direction==="asc"?"desc":"asc"}else{currentSort.field=field;currentSort.direction="asc"}' +
          'allRows.sort((a,b)=>{let result=0;' +
          'if(field==="orderNumber"){let aVal=a.orderNumber||"";let bVal=b.orderNumber||"";result=currentSort.direction==="asc"?aVal.localeCompare(bVal):bVal.localeCompare(aVal);' +
          'if(result===0){result=a.itemIndex-b.itemIndex}}' +
          'else if(field==="productCode"){let aVal=a.productCodeNormalized||"";let bVal=b.productCodeNormalized||"";result=currentSort.direction==="asc"?aVal.localeCompare(bVal):bVal.localeCompare(aVal);' +
          'if(result===0){result=a.amountNumeric-b.amountNumeric;if(result===0){result=(a.originalOrderIndex*1000+a.itemIndex)-(b.originalOrderIndex*1000+b.itemIndex)}}}' +
          'else if(field==="amount"){result=currentSort.direction==="asc"?(a.amountNumeric-b.amountNumeric):(b.amountNumeric-a.amountNumeric);' +
          'if(result===0){result=(a.originalOrderIndex*1000+a.itemIndex)-(b.originalOrderIndex*1000+b.itemIndex)}}' +
          'else{result=currentSort.direction==="asc"?(a.originalIndex-b.originalIndex):(b.originalIndex-a.originalIndex)}' +
          'return result});' +
          'document.querySelectorAll("th.sortable").forEach(th=>{th.classList.remove("sort-asc","sort-desc");if(th.dataset.sort===field){th.classList.add(currentSort.direction==="asc"?"sort-asc":"sort-desc")}});' +
          'currentPage=1;renderTable()}' +
          'function renderTable(){const tbody=document.getElementById("tableBody");const startIndex=(currentPage-1)*itemsPerPage;' +
          'const endIndex=startIndex+itemsPerPage;const pageRows=allRows.slice(startIndex,endIndex);' +
          'tbody.innerHTML=pageRows.map(row=>"<tr style=\\"height:30mm\\"><td>"+row.index+"</td><td>"+row.orderNumber+"</td><td>"+row.username+"</td><td>"+row.productCode+"</td><td>"+row.amount+"</td><td>"+row.quantity+"</td><td>"+row.itemPosition+"</td><td>"+row.postage+"</td><td>"+row.address+"</td><td>"+(row.comment||"")+"</td><td>"+(row.orderSource||"")+"</td></tr>").join("");' +
          'const totalPages=Math.ceil(allRows.length/itemsPerPage);document.getElementById("pageInfo").textContent="Page "+currentPage+" of "+totalPages+" ("+allRows.length+" total items)";' +
          'document.getElementById("prevBtn").disabled=currentPage===1;document.getElementById("nextBtn").disabled=currentPage===totalPages||totalPages===0}' +
          'function changePage(direction){const totalPages=Math.ceil(allRows.length/itemsPerPage);const newPage=currentPage+direction;' +
          'if(newPage>=1&&newPage<=totalPages){currentPage=newPage;renderTable();window.scrollTo({top:0,behavior:"smooth"})}}' +
          'function generateAddressLabels(){const config={' +
          'cardWidth:parseFloat(document.getElementById("cardWidth").value),' +
          'cardHeight:parseFloat(document.getElementById("cardHeight").value),' +
          'cardPadding:parseFloat(document.getElementById("cardPadding").value),' +
          'cardMargin:parseFloat(document.getElementById("cardMargin").value),' +
          'fontSize:parseFloat(document.getElementById("fontSize").value),' +
          'lineHeight:parseFloat(document.getElementById("lineHeight").value),' +
          'pageMargin:parseFloat(document.getElementById("pageMargin").value),' +
          'layout:document.getElementById("layout").value,' +
          'horizontalAlign:document.querySelector("input[name=\\"hAlign\\"]:checked").value,' +
          'verticalAlign:document.querySelector("input[name=\\"vAlign\\"]:checked").value,' +
          'orientation:document.querySelector("input[name=\\"orientation\\"]:checked").value,' +
          'addressesPerLabel:document.querySelector("input[name=\\"orientation\\"]:checked").value==="portrait"?3:1};' +
          'const uniqueOrderSns=new Set();allRows.forEach(row=>{uniqueOrderSns.add(row.orderSn)});' +
          'const sortedOrders=Array.from(uniqueOrderSns).map(orderSn=>{const order=originalOrders.find(o=>o.order_sn===orderSn);return order||{}});' +
          'createAddressHTML(sortedOrders,config);closeAddressModal()}' +
          'function getAddressLineCount(order){const hasLine2=order.address_line2&&order.address_line2.trim()!=="";' +
          'const hasLine3=order.address_line3&&order.address_line3.trim()!=="";return hasLine3?5:(hasLine2?4:3)}' +
          'function calculateFontSize(lineCount,config){const mmToPx=3.779527559;const cardPaddingPx=config.cardPadding*mmToPx*2;' +
          'const marginSpacingPx=(lineCount-1)*2;const availableHeightPx=(config.cardHeight*mmToPx)-cardPaddingPx-marginSpacingPx;' +
          'const calculatedFontSize=availableHeightPx/(lineCount*config.lineHeight);return Math.max(6,Math.min(12,Math.round(calculatedFontSize)))}' +
          'function formatAddressLines(order){const lastLine=[order.city,order.state,order.post_code].filter(x=>x).join(" ");' +
          'const hasLine2=order.address_line2&&order.address_line2.trim()!=="";const hasLine3=order.address_line3&&order.address_line3.trim()!=="";' +
          'if(hasLine3){return"<div>"+order.receiver_name+"</div><div>"+order.address_line1+"</div><div>"+order.address_line2+"</div><div>"+order.address_line3+"</div><div>"+lastLine+"</div>"}' +
          'else if(hasLine2){return"<div>"+order.receiver_name+"</div><div>"+order.address_line1+"</div><div>"+order.address_line2+"</div><div>"+lastLine+"</div>"}' +
          'else{return"<div>"+order.receiver_name+"</div><div>"+order.address_line1+"</div><div>"+lastLine+"</div>"}}' +
          'function createAddressHTML(orders,config){const mmToPx=3.779527559;const columns=config.layout==="single"?1:config.layout==="two"?2:config.layout==="three"?3:4;' +
          'const containerWidthMm=(config.cardWidth+config.cardMargin*2)*columns+config.cardMargin*2+config.pageMargin*2;' +
          'const containerWidthPx=containerWidthMm*mmToPx;let html="<!DOCTYPE html><html><head><meta charset=\\"UTF-8\\"><title>Address Labels - "+new Date().toLocaleDateString()+"</title>";' +
          'html+="<style>*{margin:0;padding:0;box-sizing:border-box}body{font-family:Arial,sans-serif;margin:"+(config.pageMargin*mmToPx)+"px;background-color:#f5f5f5}";' +
          'html+=".container{width:"+containerWidthPx+"px;margin:0 auto;background-color:white;padding:"+(config.cardMargin*mmToPx)+"px;display:flex;flex-wrap:wrap;gap:"+(config.cardMargin*2*mmToPx)+"px;justify-content:center}";' +
          'html+=".address-card{width:"+(config.cardWidth*mmToPx)+"px;height:"+(config.cardHeight*mmToPx)+"px;border:2px solid #333;border-radius:4px;padding:"+(config.cardPadding*mmToPx)+"px;background-color:white;page-break-inside:avoid;display:flex;flex-direction:column;justify-content:"+(config.verticalAlign==="top"?"flex-start":config.verticalAlign==="middle"?"center":"flex-end")+"}";' +
          'html+=".address-lines{line-height:"+config.lineHeight+";color:#000;font-weight:500;display:flex;flex-direction:column;justify-content:"+(config.verticalAlign==="top"?"flex-start":config.verticalAlign==="middle"?"center":"flex-end")+";width:100%;text-align:"+config.horizontalAlign+"}";' +
          'html+=".address-lines div{margin:2px 0;width:100%}.print-button{position:fixed;top:20px;right:20px;padding:10px 20px;background-color:#409EFF;color:white;border:none;border-radius:5px;cursor:pointer;font-size:14px;z-index:1000}";' +
          'html+=".print-button:hover{background-color:#66b1ff}@media print{body{background-color:white;margin:0}.container{width:100%;max-width:none;padding:"+config.pageMargin+"mm;gap:"+config.cardMargin+"mm;display:flex;flex-wrap:wrap;justify-content:center}";' +
          'html+=".address-card{width:"+config.cardWidth+"mm!important;height:"+config.cardHeight+"mm!important;padding:"+config.cardPadding+"mm;margin:"+config.cardMargin+"mm;border:1px solid #333;page-break-inside:avoid;page-break-after:"+(config.addressesPerLabel===1?"always":"auto")+";flex-shrink:0}";' +
          'html+=(config.addressesPerLabel===3?".address-card:nth-child(3n){page-break-after:always}":"")+".address-card:last-child{page-break-after:auto}.print-button{display:none}@page{size:"+config.cardWidth+"mm "+(config.addressesPerLabel===3?config.cardHeight*3:config.cardHeight)+"mm;margin:0}}";' +
          'html+="</style></head><body><button class=\\"print-button\\" onclick=\\"window.print()\\">🖨️ Print</button><div class=\\"container\\">";' +
          'orders.forEach(order=>{const lineCount=getAddressLineCount(order);const fontSize=calculateFontSize(lineCount,config);' +
          'const formattedAddress=formatAddressLines(order);html+="<div class=\\"address-card\\"><div class=\\"address-lines\\" style=\\"font-size:"+fontSize+"px;\\">"+formattedAddress+"</div></div>"});' +
          'html+="</div></body></html>";const blob=new Blob([html],{type:"text/html;charset=utf-8"});const url=window.URL.createObjectURL(blob);' +
          'const newWindow=window.open(url,"_blank");if(!newWindow||newWindow.closed){const link=document.createElement("a");link.href=url;' +
          'link.download="address_labels_"+new Date().getTime()+".html";document.body.appendChild(link);link.click();document.body.removeChild(link)}' +
          'else{setTimeout(()=>{window.URL.revokeObjectURL(url)},1000)}}' +
          'sortTable("' + (sortBy === 'procode' ? 'productCode' : sortBy === 'orderNumber' ? 'orderNumber' : 'amount') + '");</' + 'script></body></html>';
        
        // 创建并打开HTML文件
        const blob = new Blob([html], { type: 'text/html;charset=utf-8' });
        const url = window.URL.createObjectURL(blob);
        const newWindow = window.open(url, '_blank');
        
        if (!newWindow || newWindow.closed || typeof newWindow.closed === 'undefined') {
          const link = document.createElement('a');
          link.href = url;
          link.download = 'orders_ph2_' + new Date().getTime() + '.html';
          document.body.appendChild(link);
          link.click();
          document.body.removeChild(link);
          this.$modal.msgWarning('Popup blocked. File downloaded instead.');
        } else {
          setTimeout(() => {
            window.URL.revokeObjectURL(url);
          }, 1000);
          this.$modal.msgSuccess('HTML (ph)-2 page opened in new tab');
        }
      },
      /** Export to HTML (Address Only) */
      handleExportHTML() {
        this.downloadLoading = true;
        // 获取当前过滤后的订单列表
        return listVOrder(this.addDateRange(this.queryParams, this.dateRange)).then(response => {
          this.pendingOrdersForExport = response.rows;
          this.downloadLoading = false;
          // 显示配置对话框
          this.htmlExportDialogVisible = true;
        }).catch(() => {
          this.downloadLoading = false;
        });
      },
      /** 处理标签纸方向切换 */
      handleOrientationChange(orientation) {
        if (orientation === 'portrait') {
          // 竖向: 29mm × 90mm, 3个地址/标签
          this.htmlExportConfig.cardWidth = 29;
          this.htmlExportConfig.cardHeight = 30;
          this.htmlExportConfig.cardPadding = 1;
          this.htmlExportConfig.fontSize = 8;
          this.htmlExportConfig.lineHeight = 1.2;
          this.htmlExportConfig.horizontalAlign = 'left';
          this.htmlExportConfig.verticalAlign = 'top';
          this.htmlExportConfig.addressesPerLabel = 3;
        } else if (orientation === 'landscape') {
          // 横向: 90mm × 29mm, 1个地址/标签
          this.htmlExportConfig.cardWidth = 90;
          this.htmlExportConfig.cardHeight = 29;
          this.htmlExportConfig.cardPadding = 0;
          this.htmlExportConfig.cardMargin = 0;
          this.htmlExportConfig.fontSize = 24;
          this.htmlExportConfig.lineHeight = 1.3;
          this.htmlExportConfig.horizontalAlign = 'center';
          this.htmlExportConfig.verticalAlign = 'middle';
          this.htmlExportConfig.addressesPerLabel = 1;
        }
      },
      /** 确认HTML导出 */
      confirmHtmlExport() {
        if (!this.pendingOrdersForExport || this.pendingOrdersForExport.length === 0) {
          this.$modal.msgError('No orders to export');
          return;
        }
        this.htmlExportDialogVisible = false;
        this.generateAddressHTML(this.pendingOrdersForExport, this.htmlExportConfig);
        this.pendingOrdersForExport = null;
      },
      /** Generate HTML file with addresses */
      generateAddressHTML(orders, config) {
        // 转换mm到px（1mm ≈ 3.779527559px，但使用mm单位更准确）
        const mmToPx = 3.779527559;
        
        // 计算列数
        const columns = config.layout === 'single' ? 1 : 
                       config.layout === 'two' ? 2 : 
                       config.layout === 'three' ? 3 : 4;
        
        // 计算每列宽度（考虑边距和间距）
        const cardWidthMm = config.cardWidth;
        const cardHeightMm = config.cardHeight;
        const cardPaddingMm = config.cardPadding;
        const cardMarginMm = config.cardMargin;
        const pageMarginMm = config.pageMargin;
        
        // 生成CSS样式
        const cardWidthPx = cardWidthMm * mmToPx;
        const cardHeightPx = cardHeightMm * mmToPx;
        const cardPaddingPx = cardPaddingMm * mmToPx;
        const cardMarginPx = cardMarginMm * mmToPx;
        const pageMarginPx = pageMarginMm * mmToPx;
        
        // 计算容器宽度（考虑列数、卡片宽度、边距和间距）
        const containerWidthMm = (cardWidthMm + cardMarginMm * 2) * columns + cardMarginMm * 2 + pageMarginMm * 2;
        const containerWidthPx = containerWidthMm * mmToPx;
        
        // 生成HTML内容
        let htmlContent = `
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <title>Order Addresses - ${new Date().toLocaleDateString()}</title>
  <style>
    * {
      margin: 0;
      padding: 0;
      box-sizing: border-box;
    }
    body {
      font-family: Arial, sans-serif;
      margin: ${pageMarginPx}px;
      background-color: #f5f5f5;
    }
    .container {
      width: ${containerWidthPx}px;
      ${config.containerAlign === 'left' ? 'margin: 0 0 0 0;' : config.containerAlign === 'center' ? 'margin: 0 auto;' : 'margin: 0 0 0 auto;'}
      background-color: white;
      padding: ${cardMarginPx}px;
      display: flex;
      flex-wrap: wrap;
      gap: ${cardMarginPx * 2}px;
      ${config.containerAlign === 'left' ? 'justify-content: flex-start;' : config.containerAlign === 'center' ? 'justify-content: center;' : 'justify-content: flex-end;'}
    }
    h1 {
      text-align: center;
      color: #333;
      border-bottom: 3px solid #409EFF;
      padding-bottom: 10px;
      margin-bottom: 20px;
      width: 100%;
    }
    .summary {
      text-align: center;
      color: #666;
      margin-bottom: 20px;
      width: 100%;
    }
    .address-card {
      width: ${cardWidthPx}px;
      height: ${cardHeightPx}px;
      border: 2px solid #333;
      border-radius: 4px;
      padding: ${cardPaddingPx}px;
      background-color: white;
      page-break-inside: avoid;
      display: flex;
      flex-direction: column;
      justify-content: ${config.verticalAlign === 'top' ? 'flex-start' : config.verticalAlign === 'middle' ? 'center' : 'flex-end'};
      overflow: hidden;
    }
    .address-lines {
      line-height: ${config.lineHeight};
      color: #000;
      font-weight: 500;
      display: flex;
      flex-direction: column;
      justify-content: ${config.verticalAlign === 'top' ? 'flex-start' : config.verticalAlign === 'middle' ? 'center' : 'flex-end'};
      width: 100%;
      text-align: ${config.horizontalAlign};
    }
    .address-lines div {
      margin: 2px 0;
      width: 100%;
    }
    .print-button {
      position: fixed;
      top: 20px;
      right: 20px;
      padding: 10px 20px;
      background-color: #409EFF;
      color: white;
      border: none;
      border-radius: 5px;
      cursor: pointer;
      font-size: 14px;
      box-shadow: 0 2px 4px rgba(0,0,0,0.2);
      z-index: 1000;
    }
    .print-button:hover {
      background-color: #66b1ff;
    }
    @media print {
      body {
        background-color: white;
        margin: 0;
      }
      .container {
        width: 100%;
        max-width: none;
        padding: ${pageMarginMm}mm;
        gap: ${cardMarginMm}mm;
        display: flex;
        flex-wrap: wrap;
        align-content: flex-start;
        ${config.containerAlign === 'left' ? 'justify-content: flex-start;' : config.containerAlign === 'center' ? 'justify-content: center;' : 'justify-content: flex-end;'}
      }
      .address-card {
        width: ${cardWidthMm}mm !important;
        height: ${cardHeightMm}mm !important;
        padding: ${cardPaddingMm}mm;
        margin: ${cardMarginMm}mm;
        border: 1px solid #333;
        page-break-inside: avoid;
        page-break-after: ${config.addressesPerLabel === 1 ? 'always' : 'auto'};
        flex-shrink: 0;
        box-sizing: border-box;
        display: flex;
        flex-direction: column;
        justify-content: ${config.verticalAlign === 'top' ? 'flex-start' : config.verticalAlign === 'middle' ? 'center' : 'flex-end'};
      }
      ${config.addressesPerLabel === 3 ? '.address-card:nth-child(3n) { page-break-after: always; }' : ''}
      .address-card:last-child {
        page-break-after: auto;
      }
      .address-lines {
        display: flex;
        flex-direction: column;
        justify-content: ${config.verticalAlign === 'top' ? 'flex-start' : config.verticalAlign === 'middle' ? 'center' : 'flex-end'};
        width: 100%;
        text-align: ${config.horizontalAlign};
      }
      .address-lines div {
        width: 100%;
      }
      .print-button {
        display: none;
      }
      h1, .summary {
        display: none;
      }
      @page {
        size: ${cardWidthMm}mm ${config.addressesPerLabel === 3 ? cardHeightMm * 3 : cardHeightMm}mm;
        margin: 0;
      }
    }
  </style>
</head>
<body>
  <button class="print-button" onclick="window.print()">🖨️ Print</button>
  <div class="container">
    <h1>Order Shipping Addresses</h1>
    <p class="summary">
      Generated on ${new Date().toLocaleString()} | Total Orders: ${orders.length}
    </p>
`;

        // 遍历所有订单，生成地址卡片
        orders.forEach((order, index) => {
          // 格式化地址
          const formattedAddress = this.formatAddressLines(order);
          // 计算地址行数
          const lineCount = this.getAddressLineCount(order);
          // 根据行数和固定高度计算合适的字体大小
          const calculatedFontSize = this.calculateFontSize(lineCount, config);
          
          htmlContent += `
    <div class="address-card">
      <div class="address-lines" style="font-size: ${calculatedFontSize}px;">
        ${formattedAddress}
      </div>
    </div>
`;
        });

        htmlContent += `
  </div>
</body>
</html>
`;

        // 创建Blob并在新标签页中打开
        const blob = new Blob([htmlContent], { type: 'text/html;charset=utf-8' });
        const url = window.URL.createObjectURL(blob);
        const newWindow = window.open(url, '_blank');
        
        // 如果浏览器阻止了弹窗，则回退到下载方式
        if (!newWindow || newWindow.closed || typeof newWindow.closed === 'undefined') {
          const link = document.createElement('a');
          link.href = url;
          link.download = `order_addresses_${new Date().getTime()}.html`;
          document.body.appendChild(link);
          link.click();
          document.body.removeChild(link);
          this.$modal.msgWarning('Popup blocked. File downloaded instead. Please allow popups for this site.');
        } else {
          // 成功打开新标签页后，延迟释放 URL（给浏览器时间加载）
          setTimeout(() => {
            window.URL.revokeObjectURL(url);
          }, 1000);
          this.$modal.msgSuccess('Address HTML file opened in new tab');
        }
      },
      /** Get address line count */
      getAddressLineCount(order) {
        const addressLine2 = order.address_line2 || '';
        const addressLine3 = order.address_line3 || '';
        const hasLine2 = addressLine2 && addressLine2.trim() !== '';
        const hasLine3 = addressLine3 && addressLine3.trim() !== '';
        
        if (hasLine3) {
          return 5; // Name, Address Line 1, Address Line 2, Address Line 3, City State PostCode
        } else if (hasLine2) {
          return 4; // Name, Address Line 1, Address Line 2, City State PostCode
        } else {
          return 3; // Name, Address Line 1, City State PostCode
        }
      },
      /** Calculate font size based on line count and fixed card height */
      calculateFontSize(lineCount, config) {
        const mmToPx = 3.779527559;
        const cardHeightMm = config.cardHeight; // 固定高度，如 30mm
        const cardPaddingMm = config.cardPadding;
        const lineHeight = config.lineHeight;
        
        // 计算可用高度：总高度 - 上下padding - 行间距
        const cardPaddingPx = cardPaddingMm * mmToPx * 2; // 上下padding
        const marginSpacingPx = (lineCount - 1) * 2; // 行之间的间距（每个div的margin 2px）
        const availableHeightPx = (cardHeightMm * mmToPx) - cardPaddingPx - marginSpacingPx;
        
        // 根据行数和可用高度计算字体大小
        // 公式：fontSize = availableHeight / (lineCount * lineHeight)
        const calculatedFontSize = availableHeightPx / (lineCount * lineHeight);
        
        // 确保字体大小在合理范围内（最小6px，最大12px）
        // 30mm 高度下，需要更小的字体限制
        const minFontSize = 6;
        const maxFontSize = 12;
        return Math.max(minFontSize, Math.min(maxFontSize, Math.round(calculatedFontSize)));
      },
      /** Format address lines based on address line presence */
      formatAddressLines(order) {
        const receiverName = order.receiver_name || '';
        const addressLine1 = order.address_line1 || '';
        const addressLine2 = order.address_line2 || '';
        const addressLine3 = order.address_line3 || '';
        const city = order.city || '';
        const state = order.state || '';
        const postCode = order.post_code || '';
        
        // 构建最后一行：City State PostCode
        const lastLine = [city, state, postCode].filter(x => x).join(' ');
        
        // 检查各个地址行的存在性
        const hasLine2 = addressLine2 && addressLine2.trim() !== '';
        const hasLine3 = addressLine3 && addressLine3.trim() !== '';
        
        if (hasLine3) {
          // 5-Lines Format: Name, Address Line 1, Address Line 2, Address Line 3, City State PostCode
          return `
        <div>${receiverName}</div>
        <div>${addressLine1}</div>
        <div>${addressLine2}</div>
        <div>${addressLine3}</div>
        <div>${lastLine}</div>
          `.trim();
        } else if (hasLine2) {
          // 4-Lines Format: Name, Address Line 1, Address Line 2, City State PostCode
          return `
        <div>${receiverName}</div>
        <div>${addressLine1}</div>
        <div>${addressLine2}</div>
        <div>${lastLine}</div>
          `.trim();
        } else {
          // 3-Lines Format: Name, Address Line 1, City State PostCode
          return `
        <div>${receiverName}</div>
        <div>${addressLine1}</div>
        <div>${lastLine}</div>
          `.trim();
        }
      },
      /** Export button operation (deprecated, keeping for backward compatibility) */
      handleExport() {
        this.handleExportExcel();
      },
      /** Status change operation */
      handleStatusChange(row, status) {
        // 检查是否试图从Cancelled状态直接修改为Shipped
        if (row.status === 4 && status === 3) {
          this.$modal.msgError('Cannot change status from Cancelled to Shipped directly. Please change the status to Paid first.');
          return;
        }
        
        // 检查是否试图从Pending状态直接修改为Shipped
        if (row.status === 0 && status === 3) {
          this.$modal.msgError('Cannot change status from Pending to Shipped directly. Please change the status to Paid first.');
          return;
        }
        
        if (status === 3) { // 当点击查看图标时
          this.shippingForm.orderId = row.id;
          // 获取订单详情
          request({
            url: `/admin/mall/order/detail/${row.id}`,
            method: 'get'
          }).then(res => {
            // 设置运单号
            this.shippingForm.trackingNumber = res.data.shipping?.shippingNumber || '';
            this.shippingDialogVisible = true;
          });
        } else {
          const statusMap = {
            0: 'Pending',
            1: 'Paid',
            2: 'Fulfilled',
            3: 'Shipped',
            4: 'Cancelled',
            5: 'isDeleted'
          };
          const statusText = statusMap[status] || 'Unknown';
          this.$modal.confirm(`Are you sure to change the order status to "${statusText}"?`).then(() => {
            const form = {
              id: row.id,
              status: status,
              updateBy: this.$store.state.user.name
            };
            return updateOrder(form);
          }).then(() => {
            this.getList();
            this.$modal.msgSuccess("Status update successful");
          }).catch(() => {});
        }
      },
      /** Status filter button operation */
      handleStatusFilter(status) {
        // 重置isDispute参数，避免与status冲突
        this.queryParams.isDispute = undefined;
        if (status === undefined) {
          this.queryParams.status = '';  // 使用空字符串代替 undefined
        } else {
          this.queryParams.status = status;
        }
        this.handleQuery();
      },
      /** Disputes filter button operation */
      handleDisputesFilter() {
        // 重置status参数，避免与isDispute冲突
        this.queryParams.status = undefined;
        this.queryParams.isDispute = this.queryParams.isDispute === 1 ? undefined : 1;
        this.queryParams.pageNum = 1;
        this.getList();
      },
      /** Mark selected orders as paid */
      handleStatusUpdate() {
        if (this.selectedOrders.length === 0) {
          this.$modal.msgError("Please select at least one order");
          return;
        }

        // 获取目标状态
        let targetStatus;
        let confirmMessage;
        
        if (this.selectedOrders.every(order => order.status === 0)) {
          targetStatus = 1;
          confirmMessage = 'Are you sure to mark the selected orders as paid?';
        } else if (this.selectedOrders.every(order => order.status === 1)) {
          targetStatus = 2;
          confirmMessage = 'Are you sure to mark the selected orders as fulfilled?';
        } else if (this.selectedOrders.every(order => order.status === 2)) {
          targetStatus = 3;
          confirmMessage = 'Are you sure to mark the selected orders as shipped?';
        } else if (this.selectedOrders.every(order => order.status === 4)) {
          targetStatus = 1;
          confirmMessage = 'Are you sure to reactivate the cancelled orders and mark them as paid?';
        } else {
          this.$modal.msgError("Selected orders have different statuses");
          return;
        }
        
        // 检查是否有Cancelled订单试图修改为Shipped
        if (targetStatus === 3 && this.selectedOrders.some(order => order.status === 4)) {
          this.$modal.msgError("Cannot change status from Cancelled to Shipped directly. Please change the status to Paid first.");
          return;
        }
        
        // 检查是否有Pending订单试图修改为Shipped
        if (targetStatus === 3 && this.selectedOrders.some(order => order.status === 0)) {
          this.$modal.msgError("Cannot change status from Pending to Shipped directly. Please change the status to Paid first.");
          return;
        }

        this.$modal.confirm(confirmMessage).then(() => {
          this.loading = true;
          const promises = this.selectedOrders.map(order => {
            // ✅ 只传递后端Order对象需要的字段，避免JSON反序列化错误
            const form = {
              id: order.id,
              orderSn: order.order_sn,
              memberId: order.member_id,
              status: targetStatus,
              totalAmount: order.total_amount,
              sourceType: order.channel,
              couponId: order.coupon_id,
              couponAmount: order.coupon_amount,
              discountAmount: order.discount_amount,
              freightAmount: order.freight_amount,
              remark: order.remark,
              updateBy: this.$store.state.user.name
            };
            return updateOrder(form).catch(error => {
              console.error('Error updating order:', error);
              this.$modal.msgError(`Failed to update order ${order.id}`);
              return Promise.reject(error);
            });
          });
          return Promise.all(promises);
        }).then(() => {
          this.getList();
          this.$modal.msgSuccess("Successfully updated order status");
        }).catch(error => {
          console.error('Batch update error:', error);
          this.$modal.msgError("Failed to update some orders");
        }).finally(() => {
          this.loading = false;
        });
      },
      /** Batch delete (close) selected orders */
      handleBatchDelete() {
        if (this.ids.length === 0) {
          this.$modal.msgError("Please select at least one order");
          return;
        }
        this.$modal.confirm('Are you sure to close the selected orders?').then(() => {
          const promises = this.ids.map(id => {
            const form = {
              id: id,
              status: 5, // 5 represents "Closed" status
              updateBy: this.$store.state.user.name
            };
            return updateOrder(form);
          });
          return Promise.all(promises);
        }).then(() => {
          this.getList();
          this.$modal.msgSuccess("Successfully closed selected orders");
        }).catch(() => {});
      },
      /** New order button operation */
      handleNewOrder() {
        //this.$router.push('/hide/vorder/neworder');
        this.$router.push({ path: '/hide/vorder/neworder'});
      },
      handleMarkAsShipped(row) {
        // 检查是否是Cancelled状态的订单
        if (row.status === 4) {
          this.$modal.msgError('Cannot change status from Cancelled to Shipped directly. Please change the status to Paid first.');
          return;
        }
        
        // 检查是否是Pending状态的订单
        if (row.status === 0) {
          this.$modal.msgError('Cannot change status from Pending to Shipped directly. Please change the status to Paid first.');
          return;
        }
        
        // 先更新运单号
        request({
          url: '/admin/mall/order/updateShippingInfo',
          method: 'post',
          data: {
            orderId: row.id,
            shippingNumber: row.trackingNumber
          }
        }).then(res => {
          if (res.code === 200) {
            // 更新订单状态为已发货
            const form = {
              id: row.id,
              status: 3, // 已发货状态
              updateBy: this.$store.state.user.name
            };
            return updateOrder(form);
          } else {
            this.$message.error('Failed to update shipping information');
            return Promise.reject();
          }
        }).then(() => {
          this.$message.success('Order marked as shipped successfully');
          this.shippingDialogVisible = false;
          
          // 先刷新列表，然后在下一个tick中设置焦点
          this.getList().then(() => {
            // 在列表刷新后设置焦点到下一个订单
            this.$nextTick(() => {
              this.focusNextTrackingInput(row.id);
            });
          });
        }).catch(error => {
          this.$message.error('Failed to update order status');
          console.error('Error updating order:', error);
        });
      },
      
      /** 聚焦到下一行tracking number输入框 */
      focusNextTrackingInput(currentIdentifier, isIndex = false) {
        let currentIndex = -1;
        if (isIndex) {
          currentIndex = currentIdentifier;
        } else {
          currentIndex = this.orderList.findIndex(order => order.id === currentIdentifier);
        }
        if (currentIndex === -1) {
          return;
        }
        let targetOrder = null;
        if (isIndex) {
          targetOrder = this.orderList[currentIndex + 1];
        } else {
          for (let i = currentIndex + 1; i < this.orderList.length; i += 1) {
            if (this.orderList[i].status !== 3) {
              targetOrder = this.orderList[i];
              break;
            }
          }
        }
        if (!targetOrder) {
          return;
        }
        const refName = `trackingInput_${targetOrder.id}`;
        this.$nextTick(() => {
          let inputRef = this.$refs[refName];
          if (Array.isArray(inputRef)) {
            inputRef = inputRef[0];
          }
          if (inputRef && typeof inputRef.focus === 'function') {
            inputRef.focus();
          } else if (inputRef && inputRef.$el) {
            const inputEl = inputRef.$el.querySelector('input');
            if (inputEl) {
              inputEl.focus();
            }
          }
        });
      },
      
      /** 批量标记为已发货 */
      handleBulkMarkAsShipped() {
        // 获取所有填写了tracking number的Fulfilled订单
        const ordersToShip = this.orderList.filter(order => 
          order.trackingNumber && 
          order.trackingNumber.trim() !== '' &&
          order.status == 2 // 使用宽松比较处理Fulfilled状态
        );
        
        if (ordersToShip.length === 0) {
          this.$modal.msgError('No orders with tracking numbers to ship');
          return;
        }
        
        this.$modal.confirm(
          `Are you sure to mark ${ordersToShip.length} order(s) as shipped?`
        ).then(() => {
          this.loading = true;
          
          // 创建所有更新请求
          const updatePromises = ordersToShip.map(order => {
            // 先更新运单号
            return request({
              url: '/admin/mall/order/updateShippingInfo',
              method: 'post',
              data: {
                orderId: order.id,
                shippingNumber: order.trackingNumber
              }
            }).then(res => {
              if (res.code === 200) {
                // 更新订单状态为已发货
                const form = {
                  id: order.id,
                  status: 3, // 已发货状态
                  updateBy: this.$store.state.user.name
                };
                return updateOrder(form);
              } else {
                console.error(`Failed to update shipping info for order ${order.order_sn}`);
                return Promise.reject(new Error(`Failed to update order ${order.order_sn}`));
              }
            }).catch(error => {
              console.error(`Error processing order ${order.order_sn}:`, error);
              return Promise.reject(error);
            });
          });
          
          // 执行所有更新
          return Promise.all(updatePromises);
        }).then(() => {
          this.$modal.msgSuccess(`Successfully marked ${ordersToShip.length} order(s) as shipped`);
          // 刷新列表
          this.getList();
        }).catch(error => {
          if (error !== 'cancel') {
            console.error('Bulk update error:', error);
            this.$modal.msgError('Some orders failed to update. Please check and try again.');
          }
        }).finally(() => {
          this.loading = false;
        });
      },
      /** 从 remark 中提取自定义渠道名称 */
      extractCustomChannel(remark) {
        if (!remark) return 'Other';
        const match = remark.match(/Custom Channel:\s*(.+?)(?:\s*\[|$)/);
        return match ? match[1].trim() : 'Other';
      }
    }
  };
  </script>
  
  <style scoped>
  .status-filter-container {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 15px;
  }
  .el-button-group {
    margin-right: 0;
  }
  .el-button-group .el-button {
    margin-right: 0;
  }
  .search-form {
    transition: all 0.3s ease;
    overflow: hidden;
  }
  .search-container {
    display: flex;
    justify-content: flex-end;
    margin-bottom: 15px;
  }
  /* HTML导出配置对话框样式 */
  .html-export-form .el-form-item__label {
    white-space: nowrap;
    overflow: visible;
    text-overflow: clip;
  }
  /* 批量发货按钮容器样式 */
  .bulk-shipped-container {
    position: fixed;
    bottom: 30px;
    left: 50%;
    transform: translateX(-50%);
    z-index: 1000;
    animation: slideUp 0.3s ease-out;
  }
  .bulk-shipped-button {
    padding: 15px 40px;
    font-size: 16px;
    font-weight: 600;
    box-shadow: 0 4px 12px rgba(103, 194, 58, 0.4);
    border-radius: 8px;
  }
  .bulk-shipped-button:hover {
    box-shadow: 0 6px 16px rgba(103, 194, 58, 0.5);
    transform: translateY(-2px);
    transition: all 0.3s ease;
  }
  @keyframes slideUp {
    from {
      bottom: -100px;
      opacity: 0;
    }
    to {
      bottom: 30px;
      opacity: 1;
    }
  }
  </style>
  