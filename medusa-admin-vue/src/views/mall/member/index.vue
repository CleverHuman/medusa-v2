<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="" prop="level">
        <el-select v-model="queryParams.level" placeholder="select level" clearable size="small">
          <el-option
            v-for="(levelName, levelId) in benefitDict"
            :key="levelId"
            :label="levelName"
            :value="levelId"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="" prop="username">
        <el-input
          v-model = "queryParams.username"
          placeholder="username"
          clearable
          size="small"
          @keyup.enter.native = "handleQuery"
        />
      </el-form-item>
      <el-form-item label="" prop="primaryContact">
        <el-input
          v-model = "queryParams.primaryContact"
          placeholder="primaryContact"
          clearable
          size="small"
          @keyup.enter.native = "handleQuery"
        />
      </el-form-item>
      <el-form-item label="" prop="secondaryContact">
        <el-input
          v-model = "queryParams.secondaryContact"
          placeholder="secondaryContact"
          clearable
          size="small"
          @keyup.enter.native = "handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">search</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">refresh</el-button>
      </el-form-item>
    </el-form>


    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          :disabled="multiple"
          @click = "handleBatchChangeLevel"
        >Change Level</el-button>
      </el-col>
      <el-button
        type="warning"
        :disabled="multiple"
        @click="handleBatchSuspend"
      >
        Suspend
      </el-button>
      <el-button
        type="danger"
        :disabled="multiple"
        @click="handleBatchDelete"
      >
        Delete
      </el-button>
    </el-row>

    <!-- Level Filter Buttons -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="info"
          @click="filterByLevel(null)"
          :class="{ 'is-active': queryParams.currentLevel === null }"
        >All Levels</el-button>
      </el-col>
      <el-col :span="1.5" v-for="(levelName, levelId) in benefitDict" :key="levelId">
        <el-button
          type="info"
          @click="filterByLevel(levelId)"
          :class="{ 'is-active': queryParams.currentLevel === levelId }"
        >{{ levelName }}</el-button>
      </el-col>
    </el-row>

    
    <!-- 表格 -->
    <el-table
      :data="memberList"
      v-loading="loading"
      style="width: 100%"
      border
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="55" />
      <el-table-column prop="createTime" label="First Joined" width="160" />
      <el-table-column prop="lastSeen" label="Last Seen" width="160" />
      <el-table-column prop="username" label="Username" />
      <el-table-column prop="primaryContact" label="Primary Contact" />
      <el-table-column prop="secondaryContact" label="Secondary Contact" />
      <el-table-column
        prop="status"
        label="Status"
        :formatter="(row) => statusDict[row.status] || 'Unknown'"
      />
      <el-table-column
        prop="currentLevel"
        label="Level"
        :formatter="(row) => benefitDict[row.currentLevel] || 'Unknown'"
      />
      <el-table-column prop="totalOrders" label="Orders" width="80" />
      <el-table-column label="Edit" width="200" align="center">
        <template slot-scope="scope">
          <el-button 
            icon="el-icon-edit" 
            size="mini" 
            type="primary"
            @click="handleAddComment(scope.row)" 
          ></el-button>
          <el-button 
            icon="el-icon-user" 
            size="mini" 
            type="success"
            @click="viewDetail(scope.row)" 
          ></el-button>
          <el-button 
            icon="el-icon-delete" 
            size="mini" 
            type="danger" 
            @click="deleteMember(scope.row)" 
          ></el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 批量修改等级弹窗 -->
    <el-dialog title="Change Level" :visible.sync="levelDialogVisible" width="30%">
      <el-select v-model="selectedLevel" placeholder="Select New Level">
        <el-option
          v-for="(levelName, levelId) in benefitDict"
          :key="levelId"
          :label="levelName"
          :value="levelId"
        />
      </el-select>
      <span slot="footer" class="dialog-footer">
        <el-button @click="levelDialogVisible = false">Cancel</el-button>
        <el-button type="primary" @click="confirmChangeLevel">Confirm</el-button>
      </span>
    </el-dialog>

    <!-- 添加评论弹窗 -->
    <el-dialog title="Add Comment" :visible.sync="commentDialogVisible" width="50%">
      <el-form :model="commentForm" ref="commentForm" :rules="commentRules" label-width="80px">
        <el-form-item label="Member" prop="memberName">
          <el-input v-model="commentForm.memberName" disabled />
        </el-form-item>
        <el-form-item label="Comment" prop="comment">
          <el-input 
            v-model="commentForm.comment" 
            type="textarea" 
            :rows="4" 
            placeholder="Please enter your comment"
          />
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="commentDialogVisible = false">Cancel</el-button>
        <el-button type="primary" @click="confirmAddComment">Confirm</el-button>
      </span>
    </el-dialog>

  </div>
</template>

<script>
import {
  listMembers,
  batchUpdate,
  suspendMembers,
  resumeMembers,
  delMembers,
  addComment
} from "@/api/mall/member";
import {listBenefit} from "@/api/mall/benefit"; // 后端接口

