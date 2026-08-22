import Cookies from "js-cookie";

const TokenKey = "Adminpc-Token";
const info = "authinfo";
export function getToken() {
  return Cookies.get(TokenKey);
}

export function setToken(token) {
  return Cookies.set(TokenKey, token);
}

export function removeToken() {
  return Cookies.remove(TokenKey);
}
export function getinfo() {
  return localStorage.getItem(info)
}

export function setinfo(authinfo) {
  return localStorage.setItem(info, authinfo);
}

export function removeinfo() {
 return localStorage.removeItem(info);
}
export function generatePwd() {
  const chars =
    "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!#$%^&*";
  let password = "";
  for (let i = 0; i < 16; i++) {
    const randomIndex = Math.floor(Math.random() * chars.length);
    password += chars[randomIndex];
  }
  return password;
}
