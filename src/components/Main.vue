<script setup lang="ts">
import {ref} from "vue";
import axiosRequest from "../utils/axiosRequest.ts";
import router from "../router.ts";
import {showNotify} from "vant";
import '@/utils/base.ts';
import {onMounted, computed, watch} from "vue";

interface Cart {
  id: string,
  name: string,
  userId: string,
  dishId: string,
  setMealId: string,
  // 菜品的口味信息, 注意这里叫 dishFlavor 而不是 flavors
  // 所以从 dish 添加到购物车中时要把 flavors 改成 dishFlavor
  dishFlavor: string[],
  number: number,
  amount: number,
  image: string
}

interface Dish {
  // 前端需要展示的数据
  id: string,
  name: string,
  categoryId: string,
  price: number,
  description: string,
  flavors: [],
  image: string,
  // 前端需要的额外数据
  number: number, // 购物车中该菜品的数量
  saleNum: number
}

let activeType = ref(0)
let categoryList = ref([])
let dishList = ref<Dish[]>([])
let cartData = ref<Cart[]>([])
let dialogFlavor = ref({
  name: '',
  // 这里的 flavors 中的每一项包含一个 name 和 value
  flavors: [],
  dishId: undefined,
  price: undefined,
  show: false,
  image: ''
})
let cartDialogShow = ref(false)
let detailsDialog = ref({
  show: false,
  item: {}
})
let setMealDialog = ref({
  show: false,
  item: {}
})

// 实时计算购物车中物品的数量
let goodsNum = computed(() => {
  let num = 0
  cartData.value.forEach(item => {
    num += item.number
  })
  if (num < 99)
    return num
  else
    return '99+'

})

// 实施计算购物车中物品的价格
let goodsPrice = computed(() => {
  let price = 0
  cartData.value.forEach(item => {
    price += (item.number * item.amount)
  })
  return price
})

watch(() => dialogFlavor.value.show, (flag) => {
  const divCart = document.querySelector('.divCart') as HTMLElement
  console.log("跳转到 watch 函数中来, flag", flag)
  if (flag)
    divCart.style.zIndex = String(1)
  else
    divCart.style.zIndex = String(3000)
  console.log("跳转到 watch 函数中来, divCart.style.zIndex", divCart.style.zIndex)
})

//获取所有的菜品分类
function categoryListApi() {
  return axiosRequest({
    url: '/category/list',
    method: 'get',
  })
}

//获取购物车内商品的集合
function cartListApi(data: any) {
  return axiosRequest({
    url: '/shoppingCart/list',
    method: 'get',
    params: {...data}
  })
}

async function getDishOrSetMealList(categoryId: string, type: number) {
  let url = '';
  if (type == 1)
    url = '/dish/list'
  else
    url = '/setMeal/list'

  try {
    let r = await axiosRequest({
      url: `${url}?categoryId=${categoryId}`,
      method: 'get',
    })

    if (r.code == 1) {
      let dishListValue = r.data
      let cartDataValue = cartData.value
      // 购物车和菜品列表界面的同步
      if (dishListValue.length > 0 && cartDataValue.length > 0) {
        dishListValue.forEach((dish: any) => {
          cartDataValue.forEach((cart: Cart) => {
            if (dish.id == cart.dishId)
              dish.number = cart.number
          })
        })
      }

      dishList.value = dishListValue.map((item: any) => {
        return {
          id: item.id,
          name: item.name,
          categoryId: item.categoryId,
          price: item.price,
          description: item.description,
          flavors: item.flavors,
          image: item.image,
          number: item.number,
          saleNum: 0
        }
      }) || []
    } else
      throw new Error(r.msg)
  } catch (e: any) {
    showNotify({type: 'warning', message: e.message});
  }
}

function getDishList(categoryId: string) {
  getDishOrSetMealList(categoryId, 1)
}

function getSetMealData(categoryId: string) {
  getDishOrSetMealList(categoryId, 2)
}

