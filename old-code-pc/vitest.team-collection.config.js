import { defineConfig } from 'vitest/config'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath } from 'node:url'

export default defineConfig({
  plugins: [vue()],
  resolve: { alias: { '@': fileURLToPath(new URL('./src', import.meta.url)) } },
  test: { environment: 'happy-dom', include: ['src/views/personal/TeamCollection.test.js', 'src/utils/embeddedEntryPath.test.js', 'src/views/personal/TeamCollectionNative.test.js', 'src/utils/nativeCollectionEntry.test.js', 'src/utils/nativeCollectionProgress.test.js'] },
})
