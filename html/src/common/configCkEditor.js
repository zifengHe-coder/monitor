import api from '@/request/api'

const config = {
  language: 'zh-cn',
  toolbarGroups: [
    { name: 'document', groups: [ 'mode', 'document', 'doctools' ] },
    { name: 'clipboard', groups: [ 'clipboard', 'undo' ] },
    { name: 'editing', groups: [ 'find', 'selection', 'spellchecker', 'editing' ] },
    { name: 'forms', groups: [ 'forms' ] },
    { name: 'basicstyles', groups: [ 'basicstyles', 'cleanup' ] },
    { name: 'paragraph', groups: [ 'list', 'indent', 'blocks', 'align', 'bidi', 'paragraph' ] },
    { name: 'links', groups: [ 'links' ] },
    { name: 'insert', groups: [ 'insert' ] },
    '/',
    { name: 'styles', groups: [ 'styles' ] },
    { name: 'colors', groups: [ 'colors' ] },
    { name: 'tools', groups: [ 'tools' ] },
    { name: 'others', groups: [ 'others' ] },
    { name: 'about', groups: [ 'about' ] }
  ],
  resize_enabled: false,
  font_names: '宋体/SimSun;新宋体/NSimSun;仿宋_GB2312/FangSong_GB2312;微软雅黑/Microsoft YaHei;楷体/KaiTi;黑体/SimHei;隶书/隶书;',
  removeButtons: 'Form,Checkbox,Radio,TextField,Textarea,Select,Button,ImageButton,HiddenField,Save,NewPage,Print,Templates,Scayt,Language,Flash,Smiley,SpecialChar,About',
  filebrowserUploadUrl: api.adminFileUploadFileCKEditor,
  filebrowserImageUploadUrl: api.adminFileUploadImageCKEditor
}

export default config
