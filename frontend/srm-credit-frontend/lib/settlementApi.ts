import { Settlement, SettlementRequest } from "@/types";
import { apiFetch } from "./api";

export function createSettlement(data: SettlementRequest): Promise<Settlement> {
  return apiFetch<Settlement>("/settlements", {
    method: "POST",
    body: JSON.stringify(data),
  });
}

export function getSettlement(id: number): Promise<Settlement> {
  return apiFetch<Settlement>(`/settlements/${id}`);
}
