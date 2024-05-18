/**
 * 与分页有关的常用方法
 */
import {ref, watch} from "vue";

export let page = ref(1);
export let pageSize = ref(10);
export let total = ref(0);
export let pageSizes = [10, 20, 30, 40]
let init: Function;

export function initPage(_init: Function) {
    page.value = 1;
    pageSize.value = 10;
    init = _init;
}

watch(() => page.value, () => {
    init()
})

watch(() => pageSize.value, () => {
    init()
})

export function getImage(image: string) {
    return `/api/download?name=${image}`
}

/**
 * 获取 URL 地址上面的参数
 * */
export function getURLParam(args: string) {
    let url = location.href; //获取完整的请求url路径
    let arrStr = url.substring(url.indexOf("?") + 1).split("&");
    for (let i = 0; i < arrStr.length; i++) {
        let loc = arrStr[i].indexOf(args + "=");
        if (loc != -1)
            return arrStr[i].replace(args + "=", "").replace("?", "")
    }
    return ""
}
