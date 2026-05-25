import { mkdir } from "node:fs/promises"
import path from "node:path"
import { fileURLToPath } from "node:url"

const __filename = fileURLToPath(import.meta.url)
const __dirname = path.dirname(__filename)
const projectRoot = path.resolve(__dirname, "..")
const artifactDir = path.join(projectRoot, "output", "playwright", "admin-web-ui-smoke")

const adminWebBaseUrl = process.env.SHELFFLOW_ADMIN_BASE_URL ?? "http://127.0.0.1:3000"
const adminUsername = process.env.ADMIN_USERNAME ?? "admin"
const adminPassword = process.env.ADMIN_PASSWORD ?? "123456"
const smokeTimestamp = Date.now()
const productName = `UI验收商品${smokeTimestamp}`
let batchCode = ""
const pricingRuleName = `UI验收规则${smokeTimestamp}`
const knowledgeTitle = `UI验收知识${smokeTimestamp}`
const timeoutMs = Number(process.env.SHELFFLOW_UI_SMOKE_TIMEOUT_MS ?? "20000")

function assert(condition, message) {
  if (!condition) {
    throw new Error(message)
  }
}

function toDateTimeLocal(date) {
  const offset = date.getTimezoneOffset()
  const adjusted = new Date(date.getTime() - offset * 60 * 1000)
  return adjusted.toISOString().slice(0, 16)
}

async function saveScreenshot(page, name) {
  await page.screenshot({ path: path.join(artifactDir, `${name}.png`), fullPage: true })
}

async function waitForUrl(page, expectedPathname) {
  await page.waitForURL((url) => url.pathname === expectedPathname, { timeout: timeoutMs })
}

async function ensurePageReady(page) {
  await page.waitForLoadState("networkidle", { timeout: timeoutMs }).catch(() => undefined)
}

async function login(page) {
  await page.goto(`${adminWebBaseUrl}/login`, { waitUntil: "domcontentloaded", timeout: timeoutMs })
  await page.getByTestId("admin-login-form").waitFor({ timeout: timeoutMs })
  await page.waitForFunction(() => {
    const form = document.querySelector('[data-testid="admin-login-form"]')
    return form?.getAttribute("data-hydrated") === "true"
  }, undefined, { timeout: timeoutMs })
  await page.getByTestId("admin-login-username").fill(adminUsername)
  await page.getByTestId("admin-login-password").fill(adminPassword)
  await page.getByTestId("admin-login-submit").click()
  await page.waitForURL((url) => url.pathname.startsWith("/dashboard"), { timeout: timeoutMs })
  await ensurePageReady(page)
}

async function createProduct(page) {
  await page.goto(`${adminWebBaseUrl}/dashboard/products`, { waitUntil: "domcontentloaded", timeout: timeoutMs })
  await ensurePageReady(page)
  await page.getByRole("heading", { name: "商品管理" }).waitFor({ timeout: timeoutMs })
  await page.getByTestId("admin-create-product-open").click()
  await page.getByTestId("admin-product-name").fill(productName)
  const categorySelect = page.getByTestId("admin-product-category-id")
  await page.waitForFunction(() => {
    const select = document.querySelector('[data-testid="admin-product-category-id"]')
    if (!(select instanceof HTMLSelectElement)) {
      return false
    }
    return Array.from(select.options).some((option) => option.value.length > 0)
  }, undefined, { timeout: timeoutMs })
  const categoryOptions = await categorySelect.locator("option").evaluateAll((options) =>
    options.map((option) => option.getAttribute("value")).filter((value) => value && value.length > 0),
  )
  assert(categoryOptions.length > 0, "商品分类下拉框没有可选分类")
  await categorySelect.selectOption(categoryOptions[0])
  await page.getByTestId("admin-product-price").fill("19.90")
  await page.getByTestId("admin-product-description").fill("管理端 UI smoke 自动创建商品")
  await page.getByTestId("admin-product-submit").click()
  await page.getByText(`商品 ${productName} 已创建`).waitFor({ timeout: timeoutMs })
  await saveScreenshot(page, "02-product-created")
}

async function createBatch(page) {
  const productionDate = toDateTimeLocal(new Date())
  const expiryDate = toDateTimeLocal(new Date(Date.now() + 7 * 24 * 60 * 60 * 1000))

  await page.goto(`${adminWebBaseUrl}/dashboard/batches`, { waitUntil: "domcontentloaded", timeout: timeoutMs })
  await ensurePageReady(page)
  await page.getByRole("heading", { name: "批次管理" }).waitFor({ timeout: timeoutMs })
  await page.getByTestId("admin-create-batch-open").click()
  await page.getByPlaceholder("先搜索商品名称或 ID").fill(productName)
  await page.getByTestId("admin-batch-product-id").selectOption({ label: productName })
  batchCode = await page.getByTestId("admin-batch-code").inputValue()
  assert(batchCode.length > 0, "批次号未自动生成")
  await page.getByTestId("admin-batch-stock-quantity").fill("15")
  await page.getByTestId("admin-batch-production-date").fill(productionDate)
  await page.getByTestId("admin-batch-expiry-date").fill(expiryDate)
  await page.getByTestId("admin-batch-submit").click()
  await page.getByText("批次已创建").waitFor({ timeout: timeoutMs })
  await saveScreenshot(page, "03-batch-created")
}