export default {
  name: "MemberList",
  data() {
    return {
      loading :true,
      ids : [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0 ,
      memberList: [],
      benefitDict: [],
      queryParams: {
        currentLevel: null,
        name: null,
        primaryContact: null,
        secondaryContact: null,
        pageNum: 1,
        pageSize: 10
      },
      statusDict:{
        0:"normal",
        1:"del",
        2:"suspend"
      },
      levelDialogVisible: false,
      selectedLevel: null,
      commentDialogVisible: false,
      commentForm: {
        memberId: null,
        memberName: '',
        comment: ''
      },
      commentRules: {
        comment: [
          { required: true, message: "Comment cannot be empty", trigger: "blur" }
        ]
      }
    };
  },
  created() {
    this.getBenefitDict();
    this.getList();
  },
  methods: {
    getBenefitDict(){
      listBenefit().then(response => {
        this.benefitDict = response.rows.reduce((acc, cur) => {
          acc[cur.levelId] = cur.levelName;
          return acc;
        }, {});
      })
    },
    getList() {
      this.loading = true;
      const params = this.addDateRange(this.queryParams, this.dateRange);
      listMembers(params).then(response => {
        this.memberList = response.rows;
        this.total = response.total;
        this.loading = false;
      })
    },
    // 搜索
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },

    resetQuery() {
      this.resetForm("queryForm")
      this.getList();
    },

    handleSelectionChange(selection){
      this.ids =  selection.map(item => item.memberId);
      this.single = selection.length !=1 ;
      this.multiple = !selection.length;
    },

    // 批量修改等级
    handleBatchChangeLevel() {
      if (this.ids.length === 0) return;
      this.levelDialogVisible = true;
    },

    // 确认修改等级
    confirmChangeLevel() {
      this.$confirm('Confirm to change level?', 'Warning', {
        confirmButtonText: 'Confirm',
        cancelButtonText: 'Cancel',
        type: 'warning'
      }).then(() => {
        // const levelId = this.getLevelIdByName(this.selectedLevel);
        batchUpdate(this.ids, Number(this.selectedLevel)).then(() => {
          this.$message.success('Level updated');
          this.getList();
          this.levelDialogVisible = false;
        });
      });
    },

    // 批量停用
    handleBatchSuspend() {
      this.$confirm('Confirm to suspend selected members?', 'Warning', {
        confirmButtonText: 'Confirm',
        cancelButtonText: 'Cancel',
        type: 'warning'
      }).then(() => {
        suspendMembers(this.ids).then(() => {
          this.$message.success('Operation success');
          this.getList();
        });
      });
    },

    // 批量删除
    handleBatchDelete() {
      this.$confirm('This will permanently delete members. Continue?', 'Warning', {
        confirmButtonText: 'Confirm',
        cancelButtonText: 'Cancel',
        type: 'warning'
      }).then(() => {
        delMembers(this.ids).then(() => {
          this.$message.success('Deleted');
          this.getList();
        });
      });
    },

    // 添加评论
    handleAddComment(row) {
      this.commentForm.memberId = row.memberId;
      this.commentForm.memberName = row.username;
      this.commentForm.comment = '';
      this.commentDialogVisible = true;
    },

    // 确认添加评论
    confirmAddComment() {
      this.$refs["commentForm"].validate(valid => {
        if (valid) {
          addComment(this.commentForm.memberId, this.commentForm.comment).then(response => {
            this.$message.success('Comment added successfully');
            this.commentDialogVisible = false;
            this.getList();
          }).catch(error => {
            this.$message.error('Failed to add comment');
          });
        }
      });
    },

    // 查看详情
    viewDetail(row) {
      this.$router.push({
        path: '/hide/member/mdetail',
        query: {
          memberId: row.memberId
        }
      });
    },

    // 删除单个成员
    deleteMember(row) {
      this.$confirm('This will permanently delete this member. Continue?', 'Warning', {
        confirmButtonText: 'Confirm',
        cancelButtonText: 'Cancel',
        type: 'warning'
      }).then(() => {
        delMembers([row.memberId]).then(() => {
          this.$message.success('Member deleted successfully');
          this.getList();
        }).catch(error => {
          this.$message.error('Failed to delete member');
        });
      });
    },

    // 单个状态切换
    // toggleStatus(row) {
    //   const newStatus = row.status === 2 ? 0 : 2;
    //   changeStatus({ ids: [row.memberId], status: newStatus }).then(() => {
    //     this.$message.success('Status updated');
    //     this.getList();
    //   });
    // }

    filterByLevel(levelId) {
      this.queryParams.currentLevel = Number(levelId);
      this.getList();
    }
  }
};
</script>

<style scoped>
.app-container {
  background: #fff;
  padding: 20px;
}

.el-button.is-active {
  background-color: #409eff;
  border-color: #409eff;
  color: white;
}

.el-button.is-active:hover {
  background-color: #66b1ff;
  border-color: #66b1ff;
}
</style>
