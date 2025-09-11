<template>
  <nav_bar/>
<!--  <div class="upload-container">-->
<!--    <input id="file-input" type="file" @change="handleFileChange" />-->
<!--    <button id="up-btn" @click="uploadFile">上传</button>-->
<!--    <p v-if="fileName">已选择文件: {{ fileName }}</p>-->
<!--    <p v-if="uploadStatus" :class="{ 'up-success': uploadSuccess, 'up-error': !uploadSuccess }">-->
<!--      {{ uploadStatus }}-->
<!--    </p>-->
<!--  </div>-->

<!--  ref="upload"-->
<!--  v-model:file-list="fileList"-->
  <div class="upload-container">
    <el-upload
        v-model:file-list="fileList"
        ref="upload"
        class="upload-demo"
        action="http://localhost:18080/research-agent/ai/vector/upload"
        :limit="1"
        :auto-upload="false"
        :on-change="handleChange"
        :on-exceed="handleExceed"
        :on-remove="handleRemove"
    >

      <template #trigger>
        <el-button type="primary">选择文件</el-button>
      </template>

      <el-button class="ml-3" type="success" @click="submitUpload">上传文件</el-button>

      <template #tip>
        <div class="el-upload__tip text-red">
          上传文件限制：单个文件不超过10MB
        </div>
        <div class="el-upload__tip text-red">
          <p>{{ uploadStatus }}</p>
        </div>
      </template>
    </el-upload>
  </div>
</template>

<script lang="ts" setup>
import axios from 'axios';
// 导入组件
import nav_bar from '@/components/bar/NavBar.vue'

import { ref } from 'vue'
import { UploadProps, UploadUserFile, genFileId  } from 'element-plus'

const fileList = ref([])
const selectedFile = ref(null)
const uploadStatus = ref("")
const uploadSuccess = ref(false)

const upload = ref<UploadInstance>()

const handleExceed: UploadProps['onExceed'] = (files) => {
  upload.value!.clearFiles()

  const file = files[0] as UploadRawFile
  file.uid = genFileId()
  upload.value!.handleStart(file)

  // selectedFile.value = file
}

const handleRemove: UploadProps['onRemove'] = (uploadFile, uploadFiles) => {
  fileList.value = fileList.value.filter(file => file !== uploadFile.raw!)
  selectedFile.value = null
}

const handleChange: UploadProps['onChange'] = (uploadFile, uploadFiles) => {
  fileList.value.push(uploadFile.raw!)
  selectedFile.value = uploadFile.raw!
}

const submitUpload = async () => {
  // upload.value!.submit()

  if (!selectedFile.value) {
    uploadStatus.value = '请选择文件';
    uploadSuccess.value = false;
    return;
  }

  try {
    const formData = new FormData();
    formData.append('file', selectedFile.value);

    // 文件上传
    const response = await axios.post('http://localhost:18080/research-agent/ai/vector/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    });

    uploadStatus.value = '上传成功, 文件名id：' + response.data;
    uploadSuccess.value = true;
    console.log('上传响应:', response);
  } catch (error) {
    uploadStatus.value = '上传失败, 错误信息：' + error.message;
    uploadSuccess.value = false;
    console.error('上传错误:', error);
  }
}

// export default {
//   components: {
//     nav_bar
//   },
//   data() {
//     return {
//       selectedFile: null,
//       fileName: '',
//       uploadStatus: '',
//       uploadSuccess: false
//     };
//   },
//   methods: {
//     handleFileChange(event) {
//       this.selectedFile = event.target.files[0];
//       this.fileName = this.selectedFile ? this.selectedFile.name : '';
//       this.uploadStatus = '';
//     },
//     async uploadFile() {
//       if (!this.selectedFile) {
//         this.uploadStatus = '请选择文件';
//         this.uploadSuccess = false;
//         return;
//       }
//
//       try {
//         const formData = new FormData();
//         formData.append('file', this.selectedFile);
//
//         // 文件上传
//         const response = await axios.post('http://localhost:18080/research-agent/ai/vector/upload', formData, {
//           headers: {
//             'Content-Type': 'multipart/form-data'
//           }
//         });
//
//         this.uploadStatus = '上传成功, 文件名id：' + response.data;
//         this.uploadSuccess = true;
//         console.log('上传响应:', response);
//       } catch (error) {
//         this.uploadStatus = '上传失败, 错误信息：' + error.message;
//         this.uploadSuccess = false;
//         console.error('上传错误:', error);
//       }
//     }
//   },
//   mounted () {
//     // 初始化时可以添加一些逻辑
//   }
// };
</script>

<style scoped>
.upload-container {
  padding: 20px;
  border: 1px solid #ccc;
  border-radius: 4px;
  max-width: 600px;
  margin: 50px auto;
}

#file-input {
  margin-bottom: 10px;
}

#up-btn {
  padding: 8px 16px;
  background-color: #027cff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

#up-btn:hover {
  background-color: rgba(2, 124, 255, 0.48);
}

.up-success {
  color: green;
}

.up-error {
  color: red;
}

</style>