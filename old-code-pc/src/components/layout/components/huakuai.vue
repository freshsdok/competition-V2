<!-- DragCaptcha.vue -->
<template>
  <div class="captcha-container" >
    <!-- 带缺口的背景图 + 可移动的拼图块 -->


    <div class="captcha-bg-wrapper">
          <div @click="reset" style="    position: absolute;
    right: 10px;
    top: 10px;"><img src="@/assets/icon/shuaxinyanzhengma.png" style="width: 18px;cursor: pointer;" alt=""></div>
      <img :src="bgImageWithGap" class="captcha-bg" alt="验证码背景" />
       
      <!-- 可移动的拼图块（叠加在背景图上） -->
      <div
        class="moving-piece"
        :style="{ left: `${pieceLeft}PX`, top: `${pieceTop}PX` }"
      >
        <img
          v-if="pieceImage"
          :src="pieceImage"
          class="piece-img"
          alt="拼图块"
        />
      </div>
    </div>

    <!-- 滑轨 -->
    <div class="track" ref="trackRef">
      <div
        class="slider-thumb"
        ref="thumbRef"
        @mousedown="startDrag"
        @touchstart="startDrag"
      ></div>
          <div class="slider-text">{{ sliderText }}</div>
    </div>

  </div>


</template>

<script setup>
import { ref, onMounted, nextTick, computed } from "vue";
import { useCounterStore } from "@/stores/index";
import img1 from "@/assets/huakuaitu/1.jpg"
import img2 from "@/assets/huakuaitu/2.jpg"
import img3 from "@/assets/huakuaitu/3.jpg"
import img4 from "@/assets/huakuaitu/4.jpg"
import img5 from "@/assets/huakuaitu/5.jpg"
const counterStore = useCounterStore();
const emit = defineEmits(['close'])
// 状态
const isVerified = ref(false);
const isDragging = ref(false);
const isSuccess = ref(false);
const sliderText = ref("向右拖动滑块完成拼图");
const thumbRef = ref(null);
const trackRef = ref(null);

// 图像数据
const originalImage = ref("");
const bgImageWithGap = ref("");
const pieceImage = ref("");
const gapPosition = ref(0); // 缺口水平位置（固定）
const pieceLeft = ref(0); // 拼图块在图片上的左位置
const gapWidth = 40;
const gapHeight = 40;
const pieceTop =ref(Math.floor(Math.random() * 100)) ; // 固定垂直位置

// 固定图片尺寸
const imageWidth = 300;
const imageHeight = 150;

const maxTrackPos = computed(() => {
  return imageWidth - gapWidth;
});

// 重置验证码
async function reset() {
  isVerified.value = false;
  isDragging.value = false;
  isSuccess.value = false;
  sliderText.value = "向右拖动滑块完成拼图";
  pieceLeft.value = 0;
  pieceTop.value=Math.floor(Math.random() * 100)
  var ss=[img1,img2,img3,img4,img5]
  let url = ss[Math.floor(Math.random() * 5) ]
  
  originalImage.value = url;

  await nextTick();
  await generateImages(url);
}

// 生成带缺口的背景图 + 拼图块
function generateImages(imgUrl) {
  return new Promise((resolve) => {
    const img = new Image();
    img.crossOrigin = "anonymous";
    img.src = imgUrl;
    img.onload = () => {
      const canvasBg = document.createElement("canvas");
      const ctxBg = canvasBg.getContext("2d");
      canvasBg.width = imageWidth;
      canvasBg.height = imageHeight;

      const canvasPiece = document.createElement("canvas");
      const ctxPiece = canvasPiece.getContext("2d");
      canvasPiece.width = gapWidth;
      canvasPiece.height = gapHeight;

      // 绘制完整背景
      ctxBg.drawImage(img, 0, 0, imageWidth, imageHeight);

      // 随机缺口位置（水平）
      const maxWidth = imageWidth - gapWidth;
      const left = Math.floor(50 + Math.random() * (maxWidth - 100));
      gapPosition.value = left;

      // 在背景图上“挖洞”（填充白色）
      ctxBg.fillStyle = "#fff";
      ctxBg.fillRect(left, pieceTop.value, gapWidth, gapHeight);

      // 提取拼图块（从原图相同位置裁剪）
      ctxPiece.drawImage(
        img,
        left,
        pieceTop.value,
        gapWidth,
        gapHeight,
        0,
        0,
        gapWidth,
        gapHeight
      );

      // 转为 Data URL
      bgImageWithGap.value = canvasBg.toDataURL("image/png");
      pieceImage.value = canvasPiece.toDataURL("image/png");

      resolve();
    };
    img.onerror = () => {
      console.error("图片加载失败");
      resolve();
    };
  });
}


