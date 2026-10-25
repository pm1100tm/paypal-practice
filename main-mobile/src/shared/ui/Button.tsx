import type { ButtonHTMLAttributes } from 'react'
import { cn } from '@/shared/lib/cn'

type Variant = 'primary' | 'outline' | 'ghost'

const VARIANT_CLASS: Record<Variant, string> = {
  primary: 'bg-brand text-white hover:bg-black/85 disabled:bg-neutral-300',
  outline: 'border border-neutral-300 bg-white text-neutral-900 hover:bg-neutral-50 disabled:text-neutral-400',
  ghost: 'bg-transparent text-neutral-600 hover:text-neutral-900 disabled:text-neutral-300',
}

export interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: Variant
  block?: boolean
}

export function Button({ variant = 'primary', block, className, type = 'button', ...props }: ButtonProps) {
  return (
    <button
      type={type}
      className={cn(
        'inline-flex h-11 items-center justify-center rounded-md px-4 text-sm font-semibold transition-colors disabled:cursor-not-allowed',
        VARIANT_CLASS[variant],
        block && 'w-full',
        className,
      )}
      {...props}
    />
  )
}
