import type { NextConfig } from 'next'

/**
 * 백엔드(sk-main-app-v1) API 는 same-origin `/api/v2/*` 로 프록시한다.
 * - 브라우저는 항상 상대 경로로 호출 → CORS / 쿠키 이슈 없음
 * - MAIN_APP_ENDPOINT 는 `next build` 시점에 rewrites 에 반영된다 (빌드 환경에 지정 필요)
 */
const nextConfig: NextConfig = {
  devIndicators: false,
  output: 'standalone',
  reactStrictMode: true,
  async rewrites() {
    const backend = process.env['MAIN_APP_ENDPOINT'] ?? 'http://localhost:8080'
    return [{ source: '/api/v2/:path*', destination: `${backend}/api/v2/:path*` }]
  },
}

export default nextConfig
