import { defineConfig } from 'steiger'
import fsd from '@feature-sliced/steiger-plugin'

export default defineConfig([
  ...fsd.configs.recommended,
  {
    // Next.js App Router 가 src/app 을 라우팅 디렉터리로 사용 — FSD app 레이어와 겸용
    files: ['./src/app/**'],
    rules: {
      'fsd/no-segmentless-slices': 'off',
    },
  },
  {
    // v1 은 구매 흐름(장바구니 → 체크아웃 → PayPal)만 담은 최소 앱이라 대부분의 슬라이스 사용처가 1곳이다.
    // 레이어/교차 import 규칙은 그대로 유지하고, "사용처가 1곳이면 병합 고려" 휴리스틱만 끈다.
    rules: {
      'fsd/insignificant-slice': 'off',
    },
  },
])
