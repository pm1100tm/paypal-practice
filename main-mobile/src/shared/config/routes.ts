export const routes = {
  home: '/',
  cart: '/cart',
  checkout: '/order/checkout',
  orderComplete: (orderId: string) => `/order-complete?id=${encodeURIComponent(orderId)}`,
} as const
