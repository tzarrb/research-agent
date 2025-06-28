<template>
  <nav_bar/>
  <div class="upload-container">
    <input type="file" @change="handleFileChange" />
    <button @click="uploadFile">上传</button>
    <p v-if="fileName">已选择文件: {{ fileName }}</p>
    <p v-if="uploadStatus" :class="{ 'success': uploadSuccess, 'error': !uploadSuccess }">
      {{ uploadStatus }}
    </p>
  </div>
</template>

<script>
import axios from 'axios';
// 导入组件
import nav_bar from '@/components/bar/NavBar.vue'

export default {
  components: {
    nav_bar
  },
  data() {
    return {
      selectedFile: null,
      fileName: '',
      uploadStatus: '',
      uploadSuccess: false
    };
  },
  methods: {
    handleFileChange(event) {
      this.selectedFile = event.target.files[0];
      this.fileName = this.selectedFile ? this.selectedFile.name : '';
      this.uploadStatus = '';
    },
    async uploadFile() {
      if (!this.selectedFile) {
        this.uploadStatus = '请选择文件';
        this.uploadSuccess = false;
        return;
      }

      try {
        const formData = new FormData();
        formData.append('file', this.selectedFile);

        // 文件上传
        const response = await axios.post('http://localhost:18080/research-agent/ai/vector/upload', formData, {
          headers: {
            'Content-Type': 'multipart/form-data'
          }
        });

        this.uploadStatus = '上传成功, 文件名id：' + response.data;
        this.uploadSuccess = true;
        console.log('上传响应:', response);
      } catch (error) {
        this.uploadStatus = '上传失败, 错误信息：' + error.message;
        this.uploadSuccess = false;
        console.error('上传错误:', error);
      }
    }
  },
  mounted () {
    // 初始化时可以添加一些逻辑
  }
};
</script>

<style scoped>
.upload-container {
  padding: 20px;
  border: 1px solid #ccc;
  border-radius: 4px;
  max-width: 600px;
  margin: 50px auto;
}

input[type="file"] {
  margin-bottom: 10px;
}

button {
  padding: 8px 16px;
  background-color: #42b883;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

button:hover {
  background-color: #3aa876;
}

.success {
  color: green;
}

.error {
  color: red;
}
</style>