<template>
  <div id="baseRichTextEditor">
    <textarea :id="editorKey"></textarea>
  </div>
</template>

<script>
import config from '@/common/configCkEditor'

let ckeditor
export default {
  name: 'BaseRichTextEditorCom',
  props: {
    editorKey: {
      type: String,
      default: 'richTextEditor'
    },//替换元素id
    content: {
      type: String,
      default: ''
    }//富文本编辑器内容
  },
  data () {
    return {
    }
  },
  created () {
  },
  watch: {
    content: {
      handler(newVal){
        if(newVal)
          setTimeout(() => {
            this.setValue(newVal);
          }, 500)
      }
    }
  },
  mounted () {
    window.CKEDITOR.replace(this.editorKey, {
      ...config,
      height: '300px',
      width: '100%',
      filebrowserImageUploadUrl: `${window.origin}${this.$api.adminFileUploadImageCKEditor}`//图片上传
    })
    ckeditor = window.CKEDITOR.instances
  },
  methods: {
    //获取值
    getValue () {
      return {
        editorKey: this.editorKey,
        getValue: ckeditor[this.editorKey].getData()
      }
    },
    //设置值
    setValue (value) {
      ckeditor[this.editorKey].setData(value)
    }
  }
}
</script>
