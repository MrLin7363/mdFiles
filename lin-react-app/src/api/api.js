import axios from '../util/axios';

export async function getUserList() {
    return await axios.get(
        `http://127.0.0.1:8003/vuln/redis/api/v1/lin/list`)
        .then(resp => resp.data && resp.data)
        .catch(() => ({ status: 'error' })); 
}