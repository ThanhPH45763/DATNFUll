<template>
  <div class="rich-text-editor">
    <QuillEditor
      v-model:content="content"
      contentType="html"
      :toolbar="toolbarOptions"
      theme="snow"
      :placeholder="placeholder"
      @change="handleChange"
      class="editor-container"
    />
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue';
import { QuillEditor } from '@vueup/vue-quill';
import '@vueup/vue-quill/dist/vue-quill.snow.css';

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  placeholder: {
    type: String,
    default: 'Nhập mô tả sản phẩm...'
  }
});

const emit = defineEmits(['update:modelValue', 'change']);

const content = ref(props.modelValue || '');

// Đầy đủ các tùy chọn cho toolbar
const toolbarOptions = [
  ['bold', 'italic', 'underline', 'strike'],
  [{ 'header': 1 }, { 'header': 2 }],
  [{ 'list': 'ordered' }, { 'list': 'bullet' }],
  [{ 'script': 'sub' }, { 'script': 'super' }],
  [{ 'indent': '-1' }, { 'indent': '+1' }],
  [{ 'direction': 'rtl' }],
  [{ 'size': ['small', false, 'large', 'huge'] }],
  [{ 'header': [1, 2, 3, 4, 5, 6, false] }],
  [{ 'color': [] }, { 'background': [] }],
  [{ 'font': [] }],
  [{ 'align': [] }],
  ['clean'],
  ['link', 'image', 'video']
];

// Cập nhật giá trị khi prop thay đổi
watch(() => props.modelValue, (newVal) => {
  if (newVal !== content.value) {
    content.value = newVal;
  }
});

// Emit sự kiện khi nội dung thay đổi
watch(content, (newVal) => {
  emit('update:modelValue', newVal);
  emit('change', newVal);
});

const handleChange = (editor, html, text) => {
  emit('update:modelValue', html);
  emit('change', html);
};

onMounted(() => {
  if (props.modelValue) {
    content.value = props.modelValue;
  }
});
</script>

<style scoped>
.rich-text-editor {
  width: 100%;
  margin-bottom: 16px;
}

.editor-container {
  height: 300px;
  border-radius: 6px;
}

:deep(.ql-toolbar) {
  border-top-left-radius: 6px;
  border-top-right-radius: 6px;
  background-color: #f6f6f6;
  border-color: #d9d9d9;
}

:deep(.ql-container) {
  border-bottom-left-radius: 6px;
  border-bottom-right-radius: 6px;
  border-color: #d9d9d9;
  min-height: 250px;
}

:deep(.ql-editor) {
  font-family: 'Roboto', sans-serif;
  font-size: 14px;
  line-height: 1.6;
}

:deep(.ql-container:hover), :deep(.ql-toolbar:hover) {
  border-color: #f33b47;
}

:deep(.ql-toolbar .ql-stroke) {
  stroke: #333;
}

:deep(.ql-toolbar .ql-fill) {
  fill: #333;
}

:deep(.ql-toolbar button:hover .ql-stroke) {
  stroke: #f33b47;
}

:deep(.ql-toolbar button:hover .ql-fill) {
  fill: #f33b47;
}

:deep(.ql-toolbar button.ql-active .ql-stroke) {
  stroke: #f33b47;
}

:deep(.ql-toolbar button.ql-active .ql-fill) {
  fill: #f33b47;
}
</style> 