async function getCartData() {
  try {
    let r = await axiosRequest({
      url: '/shoppingCart/list',
      method: 'get',
    })

    if (r.code == 1) {
      cartData.value = r.data.map((item: any) => {
        return {
          id: item.id,
          name: item.name,
          userId: item.userId,
          dishId: item.dishId,
          setMealId: item.setMealId,
          dishFlavor: item.dishFlavor,
          number: item.number,
          amount: item.amount,
          image: item.image
        }
      }) || []
    } else
      throw new Error(r.msg)
  } catch (e: any) {
    showNotify({type: 'warning', message: e.message});
  }
}

// 购物车中增加商品数量
async function cartNumAdd(item: any) {
  await addCart(item)
}

// 购物车中减少商品数量
async function cartNumberSubtract(item: any) {
  await subtractCart(item)
}


// 清空购物车
async function clearCart() {
  try {
    let r = await axiosRequest({
      url: '/shoppingCart/clean',
      method: 'delete',
    })

    if (r.code == 1) {
      for (let ele of dishList.value)
        ele.number = undefined

      cartData.value = []
      cartDialogShow.value = false
    } else
      throw new Error(r.msg)
  } catch (e: any) {
    showNotify({type: 'warning', message: e.meassage});
  }
}


/**
 * 规格选择
 * @param item
 */
function chooseFlavor(item: any) {
  console.log("跳转到 chooseFlavor 中来了")
  // TODO: 为什么有这部分代码呢?
  // dialogFlavor.value = {
  //   name: '',
  //   flavors: [],
  //   dishId: undefined,
  //   price: undefined,
  //   show: false
  // }
  dialogFlavor.value = {
    name: item.name,
    flavors: item.flavors,
    dishId: item.id,
    price: item.price,
    show: true,
    image: item.image
  }
  console.log("dialogFlavor.value: ", dialogFlavor.value)
}

function flavorClick(flavor: any, item: any) {
  flavor.dishFlavor = item
  // 强制刷新dialog的dom
  dialogFlavor.value.show = false
  dialogFlavor.value.show = true
}

// 选择规格加入购物车
function dialogFlavorAddCart() {
  const dialogFlavorValue = dialogFlavor.value
  let flag = true
  let dishFlavorValue = []
  dialogFlavorValue.flavors.forEach(item => {
    if (item.dishFlavor) {
      console.log("item.dishFlavor: ", item.dishFlavor)
      dishFlavorValue.push(item.dishFlavor)
    } else {
      flag = false
      showNotify({type: 'warning', message: '请选择' + item.name});
    }
  })
  if (flag) {
    addCart({
      price: dialogFlavorValue.price,
      dishFlavor: dishFlavorValue,
      id: dialogFlavorValue.dishId,
      flavors: [],
      image: dialogFlavorValue.image,
      name: dialogFlavorValue.name
    })
    dialogFlavor.value.show = false
  }
}

//网络图片路径转换
function imgPathConvert(path: string) {
  return '/download?name=' + path
}

//跳转到去结算界面
function toAddOrderPage() {
  if (cartData.value.length > 0)
    router.push('/addOrder')
}

function toUserPage() {
  router.push('/user')
}

async function dishDetails(item: any) {
  console.log("跳转到 dishDetails 中来了")
  console.log("item: ", item)
  // 先清除对象数据，如果不行的话dialog使用v-if
  detailsDialog.value.item = {}
  setMealDialog.value.item = {}
  if (Array.isArray(item.flavors)) {
    console.log("item.flavors: ", item.flavors)
    detailsDialog.value.item = JSON.parse(JSON.stringify(item))
    detailsDialog.value.show = true
    console.log("detailsDialog.value: ", detailsDialog.value)
  } else {
    // 显示套餐的数据
    try {
      let r = await axiosRequest({
        url: `/setMeal/dish/${item.id}`,
        method: 'get',
      })

      if (r.code == 1) {
        setMealDialog.value.item = {...JSON.parse(JSON.stringify(item)), list: r.data}
        setMealDialog.value.show = true
      }
      throw new Error(r.msg)
    } catch (e: any) {
      showNotify({type: 'warning', message: e.message});
    }
  }
}

