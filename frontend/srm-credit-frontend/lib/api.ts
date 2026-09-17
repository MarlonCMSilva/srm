const API_URL = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080";

interface ApiError {
  message?: string;
  error?: string;
}

export async function apiFetch<T>(
  path: string,
  options?: RequestInit,
): Promise<T> {
  const response = await fetch(`${API_URL}${path}`, {
    ...options,
    headers: {
      "Content-Type": "application/json",
      ...(options?.headers ?? {}),
    },
  });

  if (!response.ok) {
    let errorMessage = "Ocorreu um erro na API";

    try {
      const errorBody = (await response.json()) as ApiError;

      errorMessage = errorBody.message ?? errorBody.error ?? errorMessage;
    } catch {
      // A resposta pode não possuir JSON
    }

    throw new Error(errorMessage);
  }

  return response.json() as Promise<T>;
}
