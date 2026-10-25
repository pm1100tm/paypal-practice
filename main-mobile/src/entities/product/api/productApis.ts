import { z } from 'zod'
import { fetchJson } from '@/shared/api'
import { ProductSchema, type Product } from '../model/types'

/** 상품 목록 (GET /api/v2/products) */
export const getProducts = (): Promise<Product[]> =>
  fetchJson('/api/v2/products', z.array(ProductSchema))
