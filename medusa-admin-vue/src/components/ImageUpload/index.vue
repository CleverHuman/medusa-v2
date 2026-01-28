<template>
  <div class="component-upload-image">
    <el-upload
      multiple
      :action="uploadImgUrl"
      list-type="picture-card"
      :on-success="handleUploadSuccess"
      :before-upload="handleBeforeUpload"
      :limit="limit"
      :on-error="handleUploadError"
      :on-exceed="handleExceed"
      ref="imageUpload"
      :on-remove="handleDelete"
      :show-file-list="true"
      :headers="headers"
      :file-list="fileList"
      :on-preview="handlePictureCardPreview"
      :class="{hide: this.fileList.length >= this.limit}"
    >
      <i class="el-icon-plus"></i>
    </el-upload>

    <!-- Upload Tips -->
    <div class="el-upload__tip" slot="tip" v-if="showTip">
      Please upload
      <template v-if="fileSize"> files no larger than <b style="color: #f56c6c">{{ fileSize }}MB</b> </template>
      <template v-if="fileType"> in <b style="color: #f56c6c">{{ fileType.join("/") }}</b> format </template>
    </div>

    <el-dialog
      :visible.sync="dialogVisible"
      title="Preview"
      width="800"
      append-to-body
    >
      <img
        :src="dialogImageUrl"
        style="display: block; max-width: 100%; margin: 0 auto"
      />
    </el-dialog>
  </div>
</template>

<script>
import { getToken } from "@/utils/auth";
import { isExternal } from "@/utils/validate";

export default {
  props: {
    value: [String, Object, Array],
    // Image count limit
    limit: {
      type: Number,
      default: 5,
    },
    // Size limit (MB)
    fileSize: {
       type: Number,
      default: 5,
    },
    // File types, e.g. ['png', 'jpg', 'jpeg']
    fileType: {
      type: Array,
      default: () => ["png", "jpg", "jpeg"],
    },
    // Whether to show tips
    isShowTip: {
      type: Boolean,
      default: true
    }
  },
  data() {
    return {
      number: 0,
      uploadList: [],
      dialogImageUrl: "",
      dialogVisible: false,
      hideUpload: false,
      baseUrl: process.env.VUE_APP_BASE_API,
      uploadImgUrl: process.env.VUE_APP_BASE_API + "/common/upload", // Image upload server address
      headers: {
        Authorization: "Bearer " + getToken(),
      },
      fileList: []
    };
  },
  watch: {
    value: {
      handler(val) {
        if (val) {
          // First convert value to array
          const list = Array.isArray(val) ? val : this.value.split(',');
          // Then convert array to object array
          this.fileList = list.map(item => {
            if (typeof item === "string") {
              if (item.indexOf(this.baseUrl) === -1 && !isExternal(item)) {
                  item = { name: this.baseUrl + item, url: this.baseUrl + item };
              } else {
                  item = { name: item, url: item };
              }
            }
            return item;
          });
        } else {
          this.fileList = [];
          return [];
        }
      },
      deep: true,
      immediate: true
    }
  },
  computed: {
    // Whether to show tips
    showTip() {
      return this.isShowTip && (this.fileType || this.fileSize);
    },
  },
  methods: {
    // Loading before upload
    handleBeforeUpload(file) {
      let isImg = false;
      if (this.fileType.length) {
        let fileExtension = "";
        if (file.name.lastIndexOf(".") > -1) {
          fileExtension = file.name.slice(file.name.lastIndexOf(".") + 1);
        }
        isImg = this.fileType.some(type => {
          if (file.type.indexOf(type) > -1) return true;
          if (fileExtension && fileExtension.indexOf(type) > -1) return true;
          return false;
        });
      } else {
        isImg = file.type.indexOf("image") > -1;
      }

      if (!isImg) {
        this.$modal.msgError(`Invalid file format. Please upload ${this.fileType.join("/")} image files!`);
        return false;
      }
      if (file.name.includes(',')) {
        this.$modal.msgError('Invalid filename. Cannot contain commas!');
        return false;
      }
      if (this.fileSize) {
        const isLt = file.size / 1024 / 1024 < this.fileSize;
        if (!isLt) {
          this.$modal.msgError(`Uploaded image size cannot exceed ${this.fileSize} MB!`);
          return false;
        }
      }
      this.$modal.loading("Uploading image, please wait...");
      this.number++;
    },
    // File count exceeded
    handleExceed() {
      this.$modal.msgError(`Cannot upload more than ${this.limit} files!`);
    },
    // Upload success callback
    handleUploadSuccess(res, file) {
      if (res.code === 200) {
        this.uploadList.push({ name: res.fileName, url: res.fileName });
        this.uploadedSuccessfully();
      } else {
        this.number--;
        this.$modal.closeLoading();
        this.$modal.msgError(res.msg);
        this.$refs.imageUpload.handleRemove(file);
        this.uploadedSuccessfully();
      }
    },
    // Delete image
    handleDelete(file) {
      const findex = this.fileList.map(f => f.name).indexOf(file.name);
      if (findex > -1) {
        this.fileList.splice(findex, 1);
        this.$emit("input", this.listToString(this.fileList));
      }
    },
    // Upload failed
    handleUploadError() {
      this.$modal.msgError("Failed to upload image, please try again");
      this.$modal.closeLoading();
    },
    // Upload completion handling
    uploadedSuccessfully() {
      if (this.number > 0 && this.uploadList.length === this.number) {
        this.fileList = this.fileList.concat(this.uploadList);
        this.uploadList = [];
        this.number = 0;
        this.$emit("input", this.listToString(this.fileList));
        this.$modal.closeLoading();
      }
    },
    // Preview
    handlePictureCardPreview(file) {
      this.dialogImageUrl = file.url;
      this.dialogVisible = true;
    },
    // Convert object to specified string separator
    listToString(list, separator) {
      let strs = "";
      separator = separator || ",";
      for (let i in list) {
        if (list[i].url) {
          strs += list[i].url.replace(this.baseUrl, "") + separator;
        }
      }
      return strs != '' ? strs.substr(0, strs.length - 1) : '';
    }
  }
};
</script>
<style scoped lang="scss">
// .el-upload--picture-card controls the plus sign part
::v-deep.hide .el-upload--picture-card {
    display: none;
}
// Remove animation effects
::v-deep .el-list-enter-active,
::v-deep .el-list-leave-active {
    transition: all 0s;
}

::v-deep .el-list-enter, .el-list-leave-active {
  opacity: 0;
  transform: translateY(0);
}
</style>