// 菜单中往购物车中添加商品
async function addCart(item: any) {
  let params = {
    amount: item.price / 100,//金额
    dishFlavor: item.dishFlavor, //口味  如果没有传undefined
    dishId: undefined,//菜品id
    setMealId: undefined,//套餐id
    name: item.name,
    image: item.image
  }
  // 如果有口味, 则说明时菜品, 否则说明是套餐
  if (Array.isArray(item.flavors))
    params.dishId = item.id
  else
    params.setMealId = item.id

  try {
    let r = await axiosRequest({
      url: '/shoppingCart/add',
      method: 'post',
      data: params
    })
    // 成功向后端发送数据后, 根据后端返回的数据对购物车数据进行同步
    if (r.code == 1) {
      dishList.value.forEach(dish => {
        if (dish.id === item.id)
          dish.number = r.data.number
      })
      if (setMealDialog.value.show)
        item.number = r.data.number

      await getCartData()
    } else
      throw new Error(r.msg)
  } catch (e: any) {
    showNotify({type: 'warning', message: e.message});
  }
}

// 菜单中减少选中的商品
// TODO: 后端的处理逻辑是将该物品清零
async function subtractCart(item: any) {
  let params: {}
  if (Array.isArray(item.flavors))
    params = {
      dishId: item.id,
    }
  else
    params = {
      setMealId: item.id,
    }

  try {
    let r = await axiosRequest({
      url: '/shoppingCart/sub',
      method: 'post',
      params: params
    })

    if (r.code == 1) { // 删除成功, 则把购物车里的东西也一并删除掉
      dishList.value.forEach(dish => {
        if (dish.id === item.id)
          dish.number = (r.data.number === 0 ? undefined : r.data.number)
      })
      if (setMealDialog.value.show)
        item.number = (r.data.number === 0 ? undefined : r.data.number)
      await getCartData()
    } else
      throw new Error(r.msg)
  } catch (e) {
    showNotify({type: 'warning', message: e.message});
  }
}

// 展开购物车
function openCart() {
  if (cartData.value.length > 0)
    cartDialogShow.value = true
}

function initData() {
  Promise.all([categoryListApi(), cartListApi({})]).then(r => {
    //获取分类数据
    if (r[0].code === 1) {
      categoryList.value = r[0].data
      if (Array.isArray(r[0].data) && r[0].data.length > 0) {
        if (r[0].data[0].type === 1)
          getDishList(r[0].data[0].id)
        else
          getSetMealData(r[0].data[0].id)

      }
    } else {
      showNotify({type: 'warning', message: r[0].msg});
    }
    //获取菜品数据
    if (r[1].code === 1)
      cartData.value = r[1].data
    else
      showNotify({type: 'warning', message: r[1].msg});

  })
}

onMounted(() => {
  initData()
})

/**
 * 修改分类时, 获取菜品或者套餐数据
 * @param index
 * @param id
 * @param type
 */
function changeCate(index: any, id: string, type: number) {
  activeType.value = index
  if (type === 1) //菜品
    getDishList(id)
  else
    getSetMealData(id)
}

</script>

