const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL ?? "";

export type ApiProblem = { title?: string; detail?: string; status?: number };

export class ApiError extends Error {
  constructor(public readonly problem: ApiProblem, public readonly status: number) {
    super(problem.detail ?? "Não foi possível concluir a ação.");
  }
}

function cookieValue(name: string): string | undefined {
  return document.cookie
    .split("; ")
    .find((part) => part.startsWith(`${name}=`))
    ?.slice(name.length + 1);
}

async function csrfHeaders(): Promise<Record<string, string>> {
  const response = await fetch(`${API_BASE_URL}/api/v1/auth/csrf`, {
    credentials: "include",
    cache: "no-store",
  });
  if (!response.ok) throw new ApiError({ detail: "Não foi possível iniciar uma ação segura." }, response.status);
  const token = cookieValue("XSRF-TOKEN");
  if (!token) throw new ApiError({ detail: "Não foi possível iniciar uma ação segura." }, 0);
  return { "X-XSRF-TOKEN": decodeURIComponent(token) };
}

async function perform(path: string, init: RequestInit): Promise<Response> {
  const method = (init.method ?? "GET").toUpperCase();
  const unsafe = !["GET", "HEAD", "OPTIONS"].includes(method);
  return fetch(`${API_BASE_URL}/api/v1${path}`, {
    ...init,
    credentials: "include",
    cache: "no-store",
    headers: {
      Accept: "application/json",
      ...(init.body ? { "Content-Type": "application/json" } : {}),
      ...(unsafe ? await csrfHeaders() : {}),
      ...init.headers,
    },
  });
}

export async function apiFetch<T>(path: string, init: RequestInit = {}): Promise<T> {
  let response = await perform(path, init);
  if (response.status === 401 && !path.startsWith("/auth/")) {
    const refreshed = await perform("/auth/sessions/refresh", { method: "POST" });
    if (refreshed.ok) response = await perform(path, init);
  }

  if (!response.ok) {
    let problem: ApiProblem = {};
    try {
      problem = (await response.json()) as ApiProblem;
    } catch {
      problem = { detail: "Não foi possível concluir a ação." };
    }
    throw new ApiError(problem, response.status);
  }
  if (response.status === 204) return undefined as T;
  return (await response.json()) as T;
}

export function idempotencyKey(action: string): string {
  return `${action}-${crypto.randomUUID()}`;
}
