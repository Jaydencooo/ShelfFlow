import { mkdir, writeFile } from "node:fs/promises"
import path from "node:path"

import { AppError, buildErrorResponse, buildSuccessResponse, errorCodes } from "@shelfflow/shared"
import { NextResponse } from "next/server"

import { readAccessToken } from "@/lib/server/session"

const PRODUCT_IMAGE_FIELD_NAME = "image"
const PRODUCT_IMAGE_UPLOAD_DIR = "uploads/products"
const MAX_PRODUCT_IMAGE_BYTES = 3 * 1024 * 1024
const ALLOWED_PRODUCT_IMAGE_TYPES = new Map([
  ["image/jpeg", "jpg"],
  ["image/png", "png"],
  ["image/webp", "webp"],
])

async function requireAccessToken() {
  const accessToken = await readAccessToken()

  if (!accessToken) {
    throw new AppError(errorCodes.UNAUTHORIZED, 401, "当前未登录")
  }

  return accessToken
}

function resolveExtension(contentType: string) {
  const extension = ALLOWED_PRODUCT_IMAGE_TYPES.get(contentType)
  if (!extension) {
    throw new AppError(errorCodes.VALIDATION_ERROR, 400, "仅支持 JPG、PNG、WEBP 商品图片")
  }

  return extension
}

export async function POST(request: Request) {
  const requestId = request.headers.get("x-request-id") ?? crypto.randomUUID()

  try {
    await requireAccessToken()

    const formData = await request.formData()
    const upload = formData.get(PRODUCT_IMAGE_FIELD_NAME)
    if (!(upload instanceof File)) {
      throw new AppError(errorCodes.VALIDATION_ERROR, 400, "请选择商品图片")
    }
    if (upload.size <= 0) {
      throw new AppError(errorCodes.VALIDATION_ERROR, 400, "商品图片不能为空")
    }
    if (upload.size > MAX_PRODUCT_IMAGE_BYTES) {
      throw new AppError(errorCodes.VALIDATION_ERROR, 400, "商品图片不能超过 3MB")
    }

    const extension = resolveExtension(upload.type)
    const publicDir = path.join(process.cwd(), "public", PRODUCT_IMAGE_UPLOAD_DIR)
    const fileName = `${crypto.randomUUID()}.${extension}`
    const relativePath = `/${PRODUCT_IMAGE_UPLOAD_DIR}/${fileName}`

    await mkdir(publicDir, { recursive: true })
    await writeFile(path.join(publicDir, fileName), Buffer.from(await upload.arrayBuffer()))

    return NextResponse.json(buildSuccessResponse({ url: relativePath }, requestId, "商品图片上传成功"))
  } catch (error) {
    const appError = error instanceof AppError
      ? error
      : new AppError(errorCodes.INTERNAL_ERROR, 500, "商品图片上传失败")

    return NextResponse.json(buildErrorResponse(appError, requestId), { status: appError.statusCode })
  }
}
