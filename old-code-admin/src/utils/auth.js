import Cookies from 'js-cookie'

const TokenKey = 'PC-Token'

const ExpiresInKey = 'PC-Expires-In'

export function getToken() {
  return Cookies.get(TokenKey)
}

export function setToken(token) {
  return Cookies.set(TokenKey, token)
}

export function removeToken() {
  return Cookies.remove(TokenKey)
}

export function getExpiresIn() {
  return Cookies.get(ExpiresInKey) || -1
}

export function setExpiresIn(time) {
  return Cookies.set(ExpiresInKey, time)
}

export function removeExpiresIn() {
  return Cookies.remove(ExpiresInKey)
}

export function getPathByOrgId(list, targetId) {
  const path = [];
  let currentId = targetId;

  while (currentId !== null && currentId !== undefined) {
    // 找到当前 id 对应的节点
    const node = list.find(item => item.orgId == currentId);
    if (node) {
      path.unshift(node.orgId); // 加入路径
      currentId = node.parentId; // 继续向上找父节点

      // 如果 parentId 是 0 或 null，停止（根节点）
      if (node.parentId === 0 || node.parentId === null) break;
    } else {
      break; // 找不到节点也退出
    }
  }

  return path;
}

export function getPathByOrg(list, targetId) {
  const path = [];
  let currentId = targetId;

  while (currentId !== null && currentId !== undefined) {
    // 找到当前 id 对应的节点
    const node = list.find(item => item.orgId === currentId);
    
    if (node) {
      path.unshift(node); // 加入路径
      currentId = node.parentId; // 继续向上找父节点

      // 如果 parentId 是 0 或 null，停止（根节点）
      if (node.parentId === 0 || node.parentId === null) break;
    } else {
      break; // 找不到节点也退出
    }
  }

  return path;
}