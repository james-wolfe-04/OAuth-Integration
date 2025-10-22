import axios from 'axios';
import { getCookie } from './csrf';

axios.defaults.withCredentials = true; // send cookies
axios.interceptors.request.use(config => {
  const token = getCookie('XSRF-TOKEN') || getCookie('XSRF-TOKEN'.toLowerCase());
  if (token) config.headers['X-XSRF-TOKEN'] = token;
  return config;
});
export default axios;
