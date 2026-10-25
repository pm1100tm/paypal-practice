/**
 * 최소 Toast Store (imperative API) — 외부 라이브러리 없이 useSyncExternalStore 로 구독
 */

export interface ToastItem {
  id: number
  text: string
  variant: 'info' | 'success' | 'error'
}

type Listener = () => void

const DURATION_MS = 3500
let nextId = 0
let items: ToastItem[] = []
const listeners = new Set<Listener>()

const emit = () => {
  for (const listener of listeners) listener()
}

const push = (text: string, variant: ToastItem['variant']) => {
  const id = ++nextId
  items = [...items, { id, text, variant }]
  emit()
  setTimeout(() => dismiss(id), DURATION_MS)
}

export const dismiss = (id: number) => {
  items = items.filter((item) => item.id !== id)
  emit()
}

export const subscribeToasts = (listener: Listener) => {
  listeners.add(listener)
  return () => {
    listeners.delete(listener)
  }
}

const EMPTY: ToastItem[] = []
export const getToasts = () => items
export const getServerToasts = () => EMPTY

export const toast = {
  info: (text: string) => push(text, 'info'),
  success: (text: string) => push(text, 'success'),
  error: (text: string) => push(text, 'error'),
}
