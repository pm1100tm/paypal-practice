'use client'

import { useQuery } from '@tanstack/react-query'
import { getProducts, ProductCard } from '@/entities/product'
import { AddToCartButton } from '@/features/cart'
import { Spinner } from '@/shared/ui'

export function ProductList() {
  const { data: products, isLoading, isError, refetch } = useQuery({
    queryKey: ['products'],
    queryFn: getProducts,
  })

  if (isLoading) return <Spinner />
  if (isError || !products) {
    return (
      <div className="flex flex-col items-center gap-3 py-16 text-sm text-neutral-600">
        <p>Products could not be loaded.</p>
        <button type="button" className="underline" onClick={() => refetch()}>
          Retry
        </button>
      </div>
    )
  }

  return (
    <ul className="grid grid-cols-2 gap-x-3 gap-y-6">
      {products.map((product) => (
        <li key={product.productId}>
          <ProductCard
            product={product}
            action={<AddToCartButton productId={product.productId} disabled={!product.purchasable} />}
          />
        </li>
      ))}
    </ul>
  )
}
