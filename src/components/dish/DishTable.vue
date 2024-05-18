<script lang="ts" setup>
import {ElMessage} from "element-plus";
import {ref, onMounted} from 'vue'
import {goBack, reloadHeader} from "@/ts/container.ts";
import axiosRequest from "@/utils/axiosRequest.ts";
import {getURLParam} from "@/ts/common.ts";
import {Plus} from "@element-plus/icons-vue";

let id = ''
let index = ref(0)

// 添加的口味列表
let dishFlavors = ref<{ name: string, value: string[], show: boolean }[]>([])
let imageURL = ref('')
let dishForm = ref({
  name: '',
  price: '',
  code: '',
  image: '',
  description: '',
  flavors: [],
  status: 1,
  categoryId: ''
})

let rules = ref({
  name: [
    {required: true, message: '请输入菜品名称', trigger: 'blur'},
    {min: 1, max: 20, message: '长度在 1 到 20 个字符', trigger: 'blur'}
  ],
  categoryId: [
    {required: true, message: '请选择菜品分类', trigger: 'change'}
  ],
  price: [
    {required: true, message: '请输入菜品价格', trigger: 'blur'},
    {
      validator: (rule: any, value: any, callback: any) => {
        if (!value) {
          callback(new Error('请填写菜品价格'))
        } else {
          let reg = /^\d+(\.\d{0,2})?$/
          if (reg.test(value)) {
            if (value < 10000)
              callback()
            else
              callback(new Error('菜品价格小于10000'))
          } else
            callback(new Error('菜品价格格式只能为数字, 且最多保留两位小数'))
        }
      },
      trigger: 'blur'
    }
  ]
})

let dishFormRef = ref(null)
let categoryList = ref([])
// 可选的口味列表
let dishFlavorsData = [
  {name: '甜味', value: ['无糖', '少糖', '半糖', '多糖', '全糖']},
  {name: '温度', value: ['热饮', '常温', '去冰', '少冰', '多冰']},
  {name: '忌口', value: ['不要葱', '不要蒜', '不要香菜', '不要辣']},
  {name: '辣度', value: ['不辣', '微辣', '中辣', '重辣']}
]
let actionType = ref('')

onMounted(async () => {
  await getCategoryList()
  id = getURLParam('id')
  actionType.value = id ? 'edit' : 'add'
  if (id) {
    await getDishById()
    reloadHeader('修改菜品', true, '/dish');
  } else
    reloadHeader('添加菜品', true, '/dish');
})

async function getCategoryList() {
  try {
    // 获取分类列表
    let r = await axiosRequest({
      url: '/category/list',
      method: 'get',
      params: {type: 1}
    })

    if (r.code == 1) {
      categoryList.value = r.data.map((item: any) => {
        return {
          id: item.id,
          name: item.name
        }
      })
    } else
      throw new Error(r.msg || '操作失败')
  } catch (e: any) {
    ElMessage.error('出错了: ' + e.message);
  }
}

async function getDishById() {
  try {
    let r = await axiosRequest({
      url: `/dish/${id}`,
      method: 'get'
    })

    if (r.code == 1) {
      dishForm.value = {
        name: r.data.name,
        price: String(r.data.price / 100),
        code: r.data.code,
        image: r.data.image,
        description: r.data.description,
        flavors: r.data.flavors, // 菜品口味 TODO: 需要处理一下
        status: r.data.status,
        categoryId: r.data.categoryId
      }

      if (r.data.flavors != null) {
        // 单独给 flavors 赋值
        dishFlavors.value = r.data.flavors.map((item: any) => {
              return {
                name: item.name,
                value: item.value,
                showOption: false
              }
            }
        )
      }
      imageURL.value = `/download?name=${r.data.image}`
    } else
      throw new Error(r.msg || '操作失败')
  } catch (e: any) {
    ElMessage.error('出错了: ' + e.message);
  }
}

function addFlavor() {
  dishFlavors.value.push({
    name: '',
    value: [],
    show: false
  })
}

function delFlavor(index: number) {
  dishFlavors.value.splice(index, 1)
}

