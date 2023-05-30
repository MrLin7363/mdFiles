import axios from '../util/axios';

export async function getUserList() {
    return await axios.get(
        `http://127.0.0.1:8003/test/list`)
        .then(resp => resp.data && resp.data)
        .catch(() => ({ status: 'error' })); 
}


export async function getUriList(data) {
    return await axios.post(
        `http://127.0.0.1:8003/test/detail`, data)
        .then(resp => resp.data && resp.data)
        .catch(() => ({ status: 'error' })); 
        // 返回 {request:100,successRate:"40"}
}