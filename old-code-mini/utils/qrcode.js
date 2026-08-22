const ECC_CODEWORDS_PER_BLOCK = [
  -1, 10, 16, 26, 18, 24, 16, 18, 22, 22, 26, 30, 22, 22, 24, 24, 28, 28, 26,
  26, 26, 26, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28,
  28, 28, 28
]

const NUM_ERROR_CORRECTION_BLOCKS = [
  -1, 1, 1, 1, 2, 2, 4, 4, 4, 5, 5, 5, 8, 9, 9, 10, 10, 11, 13, 14, 16, 17,
  17, 18, 20, 21, 23, 25, 26, 28, 29, 31, 33, 35, 37, 38, 40, 43, 45, 47, 49
]

const MIN_VERSION = 1
const MAX_VERSION = 40
const FORMAT_BITS_MEDIUM = 0

export function createQrModules(text) {
  const bytes = utf8Bytes(String(text || ''))
  const version = findVersion(bytes)
  const size = version * 4 + 17
  const modules = createGrid(size, false)
  const isFunction = createGrid(size, false)

  drawFunctionPatterns(modules, isFunction, version)
  drawCodewords(modules, isFunction, addEccAndInterleave(buildDataCodewords(bytes, version), version))
  applyMask(modules, isFunction, 0)
  drawFormatBits(modules, isFunction, 0)
  return modules
}

function findVersion(bytes) {
  for (let version = MIN_VERSION; version <= MAX_VERSION; version++) {
    const countBits = version < 10 ? 8 : 16
    const dataCapacityBits = getNumDataCodewords(version) * 8
    if (bytes.length < (1 << countBits) && 4 + countBits + bytes.length * 8 <= dataCapacityBits) {
      return version
    }
  }
  throw new Error('QR content too long')
}

function buildDataCodewords(bytes, version) {
  const bits = []
  const countBits = version < 10 ? 8 : 16
  const dataCapacityBits = getNumDataCodewords(version) * 8
  appendBits(0x4, 4, bits)
  appendBits(bytes.length, countBits, bits)
  bytes.forEach(item => appendBits(item, 8, bits))
  appendBits(0, Math.min(4, dataCapacityBits - bits.length), bits)
  appendBits(0, (8 - bits.length % 8) % 8, bits)
  for (let padByte = 0xec; bits.length < dataCapacityBits; padByte ^= 0xec ^ 0x11) {
    appendBits(padByte, 8, bits)
  }
  const result = []
  while (result.length * 8 < bits.length) result.push(0)
  bits.forEach((bit, index) => {
    result[index >>> 3] |= bit << (7 - (index & 7))
  })
  return result
}

function drawFunctionPatterns(modules, isFunction, version) {
  const size = modules.length
  for (let i = 0; i < size; i++) {
    setFunctionModule(modules, isFunction, 6, i, i % 2 === 0)
    setFunctionModule(modules, isFunction, i, 6, i % 2 === 0)
  }
  drawFinderPattern(modules, isFunction, 3, 3)
  drawFinderPattern(modules, isFunction, size - 4, 3)
  drawFinderPattern(modules, isFunction, 3, size - 4)

  const alignPatPos = getAlignmentPatternPositions(version)
  const numAlign = alignPatPos.length
  for (let i = 0; i < numAlign; i++) {
    for (let j = 0; j < numAlign; j++) {
      if (!(i === 0 && j === 0 || i === 0 && j === numAlign - 1 || i === numAlign - 1 && j === 0)) {
        drawAlignmentPattern(modules, isFunction, alignPatPos[i], alignPatPos[j])
      }
    }
  }

  drawFormatBits(modules, isFunction, 0)
  drawVersion(modules, isFunction, version)
}

function drawFormatBits(modules, isFunction, mask) {
  const size = modules.length
  const data = FORMAT_BITS_MEDIUM << 3 | mask
  let rem = data
  for (let i = 0; i < 10; i++) {
    rem = (rem << 1) ^ ((rem >>> 9) * 0x537)
  }
  const bits = (data << 10 | rem) ^ 0x5412

  for (let i = 0; i <= 5; i++) setFunctionModule(modules, isFunction, 8, i, getBit(bits, i))
  setFunctionModule(modules, isFunction, 8, 7, getBit(bits, 6))
  setFunctionModule(modules, isFunction, 8, 8, getBit(bits, 7))
  setFunctionModule(modules, isFunction, 7, 8, getBit(bits, 8))
  for (let i = 9; i < 15; i++) setFunctionModule(modules, isFunction, 14 - i, 8, getBit(bits, i))

  for (let i = 0; i < 8; i++) setFunctionModule(modules, isFunction, size - 1 - i, 8, getBit(bits, i))
  for (let i = 8; i < 15; i++) setFunctionModule(modules, isFunction, 8, size - 15 + i, getBit(bits, i))
  setFunctionModule(modules, isFunction, 8, size - 8, true)
}