async function createPricingRule(page) {
  await page.goto(`${adminWebBaseUrl}/dashboard/pricing`, { waitUntil: "domcontentloaded", timeout: timeoutMs })
  await ensurePageReady(page)
  await page.getByRole("heading", { name: "定价规则" }).waitFor({ timeout: timeoutMs })
  await page.getByTestId("admin-create-pricing-rule-open").click()
  await page.getByTestId("admin-pricing-rule-name").fill(pricingRuleName)
  await page.getByTestId("admin-pricing-rule-min-days").fill("0")
  await page.getByTestId("admin-pricing-rule-max-days").fill("3")
  await page.getByTestId("admin-pricing-rule-discount-rate").fill("0.82")
  await page.getByTestId("admin-pricing-rule-priority").fill("88")
  await page.getByTestId("admin-pricing-rule-submit").click()
  await page.getByText("定价规则已创建").waitFor({ timeout: timeoutMs })
  await saveScreenshot(page, "04-pricing-rule-created")
}

async function inspectOrdersAndLossStats(page) {
  await page.goto(`${adminWebBaseUrl}/dashboard/order-monitor`, { waitUntil: "domcontentloaded", timeout: timeoutMs })
  await ensurePageReady(page)
  await page.getByRole("heading", { name: "订单管理" }).waitFor({ timeout: timeoutMs })
  await saveScreenshot(page, "05-order-monitor")

  await page.goto(`${adminWebBaseUrl}/dashboard/orders`, { waitUntil: "domcontentloaded", timeout: timeoutMs })
  await ensurePageReady(page)
  await page.getByRole("heading", { name: "订单管理" }).waitFor({ timeout: timeoutMs })
  await saveScreenshot(page, "06-order-fulfillment")

  await page.goto(`${adminWebBaseUrl}/dashboard/loss-statistics`, { waitUntil: "domcontentloaded", timeout: timeoutMs })
  await ensurePageReady(page)
  await page.getByRole("heading", { name: "经营分析" }).waitFor({ timeout: timeoutMs })
  await saveScreenshot(page, "07-loss-statistics")
}

async function createKnowledge(page) {
  await page.goto(`${adminWebBaseUrl}/dashboard/ai-assistant`, { waitUntil: "domcontentloaded", timeout: timeoutMs })
  await ensurePageReady(page)
  await page.getByRole("heading", { name: "AI 运营助手" }).waitFor({ timeout: timeoutMs })
  await page.getByRole("tab", { name: /知识库管理/ }).click()
  await page.getByTestId("admin-ai-knowledge-open").click()
  await page.getByTestId("admin-ai-knowledge-title").fill(knowledgeTitle)
  await page.getByTestId("admin-ai-knowledge-category").fill("UI验收")
  await page.getByTestId("admin-ai-knowledge-content").fill("这是一条由管理端 UI smoke 创建的运营知识，用于验证知识库前后端链路。")
  await page.getByTestId("admin-ai-knowledge-submit").click()
  await page.getByText("知识条目已创建").waitFor({ timeout: timeoutMs })
  await saveScreenshot(page, "08-ai-knowledge-created")
}

async function main() {
  await mkdir(artifactDir, { recursive: true })
  const { chromium } = await import("playwright")
  const browser = await chromium.launch({ headless: true })
  const context = await browser.newContext({
    baseURL: adminWebBaseUrl,
    viewport: { width: 1440, height: 1000 },
  })
  const page = await context.newPage()

  try {
    await login(page)
    await waitForUrl(page, "/dashboard/overview")
    await saveScreenshot(page, "01-after-login")

    await page.goto(`${adminWebBaseUrl}/dashboard/overview`, { waitUntil: "domcontentloaded", timeout: timeoutMs })
    await ensurePageReady(page)
    await page.getByRole("heading", { name: "系统概览" }).waitFor({ timeout: timeoutMs })

    await createProduct(page)
    await createBatch(page)
    await createPricingRule(page)
    await inspectOrdersAndLossStats(page)
    await createKnowledge(page)

    console.log(`Admin UI smoke passed: ${adminWebBaseUrl}`)
    console.log(`Created product: ${productName}`)
    console.log(`Created batch: ${batchCode}`)
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
