import { createOpenAPI } from "fumadocs-openapi/server";

export const openapi = createOpenAPI({
  input: ["https://staging.open-isle.com/api/v3/api-docs"],
});
