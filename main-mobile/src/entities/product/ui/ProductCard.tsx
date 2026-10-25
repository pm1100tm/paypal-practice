import type { ReactNode } from 'react'
import { cn } from '@/shared/lib/cn'
import { formatMoney } from '@/shared/lib/money'
import type { Product } from '../model/types'
import { Thumbnail } from '@/shared/ui'

interface ProductCardProps {
  product: Product
  /** 장바구니 담기 버튼 등 상위 레이어 액션 슬롯 */
  action?: ReactNode
}

export function ProductCard({ product, action }: ProductCardProps) {
  return (
    <article className="flex h-full flex-col gap-2">
      <div className="relative overflow-hidden rounded-md">
        <Thumbnail
          name={product.name}
          imageUrl={product.imageUrl}
          className={cn('aspect-square w-full', !product.purchasable && 'opacity-50')}
        />
        {/* 품절 표시는 이미지 하단 오버레이 — 카드 높이에 영향을 주지 않아 그리드 정렬이 유지된다 */}
        {!product.purchasable && (
          <span className="absolute inset-x-0 bottom-0 bg-neutral-900/75 py-1.5 text-center text-xs font-semibold uppercase tracking-wide text-white">
            Sold out
          </span>
        )}
      </div>
      <div className="flex flex-1 flex-col gap-0.5">
        {product.brand && <p className="text-xs text-neutral-500">{product.brand}</p>}
        <h3 className="line-clamp-2 text-sm font-medium text-neutral-900">{product.name}</h3>
        <p className="text-sm font-semibold">{formatMoney(product.price)}</p>
      </div>
      {action}
    </article>
  )
}