onMounted(() => {
  reset();
});

// 拖拽逻辑
function startDrag(e) {
  if (isSuccess.value || isDragging.value) return;
  isDragging.value = true;
  e.preventDefault();

  const clientX = "touches" in e ? e.touches[0].clientX : e.clientX;
  const startPos = parseInt(thumbRef.value.style.left) || 0;

  const moveHandler = (e) => {
    if (!isDragging.value) return;
    const currentX = "touches" in e ? e.touches[0].clientX : e.clientX;
    let newX = startPos + (currentX - clientX);
    newX = Math.max(0, Math.min(newX, maxTrackPos.value));
    thumbRef.value.style.left = `${newX}PX`;

    // 同步更新拼图块位置
    pieceLeft.value = newX;
  };

  const endHandler = () => {
    isDragging.value = false;
    const tolerance = 10;
    console.log(pieceLeft.value, "拼图块位置");
    console.log(gapPosition.value, "空白块位置");
    if (Math.abs(pieceLeft.value - gapPosition.value) <= tolerance) {
      success();
    } else {
      fail();
    }

    // 清理事件
    document.removeEventListener("mousemove", moveHandler);
    document.removeEventListener("mouseup", endHandler);
    document.removeEventListener("touchmove", moveHandler, { passive: false });
    document.removeEventListener("touchend", endHandler);
  };

  document.addEventListener("mousemove", moveHandler);
  document.addEventListener("mouseup", endHandler);
  document.addEventListener("touchmove", moveHandler, { passive: false });
  document.addEventListener("touchend", endHandler);
}

function success() {
  isSuccess.value = true;
  sliderText.value = "验证通过";
  setTimeout(() => {

    counterStore.shimingtrue()
     emit('close')
    // isVerified.value = true;
  }, 500);
}

function fail() {
  sliderText.value = "再试一次";
  thumbRef.value.style.left = "0PX";
  pieceLeft.value = 0;
  setTimeout(() => {
    if (!isSuccess.value) sliderText.value = "向右拖动滑块完成拼图";
  }, 1000);
}
</script>

<style scoped>
.captcha-container {
  width: 300PX;
  position: relative;
  user-select: none;
  font-family: -apple-system, BlinkMacSystemFont, sans-serif;
  touch-action: none;
}

/* 背景图容器 */
.captcha-bg-wrapper {
  position: relative;
  width: 300PX;
  height: 150PX;
  overflow: hidden;
}

/* 背景图 */
.captcha-bg {
  width: 300PX;
  height: 150PX;
  object-fit: none; /* 关键！禁止缩放 */
  border-radius: 4PX;
  display: block;
}

/* 可移动的拼图块 */
.moving-piece {
  position: absolute;
  width: 40PX;
  height: 40PX;
  top: 25PX;
  left: 0;
  transition: left 0.1s ease;
  z-index: 1000;
}

.piece-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 4PX;
}

/* 滑轨 */
.track {
  width: 300PX; /* 与图片宽度一致 */
  height: 40PX;
  background: #e0e0e0;
  border-radius: 20PX;
  margin-top: 12PX;
  position: relative;
  cursor: pointer;
  box-shadow: inset 0 1PX 3PX rgba(0, 0, 0, 0.1);
}

/* 滑块（仅用于拖动） */
.slider-thumb {
  position: absolute;
  top: 0;
  left: 0;
  width: 40PX;
  height: 40PX;
  background: white;
  border: 2PX solid #ccc;
  border-radius: 50%;
  box-shadow: 0 2PX 6PX rgba(0, 0, 0, 0.2);
  cursor: grab;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.1s;
}

.slider-thumb:active {
  cursor: grabbing;
  transform: scale(1.05);
}

.slider-text {
  text-align: center;
  color: #777;
  font-size: 13PX;
  margin-top: 8PX;
  min-height: 18PX;
  line-height: 40PX;
}

.captcha-success {
  padding: 20PX;
  text-align: center;
  color: #67c23a;
  font-weight: bold;
}

.captcha-success button {
  margin-top: 10PX;
  padding: 6PX 12PX;
  background: #67c23a;
  color: white;
  border: none;
  border-radius: 4PX;
  cursor: pointer;
  font-size: 14PX;
}
</style>