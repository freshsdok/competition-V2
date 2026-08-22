<template>
    <div id="indexLizi" ref="containerRef" />
  </template>

<script setup>
import { ref, onMounted, onBeforeUnmount, onActivated, onDeactivated, defineProps } from 'vue'
import * as THREE from 'three'

// Props
const props = defineProps({
  amountX: {
    type: Number,
    default: 80
  },
  amountY: {
    type: Number,
    default: 50
  },
  color: {
    type: Number,
    default: 0xffffff
  },
  top: {
    type: Number,
    default: 500
  }
})
// DOM 引用
const containerRef = ref(null)

// 鼠标位置
const mouseX = ref(0)
const windowHalfX = ref(window.innerWidth / 2)

// Three.js 实例（markRaw 避免响应式代理）
let camera = null
let scene = null
let particles = null
let renderer = null
let animationId = null

// 初始化
const init = () => {
  const SEPARATION = 60
  const container = containerRef.value
  if (!container) return

  // 设置容器样式
  container.style.position = 'absolute'
  container.style.top = `${props.top}px`
  container.style.height = `${window.innerHeight - props.top}px`

  // 相机
  camera = new THREE.PerspectiveCamera(75, window.innerWidth / window.innerHeight, 1, 10000)
  camera.position.z = 1000

  // 场景
  scene = new THREE.Scene()

  // 粒子数量
  const numParticles = props.amountX * props.amountY
  const positions = new Float32Array(numParticles * 3)
  const scales = new Float32Array(numParticles)

  let i = 0
  let j = 0
  for (let ix = 0; ix < props.amountX; ix++) {
    for (let iy = 0; iy < props.amountY; iy++) {
      positions[i] = ix * SEPARATION - ((props.amountX * SEPARATION) / 2)
      positions[i + 1] = 0
      positions[i + 2] = iy * SEPARATION - ((props.amountY * SEPARATION) / 2)
      scales[j] = 1
      i += 3
      j++
    }
  }

  // 几何体（使用 setAttribute）
  const geometry = new THREE.BufferGeometry()
  geometry.setAttribute('position', new THREE.Float32BufferAttribute(positions, 3))
  geometry.setAttribute('scale', new THREE.Float32BufferAttribute(scales, 1))

  // 材质（Shader）
  const material = new THREE.ShaderMaterial({
    uniforms: {
      color: { value: new THREE.Color(props.color) }
    },
    vertexShader: `
        attribute float scale;
        void main() {
          vec4 mvPosition = modelViewMatrix * vec4(position, 1.0);
          gl_PointSize = scale * (180.0 / -mvPosition.z);
          gl_Position = projectionMatrix * mvPosition;
        }
      `,
    fragmentShader: `
        uniform vec3 color;
        void main() {
          if (length(gl_PointCoord - vec2(0.5, 0.5)) > 0.475) discard;
          gl_FragColor = vec4(color, 1.0);
        }
      `
  })

  // 粒子系统
  particles = new THREE.Points(geometry, material)
  scene.add(particles)

  // 渲染器
  renderer = new THREE.WebGLRenderer({ antialias: true, alpha: true })
  renderer.setPixelRatio(window.devicePixelRatio)
  renderer.setSize(container.clientWidth, container.clientHeight)
  renderer.setClearAlpha(0) // 背景透明
  container.appendChild(renderer.domElement)

  // 事件监听
  window.addEventListener('resize', onWindowResize, { passive: false })
  document.addEventListener('mousemove', onDocumentMouseMove, { passive: false })
  document.addEventListener('touchstart', onDocumentTouchStart, { passive: false })
  document.addEventListener('touchmove', onDocumentTouchMove, { passive: false })
}

// 动画循环
const animate = () => {
  animationId = requestAnimationFrame(animate)
  render()
}

// 渲染
const render = () => {
  const time = Date.now() * 0.001
  const waveSpeed = 1.0 // 调整这里：0.3~1.0

  camera.position.x += (mouseX.value - camera.position.x) * 0.05
  camera.position.y = 400
  camera.lookAt(scene.position)

  const positions = particles.geometry.attributes.position.array
  const scales = particles.geometry.attributes.scale.array

  let i = 0
  let j = 0
  for (let ix = 0; ix < props.amountX; ix++) {
    for (let iy = 0; iy < props.amountY; iy++) {
      const phase = (ix * 0.1 + iy * 0.1) // 创造波浪推进感

      positions[i + 1] = Math.sin((ix + time) * 0.2 * waveSpeed + phase) * 150 +
        Math.sin((iy + time) * 0.4 * waveSpeed + phase) * 100

      scales[j] = (Math.sin((ix + time) * 0.1 * waveSpeed + phase) + 1) * 6 +
        (Math.sin((iy + time) * 0.2 * waveSpeed + phase) + 1) * 6

      i += 3
      j++
    }
  }

  particles.geometry.attributes.position.needsUpdate = true
  particles.geometry.attributes.scale.needsUpdate = true

  renderer.render(scene, camera)
}

// 事件处理
const onDocumentMouseMove = (event) => {
  mouseX.value = event.clientX - windowHalfX.value
}

const onDocumentTouchStart = (event) => {
  if (event.touches.length === 1) {
    mouseX.value = event.touches[0].pageX - windowHalfX.value
  }
}

const onDocumentTouchMove = (event) => {
  if (event.touches.length === 1) {
    event.preventDefault()
    mouseX.value = event.touches[0].pageX - windowHalfX.value
  }
}

const onWindowResize = () => {
  windowHalfX.value = window.innerWidth / 2
  camera.aspect = window.innerWidth / window.innerHeight
  camera.updateProjectionMatrix()
  renderer.setSize(window.innerWidth, window.innerHeight)
}

// 生命周期
onMounted(() => {
  init()
  animate()
})

onBeforeUnmount(() => {
  // 清理事件
  window.removeEventListener('resize', onWindowResize)
  document.removeEventListener('mousemove', onDocumentMouseMove)
  document.removeEventListener('touchstart', onDocumentTouchStart)
  document.removeEventListener('touchmove', onDocumentTouchMove)

  // 释放 Three.js 资源
  if (animationId) cancelAnimationFrame(animationId)
  if (renderer) {
    renderer.dispose()
    renderer = null
  }
  if (scene) {
    scene.clear()
    scene = null
  }
  particles = null
  camera = null
})

// 支持 keep-alive
onActivated(() => {
  if (!animationId) animate()
})

onDeactivated(() => {
  if (animationId) {
    cancelAnimationFrame(animationId)
    animationId = null
  }
})
</script>

<style scoped>
#indexLizi {
  width: 100%;
  pointer-events: none;
  /* 避免遮挡其他元素点击 */
}
</style>