function drawVersion(modules, isFunction, version) {
  if (version < 7) return
  const size = modules.length
  let rem = version
  for (let i = 0; i < 12; i++) {
    rem = (rem << 1) ^ ((rem >>> 11) * 0x1f25)
  }
  const bits = version << 12 | rem
  for (let i = 0; i < 18; i++) {
    const color = getBit(bits, i)
    const a = size - 11 + i % 3
    const b = Math.floor(i / 3)
    setFunctionModule(modules, isFunction, a, b, color)
    setFunctionModule(modules, isFunction, b, a, color)
  }
}

function drawFinderPattern(modules, isFunction, x, y) {
  const size = modules.length
  for (let dy = -4; dy <= 4; dy++) {
    for (let dx = -4; dx <= 4; dx++) {
      const dist = Math.max(Math.abs(dx), Math.abs(dy))
      const xx = x + dx
      const yy = y + dy
      if (xx >= 0 && xx < size && yy >= 0 && yy < size) {
        setFunctionModule(modules, isFunction, xx, yy, dist !== 2 && dist !== 4)
      }
    }
  }
}

function drawAlignmentPattern(modules, isFunction, x, y) {
  for (let dy = -2; dy <= 2; dy++) {
    for (let dx = -2; dx <= 2; dx++) {
      setFunctionModule(modules, isFunction, x + dx, y + dy, Math.max(Math.abs(dx), Math.abs(dy)) !== 1)
    }
  }
}

function drawCodewords(modules, isFunction, data) {
  const size = modules.length
  let bitIndex = 0
  for (let right = size - 1; right >= 1; right -= 2) {
    if (right === 6) right = 5
    for (let vert = 0; vert < size; vert++) {
      for (let j = 0; j < 2; j++) {
        const x = right - j
        const upward = ((right + 1) & 2) === 0
        const y = upward ? size - 1 - vert : vert
        if (!isFunction[y][x] && bitIndex < data.length * 8) {
          modules[y][x] = getBit(data[bitIndex >>> 3], 7 - (bitIndex & 7))
          bitIndex++
        }
      }
    }
  }
}

function applyMask(modules, isFunction, mask) {
  const size = modules.length
  for (let y = 0; y < size; y++) {
    for (let x = 0; x < size; x++) {
      if (!isFunction[y][x] && shouldInvertMask(mask, x, y)) {
        modules[y][x] = !modules[y][x]
      }
    }
  }
}

function shouldInvertMask(mask, x, y) {
  switch (mask) {
    case 0: return (x + y) % 2 === 0
    case 1: return y % 2 === 0
    case 2: return x % 3 === 0
    case 3: return (x + y) % 3 === 0
    case 4: return (Math.floor(x / 3) + Math.floor(y / 2)) % 2 === 0
    case 5: return x * y % 2 + x * y % 3 === 0
    case 6: return (x * y % 2 + x * y % 3) % 2 === 0
    case 7: return ((x + y) % 2 + x * y % 3) % 2 === 0
    default: return false
  }
}

function addEccAndInterleave(data, version) {
  if (data.length !== getNumDataCodewords(version)) {
    throw new Error('Invalid QR data length')
  }
  const numBlocks = NUM_ERROR_CORRECTION_BLOCKS[version]
  const blockEccLen = ECC_CODEWORDS_PER_BLOCK[version]
  const rawCodewords = Math.floor(getNumRawDataModules(version) / 8)
  const numShortBlocks = numBlocks - rawCodewords % numBlocks
  const shortBlockLen = Math.floor(rawCodewords / numBlocks)
  const blocks = []
  const rsDiv = reedSolomonComputeDivisor(blockEccLen)

  for (let i = 0, k = 0; i < numBlocks; i++) {
    const dataLen = shortBlockLen - blockEccLen + (i < numShortBlocks ? 0 : 1)
    const dat = data.slice(k, k + dataLen)
    k += dat.length
    const ecc = reedSolomonComputeRemainder(dat, rsDiv)
    if (i < numShortBlocks) dat.push(0)
    blocks.push(dat.concat(ecc))
  }

  const result = []
  for (let i = 0; i < blocks[0].length; i++) {
    blocks.forEach((block, j) => {
      if (i !== shortBlockLen - blockEccLen || j >= numShortBlocks) {
        result.push(block[i])
      }
    })
  }
  return result
}