<template>
  <div id="main" class="app">
    <!-- 点菜界面表头 -->
    <div class="divHead">
      <img src="../assets/images/user.png" @click="toUserPage" alt=""/>
    </div>
    <div class="divTitle">
      <div class="divStatic">
        <img src="../assets/images/logo.png" class="logo" alt=""/>
        <div class="divDesc">
          <div class="divName">菩提阁餐厅</div>
          <div class="divSend">
            <span><img src="../assets/images/time.png" alt=""/> 距离1.5km</span>
            <span><img src="../assets/images/money.png" alt=""/> 配送费6元</span>
            <span><img src="../assets/images/location.png" alt=""/> 预计时长12min</span>
          </div>
        </div>
      </div>
      <div class="divDesc">
        简介: 菩提阁中餐厅是菩提阁点餐的独立的品牌，定位“大众 化的美食外送餐饮”，旨为顾客打造专业美食。
      </div>
    </div>

    <div class="divBody">
      <!-- 侧边栏 -->
      <div class="divType">
        <ul>
          <li v-for="(item,index) in categoryList" :key="index" @click="changeCate(index, item.id, item.type)"
              :class="{active:activeType === index}">{{ item.name }}
          </li>
        </ul>
      </div>

      <div class="divMenu">
        <div>
          <div class="divItem" v-for="(item,index) in dishList" :key="index" @click="dishDetails(item)">
            <!-- 菜品图片 -->
            <el-image :src="imgPathConvert(item.image)">
              <div slot="error" class="image-slot">
                <img src="../assets/images/noImg.png" alt=""/>
              </div>
            </el-image>
            <!-- 菜品各项信息 -->
            <div>
              <div class="divName">{{ item.name }}</div>
              <div class="divDesc">{{ item.description }}</div>
              <!-- 月销这个东西没有用哈 -->
              <div class="divDesc">{{ '月销' + (item.saleNum ? item.saleNum : 0) }}</div>
              <div class="divBottom"><span>￥</span><span>{{ item.price / 100 }}</span></div>

              <!-- 用户选择该菜品的个数 -->
              <div class="divNum">
                <div class="divSubtract" v-if="item.number > 0">
                  <img src="../assets/images/subtract.png" @click.prevent.stop="subtractCart(item)" alt=""/>
                </div>
                <div class="divDishNum">{{ item.number }}</div>
                <div class="divTypes" v-if="item.flavors && item.flavors.length > 0 && !item.number "
                     @click.prevent.stop="chooseFlavor(item)">选择规格
                </div>
                <div class="divAdd" v-else>
                  <img src="../assets/images/add.png" @click.prevent.stop="addCart(item)" alt=""/>
                </div>
              </div>
            </div>

          </div>
        </div>
      </div>
    </div>

    <div class="divLayer">
      <div class="divLayerLeft"></div>
      <div class="divLayerRight"></div>
    </div>

    <!-- 显示购物车中物品数量 -->
    <div class="divCart" v-if="categoryList.length > 0">
      <!-- 打开购物车 -->
      <div :class="{imgCartActive: cartData && cartData.length > 0, imgCart:!cartData || cartData.length<1}"
           @click="openCart"></div>
      <!-- 购物车中物品数量 -->
      <div :class="{divGoodsNum:1===1, moreGoods:cartData && cartData.length > 99}"
           v-if="cartData && cartData.length > 0">{{ goodsNum }}
      </div>
      <!-- 购物车中物品总价格 -->
      <div class="divNum">
        <span>￥</span>
        <span>{{ goodsPrice }}</span>
      </div>
      <!-- 去结算界面 -->
      <div class="divPrice"></div>
      <div :class="{btnSubmitActive: cartData && cartData.length > 0, btnSubmit:!cartData || cartData.length<1}"
           @click="toAddOrderPage">去结算
      </div>
    </div>

    <van-dialog v-model="dialogFlavor.show" :show-confirm-button="false" class="dialogFlavor" ref="flavorDialog">
      <div class="dialogTitle">{{ dialogFlavor.name }}</div>
      <div class="divContent">
        <div v-for="flavor in dialogFlavor.flavors" :key="flavor.id">
          <div class="divFlavorTitle">{{ flavor.name }}</div>

          <span v-for="item in flavor.value" :key="item" @click="flavorClick(flavor, item)"
                :class="{spanActive:flavor.dishFlavor === item}">
                      {{ item }}
          </span>

          <!--          <span v-for="item in JSON.parse(flavor.value)" :key="item" @click="flavorClick(flavor,item)"-->
          <!--                :class="{spanActive:flavor.dishFlavor === item}">-->
          <!--            {{ item }}-->
          <!--          </span>-->
        </div>
      </div>
      <div class="divBottom">
        <div><span class="spanMoney">￥</span>{{ dialogFlavor.price / 100 }}</div>
        <div @click="dialogFlavorAddCart">加入购物车</div>
      </div>
      <div class="divFlavorClose" @click="dialogFlavor.show = false">
        <img src="../assets/images/close.png" alt=""/>
      </div>
    </van-dialog>

    <van-popup v-model="cartDialogShow" position="bottom" :style="{ height: '50%' }" class="dialogCart">
      <div class="divCartTitle">
        <div class="title">购物车</div>
        <div class="clear" @click="clearCart">
          <i class="el-icon-delete"/> 清空
        </div>
      </div>
      <div class="divCartContent">
        <div v-for="item in cartData" :key="item.id" class="divCartItem">
          <el-image :src="imgPathConvert(item.image)">
            <div slot="error" class="image-slot">
              <img src="../assets/images/noImg.png" alt=""/>
            </div>
          </el-image>
          <div class="divDesc">
            <div class="name">{{ item.name }}</div>
            <div class="price">
              <span class="spanMoney">￥</span>{{ item.amount }}
            </div>
          </div>
          <div class="divNum">
            <div class="divSubtract">
              <img src="../assets/images/subtract.png" @click="cartNumberSubtract(item)" alt=""/>
            </div>
            <div class="divDishNum">{{ item.number }}</div>
            <div class="divAdd">
              <img src="../assets/images/add.png" @click="cartNumAdd(item)" alt=""/>
            </div>
          </div>
          <div class="divSplit"></div>
        </div>
      </div>
    </van-popup>

    <!-- 菜品详情页 -->
    <van-dialog  v-if="detailsDialog.show" :show-confirm-button="false" class="detailsDialog">
      <div class="divContainer">
        <el-image :src="imgPathConvert(detailsDialog.item.image)">
          <div slot="error" class="image-slot">
            <img src="../assets/images/noImg.png" alt=""/>
          </div>
        </el-image>
        <div class="title">{{ detailsDialog.item.name }}</div>
        <div class="content">{{ detailsDialog.item.description }}</div>
      </div>

      <div class="divNum">
        <div class="left">
          <span>￥</span><span>{{ detailsDialog.item.price / 100 }}</span>
        </div>
        <div class="right">
          <div class="divSubtract" v-if="detailsDialog.item.number > 0">
            <img src="../assets/images/subtract.png" @click="subtractCart(detailsDialog.item)"/>
          </div>
          <div class="divDishNum">{{ detailsDialog.item.number }}</div>
          <div class="divTypes"
               v-if="detailsDialog.item.flavors && detailsDialog.item.flavors.length > 0 && !detailsDialog.item.number "
               @click="chooseFlavor(detailsDialog.item)">选择规格
          </div>
          <div class="divAdd" v-else>
            <img src="../assets/images/add.png" @click="addCart(detailsDialog.item)"/>
          </div>
        </div>
      </div>

      <div class="detailsDialogClose" @click="detailsDialog.show = false">
        <img src="../assets/images/close.png" alt=""/>
      </div>
    </van-dialog>

    <van-dialog v-model="setMealDialog.show" :show-confirm-button="false" class="setMealDetailsDialog"
                ref="setMealDetailsDialogd" v-if="setMealDialog.show">
      <div class="divContainer">
        <div class="title">{{ setMealDialog.item.name }}</div>
        <div class="item" v-for="(item,index) in setMealDialog.item.list" :key="index">
          <el-image :src="imgPathConvert(item.image)">
            <div slot="error" class="image-slot">
              <img src="../assets/images/noImg.png" alt=""/>
            </div>
          </el-image>
          <div class="divSubTitle">{{ item.name + '(' + item.copies + '份)' }}
            <div class="divPrice">
              <span>￥</span><span>{{ item.price / 100 }}</span>
            </div>
          </div>
          <div class="content">{{ item.description }}</div>
        </div>
      </div>

      <div class="divNum">
        <div class="left">
          <span>￥</span><span>{{ setMealDialog.item.price / 100 }}</span>
        </div>
        <div class="right">
          <div class="divSubtract" v-if="setMealDialog.item.number > 0">
            <img src="../assets/images/subtract.png" @click="subtractCart(setMealDialog.item)" alt=""/>
          </div>
          <div class="divDishNum">{{ setMealDialog.item.number }}</div>
          <div class="divAdd" v-if="setMealDialog.item.number">
            <img src="../assets/images/add.png" @click="addCart(setMealDialog.item)" alt=""/>
          </div>
          <div class="addCart" @click="addCart(setMealDialog.item)" v-if="!setMealDialog.item.number">加入购物车</div>
        </div>
      </div>
      <div class="detailsDialogClose" @click="setMealDialog.show = false">
        <img src="../assets/images/close.png" alt=""/>
      </div>
    </van-dialog>
  </div>
</template>

<style scoped lang="scss">
@use "@/assets/styles/login.scss" as *;
@use "@/assets/styles/main.scss" as *;
</style>