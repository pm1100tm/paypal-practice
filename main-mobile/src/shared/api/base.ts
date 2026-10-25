/**
 * 백엔드(sk-main-app-v1) API 호출 공통 모듈.
 *
 * - 브라우저: same-origin 상대 경로 `/api/v2/*` (next.config rewrites 로 백엔드 프록시)
 * - 서버(SSR): MAIN_APP_ENDPOINT 절대 경로
 * - 로그인/세션이 없으므로 쿠키를 보내지 않는다 (credentials: 'omit')
 */

import { z } from 'zod'

/** 기본 요청 타임아웃 (10초) */
const DEFAULT_TIMEOUT_MS = 10_000

export type BaseFetchOptions = RequestInit & {
  /** 이 요청에 한해 사용할 타임아웃(ms). 미지정 시 DEFAULT_TIMEOUT_MS 사용 */
  timeoutMs?: number
}

const ApiErrorBodySchema = z.object({
  code: z.string(),
  message: z.string().nullable().optional(),
  errors: z
    .array(z.object({ field: z.string().optional(), message: z.string().nullable().optional() }))
    .optional(),
})

/**
 * API 에러 응답 — 백엔드 규격 `{ code, message, errors }`
 */
export class ApiError extends Error {
  public readonly status: number
  public readonly code: string
  public readonly errors: Array<{ field?: string; message?: string | null }>

  constructor(
    status: number,
    code: string,
    message: string,
    errors: Array<{ field?: string; message?: string | null }> = [],
  ) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.code = code
    this.errors = errors
  }
}

const buildRequestUrl = (endpoint: string): string => {
  if (endpoint.startsWith('http')) return endpoint
  if (typeof window !== 'undefined') return endpoint
  const baseUrl = process.env['MAIN_APP_ENDPOINT'] ?? 'http://localhost:8080'
  return `${baseUrl}${endpoint}`
}

const toApiError = async (response: Response): Promise<ApiError> => {
  try {
    const parsed = ApiErrorBodySchema.safeParse(await response.json())
    if (parsed.success) {
      return new ApiError(
        response.status,
        parsed.data.code,
        parsed.data.message ?? response.statusText,
        parsed.data.errors ?? [],
      )
    }
  } catch {
    // JSON 이 아닌 에러 응답
  }
  return new ApiError(response.status, 'HTTP_ERROR', response.statusText || 'Request failed')
}

/**
 * fetch 래퍼 — 타임아웃, JSON 헤더, 에러 응답 → ApiError 변환
 */
export const baseFetch = async (endpoint: string, options: BaseFetchOptions = {}) => {
  const { timeoutMs = DEFAULT_TIMEOUT_MS, headers, ...rest } = options

  const response = await fetch(buildRequestUrl(endpoint), {
    credentials: 'omit',
    cache: 'no-store',
    ...rest,
    headers: { Accept: 'application/json', ...headers },
    signal: rest.signal ?? AbortSignal.timeout(timeoutMs),
  })

  if (!response.ok) {
    throw await toApiError(response)
  }
  return response
}

/**
 * JSON 요청 + zod 스키마 검증
 */
export const fetchJson = async <T>(
  endpoint: string,
  schema: z.ZodType<T>,
  options: BaseFetchOptions & { json?: unknown } = {},
): Promise<T> => {
  const { json, headers, ...rest } = options
  const response = await baseFetch(endpoint, {
    ...rest,
    headers: json !== undefined ? { 'Content-Type': 'application/json', ...headers } : headers,
    body: json !== undefined ? JSON.stringify(json) : rest.body,
  })
  return schema.parse(await response.json())
}

export const isApiError = (error: unknown): error is ApiError => error instanceof ApiError

/** 사용자에게 보여줄 에러 메시지 추출 */
export const getErrorMessage = (error: unknown, fallback: string): string => {
  if (isApiError(error) && error.message) return error.message
  if (error instanceof Error && error.message) return error.message
  return fallback
}