function selectFlavor(show: boolean, index: number) {
  if (show)
    dishFlavors.value[index].show = show
  else // 失去焦点时 (show 为 false) 时延迟 200ms执行, 确保点击事件能够注册, 不能删去
    setTimeout(() => {
      dishFlavors.value[index].show = show
    }, 200)
}

/**
 * 向 `dishFlavors` 中添加具体的口味信息
 * @param dataIndex 需要添加的口味信息在 `dishFlavorsData` 中的位置
 * @param index 添加到 `dishFlavors` 中的具体位置
 */
function addFlavorLabel(dataIndex: number, index: number) {
  let obj = JSON.parse(JSON.stringify(dishFlavorsData[dataIndex]))
  dishFlavors.value[index].name = obj.name
  dishFlavors.value[index].value = obj.value
}

/**
 * 删除 `dishFlavors` 中某个口味中某个标签的信息
 * @param index
 * @param ind
 */
function delFlavorLabel(index: number, ind: any) {
  dishFlavors.value[index].value.splice(ind, 1)
}

function flavorPosition(_index: any) {
  index.value = _index
}

function keyDownHandle(val: any, index: any) {
  if (val.target.innerText.trim() != '') {
    dishFlavors.value[index].value.push(val.target.innerText)
    val.target.innerText = ''
  }
}

function handleAvatarSuccess(response: any, file: any, fileList: any) {
  if (response.code === 1) {
    // FIXME: 有意思, 这个地方竟然还要设置成 `/api/download` 才能上传成功
    imageURL.value = `/api/download?name=${response.data}`
    console.log(imageURL.value)
    dishForm.value.image = response.data
  }
}

function onChange(file: any) {
  if (file) {
    let suffix = file.name.split('.')[1]
    let size = file.size / 1024 / 1024 < 2
    if (['png', 'jpeg', 'jpg'].indexOf(suffix) < 0) {
      ElMessage.error('上传图片只支持 png、jpeg、jpg 格式！')
      dishFormRef.value.upload.clearFiles()
      return false
    }
    if (!size) {
      ElMessage.error('上传文件大小不能超过 2MB!')
      return false
    }
    return file
  }
}

function submitForm(_continue: boolean) {
  dishFormRef.value.validate(async (valid: boolean) => {
    if (valid) {
      let params = {...dishForm.value}
      params.price = String(Number.parseInt(params.price) * 100)
      params.flavors = dishFlavors.value.map((item: any) => {
        return {
          name: item.name,
          value: item.value
        }
      })

      if (!imageURL.value) {
        ElMessage.error('请上传菜品图片')
        return
      }

      if (actionType.value == 'add') {
        try {
          let r = await axiosRequest({
            url: '/dish/add',
            method: 'post',
            data: params
          })

          if (r.code == 1) {
            ElMessage.success('菜品添加成功!')
            if (_continue) {
              dishFlavors.value = []
              imageURL.value = ''
              dishForm.value = {
                name: '',
                price: '',
                code: '',
                image: '',
                description: '',
                flavors: [],
                status: 1,
                categoryId: ''
              }
            } else
              goBack()
          } else
            throw new Error(r.msg || '操作失败')
        } catch (e: any) {
          ElMessage.error('出错了: ' + e.message)
        }
      } else {
        params.id = id // 添加 id 这个属性
        try {
          let r = await axiosRequest({
            url: '/dish',
            method: 'put',
            data: params
          })
          if (r.code == 1) {
            ElMessage.success('菜品修改成功!')
            goBack()
          } else
            throw new Error(r.msg || '操作失败')
        } catch (e: any) {
          ElMessage.error('出错了: ' + e.message)
        }
      }
    } else
      return false
  })
}
</script>

