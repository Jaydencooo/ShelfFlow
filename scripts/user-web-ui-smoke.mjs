import { mkdir } from "node:fs/promises"
import path from "node:path"
import { fileURLToPath } from "node:url"

const __filename = fileURLToPath(import.meta.url)
const __dirname = path.dirname(__filename)
const projectRoot = path.resolve(__dirname, "..")
const artifactDir = path.join(projectRoot, "output", "playwright", "user-web-ui-smoke")

const userWebBaseUrl = process.env.SHELFFLOW_USER_WEB_BASE_URL ?? "http://127.0.0.1:3001"
const smokeOpenId = process.env.SHELFFLOW_USER_SMOKE_OPEN_ID ?? "sf-user-ui-smoke"
const smokeName = process.env.SHELFFLOW_USER_SMOKE_NAME ?? "UI Smoke User"
const smokePhone = process.env.SHELFFLOW_USER_SMOKE_PHONE ?? "13800138000"
const smokePassword = process.env.SHELFFLOW_USER_SMOKE_PASSWORD ?? "ShelfFlow#2026"
const smokeTimestamp = Date.now()
const registerOpenId = `${smokeOpenId}-${smokeTimestamp}`
const registerPhone = `139${String(smokeTimestamp).slice(-8)}`
const registerPassword = smokePassword
const resetPassword = `Reset${String(smokeTimestamp).slice(-4)}Aa!`

const timeoutMs = 20_000

function assert(condition, message) {
  if (!condition) {
    throw new Error(message)
  }
}

async function saveScreenshot(page, name) {
  await page.screenshot({ path: path.join(artifactDir, `${name}.png`), fullPage: true })
}

async function waitForUrl(page, expectedPathname) {
  await page.waitForURL((url) => url.pathname === expectedPathname, { timeout: timeoutMs })
}

async function waitForUrlPrefix(page, expectedPrefix) {
  await page.waitForURL((url) => url.pathname.startsWith(expectedPrefix), { timeout: timeoutMs })
}

async function ensurePageReady(page) {
  await page.waitForLoadState("networkidle", { timeout: timeoutMs }).catch(() => undefined)
}

async function logoutIfNeeded(page) {
  const logoutButton = page.getByTestId("header-logout")
  if (await logoutButton.count()) {
    await logoutButton.click()
    await ensurePageReady(page)
  }
}

