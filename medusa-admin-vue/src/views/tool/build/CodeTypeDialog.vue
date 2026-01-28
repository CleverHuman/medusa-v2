<template>
  <el-dialog
    :title="title"
    :visible.sync="visible"
    width="500px"
    append-to-body
    @close="handleClose"
  >
    <el-form ref="form" :model="formData" :rules="rules" label-width="120px">
      <el-form-item label="Generation Type" prop="type">
        <el-radio-group v-model="formData.type">
          <el-radio label="page">Page</el-radio>
          <el-radio label="dialog">Dialog</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item v-if="showFileName" label="File Name" prop="fileName">
        <el-input v-model="formData.fileName" placeholder="Please enter file name" clearable />
      </el-form-item>
    </el-form>
    <div slot="footer" class="dialog-footer">
      <el-button @click="handleClose">
        Cancel
      </el-button>
      <el-button type="primary" @click="handleConfirm">
        Confirm
      </el-button>
    </div>
  </el-dialog>
</template>

<script>
export default {
  name: 'CodeTypeDialog',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    title: {
      type: String,
      default: 'Select Generation Type'
    },
    showFileName: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      formData: {
        type: 'page',
        fileName: ''
      },
      rules: {
        fileName: [
          {
            required: true,
            message: 'Please enter file name',
            trigger: 'blur'
          }
        ],
        type: [
          {
            required: true,
            message: 'Generation type cannot be empty',
            trigger: 'change'
          }
        ]
      }
    }
  },
  computed: {
    typeLabels() {
      return {
        page: 'Page',
        dialog: 'Dialog'
      }
    }
  },
  methods: {
    handleClose() {
      this.$emit('update:visible', false)
      this.$refs.form.resetFields()
    },
    handleConfirm() {
      this.$refs.form.validate(valid => {
        if (valid) {
          this.$emit('confirm', this.formData)
          this.handleClose()
        }
      })
    }
  }
}
</script>
