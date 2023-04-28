import axios from 'axios';

axios.defaults.timeout=60000;
axios.interceptors.request.use(
    request => {
        return request;
    },
    error => {
        return Promise.reject(error.response);
    }
);

axios.interceptors.response.use((res) => {
    // 可以进行一些简单操作
    return res;
}, (error) => {
    console.log(error);
    return Promise.reject(error);
});

export default axios;
