import { CheckoutView } from '@/widgets/checkout'

/**
 * PAYPAL_CLIENT_ID 를 빌드 시점이 아닌 요청 시점(런타임)에 읽기 위해 동적 렌더링한다.
 * (Client ID 는 공개 값이지만, 환경별로 이미지를 다시 빌드하지 않도록 런타임 주입)
 */
export const dynamic = 'force-dynamic'

export default function CheckoutPage() {
  const paypalClientId = process.env['PAYPAL_CLIENT_ID']?.trim() || null

  return (
    <>
      <h1 className="mb-4 text-lg font-bold">Checkout</h1>
      <CheckoutView paypalClientId={paypalClientId} />
    </>
  )
}
