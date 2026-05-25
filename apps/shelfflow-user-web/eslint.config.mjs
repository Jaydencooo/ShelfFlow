import { FlatCompat } from "@eslint/eslintrc"
import { dirname } from "node:path"
import { fileURLToPath } from "node:url"
import tseslint from "typescript-eslint"

const configDirectory = dirname(fileURLToPath(import.meta.url))
const compat = new FlatCompat({
  baseDirectory: configDirectory,
})

export default tseslint.config(
  {
    ignores: [
      ".next/**",
      "next-env.d.ts",
      "node_modules/**",
    ],
  },
  ...compat.extends("next/core-web-vitals"),
  {
    files: ["**/*.{ts,tsx}"],
    languageOptions: {
      parser: tseslint.parser,
    },
    plugins: {
      "@typescript-eslint": tseslint.plugin,
    },
    rules: {
      "react-hooks/incompatible-library": "off",
      "react-hooks/purity": "off",
      "react-hooks/set-state-in-effect": "off",
      "@typescript-eslint/no-unused-vars": [
        "error",
        {
          argsIgnorePattern: "^_",
          varsIgnorePattern: "^_",
        },
      ],
    },
  },
)
