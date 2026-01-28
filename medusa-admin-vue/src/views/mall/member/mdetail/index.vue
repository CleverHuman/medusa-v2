<template>
  <div class="member-detail-container">
    <!-- Top Bar -->
    <div class="top-bar">
      <el-button type="text" icon="el-icon-arrow-left" class="back-btn" @click="$router.back()">Back to customer list</el-button>
      <el-button type="danger" class="data-clear-btn" @click="handleDataClear">Data Clear</el-button>
    </div>

    <div class="main-content">
      <!-- Left Column -->
      <div class="left-col">
        <!-- Customer Card -->
        <el-card class="customer-card">
          <div class="customer-header">
            <div class="avatar-section">
              <el-avatar size="large" icon="el-icon-user" />
              <div class="customer-info">
                <div class="customer-name">{{ memberInfo.username || 'Loading...' }}</div>
                <div class="customer-id" v-if="linkedAccountForm.currentLinkedAccount">
                  Linked Account: {{ linkedAccountForm.currentLinkedAccount.username }}
                </div>
                <div class="customer-id" v-else>
                  Telegram: @{{ memberInfo.tgUsername || 'N/A' }}
                </div>
                <el-tag :type="memberInfo.levelName" size="mini">{{ memberInfo.levelName }}</el-tag>
                <el-tag type="danger" size="mini" v-if="memberInfo.pcspStatus === 1" style="margin-left: 4px;">Premium Customer Service Package</el-tag>
              </div>
            </div>
            <div class="customer-meta">
              <div>PCSP Expiry<br><span>{{ memberInfo.pcspExpiryDate ? parseTime(memberInfo.pcspExpiryDate, '{y}-{m}-{d}') : 'N/A' }}</span></div>
              <div>First Joined<br><span>{{ memberInfo.createTime ? parseTime(memberInfo.createTime, '{y}-{m}-{d}') : 'N/A' }}</span></div>
              <div>Last Seen<br><span>{{ memberInfo.lastSeen ? parseTime(memberInfo.lastSeen, '{y}-{m}-{d}') : 'N/A' }}</span></div>
              <div>{{ getPreviousYear() }} Point<br><span>{{ memberInfo.lastPoint || 0 }}</span></div>
              <div>Customer Level<br><span>{{ memberInfo.levelName }}</span></div>
              <div>{{ new Date().getFullYear() }} Point<br><span>{{ memberInfo.currentPoint || 0 }}</span></div>
              <div>Package<br><span>{{ memberInfo.totalOrders || 0 }} orders</span></div>
            </div>
          </div>
        </el-card>

        <!-- Add Point Manually -->
        <el-card class="add-point-card">
          <div class="section-title">Adjust Points Manually</div>
          <el-form :model="pointForm" :rules="pointRules" ref="pointForm" label-width="100px">
            <el-form-item label="Operation" prop="operation">
              <el-radio-group v-model="pointForm.operation">
                <el-radio :label="1">Add Points</el-radio>
                <el-radio :label="2">Deduct Points</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="Year" prop="year">
              <el-select v-model="pointForm.year" placeholder="Select year" style="width: 100%">
                <el-option 
                  v-for="year in availableYears" 
                  :key="year" 
                  :label="year" 
                  :value="year"
                />
              </el-select>
              <div style="font-size: 12px; color: #909399; margin-top: 4px;">
                Customer level is renewed on Jan 1st each year based on previous year's expenditure
              </div>
            </el-form-item>
            <el-form-item label="Note" prop="note">
              <el-input v-model="pointForm.note" placeholder="Enter note" />
            </el-form-item>
            <el-form-item label="Platform" prop="platform">
              <el-select v-model="pointForm.platform" placeholder="Select platform" style="width: 100%">
                <el-option label="OS" :value="0" />
                <el-option label="TG" :value="1" />
              </el-select>
            </el-form-item>
            <el-form-item label="Amount (AUD)" prop="amount">
              <el-input-number v-model="pointForm.amount" :precision="2" :min="0" style="width: 100%" />
            </el-form-item>
            <div class="form-actions">
              <el-button type="primary" @click="handleAddPoint" :loading="pointLoading">Save</el-button>
              <el-button @click="resetPointForm">Cancel</el-button>
            </div>
          </el-form>
        </el-card>

        <!-- Point History Table -->
        <el-card class="manual-point-table">
          <div class="section-title">Point History</div>
          <el-table :data="pointHistory" size="small" v-loading="pointLoading">
            <el-table-column label="Date Created" prop="createTime" width="180">
              <template slot-scope="scope">
                <span>{{ scope.row.createTime ? formatDate(scope.row.createTime) : 'N/A' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="Year" prop="year" width="80">
              <template slot-scope="scope">
                <el-tag size="mini" type="info">{{ scope.row.year || 'N/A' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="Platform" prop="platform" width="100">
              <template slot-scope="scope">
                <el-tag :type="scope.row.platform === 0 ? 'primary' : scope.row.platform === 1 ? 'success' : 'info'">
                  {{ scope.row.platform === 0 ? 'OS' : scope.row.platform === 1 ? 'TG' : 'OS/TG' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="Amount (AUD)" prop="amount" width="120">
              <template slot-scope="scope">
                <span :style="{ color: scope.row.amount >= 0 ? '#67C23A' : '#F56C6C' }">
                  {{ scope.row.amount >= 0 ? '+' : '' }}${{ scope.row.amount }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="Points" prop="points" width="100">
              <template slot-scope="scope">
                <span :style="{ color: scope.row.points >= 0 ? '#67C23A' : '#F56C6C', fontWeight: 'bold' }">
                  {{ scope.row.points >= 0 ? '+' : '' }}{{ scope.row.points }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="Note" prop="note" />
          </el-table>
          <div v-if="pointHistory.length === 0" style="text-align: center; padding: 20px; color: #999;">
            No point history found
          </div>

        </el-card>
      </div>

      <!-- Right Column -->
      <div class="right-col">
        <!-- Admin Comment -->
        <el-card class="admin-comment-card">
          <div class="section-title">Admin comment</div>
          <el-input 
            type="textarea" 
            rows="4" 
            placeholder="Comment" 
            v-model="commentForm.comment"
          />
          <div class="form-actions">
            <el-button type="primary" @click="handleSaveComment" :loading="commentLoading">Save Changes</el-button>
            <el-button 
              :type="memberInfo.status === 2 ? 'success' : 'warning'" 
              @click="handleSuspend" 
              :loading="suspendLoading"
            >
              {{ memberInfo.status === 2 ? 'Activate' : 'Suspend' }}
            </el-button>
            <el-button type="danger" @click="handleDelete" :loading="deleteLoading">Delete</el-button>
          </div>
        </el-card>

        <!-- Linked Account -->
        <el-card class="linked-account-card">
          <div class="section-title">Linked account</div>
          
          <!-- 当前关联的账号 -->
          <div v-if="linkedAccountForm.currentLinkedAccount" class="current-linked-account">
            <div class="section-subtitle">Currently linked to:</div>
            <div class="linked-account-info">
              <div class="linked-account-name">{{ linkedAccountForm.currentLinkedAccount.username }}</div>
              <div class="linked-account-details">
                <span>Member ID: {{ linkedAccountForm.currentLinkedAccount.memberId }}</span>
                <span v-if="linkedAccountForm.currentLinkedAccount.tgUsername">TG: @{{ linkedAccountForm.currentLinkedAccount.tgUsername }}</span>
              </div>
              <div class="linked-account-note">
                <el-tag size="mini" type="info">Bidirectional Link</el-tag>
              </div>
            </div>
          </div>
          
          <!-- 没有关联账号时显示 -->
          <div v-else class="no-linked-account">
            <div class="section-subtitle">No linked account</div>
            <div class="no-account-message">This member is not linked to any other account.</div>
          </div>
          
          <!-- 搜索框 -->
          <el-input
            v-model="linkedAccountForm.searchKeyword"
            placeholder="Search account to link"
            prefix-icon="el-icon-search"
            clearable
            @input="handleSearchAccount"
            style="margin-top: 15px;"
          />
          
          <!-- 搜索结果 -->
          <div v-if="linkedAccountForm.searchResults.length > 0" class="search-results">
            <div class="search-result-item" v-for="member in linkedAccountForm.searchResults" :key="member.memberId">
              <el-checkbox 
                v-model="member.selected"
                @change="handleSelectAccount(member)"
              >
                {{ member.username }}
              </el-checkbox>
            </div>
          </div>
          
          <div class="form-actions">
            <el-button 
              type="primary" 
              @click="handleSaveLinkedAccount" 
              :loading="linkedAccountLoading"
              :disabled="!linkedAccountForm.selectedAccount"
            >
              Save Changes
            </el-button>
            <el-button 
              type="success" 
              @click="handleMergeAccounts" 
              :loading="mergeLoading"
              :disabled="!linkedAccountForm.currentLinkedAccount"
            >
              Merge Accounts
            </el-button>
            <el-button 
              type="danger" 
              @click="handleDeleteLinkedAccount" 
              :loading="deleteLinkedAccountLoading"
              :disabled="!linkedAccountForm.currentLinkedAccount"
            >
              Delete
            </el-button>
          </div>
        </el-card>
      </div>
    </div>

    <!-- Order History -->
    <el-card class="order-history-card">
      <div class="section-title">Order History</div>
      <div v-if="linkedAccountForm.currentLinkedAccount" class="order-history-notice">
        <el-alert
          title="Order History includes orders from both current account and linked account"
          type="info"
          :closable="false"
          show-icon
        >
          <template slot="default">
            <div>Current Account: <strong>{{ memberInfo.username }}</strong></div>
            <div>Linked Account: <strong>{{ linkedAccountForm.currentLinkedAccount.username }}</strong></div>
          </template>
        </el-alert>
      </div>
      <el-input placeholder="Search" class="order-search" v-model="orderQuery.orderSn" @keyup.enter.native="fetchOrders" />
      <el-tabs v-model="orderQuery.status" type="card" @tab-click="handleTabChange">
        <el-tab-pane label="Pending Orders" :name="'0'" />
        <el-tab-pane label="Paid Orders" :name="'1'" />
        <el-tab-pane label="Fulfilled Orders" :name="'2'" />
        <el-tab-pane label="Shipped Orders" :name="'3'" />
        <el-tab-pane label="Disputes" :name="'4'" />
      </el-tabs>
      <el-table :data="orderList" size="small" style="margin-top: 10px;">
        <el-table-column type="selection" width="40" />
        <el-table-column prop="order_sn" label="Order#" width="120">
          <template slot-scope="scope">
            <el-link type="primary" @click="handleView(scope.row)">{{ scope.row.order_sn }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="create_time" label="Date" width="100">
          <template slot-scope="scope">
            <!-- 调试信息 -->
            <div style="font-size: 12px;">
              <div>{{ scope.row.create_time ? formatDate(scope.row.create_time, '{y}-{m}-{d}') : 'N/A' }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="channel" label="Channel" width="80">
          <template slot-scope="scope">
            <dict-tag :options="dict.type.source_type" :value="scope.row.channel"/>
          </template>
        </el-table-column>
        <el-table-column label="Account" width="100">
          <template slot-scope="scope">
            <el-tag 
              :type="isCurrentAccountOrder(scope.row) ? 'primary' : 'success'" 
              size="mini"
            >
              {{ isCurrentAccountOrder(scope.row) ? 'Current' : 'Linked' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="username" label="Username" width="100" />
        <el-table-column prop="procode" label="ProCode" width="100">
          <template slot-scope="scope">
            <span v-html="(scope.row.procode || '').replace(/;/g, '<br>')"></span>
          </template>
        </el-table-column>
        <el-table-column prop="amt" label="Amt" width="80">
          <template slot-scope="scope">
            <span v-html="(scope.row.amt || '').toString().replace(/;/g, '<br>')"></span>
          </template>
        </el-table-column>
        <el-table-column prop="qty" label="Qty" width="60">
          <template slot-scope="scope">
            <span v-html="(scope.row.qty || '').toString().replace(/;/g, '<br>')"></span>
          </template>
        </el-table-column>
        <el-table-column prop="postage" label="Postage" width="80" />
        <el-table-column label="Full name & Address" min-width="250">
          <template slot-scope="scope">
            <div>
              <div><b>{{ scope.row.receiver_name }}</b></div>
              <div>{{ scope.row.address_line1 }}</div>
              <div v-if="scope.row.address_line2">{{ scope.row.address_line2 }}</div>
              <div>
                {{ scope.row.city }}, {{ scope.row.state }} {{ scope.row.post_code }}
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="tcoin" label="Total Coin" width="80">
          <template slot-scope="scope">
            <span v-html="(scope.row.tcoin || '').toString().replace(/;/g, '<br>')"></span>
          </template>
        </el-table-column>
        <el-table-column prop="paidcoin" label="Paid Coin" width="80">
          <template slot-scope="scope">
            <span v-html="(scope.row.paidcoin || '').toString().replace(/;/g, '<br>')"></span>
          </template>
        </el-table-column>
        <el-table-column prop="tamt" label="Total AUD" width="80">
          <template slot-scope="scope">
            <span v-html="(scope.row.tamt || '').toString().replace(/;/g, '<br>')"></span>
          </template>
        </el-table-column>
        <el-table-column prop="cointype" label="Coin Type" width="80">
          <template slot-scope="scope">
            <dict-tag :options="dict.type.pay_type" :value="scope.row.cointype"/>
          </template>
        </el-table-column>
        <el-table-column label="Order Status" prop="status" width="100">
          <template slot-scope="scope">
            <dict-tag :options="dict.type.order_status" :value="scope.row.status"/>
          </template>
        </el-table-column>
      </el-table>
      <pagination
        v-show="orderTotal>0"
        :total="orderTotal"
        :page.sync="orderQuery.pageNum"
        :limit.sync="orderQuery.pageSize"
        @pagination="fetchOrders"
      />
    </el-card>
  </div>
</template>

<script>
import { listVOrder } from '@/api/mall/vorder';
import { getMember, addComment, suspendMembers, resumeMembers, delMembers, getPointHistory, addPointHistory, updateLinkedAccount, removeLinkedAccount, searchMembers, getMemberByLinkedAccount, mergeAccounts } from '@/api/mall/member';
import { parseTime } from '@/utils/index';
//import { formatDate } from '../../../../utils';

export default {
  name: 'MemberDetail',
  dicts: ['order_status', 'source_type', 'pay_type'],
  data() {
    return {
      memberId: null,
      orderList: [],
      orderTotal: 0,
      orderQuery: {
        memberId: '',
        orderSn: '',
        status: '',
        isDispute: null,
        pageNum: 1,
        pageSize: 10
      },
      memberInfo: {
        username: '',
        tgUsername: '',
        status: '',
        createTime: '',
        updateTime: '',
        currentLevel: '',
        totalOrders: 0,
        remark: ''
      },
      pointForm: {
        operation: 1, // 1=Add, 2=Deduct
        year: new Date().getFullYear(), // 默认当前年份
        note: '',
        platform: 0,
        amount: 0
      },
      pointRules: {
        operation: [{ required: true, message: 'Please select an operation', trigger: 'change' }],
        year: [{ required: true, message: 'Please select a year', trigger: 'change' }],
        note: [{ required: true, message: 'Please enter a note', trigger: 'blur' }],
        platform: [{ required: true, message: 'Please select a platform', trigger: 'change' }],
        amount: [{ required: true, message: 'Please enter an amount', trigger: 'blur' }]
      },
      availableYears: [], // 可选年份列表
      pointLoading: false,
      pointHistory: [],
      commentForm: {
        comment: ''
      },
      commentLoading: false,
      suspendLoading: false,
      deleteLoading: false,
      linkedAccountForm: {
        searchKeyword: '',
        searchResults: [],
        selectedAccount: null,
        currentLinkedAccount: null
      },
      linkedAccountLoading: false,
      deleteLinkedAccountLoading: false,
      mergeLoading: false
    };
  },
  created() {
    // 初始化可选年份列表（当前年份 + 前1年）
    const currentYear = new Date().getFullYear();
    this.availableYears = [];
    for (let i = 0; i <= 1; i++) {
      this.availableYears.push(currentYear - i);
    }
    
    // 从路由参数中获取 memberId，并添加验证
    const memberIdParam = this.$route.query.memberId;
    
    console.log('=== 从路由获取的 memberId ===');
    console.log('memberId:', memberIdParam);
    console.log('============================');
    
    // 验证 memberId 参数
    if (!memberIdParam) {
      this.$message.error('Missing member ID');
      this.$router.push('/hide/member');
      return;
    }
    
    // 转换为数字并验证
    const memberId = parseInt(memberIdParam);
    if (isNaN(memberId) || memberId <= 0) {
      this.$message.error('Invalid member ID');
      this.$router.push('/hide/member');
      return;
    }
    
    this.memberId = memberId;
    this.orderQuery.memberId = memberId;
    
    // 获取会员信息和相关数据
    this.fetchMemberInfo();
    this.fetchOrders();
    this.fetchPointHistory();
  },
  methods: {
    // 获取会员信息
    fetchMemberInfo() {
      getMember(this.memberId).then(response => {
        if (response.code === 200) {
          this.memberInfo = response.data || {};
          this.commentForm.comment = this.memberInfo.remark || '';
          
          // 添加详细的调试信息
          console.log('=== 会员信息调试 ===');
          console.log('memberInfo:', this.memberInfo);
          console.log('linkedAccount:', this.memberInfo.linkedAccount);
          console.log('========================');
          
          // 获取关联账号信息
          if (this.memberInfo.linkedAccount) {
            console.log('=== 开始获取关联账号信息 ===');
            console.log('linkedAccount ID:', this.memberInfo.linkedAccount);
            
            // 直接通过ID获取关联账号信息，而不是通过linked_account字段查询
            getMember(this.memberInfo.linkedAccount).then(linkedResponse => {
              console.log('=== 关联账号API响应 ===');
              console.log('linkedResponse:', linkedResponse);
              
              if (linkedResponse.code === 200 && linkedResponse.data) {
                this.linkedAccountForm.currentLinkedAccount = linkedResponse.data;
                console.log('=== 设置关联账号成功 ===');
                console.log('currentLinkedAccount:', this.linkedAccountForm.currentLinkedAccount);
              } else {
                console.log('=== 关联账号API响应异常 ===');
                this.linkedAccountForm.currentLinkedAccount = null;
              }
            }).catch(error => {
              console.error('=== 获取关联账号信息失败 ===');
              console.error('error:', error);
              this.linkedAccountForm.currentLinkedAccount = null;
            });
          } else {
            console.log('=== 没有关联账号 ===');
            this.linkedAccountForm.currentLinkedAccount = null;
          }
        } else {
          this.$message.error('Failed to load member information');
        }
      }).catch(error => {
        console.error('Failed to fetch member info:', error);
        this.$message.error('Failed to load member information');
      });
    },
    
    fetchOrders() {
      if (!this.memberId) {
        console.error('memberId is not available');
        return;
      }
      
      this.orderQuery.memberId = this.memberId;
      console.log('=== 前端传递的参数 ===');
      console.log('memberId:', this.memberId);
      console.log('orderQuery:', this.orderQuery);
      console.log('========================');
      
      listVOrder(this.orderQuery).then(res => {
        this.orderList = res.rows || [];
        this.orderTotal = res.total || 0;
      }).catch(error => {
        console.error('Failed to fetch orders:', error);
        this.$message.error('Failed to load order history');
      });
    },
    
    handleTabChange(tab) {
      this.orderQuery.status = tab.name;
      this.orderQuery.pageNum = 1;
      
      // 如果是Disputes标签页，设置isDispute参数
      if (tab.name === '4') {
        this.orderQuery.isDispute = 1;
      } else {
        this.orderQuery.isDispute = null;
      }
      
      this.fetchOrders();
    },
    
    handleView(row) {
      this.$router.push({ path: '/hide/vorder/detail', query: { id: row.id }});
    },
    
    goBack() {
      this.$router.push('/hide/member');
    },
    
    handleDataClear() {
      this.$confirm('Are you sure you want to clear all data for this member?', 'Warning', {
        confirmButtonText: 'Yes',
        cancelButtonText: 'No',
        type: 'warning'
      }).then(() => {
        // TODO: Implement actual data clearing logic
        this.$message.success('Data cleared successfully!');
      }).catch(() => {
        // User cancelled
      });
    },
    
    // 获取积分历史记录
    fetchPointHistory() {
      console.log('=== 获取积分历史记录 ===');
      console.log('memberId:', this.memberId);
      
      getPointHistory(this.memberId).then(response => {
        console.log('积分历史记录响应:', response);
        if (response.code === 200) {
          this.pointHistory = response.data || [];
          console.log('积分历史记录数据:', this.pointHistory);
          

        } else {
          console.error('获取积分历史记录失败:', response.msg);
          this.$message.error('Failed to load point history');
        }
      }).catch(error => {
        console.error('获取积分历史记录异常:', error);
        this.$message.error('Failed to load point history');
      });
    },
    
    // 添加积分记录
    handleAddPoint() {
      this.$refs.pointForm.validate(valid => {
        if (valid) {
          this.pointLoading = true;
          
          // 根据操作类型计算实际金额（减少积分时使用负数）
          let actualAmount = parseFloat(this.pointForm.amount);
          if (this.pointForm.operation === 2) {
            actualAmount = -actualAmount; // 减少积分时转为负数
          }
          
          const data = {
            memberId: this.memberId,
            amount: actualAmount,
            year: this.pointForm.year, // 添加年份参数
            note: this.pointForm.note,
            platform: this.pointForm.platform
          };
          
          console.log('=== 添加积分记录 ===');
          console.log('操作类型:', this.pointForm.operation === 1 ? 'Add Points' : 'Deduct Points');
          console.log('年份:', this.pointForm.year);
          console.log('原始金额:', this.pointForm.amount);
          console.log('实际金额:', actualAmount);
          console.log('请求数据:', data);
          
          addPointHistory(data).then(response => {
            console.log('添加积分记录响应:', response);
            if (response.code === 200) {
              // 保存当前等级信息用于比较
              const oldLevel = this.memberInfo.levelName;
              const oldLastPoint = this.memberInfo.lastPoint || 0;
              const addedYear = this.pointForm.year;
              const previousYear = new Date().getFullYear() - 1;
              
              const operationText = this.pointForm.operation === 1 ? 'added' : 'deducted';
              this.$message.success(`Points ${operationText} successfully`);
              
              // 重置表单
              this.pointForm = {
                operation: 1, // 重置为Add Points
                year: new Date().getFullYear(), // 重置为当前年份
                note: '',
                platform: 0,
                amount: 0
              };
              this.fetchPointHistory(); // 刷新积分历史记录
              this.fetchMemberInfo(); // 刷新会员信息以显示更新后的积分
              
              // 检查等级是否发生变化（仅当添加的是去年的积分时才检查等级变化）
              if (addedYear === previousYear) {
                setTimeout(() => {
                  if (this.memberInfo.levelName !== oldLevel) {
                    const newLastPoint = this.memberInfo.lastPoint || 0;
                    if (newLastPoint > oldLastPoint) {
                      this.$message.success(`Member level upgraded from ${oldLevel} to ${this.memberInfo.levelName}!`);
                    } else {
                      this.$message.warning(`Member level downgraded from ${oldLevel} to ${this.memberInfo.levelName}`);
                    }
                  }
                }, 500);
              }
            } else {
              console.error('添加积分记录失败:', response.msg);
              this.$message.error('Failed to adjust points');
            }
            this.pointLoading = false;
          }).catch(error => {
            console.error('添加积分记录异常:', error);
            this.$message.error('Failed to adjust points');
            this.pointLoading = false;
          });
        }
      });
    },
    
    resetPointForm() {
      this.$refs.pointForm.resetFields();
    },
    
    handleSaveComment() {
      if (!this.commentForm.comment.trim()) {
        this.$message.warning('Please enter a comment');
        return;
      }
      
      this.commentLoading = true;
      addComment(this.memberId, this.commentForm.comment).then(response => {
        if (response.code === 200) {
          this.$message.success('Comment saved successfully!');
          this.fetchMemberInfo(); // Refresh member info to get updated comment
        } else {
          this.$message.error('Failed to save comment');
        }
        this.commentLoading = false;
      }).catch(error => {
        console.error('Failed to save comment:', error);
        this.$message.error('Failed to save comment');
        this.commentLoading = false;
      });
    },
    
    handleSuspend() {
      const isSuspended = this.memberInfo.status === 2;
      const action = isSuspended ? 'activate' : 'suspend';
      const confirmMessage = isSuspended 
        ? 'Are you sure you want to activate this member?' 
        : 'Are you sure you want to suspend this member?';
      
      this.$confirm(confirmMessage, 'Warning', {
        confirmButtonText: 'Yes',
        cancelButtonText: 'No',
        type: 'warning'
      }).then(() => {
        this.suspendLoading = true;
        
        const apiCall = isSuspended ? resumeMembers([this.memberId]) : suspendMembers([this.memberId]);
        
        apiCall.then(response => {
          if (response.code === 200) {
            const successMessage = isSuspended 
              ? 'Member activated successfully!' 
              : 'Member suspended successfully!';
            this.$message.success(successMessage);
            this.fetchMemberInfo(); // Refresh member info
          } else {
            const errorMessage = isSuspended 
              ? 'Failed to activate member' 
              : 'Failed to suspend member';
            this.$message.error(errorMessage);
          }
          this.suspendLoading = false;
        }).catch(error => {
          console.error(`Failed to ${action} member:`, error);
          const errorMessage = isSuspended 
            ? 'Failed to activate member' 
            : 'Failed to suspend member';
          this.$message.error(errorMessage);
          this.suspendLoading = false;
        });
      }).catch(() => {
        // User cancelled
      });
    },
    
    handleDelete() {
      this.$confirm('Are you sure you want to delete this member? This action cannot be undone.', 'Warning', {
        confirmButtonText: 'Yes',
        cancelButtonText: 'No',
        type: 'warning'
      }).then(() => {
        this.deleteLoading = true;
        delMembers([this.memberId]).then(response => {
          if (response.code === 200) {
            this.$message.success('Member deleted successfully!');
            this.$router.push('/hide/member'); // Redirect to member list after deletion
          } else {
            this.$message.error('Failed to delete member');
          }
          this.deleteLoading = false;
        }).catch(error => {
          console.error('Failed to delete member:', error);
          this.$message.error('Failed to delete member');
          this.deleteLoading = false;
        });
      }).catch(() => {
        // User cancelled
      });
    },
    
    handleSaveLinkedAccount() {
      this.linkedAccountLoading = true;
      if (!this.linkedAccountForm.selectedAccount) {
        this.$message.warning('Please select an account to link.');
        this.linkedAccountLoading = false;
        return;
      }
      
      // 检查是否选择自己
      if (this.linkedAccountForm.selectedAccount === this.memberId) {
        this.$message.warning('Cannot link account to itself.');
        this.linkedAccountLoading = false;
        return;
      }
      
      // 确认双向关联操作
      this.$confirm(
        'This will create a bidirectional link between the two accounts. Both accounts will be linked to each other. Continue?', 
        'Bidirectional Account Link Confirmation', 
        {
          confirmButtonText: 'Yes, Link Accounts',
          cancelButtonText: 'Cancel',
          type: 'warning'
        }
      ).then(() => {
        updateLinkedAccount(this.memberId, this.linkedAccountForm.selectedAccount).then(response => {
          if (response.code === 200) {
            this.$message.success('Accounts linked successfully! Both accounts are now connected.');
            this.fetchMemberInfo(); // Refresh member info
            this.fetchPointHistory(); // Refresh point history
            // 重置所有状态
            this.linkedAccountForm.searchResults = [];
            this.linkedAccountForm.selectedAccount = null;
            this.linkedAccountForm.searchKeyword = '';
          } else {
            this.$message.error('Failed to link accounts');
          }
          this.linkedAccountLoading = false;
        }).catch(error => {
          console.error('Failed to link accounts:', error);
          this.$message.error('Failed to link accounts');
          this.linkedAccountLoading = false;
        });
      }).catch(() => {
        // User cancelled
        this.linkedAccountLoading = false;
      });
    },
    
    handleDeleteLinkedAccount() {
      this.$confirm(
        'This will remove the bidirectional link between both accounts. The link will be deleted from both accounts. Continue?', 
        'Remove Bidirectional Link', 
        {
          confirmButtonText: 'Yes, Remove Link',
          cancelButtonText: 'Cancel',
          type: 'warning'
        }
      ).then(() => {
        this.deleteLinkedAccountLoading = true;
        removeLinkedAccount(this.memberId).then(response => {
          if (response.code === 200) {
            this.$message.success('Bidirectional link removed successfully! Both accounts are now unlinked.');
            this.fetchMemberInfo(); // Refresh member info
            // 重置关联账号状态
            this.linkedAccountForm.currentLinkedAccount = null;
          } else {
            this.$message.error('Failed to remove bidirectional link');
          }
          this.deleteLinkedAccountLoading = false;
        }).catch(error => {
          console.error('Failed to remove bidirectional link:', error);
          this.$message.error('Failed to remove bidirectional link');
          this.deleteLinkedAccountLoading = false;
        });
      }).catch(() => {
        // User cancelled
      });
    },

    handleMergeAccounts() {
      if (!this.memberInfo.linkedAccount) {
        this.$message.warning('No linked account to merge.');
        return;
      }
      
      this.$confirm(
        'This will merge the two accounts by combining their points and orders. This action cannot be undone. Continue?', 
        'Merge Accounts Confirmation', 
        {
          confirmButtonText: 'Yes, Merge Accounts',
          cancelButtonText: 'Cancel',
          type: 'warning'
        }
      ).then(() => {
        this.mergeLoading = true;
        mergeAccounts(this.memberId, this.memberInfo.linkedAccount).then(response => {
          if (response.code === 200) {
            this.$message.success('Accounts merged successfully! Points and orders have been combined.');
            this.fetchMemberInfo(); // Refresh member info
            this.fetchPointHistory(); // Refresh point history
          } else {
            this.$message.error('Failed to merge accounts');
          }
          this.mergeLoading = false;
        }).catch(error => {
          console.error('Failed to merge accounts:', error);
          this.$message.error('Failed to merge accounts');
          this.mergeLoading = false;
        });
      }).catch(() => {
        // User cancelled
      });
    },

    handleSearchAccount() {
      if (!this.linkedAccountForm.searchKeyword || this.linkedAccountForm.searchKeyword.length < 3) {
        this.linkedAccountForm.searchResults = [];
        return;
      }
      
      this.linkedAccountLoading = true;
      searchMembers(this.linkedAccountForm.searchKeyword).then(response => {
        if (response.code === 200) {
          // 过滤掉当前账号和已关联的账号
          const filteredResults = (response.data || []).filter(member => 
            member.memberId !== this.memberId && 
            member.memberId !== this.memberInfo.linkedAccount
          );
          
          // 为每个搜索结果添加selected属性
          this.linkedAccountForm.searchResults = filteredResults.map(member => ({
            ...member,
            selected: false
          }));
          this.linkedAccountForm.selectedAccount = null; // Clear selected account when search results change
        } else {
          this.$message.error('Failed to search accounts');
          this.linkedAccountForm.searchResults = [];
        }
        this.linkedAccountLoading = false;
      }).catch(error => {
        console.error('Failed to search accounts:', error);
        this.$message.error('Failed to search accounts');
        this.linkedAccountForm.searchResults = [];
        this.linkedAccountLoading = false;
      });
    },

    handleSelectAccount(member) {
      // 清除其他选项的选中状态
      this.linkedAccountForm.searchResults.forEach(m => {
        if (m.memberId !== member.memberId) {
          m.selected = false;
        }
      });
      
      // 设置当前选中的账号（只设置selectedAccount，不改变currentLinkedAccount）
      if (member.selected) {
        this.linkedAccountForm.selectedAccount = member.memberId;
        // 不要设置 currentLinkedAccount，它应该保持为数据库中已关联的账号
      } else {
        this.linkedAccountForm.selectedAccount = null;
      }
    },

    getStatusType(status) {
      if (status === 0) return 'success';
      if (status === 1) return 'danger';
      if (status === 2) return 'warning';
      return 'info';
    },

    getStatusText(status) {
      if (status === 0) return 'Normal';
      if (status === 1) return 'Deleted';
      if (status === 2) return 'Suspended';
      return 'Unknown';
    },

    formatDate(timestamp) {
      try {
        if (!timestamp) return 'N/A';
        
        // 直接使用 parseTime，但添加错误处理
        const result = this.parseTime(timestamp, '{y}-{m}-{d}');
        return result || 'N/A';
      } catch (error) {
        console.error('formatDate error:', error);
        return 'N/A';
      }
    },

    // 临时调试方法 - 可以在浏览器控制台中调用
    debugLinkedAccount() {
      console.log('=== 调试双向关联账号信息 ===');
      console.log('当前会员ID:', this.memberId);
      console.log('当前会员信息:', this.memberInfo);
      console.log('关联账号ID:', this.memberInfo.linkedAccount);
      console.log('当前显示的关联账号:', this.linkedAccountForm.currentLinkedAccount);
      
      // 如果有linkedAccount，直接调用API获取详细信息
      if (this.memberInfo.linkedAccount) {
        getMember(this.memberInfo.linkedAccount).then(response => {
          console.log('关联账号详细信息:', response);
        }).catch(error => {
          console.error('获取关联账号详细信息失败:', error);
        });
      }
    },

    isCurrentAccountOrder(order) {
      // 检查订单是否来自当前会员
      // 使用member_id字段来判断订单来源
      return order.member_id === this.memberId;
    },

    getPreviousYear() {
      // 返回前一年的年份
      const currentYear = new Date().getFullYear();
      return currentYear - 1;
    }
  },
  watch: {
    memberId: {
      immediate: true,
      handler(newVal) {
        if (newVal) {
          this.linkedAccountForm.searchKeyword = ''; // Clear search keyword when memberId changes
          this.linkedAccountForm.searchResults = [];
          this.linkedAccountForm.selectedAccount = null;
          this.linkedAccountForm.currentLinkedAccount = null;
        }
      }
    }
  }
};
</script>

<style scoped>
.member-detail-container {
  padding: 24px;
  background: #f7f7f7;
}
.top-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.back-btn {
  font-weight: bold;
}
.data-clear-btn {
  background: #ff4d4f;
  color: #fff;
}
.main-content {
  display: flex;
  gap: 24px;
  margin-bottom: 24px;
}
.left-col {
  flex: 2;
  display: flex;
  flex-direction: column;
  gap: 24px;
}
.right-col {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 24px;
}
.customer-card {
  padding: 16px;
}
.customer-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}
.avatar-section {
  display: flex;
  align-items: center;
  gap: 16px;
}
.customer-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.customer-name {
  font-size: 18px;
  font-weight: bold;
}
.customer-id {
  color: #888;
  font-size: 14px;
}
.customer-meta {
  display: flex;
  gap: 24px;
  font-size: 14px;
  color: #555;
}
.section-title {
  font-weight: bold;
  margin-bottom: 12px;
  font-size: 16px;
}
.section-subtitle {
  font-weight: bold;
  margin-bottom: 8px;
  font-size: 14px;
}
.search-results {
  max-height: 200px;
  overflow-y: auto;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  margin-bottom: 15px;
  padding: 5px;
}
.search-result-item {
  padding: 8px 12px;
  cursor: pointer;
  border-radius: 4px;
}
.search-result-item:hover {
  background-color: #f5f7fa;
}
.current-linked-account {
  margin-top: 15px;
  padding: 10px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  background-color: #f9fafc;
}
.linked-account-info {
  padding: 8px 0;
}
.linked-account-name {
  font-weight: bold;
  font-size: 16px;
  color: #409eff;
  margin-bottom: 4px;
}
.linked-account-details {
  display: flex;
  flex-direction: column;
  gap: 2px;
  font-size: 12px;
  color: #666;
}
.linked-account-details span {
  padding: 2px 0;
}
.linked-account-note {
  margin-top: 8px;
}
.no-linked-account {
  margin-top: 15px;
  padding: 15px;
  border: 1px solid #f0f0f0;
  border-radius: 4px;
  background-color: #fafafa;
  text-align: center;
}
.no-account-message {
  color: #999;
  font-size: 14px;
  margin-top: 8px;
}
.add-point-card, .manual-point-table, .admin-comment-card, .linked-account-card {
  margin-bottom: 0;
}
.form-actions {
  margin-top: 12px;
  display: flex;
  gap: 8px;
}
.order-history-card {
  margin-top: 24px;
}
.order-history-notice {
  margin-bottom: 12px;
}
.order-search {
  margin-bottom: 12px;
  width: 200px;
}
</style>
