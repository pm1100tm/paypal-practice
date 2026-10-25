'use client'

import { useSyncExternalStore } from 'react'
import { cn } from '@/shared/lib/cn'
import { dismiss, getServerToasts, getToasts, subscribeToasts } from '@/shared/lib/toast'

const VARIANT_CLASS = {
  info: 'bg-neutral-900 text-white',
  success: 'bg-emerald-600 text-white',
  error: 'bg-red-600 text-white',
} as const

export function Toaster() {
  const toasts = useSyncExternalStore(subscribeToasts, getToasts, getServerToasts)

  return (
    <div
      aria-live="polite"
      className="pointer-events-none fixed inset-x-0 bottom-4 z-50 mx-auto flex max-w-md flex-col gap-2 px-4"
    >
      {toasts.map((item) => (
        <button
          key={item.id}
          type="button"
          onClick={() => dismiss(item.id)}
          className={cn(
            'pointer-events-auto rounded-md px-4 py-3 text-left text-sm shadow-lg',
            VARIANT_CLASS[item.variant],
          )}
        >
          {item.text}
        </button>
      ))}
    </div>
  )
}