async function main() {
  await mkdir(artifactDir, { recursive: true })
  const { chromium } = await import("playwright")
  const browser = await chromium.launch({ headless: true })
  const context = await browser.newContext({
    baseURL: userWebBaseUrl,
    viewport: { width: 1440, height: 1000 }
  })
  const page = await context.newPage()

  try {
    await page.goto(`${userWebBaseUrl}/products`, { waitUntil: "domcontentloaded", timeout: timeoutMs })
    await ensurePageReady(page)
    await saveScreenshot(page, "01-products")

    await logoutIfNeeded(page)

    await page.goto(`${userWebBaseUrl}/account`, { waitUntil: "domcontentloaded", timeout: timeoutMs })
    await waitForUrl(page, "/login")
    await saveScreenshot(page, "01a-account-guard")

    await page.goto(`${userWebBaseUrl}/register?next=%2Fproducts`, { waitUntil: "domcontentloaded", timeout: timeoutMs })
    await waitForUrl(page, "/register")
    await page.getByTestId("user-register-openid").fill(registerOpenId)
    await page.getByTestId("user-register-name").fill(smokeName)
    await page.getByTestId("user-register-phone").fill(registerPhone)
    await page.getByTestId("user-register-password").fill(registerPassword)
    await page.getByTestId("user-register-submit").click()
    await waitForUrl(page, "/products")
    await ensurePageReady(page)
    await saveScreenshot(page, "01b-after-register")

    await logoutIfNeeded(page)
    await page.goto(`${userWebBaseUrl}/forgot-password?next=%2Fproducts`, { waitUntil: "domcontentloaded", timeout: timeoutMs })
    await waitForUrl(page, "/forgot-password")
    await page.getByTestId("user-reset-openid").fill(registerOpenId)
    await page.getByTestId("user-reset-phone").fill(registerPhone)
    await page.getByTestId("user-reset-password").fill(resetPassword)
    await page.getByTestId("user-reset-submit").click()
    await page.getByText("密码已重置，现在可以直接登录。").waitFor({ timeout: timeoutMs })
    await saveScreenshot(page, "01c-after-password-reset")

    await page.getByTestId("user-reset-login-link").click()
    await waitForUrl(page, "/login")
    await page.getByTestId("user-login-openid").fill(registerOpenId)
    await page.getByTestId("user-login-password").fill(resetPassword)
    await page.getByTestId("user-login-submit").click()
    await waitForUrl(page, "/products")
    await ensurePageReady(page)
    await saveScreenshot(page, "01d-products-after-login")

    await page.goto(`${userWebBaseUrl}/account`, { waitUntil: "domcontentloaded", timeout: timeoutMs })
    await waitForUrl(page, "/account")
    await page.getByText("账户中心").waitFor({ timeout: timeoutMs })
    await page.getByText(registerOpenId).waitFor({ timeout: timeoutMs })
    await page.getByTestId("pickup-contact-consignee").fill("UI Smoke User")
    await page.getByTestId("pickup-contact-phone").fill(registerPhone)
    await page.getByTestId("pickup-contact-label").fill("公司")
    await page.getByTestId("pickup-contact-detail").fill("滨江社区前置仓 A 区")
    await page.getByTestId("pickup-contact-default").check()
    await page.getByTestId("pickup-contact-save").click()
    await page.getByText("自提联系人已创建").waitFor({ timeout: timeoutMs })
    await saveScreenshot(page, "01e-account")

    await page.goto(`${userWebBaseUrl}/products`, { waitUntil: "domcontentloaded", timeout: timeoutMs })
    await ensurePageReady(page)

    const productCard = page.locator("[data-testid^='catalog-product-card-']").first()
    const productCount = await productCard.count()
    assert(
      productCount > 0,
      "用户端 UI smoke 未找到可售商品。请先运行 KEEP_SERVICES_RUNNING=true START_USER_WEB=true bash scripts/admin-e2e.sh 生成可售商品并保留环境。"
    )

    const detailLink = page.locator("[data-testid^='catalog-detail-']").first()
    await detailLink.click()
    await waitForUrlPrefix(page, "/products/")
    await ensurePageReady(page)
    await saveScreenshot(page, "02-product-detail")

    const increaseButton = page.getByTestId("product-detail-quantity-increase")
    if (await increaseButton.count()) {
      await increaseButton.click()
    }
    await page.getByTestId("product-detail-add-cart").click()
    await waitForUrl(page, "/cart")
    await ensurePageReady(page)
    await page.getByText("UI Smoke User").waitFor({ timeout: timeoutMs })
    await saveScreenshot(page, "03-cart")

    const cartIncreaseButton = page.locator("[data-testid^='cart-item-increase-']").first()
    if (await cartIncreaseButton.count()) {
      await cartIncreaseButton.click()
      await ensurePageReady(page)
    }

    await page.getByTestId("cart-submit-order").click()
    await page.getByText("确认提交订单").waitFor({ timeout: timeoutMs })
    await page.getByRole("button", { name: "确认提交" }).click()
    await page.waitForURL((url) => url.pathname.startsWith("/orders/"), { timeout: timeoutMs })
    await ensurePageReady(page)
    await page.getByText("订单轨迹").waitFor({ timeout: timeoutMs })
    const cancelButton = page.locator("[data-testid^='order-cancel-']").first()
    if (await cancelButton.count()) {
      await cancelButton.click()
      await page.getByTestId("order-cancel-reason").waitFor({ timeout: timeoutMs })
      await page.getByRole("button", { name: "再看看" }).click()
    }
    await saveScreenshot(page, "04-order-detail")

    const payButton = page.locator("[data-testid^='order-pay-']").first()
    if (await payButton.count()) {
      await payButton.click()
      await page.getByText("订单已支付").waitFor({ timeout: timeoutMs })
      await ensurePageReady(page)
    }

    await page.goto(`${userWebBaseUrl}/orders`, { waitUntil: "domcontentloaded", timeout: timeoutMs })
    await ensurePageReady(page)
    await saveScreenshot(page, "05-orders")

    const orderListEntry = page.locator("[data-testid^='order-list-select-']").first()
    assert(await orderListEntry.count(), "订单列表未显示任何订单")

    await page.goto(`${userWebBaseUrl}/account`, { waitUntil: "domcontentloaded", timeout: timeoutMs })
    await waitForUrl(page, "/account")
    await ensurePageReady(page)
    await page.getByText("最近订单").waitFor({ timeout: timeoutMs })
    await page.getByText("条最近订单").waitFor({ timeout: timeoutMs })
    await saveScreenshot(page, "06-account-after-order")

    console.log(`UI smoke passed: ${userWebBaseUrl}`)
    console.log(`Artifacts: ${artifactDir}`)
  } finally {
    await context.close()
    await browser.close()
  }
}

main().catch((error) => {
  console.error(error instanceof Error ? error.message : String(error))
  process.exitCode = 1
})