function reedSolomonComputeDivisor(degree) {
  const result = Array(degree - 1).fill(0)
  result.push(1)
  let root = 1
  for (let i = 0; i < degree; i++) {
    for (let j = 0; j < result.length; j++) {
      result[j] = reedSolomonMultiply(result[j], root)
      if (j + 1 < result.length) result[j] ^= result[j + 1]
    }
    root = reedSolomonMultiply(root, 0x02)
  }
  return result
}

function reedSolomonComputeRemainder(data, divisor) {
  const result = divisor.map(() => 0)
  data.forEach(byte => {
    const factor = byte ^ result.shift()
    result.push(0)
    divisor.forEach((coef, i) => {
      result[i] ^= reedSolomonMultiply(coef, factor)
    })
  })
  return result
}

function reedSolomonMultiply(x, y) {
  let z = 0
  for (let i = 7; i >= 0; i--) {
    z = (z << 1) ^ ((z >>> 7) * 0x11d)
    z ^= ((y >>> i) & 1) * x
  }
  return z
}

function getAlignmentPatternPositions(version) {
  if (version === 1) return []
  const numAlign = Math.floor(version / 7) + 2
  const step = Math.floor((version * 8 + numAlign * 3 + 5) / (numAlign * 4 - 4)) * 2
  const result = [6]
  for (let pos = version * 4 + 10; result.length < numAlign; pos -= step) {
    result.splice(1, 0, pos)
  }
  return result
}

function getNumDataCodewords(version) {
  return Math.floor(getNumRawDataModules(version) / 8) -
    ECC_CODEWORDS_PER_BLOCK[version] * NUM_ERROR_CORRECTION_BLOCKS[version]
}

function getNumRawDataModules(version) {
  let result = (16 * version + 128) * version + 64
  if (version >= 2) {
    const numAlign = Math.floor(version / 7) + 2
    result -= (25 * numAlign - 10) * numAlign - 55
    if (version >= 7) result -= 36
  }
  return result
}

function setFunctionModule(modules, isFunction, x, y, isDark) {
  modules[y][x] = isDark
  isFunction[y][x] = true
}

function createGrid(size, value) {
  return Array.from({ length: size }, () => Array(size).fill(value))
}

function appendBits(value, length, bits) {
  for (let i = length - 1; i >= 0; i--) {
    bits.push((value >>> i) & 1)
  }
}

function getBit(value, index) {
  return ((value >>> index) & 1) !== 0
}

function utf8Bytes(text) {
  if (typeof TextEncoder !== 'undefined') {
    return Array.from(new TextEncoder().encode(text))
  }
  const bytes = []
  for (let i = 0; i < text.length; i++) {
    let codePoint = text.charCodeAt(i)
    if (codePoint >= 0xd800 && codePoint <= 0xdbff && i + 1 < text.length) {
      const next = text.charCodeAt(i + 1)
      if (next >= 0xdc00 && next <= 0xdfff) {
        codePoint = 0x10000 + ((codePoint - 0xd800) << 10) + (next - 0xdc00)
        i++
      }
    }
    if (codePoint < 0x80) {
      bytes.push(codePoint)
    } else if (codePoint < 0x800) {
      bytes.push(0xc0 | (codePoint >>> 6), 0x80 | (codePoint & 0x3f))
    } else if (codePoint < 0x10000) {
      bytes.push(0xe0 | (codePoint >>> 12), 0x80 | ((codePoint >>> 6) & 0x3f), 0x80 | (codePoint & 0x3f))
    } else {
      bytes.push(
        0xf0 | (codePoint >>> 18),
        0x80 | ((codePoint >>> 12) & 0x3f),
        0x80 | ((codePoint >>> 6) & 0x3f),
        0x80 | (codePoint & 0x3f)
      )
    }
  }
  return bytes
}
