import {ref} from "vue";
import router from "../router.ts";

export let goBackFlag = ref(false);
export let headTitle = ref('');
export let goBackURL = ref('');

export function reloadHeader(title: string, flag: boolean, url: string = "") {
    headTitle.value = title;
    goBackFlag.value = flag;
    goBackURL.value = url;
}

export function goBack() {
    router.push({path: goBackURL.value});
}