<template>
  <div class="addBrand-container" id="food-add-app">
    <div class="container">
      <el-form ref="dishFormRef" :model="dishForm" :rules="rules"
               :inline="true" label-width="180px">
        <div>
          <el-form-item label="菜品名称:" prop="name">
            <el-input v-model="dishForm.name" placeholder="请填写菜品名称" maxlength="20"/>
          </el-form-item>

          <el-form-item label="菜品分类:" prop="categoryId">
            <el-select v-model="dishForm.categoryId" placeholder="请选择菜品分类">
              <el-option v-for="(item, index) in categoryList" :key="index" :label="item.name" :value="item.id"/>
            </el-select>
          </el-form-item>
        </div>

        <div>
          <el-form-item label="菜品价格:" prop="price">
            <el-input v-model="dishForm.price" placeholder="请设置菜品价格"/>
          </el-form-item>
        </div>

        <el-form-item label="口味做法配置:">
          <el-form-item>
            <div class="flavorBox">
              <span v-if="dishFlavors.length == 0" class="addBut" @click="addFlavor"> + 添加口味</span>

              <div v-if="dishFlavors.length != 0" class="flavor">
                <div class="title">
                  <span>口味名（3个字内）</span><span>口味标签（输入标签回车添加）</span>
                </div>

                <div class="cont">
                  <div v-for="(item, index) in dishFlavors" :key="index" class="items">
                    <div class="itTit">
                      <div class="selectInput">
                        <div>
                          <el-input v-model="item.name" type="text" style="width: 100%" placeholder="请输入口味"
                                    @focus="selectFlavor(true, index)" @blur="selectFlavor(false, index)"/>
                        </div>

                        <div v-show="item.show" class="flavorSelect">
                          <span v-for="(data, dataIndex) in dishFlavorsData" :key="dataIndex" class="items"
                                @click="addFlavorLabel(dataIndex, index)">{{ data.name }}</span>
                          <span v-if="dishFlavorsData.length == 0" class="none">无数据</span>
                        </div>
                      </div>
                    </div>

                    <div class="labItems" style="display: flex">
                      <span v-for="(flavor, flavorIndex) in item.value" :key="flavorIndex">{{ flavor }}
                        <i @click="delFlavorLabel(index, flavorIndex)">X</i>
                      </span>

                      <div class="inputBox" contenteditable="true" @focus="flavorPosition(index)"
                           @keydown.enter="(val)=>keyDownHandle(val,index)"/>
                    </div>

                    <span class="delFlavor delBut non" @click="delFlavor(index)">删除</span>
                  </div>
                </div>

                <div class="addBut" @click="addFlavor">
                  添加口味
                </div>
              </div>
            </div>
          </el-form-item>
        </el-form-item>

        <div/>

        <div>
          <el-form-item label="菜品图片:" prop="region">
            <!-- FIXME: 有意思, 这个地方竟然还要设置成 `/api/upload` 才能上传成功-->
            <el-upload class="avatar-uploader" action="/api/upload" :show-file-list="false"
                       :on-success="handleAvatarSuccess" :on-change="onChange">
              <img v-if="imageURL" :src="imageURL" class="avatar" alt=""/>
              <el-icon v-else class="avatar-uploader-icon">
                <Plus/>
              </el-icon>
            </el-upload>
          </el-form-item>
        </div>

        <div class="address">
          <el-form-item label="菜品描述:" prop="region">
            <el-input v-model="dishForm.description" type="textarea"
                      :rows="3" placeholder="菜品描述，最长200字" maxlength="200"/>
          </el-form-item>
        </div>

        <div class="subBox address">
          <el-form-item>
            <el-button size="large" @click="goBack()">
              取消
            </el-button>

            <el-button type="primary" size="large" @click="submitForm(false)">
              保存
            </el-button>

            <el-button v-if="actionType == 'add'" type="primary" class="continue" size="large"
                       @click="submitForm(true)">
              保存并继续添加菜品
            </el-button>
          </el-form-item>
        </div>
      </el-form>
    </div>
  </div>
</template>

<style lang="scss" scoped>
@use "@/assets/styles/common.scss" as *;
@use "@/assets/styles/button.scss" as *;
@use "@/assets/styles/table.scss" as *;

.avatar-uploader {
  .avatar {
    width: 178px;
    height: 178px;
    display: block;
  }
}

// 设置上传图片方框样式
:deep(.el-upload) {
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);

  &:hover {
    border-color: #ffc200;
  }
}

:deep(.el-icon.avatar-uploader-icon) {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 178px;
  text-align: center;
}

// 输入框和选择框样式
:deep(.el-input) {
  width: 293px;

  .el-input__wrapper {
    height: 40px;
    line-height: 40px;
  }
}

:deep(.el-select) {
  .el-select__wrapper {
    height: 40px;
    line-height: 40px;
  }

  width: 293px;
}
</style>