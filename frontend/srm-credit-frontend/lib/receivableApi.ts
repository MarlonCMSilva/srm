import { CreateReceivableRequest, Receivable, ReceivablePage } from "@/types";
import { apiFetch } from "./api";

export function createReceivable(
  data: CreateReceivableRequest,
): Promise<Receivable> {
  return apiFetch<Receivable>("/receivables", {
    method: "POST",
    body: JSON.stringify(data),
  });
}

export function getReceivable(id: number): Promise<Receivable> {
  return apiFetch<Receivable>(`/receivables/${id}`);
}

export function getReceivables(): Promise<ReceivablePage> {
  return apiFetch<ReceivablePage>("/receivables?page=0&size=20&sort=id,asc");
}
