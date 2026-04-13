/**
 * CLOUDFLARE MASTER AGENT
 * Structure: Modular "Nervous System" Architecture
 * Model: Set by user in MODEL_ID
 */

import puppeteer from "@cloudflare/puppeteer";
import { WorkflowEntrypoint, WorkflowEvent, WorkflowStep } from "cloudflare:workers";

// --- 1. CONFIGURATION (Dynamic Control) ---
let MODEL_ID = "@cf/meta/llama-3.1-8b-instruct";
let SYSTEM_PROMPT = "You are a multi-capable AI Agent. You have access to Web Browsing, SQL Databases, KV Storage, Vector Search, and Image Processing.";

// Helper to get/set config from KV
async function getConfig(env: Env) {
  const model = await env.KV.get("CONFIG_MODEL_ID") || MODEL_ID;
  const prompt = await env.KV.get("CONFIG_SYSTEM_PROMPT") || SYSTEM_PROMPT;
  return { model, prompt };
}

// --- 2. THE ENVIRONMENT (Bindings for all your snippets) ---
export interface Env {
  // AI & Knowledge
  AI: any;
  YOUR_INDEX: VectorizeIndex;

  // Storage & Memory
  KV: KVNamespace;
  MY_DB: D1Database;
  MY_BUCKET: R2Bucket;
  HYPERDRIVE: { connectionString: string };

  // Communication & Tools
  MYBROWSER: any; // Puppeteer
  SEND_EMAIL: any;
  IMAGES: any;
  MEDIA: any;

  // Logic & Safety
  MY_WORKFLOW: WorkflowNamespace;
  MY_QUEUE: Queue;
  MY_RATE_LIMITER: any;
  MY_SECRET: { get: () => Promise<string> };

  // Metadata
  CF_VERSION_METADATA: { id: string, tag: string, timestamp: string };
  ASSETS: { fetch: (request: Request) => Promise<Response> };
}

// --- 3. THE CAPABILITIES (Encapsulated Tools) ---
const AgentTools = {
  // Browser: Take screenshots of URLs
  async browse(env: Env, url: string) {
    const browser = await puppeteer.launch(env.MYBROWSER);
    const page = await browser.newPage();
    await page.goto(url);
    const img = await page.screenshot();
    await browser.close();
    return img;
  },

  // Knowledge: Vector Search
  async searchKnowledge(env: Env, queryVector: number[]) {
    return await env.YOUR_INDEX.query(queryVector, { topK: 3 });
  },

  // Memory: SQL Logging (D1)
  async logEvent(env: Env, userId: string, event: string) {
    return await env.MY_DB.prepare(
      "INSERT INTO Logs (UserId, Event, Timestamp) VALUES (?, ?, ?)"
    ).bind(userId, event, Date.now()).run();
  },

  // Image Processing: Watermarking
  async applyWatermark(env: Env, image: ReadableStream, watermark: ReadableStream) {
    return await env.IMAGES.input(image)
      .draw(env.IMAGES.input(watermark).transform({ width: 32, height: 32 }), { bottom: 32, right: 32 })
      .response();
  }
};

// --- 4. THE BACKGROUND ENGINE (Workflows & durable tasks) ---
export class MasterWorkflow extends WorkflowEntrypoint<Env, any> {
  async run(event: WorkflowEvent<any>, step: WorkflowStep) {
    const data = await step.do("Step 1: Data Gathering", async () => {
      return { msg: "Data fetched" };
    });
    await step.do("Step 2: Processing", async () => {
      console.log("Processing:", data.msg);
    });
  }
}

// Durable Object for State Management
export class AgentState extends DurableObject {
  constructor(ctx: DurableObjectState, env: Env) {
    super(ctx, env);
  }
  async handleState() {
    return this.ctx.storage.sql.exec("SELECT 'Agent Ready' as status").one();
  }
}

