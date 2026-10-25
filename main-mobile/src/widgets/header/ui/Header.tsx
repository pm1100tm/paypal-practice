'use client'

import Link from 'next/link'
import { useCartQuery } from '@/entities/cart'
import { routes } from '@/shared/config'

export function Header() {
  const { data: cart } = useCartQuery()
  const count = cart?.totalQuantity ?? 0

  return (
    <header className="sticky top-0 z-40 border-b border-neutral-200 bg-white/95 backdrop-blur">
      <div className="mx-auto flex h-14 max-w-md items-center justify-between px-4">
        <Link href={routes.home} className="text-base font-bold tracking-tight">
          ShopSWD
        </Link>
        <Link href={routes.cart} className="relative text-sm font-medium" aria-label={`Shopping bag, ${count} items`}>
          Bag
          {count > 0 && (
            <span className="absolute -right-4 -top-2 flex h-5 min-w-5 items-center justify-center rounded-full bg-brand-accent px-1 text-[11px] font-semibold text-white">
              {count > 99 ? '99+' : count}
            </span>
          )}
        </Link>
      </div>
    </header>
  )
}