// --- 5. THE MAIN HANDLER (The Nervous System) ---
export default {
  /**
   * Main Request Handler (HTTP API)
   */
  async fetch(request: Request, env: Env, ctx: ExecutionContext): Promise<Response> {
    const url = new URL(request.url);

    // Safety: Rate Limiting
    const { success } = await env.MY_RATE_LIMITER.limit({ key: url.pathname });
    if (!success) return new Response("Rate Limit Exceeded", { status: 429 });

    // Routing
    try {
      // ADMIN PANEL: UI
      if (url.pathname === "/admin" && request.method === "GET") {
        const config = await getConfig(env);
        return new Response(`
          <!DOCTYPE html>
          <html>
            <head>
              <title>Agent Admin</title>
              <style>
                body { font-family: sans-serif; padding: 20px; background: #f4f4f9; }
                .card { background: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); max-width: 600px; margin: auto; }
                textarea { width: 100%; height: 100px; margin-bottom: 10px; }
                input { width: 100%; margin-bottom: 10px; padding: 8px; }
                button { background: #007bff; color: white; border: none; padding: 10px 20px; border-radius: 4px; cursor: pointer; }
                h2 { color: #333; }
              </style>
            </head>
            <body>
              <div class="card">
                <h2>Master Agent Schema Designer</h2>
                <form action="/admin/save" method="POST">
                  <label>Model ID:</label>
                  <input type="text" name="modelId" value="${config.model}">
                  <label>System Prompt (The Agent's Persona):</label>
                  <textarea name="systemPrompt">${config.prompt}</textarea>
                  <button type="submit">Deploy Changes</button>
                </form>
                <hr>
                <h3>Plugin Portal</h3>
                <p>Status: All snippets integrated (KV, D1, R2, AI, Browser).</p>
              </div>
            </body>
          </html>
        `, { headers: { "Content-Type": "text/html" } });
      }

      // ADMIN PANEL: SAVE
      if (url.pathname === "/admin/save" && request.method === "POST") {
        const formData = await request.formData();
        const modelId = formData.get("modelId")?.toString();
        const systemPrompt = formData.get("systemPrompt")?.toString();

        if (modelId) await env.KV.put("CONFIG_MODEL_ID", modelId);
        if (systemPrompt) await env.KV.put("CONFIG_SYSTEM_PROMPT", systemPrompt);

        return new Response("Configuration Updated! Restarting Agent...", {
          status: 303,
          headers: { "Location": "/admin" }
        });
      }

      // Chat Endpoint
      if (url.pathname === "/api/chat" && request.method === "POST") {
        const config = await getConfig(env);
        const { messages = [] } = await request.json() as any;
        if (!messages.some((m: any) => m.role === "system")) {
          messages.unshift({ role: "system", content: config.prompt });
        }

        const response = await env.AI.run(config.model, { messages });

        // Async Logging (Doesn't block user)
        ctx.waitUntil(AgentTools.logEvent(env, "system", "chat_completion"));

        return Response.json(response);
      }

      // Browser Endpoint
      if (url.pathname === "/api/browse") {
        const targetUrl = url.searchParams.get("url");
        if (!targetUrl) return new Response("Missing URL", { status: 400 });
        const screenshot = await AgentTools.browse(env, targetUrl);
        return new Response(screenshot, { headers: { "content-type": "image/jpeg" } });
      }

      // Default: Serve Static Assets
      return await env.ASSETS.fetch(request);

    } catch (err: any) {
      return Response.json({ error: err.message }, { status: 500 });
    }
  },

  /**
   * Queue Handler (Async Tasks)
   */
  async queue(batch: MessageBatch<any>, env: Env) {
    for (const message of batch.messages) {
      console.log("Processing background task:", message.body);
    }
  },

  /**
   * Email Handler (Incoming Mail)
   */
  async email(message: any, env: Env, ctx: ExecutionContext) {
    await message.forward("admin@example.com");
    await env.SEND_EMAIL.send({
      from: message.to,
      to: "recipient@example.com",
      subject: "Agent Email Log",
      text: `Agent received mail from ${message.from}`,
    });
  }
};
